package journal.gratitude.com.gratitudejournal.util.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import com.presently.settings.PresentlySettings
import org.threeten.bp.LocalTime
import java.util.*

/**
 * Schedules or cancels repeating broadcasts to the ReminderReceiver at the specified time
 *
 */
class NotificationScheduler {

    companion object {
        const val ALARM_TYPE_RTC = 100
        const val PENDING_INTENT = 101
    }

    // Called when app starts, notification time changes, device reboots, time zone changes, etc
    fun configureNotifications(context: Context, settings: PresentlySettings) {
        val hasNotificationsOn = settings.hasEnabledNotifications()
        if (hasNotificationsOn) {
            val alarmTime = settings.getNotificationTime()
            setNotificationTime(context, alarmTime)
        }
    }

    fun setNotificationTime(context: Context, alarmTime: LocalTime) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val alarmIntent = intent.let {
            PendingIntent.getBroadcast(
                context,
                PENDING_INTENT,
                it,
                0 //implicitly cancel the existing alarm and then set it for the newly-specified time
            )
        }

        val alarmTimeCal = if (LocalTime.now().isAfter(alarmTime)) {
            //today's alarm already happened use start the next one tomorrow
            Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, alarmTime.hour)
                set(Calendar.MINUTE, alarmTime.minute)
                add(Calendar.DATE, 1)
            }
        } else {
            Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, alarmTime.hour)
                set(Calendar.MINUTE, alarmTime.minute)
            }
        }

        // Enable the reboot/timechange broadcast receiver
        // Programmatically enabling the receiver overrides the manifest setting, even across reboots
        val receiver = ComponentName(context, NotificationResetReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            alarmTimeCal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            alarmIntent
        )
    }

    fun disableNotifications(context: Context) {
        cancelNotifications(context)
        disableResetReceiver(context)
    }

    //cancels any existing notifications
    private fun cancelNotifications(context: Context) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = intent.let {
            PendingIntent.getBroadcast(
                context,
                PENDING_INTENT, it, 0
            )
        }

        pendingIntent.cancel()
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun disableResetReceiver(context: Context) {
        // disable the reboot/timechange broadcast receiver
        val receiver = ComponentName(context, NotificationResetReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}