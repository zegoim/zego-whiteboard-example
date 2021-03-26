package im.zego.whiteboardexample.widget.popwindow

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import im.zego.whiteboardexample.R
import im.zego.whiteboardexample.model.BackgroundFitModeData
import im.zego.zegowhiteboard.ZegoWhiteboardViewImageFitMode
import kotlinx.android.synthetic.main.popwindow_select.view.*
import java.util.ArrayList

/**
 * 选择背景模式弹窗
 */
class SelectBackgroundFitModePopWindow(context: Context) : BasePopWindow(
    context,
    contentView = LayoutInflater.from(context).inflate(R.layout.popwindow_select, null, false)
) {
    private var confirmListener: (BackgroundFitModeData) -> Unit = {}
    private var mContext = context

    var mSelectMode :BackgroundFitModeData = BackgroundFitModeData(ZegoWhiteboardViewImageFitMode.ZegoWhiteboardViewImageFitModeCenter,"居中对齐")
    var mList = ArrayList<BackgroundFitModeData>()

    init {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        contentView.title.text = context.getString(R.string.draw_set_background_fit_mode)
        contentView.type_list.let {
            // 预置背景模式列表
            mList.add(BackgroundFitModeData(ZegoWhiteboardViewImageFitMode.ZegoWhiteboardViewImageFitModeCenter,"居中对齐"))
            mList.add(BackgroundFitModeData(ZegoWhiteboardViewImageFitMode.ZegoWhiteboardViewImageFitModeLeft,"靠左对齐"))
            mList.add(BackgroundFitModeData(ZegoWhiteboardViewImageFitMode.ZegoWhiteboardViewImageFitModeRight,"靠右对齐"))
            mList.add(BackgroundFitModeData(ZegoWhiteboardViewImageFitMode.ZegoWhiteboardViewImageFitModeBottom,"底部对齐"))
            mList.add(BackgroundFitModeData(ZegoWhiteboardViewImageFitMode.ZegoWhiteboardViewImageFitModeTop,"顶部对齐"))
            it.data = mList
            it.isResetSelectedPosition = false
            it.setOnItemSelectedListener { _, any, _ ->
                mSelectMode = any as BackgroundFitModeData
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

    fun setOnConfirmClickListener(listener: (BackgroundFitModeData) -> Unit) {
        this.confirmListener = listener
    }
}
