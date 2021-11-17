package im.zego.whiteboardexample.widget.popwindow

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import im.zego.whiteboardexample.R
import im.zego.whiteboardexample.model.BackgroundData
import im.zego.whiteboardexample.model.BackgroundFitModeData
import im.zego.zegowhiteboard.ZegoWhiteboardViewImageFitMode
import kotlinx.android.synthetic.main.popwindow_select.view.*

/**
 * 选择背景弹窗
 */
class SelectBackgroundPopWindow(context: Context) : BasePopWindow(
    context,
    contentView = LayoutInflater.from(context).inflate(R.layout.popwindow_select, null, false)
) {
    private var confirmListener: (BackgroundData) -> Unit = {}
    private var mContext = context

    var mSelectMode :BackgroundData = BackgroundData("https://storage.zego.im/goclass/wbbg/1.jpg","背景图1")
    var mList = ArrayList<BackgroundData>()

    init {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        contentView.title.text = context.getString(R.string.draw_set_background)
        contentView.type_list.let {
            // 预置背景列表
            mList.add(BackgroundData("https://storage.zego.im/goclass/wbbg/1.jpg","背景图1"))
            mList.add(BackgroundData("https://storage.zego.im/goclass/wbbg/2.jpg","背景图2"))
            mList.add(BackgroundData("https://storage.zego.im/goclass/wbbg/3.png","背景图3"))
            mList.add(BackgroundData("https://storage.zego.im/goclass/wbbg/4.jpeg","背景图4"))
            it.data = mList
            it.isResetSelectedPosition = false
            it.setOnItemSelectedListener { _, any, _ ->
                mSelectMode = any as BackgroundData
            }
        }

        contentView.cancel.setOnClickListener {
            if (super.isShowing()) super.dismiss()
        }

        contentView.confirm.setOnClickListener {
            confirmListener.invoke(mSelectMode)
            if (super.isShowing()) super.dismiss()
        }
    }

    fun show(anchor: View) {
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        super.showAtLocation(anchor, Gravity.BOTTOM, 0, 0)
    }

    fun setOnConfirmClickListener(listener: (BackgroundData) -> Unit) {
        this.confirmListener = listener
    }
}
