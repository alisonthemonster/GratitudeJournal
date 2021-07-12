package journal.gratitude.com.gratitudejournal.ui.timeline

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.presently.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.databinding.TimelineFragmentBinding
import journal.gratitude.com.gratitudejournal.model.*
import journal.gratitude.com.gratitudejournal.ui.calendar.CalendarAnimation
import journal.gratitude.com.gratitudejournal.ui.calendar.EntryCalendarListener
import com.presently.ui.setStatusBarColorsForBackground
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import journal.gratitude.com.gratitudejournal.ui.search.SearchFragment
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import kotlinx.android.synthetic.main.timeline_fragment.*
import org.threeten.bp.LocalDate
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class TimelineFragment : Fragment() {

    private val viewModel: TimelineViewModel by viewModels()
    @Inject lateinit var settings: PresentlySettings

    private lateinit var adapter: TimelineAdapter
    private lateinit var binding: TimelineFragmentBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (entry_calendar?.isVisible != true) {
                    if (parentFragmentManager.backStackEntryCount > 0) {
                        parentFragmentManager.popBackStack()
                    } else {
                        //nothing left in back stack we can finish the activity
                        requireActivity().finish()
                    }
                } else {
                    val animation = CalendarAnimation(cal_fab, entry_calendar)
                    animation.closeCalendar()
                }
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TimelineFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        timeline_recycler_view.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)

        val showDayOfWeek = settings.shouldShowDayOfWeekInTimeline()
        val linesPerEntry = settings.getLinesPerEntryInTimeline()
        adapter = TimelineAdapter(
            requireActivity(),
            showDayOfWeek,
            linesPerEntry,
            object : TimelineAdapter.OnClickListener {
                override fun onClick(
                    view: View,
                    clickedDate: LocalDate,
                    isNewEntry: Boolean,
                    numEntries: Int
                ) {
                    if (isNewEntry) {
                        firebaseAnalytics.logEvent(CLICKED_NEW_ENTRY, null)
                    } else {
                        firebaseAnalytics.logEvent(CLICKED_EXISTING_ENTRY, null)
                    }
                    navigateToDate(clickedDate, isNewEntry, numEntries)
                }
            })
        timeline_recycler_view.adapter = adapter

        overflow_button.setOnClickListener {
            PopupMenu(context, it).apply {
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.notification_settings -> {
                            openSettings()
                            true
                        }
                        R.id.contact_us -> {
                            openContactForm()
                            true
                        }
                        else -> false
                    }
                }
                inflate(R.menu.overflow_menu)
                show()
            }
        }

        viewModel.entries.observe(viewLifecycleOwner, Observer {
            binding.viewModel = viewModel
        })

        viewModel.datesWritten.observe(viewLifecycleOwner, Observer { dates ->
            entry_calendar.setWrittenDates(dates)
        })

        search_icon.setOnClickListener {
            firebaseAnalytics.logEvent(CLICKED_SEARCH, null)
            openSearchScreen()
        }

        entry_calendar.setDayClickedListener(object : EntryCalendarListener {
            override fun onCloseClicked() {
                val animation = CalendarAnimation(cal_fab, entry_calendar)
                animation.closeCalendar()
            }

            override fun onDateClicked(date: Date, isNewDate: Boolean, numberOfEntries: Int) {
                if (isNewDate) {
                    firebaseAnalytics.logEvent(CLICKED_NEW_ENTRY_CALENDAR, null)
                } else {
                    firebaseAnalytics.logEvent(CLICKED_EXISTING_ENTRY_CALENDAR, null)
                }

                navigateToDate(date.toLocalDate(), isNewDate, numberOfEntries)
            }
        })

        cal_fab.setOnClickListener {
            firebaseAnalytics.logEvent(OPENED_CALENDAR, null)

            val animation = CalendarAnimation(cal_fab, entry_calendar)
            animation.openCalendar()
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            v.updatePadding(
                top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
                bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            )
            insets
        }

        val window = requireActivity().window
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(R.attr.toolbarColor, typedValue, true)
        window.statusBarColor = typedValue.data
        setStatusBarColorsForBackground(window, typedValue.data)
    }

    private fun openSearchScreen() {
        val fragment = SearchFragment()
        parentFragmentManager
            .beginTransaction()
            .addSharedElement(binding.searchIcon, "search_transition")
            .replace(R.id.container_fragment, fragment)
            .addToBackStack(TIMELINE_TO_SEARCH)
            .commit()
    }

    private fun navigateToDate(clickedDate: LocalDate, isNewEntry: Boolean, numEntries: Int) {
        val fragment = EntryFragment.newInstance(clickedDate, numEntries, isNewEntry)
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.container_fragment, fragment)
            .addToBackStack(TIMELINE_TO_ENTRY)
            .commit()
    }

    private fun openContactForm() {
        firebaseAnalytics.logEvent(OPENED_CONTACT_FORM, null)

        val context = context ?: return
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")

            val emails = arrayOf("gratitude.journal.app@gmail.com")
            val subject = "In App Feedback"
            putExtra(Intent.EXTRA_EMAIL, emails)
            putExtra(Intent.EXTRA_SUBJECT, subject)

            val packageName = context.packageName
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            val text = """
                Device: ${Build.MODEL}
                OS Version: ${Build.VERSION.RELEASE}
                App Version: ${packageInfo.versionName}
                
                
                """.trimIndent()
            putExtra(Intent.EXTRA_TEXT, text)
        }

        try {
            startActivity(intent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.recordException(activityNotFoundException)
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openSettings() {
        firebaseAnalytics.logEvent(LOOKED_AT_SETTINGS, null)

        val fragment = SettingsFragment()
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.container_fragment, fragment)
            .addToBackStack(TIMELINE_TO_SETTINGS)
            .commit()
    }

    companion object {
        fun newInstance() = TimelineFragment()

        const val TIMELINE_TO_ENTRY = "TIMELINE_TO_ENTRY"
        const val TIMELINE_TO_SEARCH = "TIMELINE_TO_SEARCH"
        const val TIMELINE_TO_SETTINGS = "TIMELINE_TO_ENTRY"
    }
}

