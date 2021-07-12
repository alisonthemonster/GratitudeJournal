package journal.gratitude.com.gratitudejournal.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import journal.gratitude.com.gratitudejournal.room.EntryDao
import journal.gratitude.com.gratitudejournal.room.EntryDatabase


@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    fun provideEntryDao(
        database: EntryDatabase
    ): EntryDao {
        return database.entryDao()
    }

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): EntryDatabase {
        return EntryDatabase.getDatabase(context)
    }

}