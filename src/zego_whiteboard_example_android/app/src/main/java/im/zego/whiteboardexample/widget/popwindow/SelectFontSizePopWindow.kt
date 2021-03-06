package im.zego.whiteboardexample.widget.popwindow

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import im.zego.zegowhiteboard.ZegoWhiteboardManager
import im.zego.whiteboardexample.R
import kotlinx.android.synthetic.main.popwindow_select.view.*

/**
 * 字号大小选择 PopWindow
 */
class SelectFontSizePopWindow(context: Context, selectSize: Int) : BasePopWindow(
    context,
    contentView = LayoutInflater.from(context).inflate(R.layout.popwindow_select, null, false)
) {
    private var confirmListener: (Int) -> Unit = {}

    // 当前所选字号大小
    private var mSelectSize = ZegoWhiteboardManager.getInstance().fontSize

    init {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        mSelectSize = selectSize
        contentView.title.text = context.getString(R.string.draw_text_size)
        contentView.type_list.let {
            val list = listOf(18, 24, 36, 48)
            it.data = list
            it.isResetSelectedPosition = false
            it.setOnItemSelectedListener { wheelView, any, position ->
                mSelectSize = any as Int
            }
        }

        contentView.cancel.setOnClickListener {
            if (super.isShowing()) super.dismiss()
        }

        contentView.confirm.setOnClickListener {
            ZegoWhiteboardManager.getInstance().fontSize = mSelectSize
            confirmListener.invoke(mSelectSize)
            if (super.isShowing()) super.dismiss()
        }
    }

    fun show(anchor: View) {
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        mSelectSize = 18
        super.showAtLocation(anchor, Gravity.BOTTOM, 0, 0)
    }

    fun setOnConfirmClickListener(listener: (Int) -> Unit) {
        this.confirmListener = listener
    }
}
