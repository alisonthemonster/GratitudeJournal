package com.presently.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle

/**
 * A daily gratitude quote widget. It pulls gratitude quotes from our inspirations.xml resource file
 * once a day. It also supports several widget sizes.
 */
class GratitudeQuoteWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }
}

/**
 * Updates a widget with new copy and the correct layout.
 */
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {

    val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
    val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
    val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
    val rows: Int = getCellsForSize(minHeight)
    val columns: Int = getCellsForSize(minWidth)
    val size = getViewSize(rows, columns)

    val quotes = context.resources.getStringArray(R.array.inspirations)
    val quoteString = getQuotesForSize(quotes.toList(), size).random()

    val splitString = quoteString.split("\n")
    val quote = splitString.first()
    val author = splitString.last()

    val remoteViews = getRemoteViewForSize(context, size)

    remoteViews.setTextViewText(R.id.quote_text, quote)
    remoteViews.setTextViewText(R.id.author_text, author)

    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
}

/**
 * Returns number of cells needed for given size of the widget.
 *
 * @param size Widget size in dp.
 * @return Size in number of cells.
 */
private fun getCellsForSize(size: Int): Int {
    var n = 2
    while (70 * n - 30 < size) {
        ++n
    }
    return n - 1
}
