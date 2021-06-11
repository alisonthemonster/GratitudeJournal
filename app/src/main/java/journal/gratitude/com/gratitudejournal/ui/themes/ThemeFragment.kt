package journal.gratitude.com.gratitudejournal.ui.themes

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.Param.*
import com.presently.ui.setStatusBarColorsForBackground
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.THEME
import journal.gratitude.com.gratitudejournal.model.Theme
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment.Companion.THEME_PREF
import kotlinx.android.synthetic.main.fragment_theme.*

class ThemeFragment : Fragment(R.layout.fragment_theme) {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var listener = object : OnThemeSelectedListener {
        override fun onThemeSelected(theme: String) {
            PreferenceManager.getDefaultSharedPreferences(activity)
                .edit()
                .putString(THEME_PREF, theme)
                .apply()
            val bundle = bundleOf(
                ITEM_NAME to theme,
                ITEM_ID to theme,
                CONTENT_TYPE to "theme"
            )
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
            firebaseAnalytics.setUserProperty(THEME, theme)
            findNavController().navigateUp()
            activity?.recreate()
        }
    }

    private val adapter = ThemeListAdapter(listener = listener)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        val themeList: List<Theme> = listOf(
            Theme(
                "Original",
                ContextCompat.getColor(requireContext(), R.color.originalTimelineColor),
                ContextCompat.getColor(requireContext(), R.color.originalBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.originalBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.originalTimelineColor),
                R.drawable.ic_flower
            ),
            Theme(
                "Dawn",
                ContextCompat.getColor(requireContext(), R.color.dawnToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.dawnBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.dawnToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.dawnMainTextAndButtonColor),
                R.drawable.ic_sun_icon
            ),
            Theme(
                "Daisy",
                ContextCompat.getColor(requireContext(), R.color.daisyToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.daisyBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.daisyToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.daisyMainTextAndButtonColor),
                R.drawable.daisies,
                true
            ),
            Theme(
                "Rem'mie",
                ContextCompat.getColor(requireContext(), R.color.loveToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.loveBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.loveToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.loveMainTextAndButtonColor),
                R.drawable.ic_rainbow,
                true
            ),
            Theme(
                "Marsha",
                ContextCompat.getColor(requireContext(), R.color.marshaToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.marshaBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.marshaToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.marshaMainTextAndButtonColor),
                R.drawable.ic_trans_hearts,
                true
            ),
            Theme(
                "Brayla",
                ContextCompat.getColor(requireContext(), R.color.braylaToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.braylaBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.braylaToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.braylaMainTextAndButtonColor),
                R.drawable.ic_brayla,
                true
            ),
            Theme(
                "Tulip",
                ContextCompat.getColor(requireContext(), R.color.tulipToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.tulipBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.tulipToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.tulipMainTextAndButtonColor),
                R.drawable.ic_tulip,
                true
            ),
            Theme(
                "Waves",
                ContextCompat.getColor(requireContext(), R.color.wavesToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.wavesBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.wavesToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.wavesMainTextAndButtonColor),
                R.drawable.ic_wave
            ),
            Theme(
                "Midnight",
                ContextCompat.getColor(requireContext(), R.color.midnightToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.midnightBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.midnightToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.midnightMainTextAndButtonColor),
                R.drawable.ic_moon
            ),
            Theme(
                "Sunlight",
                ContextCompat.getColor(requireContext(), R.color.sunlightToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.sunlightBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.sunlightToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.sunlightMainTextAndButtonColor),
                R.drawable.ic_sunshine,
                true
            ),
            Theme(
                "Katie",
                ContextCompat.getColor(requireContext(), R.color.katieToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.katieBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.katieToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.katieMainTextAndButtonColor),
                R.drawable.ic_katie,
                true
            ),
            Theme(
                "Brittany",
                ContextCompat.getColor(requireContext(), R.color.brittanyToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.brittanyBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.brittanyToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.brittanyMainTextAndButtonColor),
                R.drawable.ic_brittany,
                true
            ),
            Theme(
                "Matisse",
                ContextCompat.getColor(requireContext(), R.color.matisseToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.matisseBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.matisseToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.matisseMainTextAndButtonColor),
                R.drawable.ic_matisse
            ),
            Theme(
                "Jungle",
                ContextCompat.getColor(requireContext(), R.color.jungleToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.jungleBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.jungleToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.jungleMainTextAndButtonColor),
                R.drawable.ic_tiger,
                true
            ),
            Theme(
                "Monstera",
                ContextCompat.getColor(requireContext(), R.color.monsteraToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.monsteraBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.monsteraToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.monsteraMainTextAndButtonColor),
                R.drawable.ic_monstera
            ),
            Theme(
                "Julie",
                ContextCompat.getColor(requireContext(), R.color.julieToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.julieBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.julieToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.julieMainTextAndButtonColor),
                R.drawable.ic_julie,
                true
            ),
            Theme(
                "Clouds",
                ContextCompat.getColor(requireContext(), R.color.cloudsToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.cloudsBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.cloudsToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.cloudsMainTextAndButtonColor),
                R.drawable.clouds
            ),
            Theme(
                "Wesley",
                ContextCompat.getColor(requireContext(), R.color.wesleyToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.wesleyBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.wesleyToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.wesleyMainTextAndButtonColor),
                R.drawable.ic_cube
            ),
            Theme(
                "Beach",
                ContextCompat.getColor(requireContext(), R.color.beachToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.beachBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.beachToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.beachMainTextAndButtonColor),
                R.drawable.ic_shell
            ),
            Theme(
                "Ellen",
                ContextCompat.getColor(requireContext(), R.color.ellenToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.ellenBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.ellenToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.ellenMainTextAndButtonColor),
                R.drawable.ic_ellen,
                true
            ),
            Theme(
                "Western",
                ContextCompat.getColor(requireContext(), R.color.westernToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.westernBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.westernToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.westernMainTextAndButtonColor),
                R.drawable.ic_cactus
            ),
            Theme(
                "Lotus",
                ContextCompat.getColor(requireContext(), R.color.lotusToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.lotusBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.lotusToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.lotusMainTextAndButtonColor),
                R.drawable.ic_lotus
            ),
            Theme(
                "Sunset",
                ContextCompat.getColor(requireContext(), R.color.sunsetToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.sunsetBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.sunsetToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.sunsetAndroidWidgetColor),
                R.drawable.ic_sun_icon
            ),
            Theme(
                "Danah",
                ContextCompat.getColor(requireContext(), R.color.danahToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.danahBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.danahToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.danahMainTextAndButtonColor),
                R.drawable.ic_danah,
                true
            ),
            Theme(
                "Field",
                ContextCompat.getColor(requireContext(), R.color.fieldToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.fieldBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.fieldToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.fieldMainTextAndButtonColor),
                R.drawable.ic_field
            ),
            Theme(
                "Rosie",
                ContextCompat.getColor(requireContext(), R.color.rosieToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.rosieBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.rosieToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.rosieMainTextAndButtonColor),
                R.drawable.ic_rosie,
                true
            ),
            Theme(
                "Glacier",
                ContextCompat.getColor(requireContext(), R.color.glacierToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.glacierBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.glacierToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.glacierMainTextAndButtonColor),
                R.drawable.ic_cube
            ),

            Theme(
                "Ahalya",
                ContextCompat.getColor(requireContext(), R.color.ahalyaToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.ahalyaBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.ahalyaToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.ahalyaMainTextAndButtonColor),
                R.drawable.ic_butterfly,
                true
            ),
            Theme(
                "Moonlight",
                ContextCompat.getColor(requireContext(), R.color.moonlightToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.moonlightBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.moonlightToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.moonlightMainTextAndButtonColor),
                R.drawable.ic_moon
            ),
            Theme(
                "Ivy",
                ContextCompat.getColor(requireContext(), R.color.ivyToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.ivyBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.ivyToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.ivyMainTextAndButtonColor),
                R.drawable.ic_flower
            ),
            Theme(
                "Moss",
                ContextCompat.getColor(requireContext(), R.color.mossToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.mossBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.mossToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.mossMainTextAndButtonColor),
                R.drawable.ic_flower
            ),
            Theme(
                "Gelato",
                ContextCompat.getColor(requireContext(), R.color.gelatoToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.gelatoBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.gelatoToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.gelatoMainTextAndButtonColor),
                R.drawable.ic_flower
            ),
            Theme(
                "Clean",
                ContextCompat.getColor(requireContext(), R.color.cleanToolbarColor),
                ContextCompat.getColor(requireContext(), R.color.cleanBackgroundColor),
                ContextCompat.getColor(requireContext(), R.color.cleanToolbarItemColor),
                ContextCompat.getColor(requireContext(), R.color.cleanMainTextAndButtonColor),
                R.drawable.ic_flower
            )
        )

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val currentTheme = sharedPref.getString(THEME_PREF, "original") ?: "original"
        adapter.selectedTheme = currentTheme

        adapter.addData(themeList)

        // Set the adapter
        themes.layoutManager = GridLayoutManager(context, 3)
        themes.adapter = adapter

        back_icon.setOnClickListener {
            findNavController().navigateUp()
        }

        ViewCompat.setOnApplyWindowInsetsListener(theme_container) { v, insets ->
            v.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top)
            insets
        }

        val window = requireActivity().window
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(com.presently.sharing.R.attr.toolbarColor, typedValue, true)
        setStatusBarColorsForBackground(window, typedValue.data)
        window.statusBarColor = typedValue.data
    }

    interface OnThemeSelectedListener {
        fun onThemeSelected(theme: String)
    }

}
