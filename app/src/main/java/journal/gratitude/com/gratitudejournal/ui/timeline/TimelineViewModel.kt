package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.Milestone.Companion.milestones
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class TimelineViewModel @Inject constructor(private val repository: EntryRepository) : ViewModel() {

    val entries = MutableLiveData<List<TimelineItem>>()
    val datesWritten: LiveData<List<LocalDate>> = repository.getWrittenDates()

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    init {
        scope.launch {
            repository.getEntriesFlow().collect { list ->
                val today = LocalDate.now()
                val yesterday = LocalDate.now().minusDays(1)
                when {
                    list.isEmpty() -> {
                        entries.value = listOf<TimelineItem>(
                            Entry(today, ""),
                            Entry(yesterday, "")
                        )
                    }
                    list.size < 2 -> {
                        //user has only ever written one day
                        val newList = mutableListOf<TimelineItem>()
                        newList.addAll(list)
                        if (list[0].entryDate != today) {
                            newList.add(0, Entry(today, ""))
                        }
                        if (list[0].entryDate != yesterday) {
                            newList.add(1, Entry(yesterday, ""))
                        }
                        entries.value = newList
                    }
                    else -> {
                        val latest = list[0]
                        val listWithHints = mutableListOf<Entry>()
                        listWithHints.addAll(list)
                        if (latest.entryDate != today) {
                            //they dont have the latest
                            listWithHints.add(0, Entry(today, ""))
                        }
                        if (listWithHints[1].entryDate != yesterday) {
                            listWithHints.add(1, Entry(yesterday, ""))
                        }

                        val listWithHintsAndMilestones = mutableListOf<TimelineItem>()

                        var numEntries = 0
                        for (index in listWithHints.size - 1 downTo 0) {
                            listWithHintsAndMilestones.add(0, listWithHints[index])
                            if (listWithHints[index].entryContent.isNotEmpty()) {
                                numEntries++
                                if (milestones.contains(numEntries)) {
                                    listWithHintsAndMilestones.add(0, Milestone.create(numEntries))
                                }
                            }
                        }
                        entries.value = listWithHintsAndMilestones
                    }
                }
            }

        }
    }

    fun getTimelineItems(): List<TimelineItem> {
        return entries.value ?: emptyList()
    }

    fun addEntries(entries: List<Entry>) = scope.launch(Dispatchers.IO) {
        repository.addEntries(entries)
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}
