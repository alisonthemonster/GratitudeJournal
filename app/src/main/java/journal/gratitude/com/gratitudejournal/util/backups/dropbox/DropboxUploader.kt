package journal.gratitude.com.gratitudejournal.util.backups.dropbox

import android.content.Context
import androidx.work.WorkManager
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.presently.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.BuildConfig
import journal.gratitude.com.gratitudejournal.model.CloudUploadResult
import journal.gratitude.com.gratitudejournal.model.UploadError
import journal.gratitude.com.gratitudejournal.model.UploadSuccess
import journal.gratitude.com.gratitudejournal.util.backups.CloudProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class DropboxUploader(val context: Context, val settings: PresentlySettings):
    CloudProvider {

    override suspend fun uploadToCloud(file: File): CloudUploadResult {
        return withContext(Dispatchers.IO) {
            val accessToken = settings.getAccessToken()
            val requestConfig = DbxRequestConfig.newBuilder("PresentlyAndroid")
                .build()

            val client = DbxClientV2(requestConfig, accessToken)

            try {
                FileInputStream(file).use { inputStream ->
                    client.files().uploadBuilder("/presently-backup.csv")
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream)
                    UploadSuccess
                }
            } catch (e: DbxException) {
                UploadError(e)
            } catch (e: IOException) {
                UploadError(e)
            }
        }
    }

    companion object {

        fun authorizeDropboxAccess(context: Context, settings: PresentlySettings) {
            settings.setAccessToken("attempted")
            Auth.startOAuth2Authentication(context, BuildConfig.DROPBOX_APP_KEY)
        }

        suspend fun deauthorizeDropboxAccess(context: Context, settings: PresentlySettings) {
            withContext(Dispatchers.IO) {
                val accessToken = settings.getAccessToken()
                val requestConfig = DbxRequestConfig.newBuilder("PresentlyAndroid")
                    .build()
                val client = DbxClientV2(requestConfig, accessToken)
                client.auth().tokenRevoke()

                settings.clearAccessToken()
                WorkManager.getInstance(context).cancelAllWorkByTag(PRESENTLY_BACKUP)
            }
        }

        const val PRESENTLY_BACKUP = "PRESENTLY_BACKUP"
    }

}