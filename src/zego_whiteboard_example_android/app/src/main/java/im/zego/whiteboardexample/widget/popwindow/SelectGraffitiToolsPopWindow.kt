package im.zego.whiteboardexample.widget.popwindow

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import im.zego.zegowhiteboard.ZegoWhiteboardConstants
import im.zego.zegowhiteboard.ZegoWhiteboardManager
import im.zego.whiteboardexample.R
import im.zego.whiteboardexample.model.ToolsData
import kotlinx.android.synthetic.main.popwindow_select.view.*

/**
 * 涂鸦工具选择 PopWindow
 */
class SelectGraffitiToolsPopWindow(context: Context, selectToolName: String, isSupportClickTool: Boolean) : BasePopWindow(
    context,
    contentView = LayoutInflater.from(context).inflate(R.layout.popwindow_select, null, false)
) {
    private var confirmListener: (String) -> Unit = {}
    private val mContext = context;

    // 当前所选涂鸦工具，默认为画笔
    private var mSelectToolName = context.getString(R.string.draw_graffiti_tools_pen)
    private var mSelectToolID = ZegoWhiteboardManager.getInstance().toolType

    init {
        width = ViewGroup.LayoutParams.MATCH_PARENT

        contentView.type_list.let {
            val list: List<ToolsData> = if(isSupportClickTool) {
                listOf(
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolPen, context.getString(R.string.draw_graffiti_tools_pen)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolSelector, context.getString(R.string.draw_graffiti_tools_selector)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolClick, context.getString(R.string.draw_graffiti_tools_click)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolNone, context.getString(R.string.draw_graffiti_tools_none)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolLaser, context.getString(R.string.draw_graffiti_tools_laser)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolSelector, context.getString(R.string.draw_graffiti_tools_text)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolRect, context.getString(R.string.draw_graffiti_tools_rect)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolEllipse, context.getString(R.string.draw_graffiti_tools_ellipse)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolEraser, context.getString(R.string.draw_graffiti_tools_eraser)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolLine, context.getString(R.string.draw_graffiti_tools_line)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolCustomImage, context.getString(R.string.draw_graffiti_tools_image))
                )
            }else{
                listOf(
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolPen, context.getString(R.string.draw_graffiti_tools_pen)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolSelector, context.getString(R.string.draw_graffiti_tools_selector)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolNone, context.getString(R.string.draw_graffiti_tools_none)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolLaser, context.getString(R.string.draw_graffiti_tools_laser)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolSelector, context.getString(R.string.draw_graffiti_tools_text)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolRect, context.getString(R.string.draw_graffiti_tools_rect)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolEllipse, context.getString(R.string.draw_graffiti_tools_ellipse)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolEraser, context.getString(R.string.draw_graffiti_tools_eraser)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolLine, context.getString(R.string.draw_graffiti_tools_line)),
                    ToolsData(ZegoWhiteboardConstants.ZegoWhiteboardViewToolCustomImage, context.getString(R.string.draw_graffiti_tools_image))
                )
            }
            it.data = list
            it.isResetSelectedPosition = false

            if (selectToolName.isNotEmpty()) {
                mSelectToolName = selectToolName
                contentView.type_list.data.forEachIndexed { index, data ->
                    val toolsData = data as ToolsData
                    if (toolsData.toolName == selectToolName) {
                        mSelectToolID = toolsData.id
                        it.setSelectedItemPosition(index)
                    }
                }
            }
            it.setOnItemSelectedListener { wheelView, any, position ->
                val tool = any as ToolsData
                mSelectToolName = tool.toolName
                mSelectToolID = tool.id
            }
        }

        contentView.cancel.setOnClickListener {
            if (super.isShowing()) super.dismiss()
        }

        contentView.confirm.setOnClickListener {
            ZegoWhiteboardManager.getInstance().toolType = mSelectToolID
            confirmListener.invoke(mSelectToolName)
            if (super.isShowing()) super.dismiss()
        }
    }

    fun show(anchor: View) {
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        super.showAtLocation(anchor, Gravity.BOTTOM, 0, 0)
    }

    fun setOnConfirmClickListener(listener: (String) -> Unit) {
        this.confirmListener = listener
    }
}
