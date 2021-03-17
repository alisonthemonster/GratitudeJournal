package com.presently.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GratitudeQuoteWidgetTests {

    val applicationContext = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun getRemoteViewForSizeWithSmallUsesSmallLayout() {
        val actual = getRemoteViewForSize(applicationContext, WidgetSize.SMALL)
        assertThat(actual.layoutId).isEqualTo(R.layout.small_quote)
    }

    @Test
    fun getRemoteViewForSizeWithMediumUsesMediumLayout() {
        val actual = getRemoteViewForSize(applicationContext, WidgetSize.MEDIUM)
        assertThat(actual.layoutId).isEqualTo(R.layout.medium_quote)
    }

    @Test
    fun getRemoteViewForSizeWithLargeUsesLargeLayout() {
        val actual = getRemoteViewForSize(applicationContext, WidgetSize.LARGE)
        assertThat(actual.layoutId).isEqualTo(R.layout.large_quote)
    }

    @Test
    fun getRemoteViewForSizeWithXLargeUsesXLargeLayout() {
        val actual = getRemoteViewForSize(applicationContext, WidgetSize.XLARGE)
        assertThat(actual.layoutId).isEqualTo(R.layout.extra_large_quote)
    }

    @Test
    fun getViewSizeForSmallestWidgetGetsSMALL() {
        val expected = WidgetSize.SMALL
        val rows = 1
        val cols = 2
        val actual = getViewSize(rows, cols)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun getViewSizeForSkinnyWidgetGetsSMALL() {
        val expected = WidgetSize.SMALL
        val rows = 1
        val cols = 3
        val actual = getViewSize(rows, cols)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun getViewSizeForSkinnyButLongWidgetGetsMEDIUM() {
        val expected = WidgetSize.MEDIUM
        val rows = 1
        val cols = 4
        val actual = getViewSize(rows, cols)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun getViewSizeForVeryLongSkinnyWidgetGetsMEDIUM() {
        val expected = WidgetSize.MEDIUM
        val rows = 1
        val cols = 5
        val actual = getViewSize(rows, cols)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun getViewSizeForSmallSquareWidgetGetsMEDIUM() {
        val expected = WidgetSize.MEDIUM
        val rows = 2
        val cols = 2
        val actual = getViewSize(rows, cols)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun getViewSizeForArea6LandscapeWidgetGetsLARGE() {
        val expected = WidgetSize.LARGE
        val rows = 2
        val cols = 3
        val actual = getViewSize(rows, cols)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun getViewSizeForArea6PortraitWidgetGetsLARGE() {
        val expected = WidgetSize.LARGE
        val rows = 3
        val cols = 2
        val actual = getViewSize(rows, cols)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun getViewSizeForArea8WidgetGetsLARGE() {
        val expected = WidgetSize.LARGE
        val rows = 2
        val cols = 4
        val actual = getViewSize(rows, cols)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun getViewSizeForArea9WidgetGetsXLARGE() {
        val expected = WidgetSize.XLARGE
        val rows = 3
        val cols = 3
        val actual = getViewSize(rows, cols)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun getViewSizeForArea10WidgetGetsXLARGE() {
        val expected = WidgetSize.XLARGE
        val rows = 2
        val cols = 5
        val actual = getViewSize(rows, cols)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun getQuotesForSizeTest() {
        val quotes = listOf(
            "aaaaaaaaaa", //10
            "aaaaaaaaaaaaaaaaaaaa", //20
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", //80
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", //90
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" //150
        )
        val expectedSmallList = listOf("aaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaa")
        val expectedMediumList = listOf(
            "aaaaaaaaaa",
            "aaaaaaaaaaaaaaaaaaaa",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        )
        val expectedLargeList = listOf(
            "aaaaaaaaaa", //10
            "aaaaaaaaaaaaaaaaaaaa", //20
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", //80
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" //90
        )

        val expectedXLargeList = listOf(
            "aaaaaaaaaa", //10
            "aaaaaaaaaaaaaaaaaaaa", //20
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", //80
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", //90
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        )

        assertThat(getQuotesForSize(quotes, WidgetSize.SMALL)).isEqualTo(expectedSmallList)
        assertThat(getQuotesForSize(quotes, WidgetSize.MEDIUM)).isEqualTo(expectedMediumList)
        assertThat(getQuotesForSize(quotes, WidgetSize.LARGE)).isEqualTo(expectedLargeList)
        assertThat(getQuotesForSize(quotes, WidgetSize.XLARGE)).isEqualTo(expectedXLargeList)

    }

}