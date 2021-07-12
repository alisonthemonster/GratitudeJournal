package journal.gratitude.com.gratitudejournal.ui.calendar

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.presently.settings.PresentlySettings
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.getYearString
import journal.gratitude.com.gratitudejournal.util.toDate
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import journal.gratitude.com.gratitudejournal.util.toMonthString
import kotlinx.android.synthetic.main.calendar_fragment.view.*
import org.threeten.bp.LocalDate
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class EntryCalendarView : FrameLayout {

    @Inject lateinit var settings: PresentlySettings

    private var monthString = "${Date().toMonthString()} ${Date().getYearString()}"
    private var entryCalendarListener: EntryCalendarListener? = null
    private var writtenDates = emptyList<LocalDate>()

    private lateinit var calendar: CompactCalendarView

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet
    ) : super(context, attrs) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.calendar_fragment, this)

        val locale = Locale.getDefault()

        val firstDayOfWeek = settings.getFirstDayOfWeek()

        calendar = compactcalendar_view
        calendar.setFirstDayOfWeek(firstDayOfWeek)
        calendar.setLocale(TimeZone.getDefault(), locale)
        if (locale.language == "ar") {
            //use English characters since the library doesn't support Arabic
            calendar.setLocale(TimeZone.getDefault(), Locale.ENGLISH)
        }
        calendar.shouldDrawIndicatorsBelowSelectedDays(true)
        month_year.text = monthString

        compactcalendar_view.setListener(object :
            CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                if (!dateClicked.after(Date())) {
                    entryCalendarListener?.onDateClicked(
                        dateClicked,
                        !writtenDates.contains(dateClicked.toLocalDate()),
                        writtenDates.size
                    )
                }
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                monthString =
                    "${firstDayOfNewMonth.toMonthString()} ${firstDayOfNewMonth.getYearString()}"
                month_year.text = monthString
            }
        })

        close_button.setOnClickListener {
            entryCalendarListener?.onCloseClicked()
        }

        setOnClickListener {
            entryCalendarListener?.onCloseClicked()
        }
        random.setOnClickListener {
            val randomDate = writtenDates.randomOrNull()
            randomDate?.let {
                entryCalendarListener?.onDateClicked(
                    randomDate.toDate(),
                    false,
                    writtenDates.size
                )
            }
        }
    }

    fun setDayClickedListener(entryCalendarListener: EntryCalendarListener) {
        this.entryCalendarListener = entryCalendarListener
    }

    fun setWrittenDates(dates: List<LocalDate>) {
        calendar.removeAllEvents()
        for (date in dates) {
            calendar.addEvent(Event(getBackgroundColorForTheme(), date.toDate().time))
        }
        writtenDates = dates
        random.isVisible = dates.isNotEmpty()
    }

    private fun getBackgroundColorForTheme(): Int {
        val typedValue = TypedValue()
        val a = context.obtainStyledAttributes(
            typedValue.data,
            intArrayOf(android.R.attr.windowBackground)
        )
        val color = a.getColor(0, 0)
        a.recycle()

        return color
    }

}

interface EntryCalendarListener {
    fun onDateClicked(date: Date, isNewDate: Boolean, numberOfEntries: Int)

    fun onCloseClicked()
}
