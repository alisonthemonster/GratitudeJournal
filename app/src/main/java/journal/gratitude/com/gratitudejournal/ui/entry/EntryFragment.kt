package journal.gratitude.com.gratitudejournal.ui.entry

import android.content.*
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.rxbinding2.widget.RxTextView
import com.presently.settings.BackupCadence
import com.presently.settings.PresentlySettings
import com.presently.sharing.view.SharingFragment
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.*
import journal.gratitude.com.gratitudejournal.ui.dialog.CelebrateDialogFragment
import journal.gratitude.com.gratitudejournal.util.backups.UploadToCloudWorker
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader
import com.presently.ui.setStatusBarColorsForBackground
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import journal.gratitude.com.gratitudejournal.util.toFullString
import kotlinx.android.synthetic.main.entry_fragment.*
import org.threeten.bp.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class EntryFragment : Fragment(R.layout.entry_fragment), MavericksView {

    private val viewModel: EntryViewModel by fragmentViewModel()
    @Inject lateinit var settings: PresentlySettings

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val passedInDate = arguments?.getString(ENTRY_DATE)
            viewModel.setDate(requireNotNull(passedInDate))
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                withState(viewModel, {
                    val isEdited = it.hasUserEdits
                    val isEmpty = it.isEmpty
                    if (isEdited && !isEmpty) {
                        showUnsavedEntryDialog()
                    } else {
                        parentFragmentManager.popBackStack()
                    }
                })
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        prepareWindow()

        prompt_button.setOnClickListener {
            val v = it as ImageView
            val d = v.drawable as Animatable?
            d?.start()

            firebaseAnalytics.logEvent(CLICKED_PROMPT, null)

            viewModel.changePrompt()
        }

        share_button.setOnClickListener {
            firebaseAnalytics.logEvent(SHARED_ENTRY, null)
            withState(viewModel, {
                openSharingScreen(it.entryContent, date.text.toString())
            })
        }

        save_button.setOnClickListener {
            viewModel.saveEntry()
        }

        val showQuote = settings.shouldShowQuote()
        if (!showQuote) {
            inspiration.visibility = View.GONE
        }

        inspiration.setOnLongClickListener {
            withState(viewModel, {
                val quote = it.quote
                val clipboard =
                    ContextCompat.getSystemService<ClipboardManager>(
                        requireContext(),
                        ClipboardManager::class.java
                    )
                clipboard?.setPrimaryClip(ClipData.newPlainText("Gratitude quote", quote))
                firebaseAnalytics.logEvent(COPIED_QUOTE, null)
                Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
            })
            true
        }

        val disposable = RxTextView.afterTextChangeEvents(entry_text)
            .debounce(500, TimeUnit.MILLISECONDS)
            .skip(1) //skip data binding
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                viewModel.onTextChanged(it.editable().toString())
            }
        compositeDisposable.add(disposable)
    }

    private fun openSharingScreen(entryContent: String, entryDate: String) {
        val fragment = SharingFragment.newInstance(entryDate, entryContent)
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.container_fragment, fragment)
            .addToBackStack(ENTRY_TO_SHARE)
            .commit()
    }

    override fun invalidate() = withState(viewModel) { state ->
        when (state.date) {
            LocalDate.now() -> {
                date.text = resources.getString(R.string.today)
                thankful_for.text = resources.getString(R.string.iam)
            }
            LocalDate.now().minusDays(1) -> {
                date.text = resources.getString(R.string.yesterday)
                thankful_for.text = resources.getString(R.string.iwas)
            }
            else -> {
                date.text = state.date.toFullString()
                thankful_for.text = resources.getString(R.string.iwas)
            }
        }
        inspiration.text = state.quote
        entry_text.hint = state.hint ?: getHintString(state.date)
        setEditText(state.entryContent)
        share_button.isVisible = !state.isEmpty
        prompt_button.isVisible = state.isEmpty
        if (state.isSaved) {
            onEntrySaved()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }

    private fun getHintString(date: LocalDate) = resources.getString(
        if (date == LocalDate.now()) R.string.what_are_you_thankful_for
        else R.string.what_were_you_thankful_for
    )

    private fun setEditText(newText: String) {
        val oldText = entry_text.text.toString()
        if (newText != oldText && newText.isNotEmpty()) {
            entry_text.setText(newText)
            entry_text.setSelection(newText.length)
        }
    }

    private fun prepareWindow() {
        ViewCompat.setOnApplyWindowInsetsListener(entry) { v, insets ->
            val sysWindow =
                insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.updatePadding(bottom = sysWindow.bottom, top = sysWindow.top)
            insets
        }

        val window = requireActivity().window
        window.statusBarColor = Color.TRANSPARENT
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(R.attr.backgroundColor, typedValue, true)
        setStatusBarColorsForBackground(window, typedValue.data)
    }

    private fun onEntrySaved() {
        val isNewEntry = requireNotNull(arguments?.getBoolean(ENTRY_IS_NEW))

        if (isNewEntry) {
            val numEntries = requireNotNull(arguments?.getInt(ENTRY_NUM_ENTRIES))
            val bundle = Bundle()
            bundle.putInt(FirebaseAnalytics.Param.LEVEL, (numEntries + 1))

            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle)
            if (Milestone.milestones.contains(numEntries + 1)) {
                CelebrateDialogFragment.newInstance(numEntries + 1)
                    .show(requireFragmentManager(), "CelebrateDialogFragment")
            }
        } else {
            firebaseAnalytics.logEvent(EDITED_EXISTING_ENTRY, null)
        }
        viewModel.saveEntry()
        val imm =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(entry_text.windowToken, 0)

        val accessToken = settings.getAccessToken()
        val cadence = settings.getAutomaticBackupCadence()
        if (accessToken != null && cadence == BackupCadence.EVERY_CHANGE) {
            val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadToCloudWorker>()
                .addTag(DropboxUploader.PRESENTLY_BACKUP)
                .build()
            WorkManager.getInstance(requireContext()).enqueue(uploadWorkRequest)
        }

        parentFragmentManager.popBackStack()
    }

    private fun showUnsavedEntryDialog() {
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.are_you_sure)
                setMessage(R.string.unsaved_text)
                setPositiveButton(R.string.continue_to_exit) { dialog, id ->
                    parentFragmentManager.popBackStack()
                }
                setNegativeButton(R.string.cancel) { _, _ -> }
            }
            // Create the AlertDialog
            builder.create()
        }
        alertDialog?.show()
    }

    companion object {
        fun newInstance(date: LocalDate, numEntries: Int?, isNewEntry: Boolean): EntryFragment {
            if (isNewEntry && numEntries == null) {
                throw IllegalArgumentException("New entries need to keep track of the total entries so far!")
            }
            val args = Bundle()
            args.putString(ENTRY_DATE, date.toString())
            numEntries?.let { args.putInt(ENTRY_NUM_ENTRIES, numEntries) }
            args.putBoolean(ENTRY_IS_NEW, isNewEntry)
            val fragment = EntryFragment()
            fragment.arguments = args
            return fragment
        }

        const val ENTRY_DATE = "ENTRY_DATE"
        const val ENTRY_NUM_ENTRIES = "ENTRY_NUM_ENTRIES"
        const val ENTRY_IS_NEW = "ENTRY_IS_NEW"
        const val ENTRY_TO_SHARE = "ENTRY_TO_SHARE"
    }
}