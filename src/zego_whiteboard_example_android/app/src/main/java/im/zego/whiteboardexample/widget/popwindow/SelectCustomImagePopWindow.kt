package im.zego.whiteboardexample.widget.popwindow

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import im.zego.whiteboardexample.R
import im.zego.whiteboardexample.model.ImageData
import im.zego.whiteboardexample.util.ToastUtils
import kotlinx.android.synthetic.main.popwindow_select.view.*

/**
 * 自定义图形选择 PopWindow
 */
class SelectCustomImagePopWindow(context: Context) : BasePopWindow(
    context,
    contentView = LayoutInflater.from(context).inflate(R.layout.popwindow_select, null, false)
) {
    private var confirmListener: (ImageData) -> Unit = {}
    private var mContext = context

    var mSelectImage :ImageData = ImageData("https://storage.zego.im/goclass/wbpic/star.svg", mContext.getString(R.string.draw_custom_star))
    var mList = ArrayList<ImageData>()

    init {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        contentView.title.text = context.getString(R.string.draw_graffiti_tools_image)
        contentView.type_list.let {
            // 预置自定义图形列表
            mList.add(ImageData("https://storage.zego.im/goclass/wbpic/star.svg", mContext.getString(R.string.draw_custom_star)))
            mList.add(ImageData("https://storage.zego.im/goclass/wbpic/diamond.svg", mContext.getString(R.string.draw_custom_diamond)))
            mList.add(ImageData("https://storage.zego.im/goclass/wbpic/axis.svg", mContext.getString(R.string.draw_custom_axis)))
            mList.add(ImageData("https://storage.zego.im/goclass/wbpic/chemical_instrument.svg", mContext.getString(R.string.draw_custom_chemical)))
            it.data = mList
            it.isResetSelectedPosition = false
            it.setOnItemSelectedListener { _, any, _ ->
                mSelectImage = any as ImageData
            }
        }

        contentView.cancel.setOnClickListener {
            if (super.isShowing()) super.dismiss()
        }

        contentView.confirm.setOnClickListener {
            confirmListener.invoke(mSelectImage)
            if (super.isShowing()) super.dismiss()
        }
    }

    fun show(anchor: View) {
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        super.showAtLocation(anchor, Gravity.BOTTOM, 0, 0)
    }

    fun setOnConfirmClickListener(listener: (ImageData) -> Unit) {
        this.confirmListener = listener
    }

    fun addImage(url:String){
        if (url.isEmpty() || !url.contains('/')){
            ToastUtils.showCenterToast(mContext.getString(R.string.draw_input_url))
            return
        }
        // 将 URL 添加到列表中
        val name =  url.trim().substring(url.lastIndexOf("/"))
        mList.add(ImageData(url.trim(),name))
        contentView.type_list.data = mList
    }
}
