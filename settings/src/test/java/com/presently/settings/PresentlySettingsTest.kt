package com.presently.settings

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
import org.junit.Test
import org.threeten.bp.LocalTime
import java.util.*

class PresentlySettingsTest {

    @Test
    fun `GIVEN RealPresentlySettings WHEN getCurrentTheme is called THEN shared preferences is called`() {
        val expected = "currTheme"
        val sharedPrefs = getFakeSharedPreferences(expected)
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.getCurrentTheme()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN setTheme is called THEN shared preferences is called`() {
        editStringWasCalled = false
        editString = ""
        val expected = "newTheme"
        val sharedPrefs = getFakeSharedPreferences()
        val settings = RealPresentlySettings(sharedPrefs)
        settings.setTheme(expected)

        assertThat(editStringWasCalled).isTrue()
        assertThat(editString).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN isBiometricsEnabled is called THEN shared preferences is called`() {
        val expected = true
        val sharedPrefs = getFakeSharedPreferences(boolean = expected)
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.isBiometricsEnabled()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings with more than 5 minutes WHEN shouldLockApp is called THEN shared preferences is called`() {
        val expected = true
        val fiveMinutesInThePast = Date(System.currentTimeMillis()).time - 300001L
        val sharedPrefs = getFakeSharedPreferences(long = fiveMinutesInThePast)
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.shouldLockApp()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings with less than 5 minutes WHEN shouldLockApp is called THEN shared preferences is called`() {
        val expected = false
        val now = Date(System.currentTimeMillis()).time
        val sharedPrefs = getFakeSharedPreferences(long = now)
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.shouldLockApp()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN setOnPauseTime is called THEN shared preferences is called`() {
        editLongWasCalled = false
        val sharedPrefs = getFakeSharedPreferences()
        val settings = RealPresentlySettings(sharedPrefs)
        settings.setOnPauseTime()

        assertThat(editLongWasCalled).isTrue()
    }

    @Test
    fun `GIVEN RealPresentlySettings and saturday is first day of week WHEN getFirstDayOfWeek is called THEN shared preferences is called`() {
        val expected = Calendar.SATURDAY
        val sharedPrefs = getFakeSharedPreferences(string = "0")
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.getFirstDayOfWeek()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings and sunday is first day of week WHEN getFirstDayOfWeek is called THEN shared preferences is called`() {
        val expected = Calendar.SUNDAY
        val sharedPrefs = getFakeSharedPreferences(string = "1")
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.getFirstDayOfWeek()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings and monday is first day of week WHEN getFirstDayOfWeek is called THEN shared preferences is called`() {
        val expected = Calendar.MONDAY
        val sharedPrefs = getFakeSharedPreferences(string = "2")
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.getFirstDayOfWeek()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN shouldShowQuote is called THEN shared preferences is called`() {
        val expected = false
        val sharedPrefs = getFakeSharedPreferences(boolean = expected)
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.shouldShowQuote()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings and daily cadence WHEN getAutomaticBackupCadence is called THEN shared preferences is called`() {
        val expected = BackupCadence.DAILY
        val sharedPrefs = getFakeSharedPreferences(string = "0")
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.getAutomaticBackupCadence()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings and weekly cadence WHEN getAutomaticBackupCadence is called THEN shared preferences is called`() {
        val expected = BackupCadence.WEEKLY
        val sharedPrefs = getFakeSharedPreferences(string = "1")
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.getAutomaticBackupCadence()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings and every change cadence WHEN getAutomaticBackupCadence is called THEN shared preferences is called`() {
        val expected = BackupCadence.EVERY_CHANGE
        val sharedPrefs = getFakeSharedPreferences(string = "2")
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.getAutomaticBackupCadence()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings with no language preference WHEN getLocale is called THEN locale is checked`() {
        val sharedPrefs = getFakeSharedPreferences(string = null)
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.getLocale()
        assertThat(actual).isNotNull()
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN getLocale is called THEN shared preferences is called`() {
        val expected = "fr"
        val sharedPrefs = getFakeSharedPreferences(string = expected)
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.getLocale()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN hasEnabledNotifications is called THEN shared preferences is called`() {
        val expected = false
        val sharedPrefs = getFakeSharedPreferences(boolean = expected)
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.hasEnabledNotifications()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings with no time set WHEN getNotificationTime is called THEN shared preferences is called`() {
        val expected = LocalTime.parse("21:00")
        val sharedPrefs = getFakeSharedPreferences(string = null)
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.getNotificationTime()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN getNotificationTime is called THEN shared preferences is called`() {
        val expected = LocalTime.parse("11:00")
        val sharedPrefs = getFakeSharedPreferences(string = "11:00")
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.getNotificationTime()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN getLinesPerEntryInTimeline is called THEN shared preferences is called`() {
        val expected = 12
        val sharedPrefs = getFakeSharedPreferences(int = expected)
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.getLinesPerEntryInTimeline()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN shouldShowDayOfWeekInTimeline is called THEN shared preferences is called`() {
        val expected = true
        val sharedPrefs = getFakeSharedPreferences(boolean = expected)
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.shouldShowDayOfWeekInTimeline()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings and no token WHEN getAccessToken is called THEN shared preferences is called`() {
        val expected =  null
        val sharedPrefs = getFakeSharedPreferences(string = expected)
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.getAccessToken()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN getAccessToken is called THEN shared preferences is called`() {
        val expected =  "accessToken"
        val sharedPrefs = getFakeSharedPreferences(string = expected)
        val settings = RealPresentlySettings(sharedPrefs)
        val actual = settings.getAccessToken()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN setAccessToken is called THEN shared preferences is called`() {
        editStringWasCalled = false
        editString = ""
        val expected = "newAccessToken"
        val sharedPrefs = getFakeSharedPreferences()
        val settings = RealPresentlySettings(sharedPrefs)
        settings.setAccessToken(expected)

        assertThat(editStringWasCalled).isTrue()
        assertThat(editString).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN clearAccessToken is called THEN shared preferences is called`() {
        removeWasCalled = false
        val sharedPrefs = getFakeSharedPreferences()
        val settings = RealPresentlySettings(sharedPrefs)
        settings.clearAccessToken()

        assertThat(removeWasCalled).isTrue()
    }

    var editStringWasCalled = false
    var editString = ""
    var editLongWasCalled = false
    var removeWasCalled = false

    private fun getFakeSharedPreferences(string: String? = "", int: Int = 0, long: Long = 0L, boolean: Boolean = false): SharedPreferences {
        val editor = object : SharedPreferences.Editor {
            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                editStringWasCalled = true
                editString = requireNotNull(value)
                return this
            }

            override fun putStringSet(
                key: String?,
                values: MutableSet<String>?
            ): SharedPreferences.Editor {
                fail("putStringSet should not be called")
                return this
            }

            override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
                fail("putInt should not be called")
                return this
            }

            override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
                editLongWasCalled = true
                return this
            }

            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
                fail("putFloat should not be called")
                return this
            }

            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                fail("putBoolean should not be called")
                return this
            }

            override fun remove(key: String?): SharedPreferences.Editor {
                removeWasCalled = true
                return this
            }

            override fun clear(): SharedPreferences.Editor {
                fail("clear should not be called")
                return this
            }

            override fun commit(): Boolean {
                return true
            }

            override fun apply() {}

        }

        return object : SharedPreferences {
            override fun getAll(): MutableMap<String, Any> {
                fail("getAll should be used")
                return mutableMapOf()
            }

            override fun getString(key: String?, defValue: String?): String? {
                return string ?: defValue
            }

            override fun getStringSet(
                key: String?,
                defValues: MutableSet<String>?
            ): MutableSet<String> {
                fail("getStringSet should be used")
                return mutableSetOf()
            }

            override fun getInt(key: String?, defValue: Int): Int {
                return int
            }

            override fun getLong(key: String?, defValue: Long): Long {
                return long
            }

            override fun getFloat(key: String?, defValue: Float): Float {
                fail("getFloat should be used")
                return 0f
            }

            override fun getBoolean(key: String?, defValue: Boolean): Boolean {
                return boolean
            }

            override fun contains(key: String?): Boolean {
                fail("contains should be used")
                return false
            }

            override fun edit(): SharedPreferences.Editor {
                return editor
            }

            override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
                fail("registerOnSharedPreferenceChangeListener should be used")
            }

            override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
                fail("unregisterOnSharedPreferenceChangeListener should be used")
            }
        }
    }
}