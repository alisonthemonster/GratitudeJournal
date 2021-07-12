package journal.gratitude.com.gratitudejournal.util.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.presently.settings.PresentlySettings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Receives broadcasts when the user changes time zone, changes their clock time, or if the device resets.
 * It updates the notification scheduler to reset the notification time if notifications are turned on.
 */
@AndroidEntryPoint
class NotificationResetReceiver: BroadcastReceiver() {

    //TODO test this

    @Inject
    lateinit var settings: PresentlySettings

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action.equals("android.intent.action.BOOT_COMPLETED") ||
            intent?.action.equals("android.intent.action.TIMEZONE_CHANGED") ||
            intent?.action.equals("android.intent.action.TIME_SET")) {
            val notificationScheduler = NotificationScheduler()
            notificationScheduler.configureNotifications(context, settings)
        }
    }
}