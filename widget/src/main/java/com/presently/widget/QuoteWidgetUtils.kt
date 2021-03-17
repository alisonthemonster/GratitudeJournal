package com.presently.widget

import android.content.Context
import android.widget.RemoteViews

/**
 * Returns a list of quotes that are the appropriate length for the WidgetSize
 *
 * @param quotes Full list of quotes
 * @param widgetSize The WidgetSize of the widget
 * @return List<String>
 */
internal fun getQuotesForSize(quotes: List<String>, widgetSize: WidgetSize): List<String> {
    return quotes.filter { quote ->
        when (widgetSize) {
            WidgetSize.SMALL -> (quote.length < 70)
            WidgetSize.MEDIUM -> (quote.length < 90)
            WidgetSize.LARGE -> (quote.length < 150)
            WidgetSize.XLARGE -> true //add all lengths to xlarge list
        }
    }
}

/**
 * Returns the RemoteViews for the current widget size
 *
 * @param context Context
 * @param size The WidgetSize of the widget
 * @return RemoteView
 */
internal fun getRemoteViewForSize(context: Context, size: WidgetSize): RemoteViews {
    if (size == WidgetSize.SMALL) return RemoteViews(context.packageName, R.layout.small_quote)
    if (size == WidgetSize.MEDIUM) return RemoteViews(context.packageName, R.layout.medium_quote)
    if (size == WidgetSize.LARGE) return RemoteViews(context.packageName, R.layout.large_quote)
    else return RemoteViews(context.packageName, R.layout.extra_large_quote)
}

/**
 * Returns the WidgetSize enum for the current widget size
 *
 * @param rows The number of rows in the widget
 * @param columns The number of columns in the widget
 * @return WidgetSize
 */
internal fun getViewSize(rows: Int, columns: Int): WidgetSize  {
    val area = rows * columns
    if (rows == 1 && columns <= 3 || area <= 3) return WidgetSize.SMALL
    if (rows == 1 && columns > 3 || area <= 4) return WidgetSize.MEDIUM
    if (area <= 8) return WidgetSize.LARGE
    else return WidgetSize.XLARGE
}

enum class WidgetSize {
    SMALL,
    MEDIUM,
    LARGE,
    XLARGE
}
