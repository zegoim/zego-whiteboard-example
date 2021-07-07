package im.zego.whiteboardexample.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.InputFilter
import android.text.Spanned
import android.util.TypedValue
import java.util.regex.Pattern

fun dp2px(context: Context, dpValue: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dpValue,
        context.resources.displayMetrics
    )
}

fun getRoundRectDrawable(drawableColor: String, radius: Float): GradientDrawable {
    val drawable = GradientDrawable()
    drawable.shape = GradientDrawable.RECTANGLE
    drawable.setColor(Color.parseColor(drawableColor))
    drawable.cornerRadius = radius
    return drawable
}

fun getCircleDrawable(drawableColor: String, radius: Float): GradientDrawable {
    val drawable = GradientDrawable()
    drawable.shape = GradientDrawable.OVAL
    drawable.setColor(Color.parseColor(drawableColor))
    drawable.setSize((radius * 2).toInt(), (radius * 2).toInt())
    return drawable
}

val nameFilter: InputFilter = object : InputFilter.LengthFilter(50) {
    override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int,
    ): CharSequence? {
        var charSequence = super.filter(source, start, end, dest, dstart, dend)
        val regex = "^[\\u4E00-\\u9FA5a-z0-9A-Z_]+$"
        val permit = Pattern.matches(regex, source.toString())
        return if (permit) charSequence else ""
    }
}

