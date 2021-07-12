package journal.gratitude.com.gratitudejournal.di

import com.presently.settings.PresentlySettings
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SettingsEntryPoint {
    val settings: PresentlySettings
}