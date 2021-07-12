package com.presently.settings.wiring

import android.content.Context
import android.content.SharedPreferences
import com.presently.settings.PresentlySettings
import com.presently.settings.RealPresentlySettings
import dagger.Provides
import androidx.preference.PreferenceManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object PresentlySettingsModule {

    @Provides
    fun providesPresentlySettings(sharedPreferences: SharedPreferences): PresentlySettings {
        return RealPresentlySettings(sharedPreferences)
    }

    @Provides
    fun providesSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

}