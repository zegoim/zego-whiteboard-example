package im.zego.whiteboardexample.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import im.zego.whiteboardexample.R
import im.zego.whiteboardexample.widget.CircleImageView

class ColorAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val colorArray: IntArray = context.resources.getIntArray(R.array.graffiti_color)
    internal var selectedIndex = 4
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_style_color, parent, false)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val circle: CircleImageView =
                holder.itemView.findViewById(R.id.item_style_color)
        circle.circleBackgroundColor = colorArray[position]
        val ticker =
                holder.itemView.findViewById<ImageView>(R.id.item_style_ticker)
        if (position == 0) {
            ticker.setImageResource(R.drawable.click_color)
        } else {
            ticker.setImageResource(R.drawable.click_none)
        }
        ticker.visibility = if (position == selectedIndex) View.VISIBLE else View.GONE
    }

    var selectedColor: Int
        get() = colorArray[selectedIndex]
        set(value) {
            val index = colorArray.indexOfFirst { it == value }
            if (index != -1) {
                selectedIndex = index
            }
        }

    override fun getItemCount() = colorArray.size
}