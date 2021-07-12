package journal.gratitude.com.gratitudejournal.ui

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.testUtils.launchFragmentInHiltContainer
import journal.gratitude.com.gratitudejournal.ui.themes.ThemeFragment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ThemesFragmentInstrumentedTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun themes_are_presented() {
        launchFragmentInHiltContainer<ThemeFragment>(
            themeResId = R.style.Base_AppTheme
        )

        Espresso.onView(ViewMatchers.withId(R.id.themes))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }


}