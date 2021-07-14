package com.presently.settings

import android.content.SharedPreferences
import com.dropbox.core.oauth.DbxCredential
import com.presently.settings.model.*
import java.util.*
import org.threeten.bp.LocalTime
import javax.inject.Inject
import javax.inject.Named

class RealPresentlySettings @Inject constructor(
    private val sharedPrefs: SharedPreferences,
    @Named("AppKey") private val appKey: String
) : PresentlySettings {

    override fun getCurrentTheme(): String {
        return sharedPrefs.getString(THEME_PREF, "original") ?: "original"
    }

    override fun setTheme(themeName: String) {
        sharedPrefs.edit()
            .putString(THEME_PREF, themeName)
            .apply()
    }

    override fun isBiometricsEnabled(): Boolean {
        return sharedPrefs.getBoolean(FINGERPRINT, false)
    }

    override fun shouldLockApp(): Boolean {
        val lastDestroyTime = sharedPrefs.getLong(ON_PAUSE_TIME, -1L)
        val currentTime = Date(System.currentTimeMillis()).time
        val diff = currentTime - lastDestroyTime
        //if more than 5 minutes (300000ms) have passed since last destroy, lock out user
        return diff > 300000L
    }

    override fun setOnPauseTime() {
        val date = Date(System.currentTimeMillis())
        sharedPrefs.edit().putLong(ON_PAUSE_TIME, date.time).apply()
    }

    override fun getFirstDayOfWeek(): Int {
        return when (sharedPrefs.getString(FIRST_DAY_OF_WEEK, "monday")) {
            "0" -> Calendar.SATURDAY
            "1" -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }
    }

    override fun shouldShowQuote(): Boolean {
        return sharedPrefs.getBoolean(SHOW_QUOTE, true)
    }

    override fun getAutomaticBackupCadence(): BackupCadence {
        return when (sharedPrefs.getString(BACKUP_CADENCE, "0") ?: "0") {
            "0" -> BackupCadence.DAILY
            "1" -> BackupCadence.WEEKLY
            else -> BackupCadence.EVERY_CHANGE
        }
    }

    override fun getLocale(): String {
        val languagePref = sharedPrefs.getString(APP_LANGUAGE, NO_LANG_PREF) ?: NO_LANG_PREF
        return if (languagePref == NO_LANG_PREF) {
            getDeviceLanguage()
        } else {
            languagePref
        }
    }

    override fun hasEnabledNotifications(): Boolean {
        return sharedPrefs.getBoolean(NOTIFS, true)
    }

    override fun getNotificationTime(): LocalTime {
        val prefTime = sharedPrefs.getString(NOTIF_PREF_TIME, "21:00")
        return LocalTime.parse(prefTime)
    }

    override fun getLinesPerEntryInTimeline(): Int {
        return sharedPrefs.getInt(LINES_PER_ENTRY_IN_TIMELINE, 10)
    }

    override fun shouldShowDayOfWeekInTimeline(): Boolean {
        return sharedPrefs.getBoolean(DAY_OF_WEEK, false) ?: false
    }

    override fun getAccessToken(): DbxCredential? {
        val serializedToken = sharedPrefs.getString(ACCESS_TOKEN, null) ?: return null
        if (serializedToken == "attempted") return null
        if (serializedToken.contains("{")) {
            //this is a Dropbox auth user with refresh tokens
            return DbxCredential.Reader.readFully(serializedToken)
        }
        return DbxCredential(serializedToken) //this is a legacy Dropbox auth user with a long lasting token
    }

    override fun setAccessToken(newToken: DbxCredential) {
        sharedPrefs.edit().putString(ACCESS_TOKEN, newToken.toString()).apply()
    }

    override fun markDropboxAuthInitiated() {
        sharedPrefs.edit().putString(ACCESS_TOKEN, "attempted").apply()
    }

    override fun clearAccessToken() {
        sharedPrefs.edit().remove(ACCESS_TOKEN).apply()
    }

    override fun getDropboxAppKey(): String {
        return appKey
    }

    private fun getDeviceLanguage(): String {
        return Locale.getDefault().toLanguageTag()
    }

}