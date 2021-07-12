package journal.gratitude.com.gratitudejournal.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dropbox.core.android.Auth
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.presently.settings.BackupCadence
import com.presently.settings.PresentlySettings
import com.presently.settings.model.*
import journal.gratitude.com.gratitudejournal.BuildConfig
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.*
import journal.gratitude.com.gratitudejournal.util.backups.LocalExporter.convertCsvToEntries
import journal.gratitude.com.gratitudejournal.util.backups.LocalExporter.exportEntriesToCsvFile
import journal.gratitude.com.gratitudejournal.util.backups.RealCsvParser
import journal.gratitude.com.gratitudejournal.util.backups.UploadToCloudWorker
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader.Companion.PRESENTLY_BACKUP
import journal.gratitude.com.gratitudejournal.util.reminders.NotificationScheduler
import journal.gratitude.com.gratitudejournal.util.reminders.TimePreference
import journal.gratitude.com.gratitudejournal.util.reminders.TimePreferenceFragment
import com.presently.ui.setStatusBarColorsForBackground
import journal.gratitude.com.gratitudejournal.ui.themes.ThemeFragment
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.launch
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.threeten.bp.LocalDateTime
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener, DialogPreference.TargetFragment {

    @Inject lateinit var repository: EntryRepository
    @Inject lateinit var settings: PresentlySettings

    private lateinit var splitInstallManager: SplitInstallManager

    private val listener = SplitInstallStateUpdatedListener { state ->
        if (state.sessionId() == requestId && state.status() == SplitInstallSessionStatus.INSTALLED) {
            firebaseAnalytics.logEvent(LANGUAGE_INSTALLED, null)
            startActivity(Intent.makeRestartActivityTask(activity?.intent?.component))
        } else if (state.sessionId() == requestId && state.status() == SplitInstallSessionStatus.FAILED) {
            val errorCode = state.errorCode()
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.recordException(Exception("SplitInstallErrorCode: $errorCode"))
            Toast.makeText(context, "Error loading language", Toast.LENGTH_SHORT).show()
        }
    }

    private var requestId = 0

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        splitInstallManager = SplitInstallManagerFactory.create(requireContext())

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            v.updatePadding(
                    top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
                    bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            )
            insets
        }

        val window = requireActivity().window
        window.statusBarColor = Color.TRANSPARENT
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(R.attr.backgroundColor, typedValue, true)
        setStatusBarColorsForBackground(window, typedValue.data)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        setPreferencesFromResource(R.xml.preferences, rootKey)

        //region App Information
        val faq = findPreference<Preference>(getString(R.string.key_faq))
        faq?.setOnPreferenceClickListener {
            openFaq()
            true
        }
        val share = findPreference<Preference>(getString(R.string.key_share_app))

        // Handle icon issues for android versions < 23
        if(Build.VERSION.SDK_INT <= 23) {
            context?.getColor(R.color.text_color)?.let { share?.icon?.setTint(it) }
            val lang = findPreference<Preference>(APP_LANGUAGE)
            context?.getColor(R.color.text_color)?.let { lang?.icon?.setTint(it) }
        }

        share?.setOnPreferenceClickListener {
            openShareApp()
            true
        }

        val privacy = findPreference<Preference>(getString(R.string.key_privacy_policy))
        privacy?.setOnPreferenceClickListener {
            openPrivacyPolicy()
            true
        }
        val terms = findPreference<Preference>(getString(R.string.key_terms_conditions))
        terms?.setOnPreferenceClickListener {
            openTermsAndConditions()
            true
        }
        val oss = findPreference<Preference>(getString(R.string.key_open_source))
        oss?.setOnPreferenceClickListener {
            startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            true
        }
        val version = findPreference<Preference>(VERSION_PREF)
        val versionNum = BuildConfig.VERSION_NAME
        version?.summary = versionNum
        //endregion

        val theme = findPreference<Preference>(THEME_PREF)
        theme?.setOnPreferenceClickListener {
            openThemes()
            true
        }

        val dropbox = findPreference<Preference>(BACKUP_TOKEN)
        val cadencePref = (findPreference<Preference>(BACKUP_CADENCE) as ListPreference)

        dropbox?.setOnPreferenceClickListener {
            val wantsToLogin = preferenceScreen.sharedPreferences.getBoolean(BACKUP_TOKEN, false)
            if (!wantsToLogin) {
                firebaseAnalytics.logEvent(DROPBOX_DEAUTH, null)
                firebaseAnalytics.setUserProperty(DROPBOX_USER, "false")
                lifecycleScope.launch {
                    DropboxUploader.deauthorizeDropboxAccess(requireContext(), settings)
                }
            } else {
                firebaseAnalytics.logEvent(DROPBOX_AUTH_ATTEMPT, null)
                DropboxUploader.authorizeDropboxAccess(requireContext(), settings)
            }
            true
        }

        val cadence = settings.getAutomaticBackupCadence()
        val index = cadence.index
        cadencePref.setValueIndex(index)
        val oneTimeExport = findPreference<Preference>(ONE_TIME_EXPORT_PREF)
        oneTimeExport?.setOnPreferenceClickListener {
            createFileOnDevice()
            true
        }

        val import = findPreference<Preference>(IMPORT_PREF)
        import?.setOnPreferenceClickListener {
            importFromCsv()
            true
        }

        val fingerprint = findPreference<Preference>(FINGERPRINT)
        val canAuthenticateUsingFingerPrint =
            BiometricManager.from(requireContext())
                .canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
        fingerprint?.parent!!.isEnabled = canAuthenticateUsingFingerPrint
    }

    override fun onResume() {
        super.onResume()
        val prefs = preferenceScreen.sharedPreferences

        // Set up a listener whenever a key changes
        prefs.registerOnSharedPreferenceChangeListener(this)

        val accessToken = prefs.getString("access-token", null)
        if (accessToken == "attempted") {
            val token = Auth.getOAuth2Token()
            if (token == null) {
                //user started to auth and didn't succeed
                firebaseAnalytics.logEvent(DROPBOX_AUTH_QUIT, null)
                prefs.edit().putBoolean(BACKUP_TOKEN, false).apply()
                prefs.edit().remove("access-token").apply()
                activity?.recreate()
            } else {
                firebaseAnalytics.logEvent(DROPBOX_AUTH_SUCCESS, null)
                firebaseAnalytics.setUserProperty(DROPBOX_USER, "true")
                prefs.edit().putString("access-token", token).apply()
                createDropboxUploaderWorker(BackupCadence.DAILY)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            NOTIFS -> {
                val notifsTurnedOn = settings.hasEnabledNotifications()
                if (notifsTurnedOn) {
                    NotificationScheduler().configureNotifications(requireContext(), settings)
                    firebaseAnalytics.setUserProperty(HAS_NOTIFICATIONS_TURNED_ON, "true")
                } else {
                    NotificationScheduler().disableNotifications(requireContext())
                    firebaseAnalytics.logEvent(CANCELLED_NOTIFS, null)
                    firebaseAnalytics.setUserProperty(HAS_NOTIFICATIONS_TURNED_ON, "false")
                }
            }
            FINGERPRINT -> {
                val biometricsEnabled = settings.isBiometricsEnabled()
                if (biometricsEnabled) {
                    firebaseAnalytics.logEvent(BIOMETRICS_SELECT, null)
                    firebaseAnalytics.setUserProperty(BIOMETRICS_ENABLED, "true")
                } else {
                    firebaseAnalytics.logEvent(BIOMETRICS_DESELECT, null)
                    firebaseAnalytics.setUserProperty(BIOMETRICS_ENABLED, "false")
                }
            }
            BACKUP_CADENCE -> {
                //todo test cadence works properly with dropbox
                val cadence = settings.getAutomaticBackupCadence()
                fireAnalyticsEventForCadence(cadence, firebaseAnalytics)
                createDropboxUploaderWorker(cadence)
            }
            APP_LANGUAGE -> {
                val language = settings.getLocale()
                updateLanguage(language)
            }
        }
    }

    private fun updateLanguage(language: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, language)
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, language)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "language")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

        val crashlytics = FirebaseCrashlytics.getInstance()

        val request = SplitInstallRequest.newBuilder()
            .addLanguage(Locale.forLanguageTag(language))
            .build()
        splitInstallManager.registerListener(listener)
        splitInstallManager.startInstall(request)
            .addOnSuccessListener {
                requestId = it
                Toast.makeText(context, R.string.loading_lang, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                crashlytics.recordException(exception)
                Toast.makeText(context, "Error loading language", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createDropboxUploaderWorker(cadence: BackupCadence) {
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(PRESENTLY_BACKUP)

        when (cadence) {
            BackupCadence.DAILY -> {
                val uploadWorkRequest =
                    PeriodicWorkRequestBuilder<UploadToCloudWorker>(1, TimeUnit.DAYS)
                        .addTag(PRESENTLY_BACKUP)
                        .build()
                WorkManager.getInstance(requireContext()).enqueue(uploadWorkRequest)
            }
            BackupCadence.WEEKLY -> {
                val uploadWorkRequest =
                    PeriodicWorkRequestBuilder<UploadToCloudWorker>(7, TimeUnit.DAYS)
                        .addTag(PRESENTLY_BACKUP)
                        .build()
                WorkManager.getInstance(requireContext()).enqueue(uploadWorkRequest)
            }
            BackupCadence.EVERY_CHANGE -> {
                //every change so do an upload now
                val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadToCloudWorker>()
                    .addTag(PRESENTLY_BACKUP)
                    .build()
                WorkManager.getInstance(requireContext()).enqueue(uploadWorkRequest)
            }
        }

    }

    private fun fireAnalyticsEventForCadence(
            cadence: BackupCadence,
            firebaseAnalytics: FirebaseAnalytics
    ) {
        val cadenceString = cadence.string
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, cadenceString)
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, cadenceString)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "cadence")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        var dialogFragment: DialogFragment? = null
        if (preference is TimePreference) {
            dialogFragment =
                TimePreferenceFragment()
            val bundle = Bundle(1)
            bundle.putString("key", preference.getKey())
            dialogFragment.setArguments(bundle)
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(parentFragmentManager, "DIALOG")
        } else {
                val dialogFragment =
                        CustomListPrefDialogFragCompat.newInstance(preference.getKey())
            dialogFragment?.setTargetFragment(this, 0)
            dialogFragment?.show(parentFragmentManager, null);

        }
    }

    override fun onDestroy() {
        splitInstallManager.unregisterListener(listener)
        super.onDestroy()
    }

    private fun openThemes() {
        firebaseAnalytics.logEvent(OPENED_THEMES, null)

        val fragment = ThemeFragment()
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.container_fragment, fragment)
            .addToBackStack(SETTINGS_TO_THEME)
            .commit()
    }

    private fun openTermsAndConditions() {
        firebaseAnalytics.logEvent(OPENED_TERMS_CONDITIONS, null)
        try {
            val browserIntent =
                Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://presently-app.firebaseapp.com/termsconditions.html")
                )
            startActivity(browserIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.recordException(activityNotFoundException)
        }
    }

    private fun openShareApp() {
        firebaseAnalytics.logEvent(OPENED_SHARE_APP, null)

        try {
            val appName = getString(R.string.app_name)
            val textIntent = Intent(Intent.ACTION_SEND)
            textIntent.type = "text/plain"
            textIntent.putExtra(Intent.EXTRA_SUBJECT, appName)

            val appPackageName = context?.packageName
            val shareApp = getString(R.string.share_app_text)
            val shareText =
                "$shareApp https://play.google.com/store/apps/details?id=$appPackageName"
            textIntent.putExtra(Intent.EXTRA_TEXT, shareText)

            val chooserIntent = Intent.createChooser(textIntent, appName)
            startActivity(chooserIntent);
        } catch (exception: Exception) {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.recordException(exception)
        }
    }

    private fun openPrivacyPolicy() {
        firebaseAnalytics.logEvent(OPENED_PRIVACY_POLICY, null)

        try {
            val browserIntent =
                Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://presently-app.firebaseapp.com/privacypolicy.html")
                )
            startActivity(browserIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.recordException(activityNotFoundException)
        }
    }

    private fun openFaq() {
        firebaseAnalytics.logEvent(OPENED_FAQ, null)

        try {
            val browserIntent =
                Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://presently-app.firebaseapp.com/faq.html")
                )
            startActivity(browserIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.recordException(activityNotFoundException)
        }
    }

    /**
     * Warns the user about importing a CSV file via an alert dialog
     * */
    private fun importFromCsv() {
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.import_data_dialog)
                setMessage(R.string.import_data_dialog_message)
                setPositiveButton(R.string.ok) { dialog, id ->
                    selectCSVFile()
                }
                setNegativeButton(R.string.cancel) { _, _ -> }
            }
            // Create the AlertDialog
            builder.create()
        }
        alertDialog?.show()
    }

    /**
     * Result contract for activity result to read from the backup CSV file
     * */
    private val readCsvResultContact =
        registerForActivityResult(OpenCsvDocumentContract()) { uri: Uri? ->
            if (uri != null) {
                if (uri.scheme == "content") {
                    val inputStream = activity?.contentResolver?.openInputStream(uri)
                    if (inputStream != null) {
                        importFromCsv(inputStream)
                    } else {
                        val crashlytics = FirebaseCrashlytics.getInstance()
                        crashlytics.recordException(NullPointerException("inputStream is null, uri: $uri"))
                        Toast.makeText(context, R.string.error_parsing, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } else {
                val crashlytics = FirebaseCrashlytics.getInstance()
                crashlytics.recordException(NullPointerException("URI was null when receiving file"))
                Toast.makeText(context, R.string.file_not_csv, Toast.LENGTH_SHORT).show()
            }
        }

    /**
     * Opens the chooser to allow the user to select their CSV file.
     * */
    private fun selectCSVFile() {
        firebaseAnalytics.logEvent(LOOKED_FOR_DATA, null)
        try {
            readCsvResultContact.launch("text/csv|text/comma-separated-values|application/csv")
        } catch (ex: ActivityNotFoundException) {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.recordException(ex)
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Takes the input stream, converts it to a list of entries, saves it to the
     * DB and lets the user know the result.
     * */
    private fun importFromCsv(inputStream: InputStream) {
        try {
            val parser = CSVParser.parse(
                    inputStream, Charset.defaultCharset(),
                    CSVFormat.DEFAULT
            )
            val realCsvParser = RealCsvParser(parser)
            val entries = convertCsvToEntries(realCsvParser)
            lifecycleScope.launch {
                repository.addEntries(entries)
                firebaseAnalytics.logEvent(IMPORTED_DATA_SUCCESS, null)
                parentFragmentManager.popBackStack()
            }

            //TODO move this hardcoded string to strings.xml
            Toast.makeText(context, "Imported successfully!", Toast.LENGTH_SHORT).show()
        } catch (exception: Exception) {
            firebaseAnalytics.logEvent(IMPORTING_BACKUP_ERROR, null)
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.recordException(exception)

            Toast.makeText(context, R.string.error_parsing, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Result contract for activity result to create backup the CSV file
     * */
    private val saveCsvResultContact =
        registerForActivityResult(CreateCsvDocumentContract()) { uri: Uri? ->
            if (uri != null) {
                lifecycleScope.launch {
                    val csvResult = exportEntriesToCsvFile(
                            requireContext(),
                            uri,
                            repository.getEntries()
                    )
                    when (csvResult) {
                        is CsvUriError -> exportCallback.onFailure(csvResult.exception)
                        is CsvUriCreated -> exportCallback.onSuccess(csvResult.uri)
                    }
                }
            } else {
                val crashlytics = FirebaseCrashlytics.getInstance()
                crashlytics.recordException(NullPointerException("URI was null after user selected file location"))
                Toast.makeText(
                        context,
                        R.string.error_creating_csv_file,
                        Toast.LENGTH_SHORT
                ).show()
            }

        }

    /**
     * Opens the Storage Access Framework and lets the user select where they want to
     * export the CSV file.
     * */
    private fun createFileOnDevice() {
        val date = LocalDateTime.now().withNano(0).toString().replace(':', '-')
        val fileName = "PresentlyBackup$date.csv"
        saveCsvResultContact.launch(fileName)
    }

    private val exportCallback: ExportCallback = object : ExportCallback {
        override fun onSuccess(uri: Uri) {
            Snackbar.make(view!!, R.string.export_success, Snackbar.LENGTH_LONG)
                .setAction(R.string.open) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(uri, "text/csv")
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        val crashlytics = FirebaseCrashlytics.getInstance()
                        crashlytics.recordException(e)
                        Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT)
                            .show()
                    }
                }.show()
        }

        override fun onFailure(exception: Exception) {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.recordException(exception)
            Toast.makeText(
                    context,
                    "Error : ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    companion object {
        const val BACKUP_TOKEN = "dropbox_pref"
        const val SETTINGS_TO_THEME = "SETTINGS_TO_THEME"
    }
}

interface ExportCallback {
    fun onSuccess(file: Uri)

    fun onFailure(exception: Exception)
}
