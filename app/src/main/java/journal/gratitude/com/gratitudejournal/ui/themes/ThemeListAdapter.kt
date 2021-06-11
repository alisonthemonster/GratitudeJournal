package journal.gratitude.com.gratitudejournal.ui.themes

import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Theme
import kotlinx.android.synthetic.main.item_theme.view.*

class ThemeListAdapter(var selectedTheme: String = "original", private val listener: ThemeFragment.OnThemeSelectedListener?) :
    ListAdapter<Theme, ThemeListAdapter.ThemeViewHolder>(ThemeDC()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ThemeViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme, parent, false), listener
    )

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) = holder.bind(getItem(position))

    fun addData(data: List<Theme>) {
        submitList(data.toMutableList())
    }

    inner class ThemeViewHolder(
        itemView: View,
        private val listener: ThemeFragment.OnThemeSelectedListener?
    ) : RecyclerView.ViewHolder(itemView), OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            if (adapterPosition == RecyclerView.NO_POSITION) return

            val clicked = getItem(adapterPosition)
            listener?.onThemeSelected(clicked.name)
        }

        fun bind(item: Theme) = with(itemView) {
            background_view.setBackgroundColor(item.backgroundColor)
            header.setBackgroundColor(item.headerColor)
            logo.setTextColor(item.headerItemColor)
            icon.setImageResource(item.icon)
            if (!item.multicolorIcon) icon.setColorFilter(item.iconColor) else icon.clearColorFilter()
            timeline_line.setBackgroundColor(item.headerColor)
            theme_name.text = item.name
            theme_name.setTextColor(item.iconColor)

            if(item.name.equals(selectedTheme))
                selectIcon.visibility = VISIBLE
            else
                selectIcon.visibility = INVISIBLE
        }
    }



    private class ThemeDC : DiffUtil.ItemCallback<Theme>() {
        override fun areItemsTheSame(
            oldItem: Theme,
            newItem: Theme
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Theme,
            newItem: Theme
        ): Boolean {
            return oldItem.name == newItem.name
        }
    }
}