package journal.gratitude.com.gratitudejournal.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.presently.settings.PresentlySettings
import com.presently.settings.model.NO_LANG_PREF
import java.util.*

object LocaleHelper {
    fun onAppAttached(context: Context, settings: PresentlySettings): Context {
        return updateLanguage(context, settings)
    }

    private fun updateLanguage(context: Context, settings: PresentlySettings): Context {
        val language = getLastLanguageSaved(settings)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateConfiguration(language, context)
        } else {
            updateConfigurationLegacy(context, language)
        }
    }

    @Suppress("DEPRECATION")
    private fun updateConfigurationLegacy(
        context: Context,
        language: String
    ): Context {
        val configuration = getConfiguration(context, language)
        val resources = context.resources
        resources.updateConfiguration(configuration, resources.displayMetrics)

        return context
    }

    private fun updateConfiguration(
        language: String,
        context: Context
    ): Context {
        val locale = getLocale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }

    /**
     * If the user has not selected a language then we should fall back to what
     * their device is using. If we do not support their language then the app
     * falls back to English.
     * */
    private fun getLastLanguageSaved(settings: PresentlySettings): String {
        val languagePref = settings.getLocale()
        return if (languagePref == NO_LANG_PREF) {
            getDeviceLanguage()
        } else {
            languagePref
        }
    }

    private fun getDeviceLanguage(): String {
        return Locale.getDefault().toLanguageTag()
    }

    private fun getConfiguration(
        context: Context,
        language: String
    ): Configuration {
        val locale = getLocale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)

        return configuration
    }

    private fun getLocale(language: String): Locale {
        return if (language.contains("-")) {
            val codes = language.split("-")
            Locale(codes[0], codes[1])
        } else {
            Locale(language)
        }
    }
}