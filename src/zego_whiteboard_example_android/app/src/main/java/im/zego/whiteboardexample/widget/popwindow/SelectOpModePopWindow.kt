package im.zego.whiteboardexample.widget.popwindow

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zyyoona7.wheel.WheelView
import im.zego.zegowhiteboard.ZegoWhiteboardManager
import im.zego.whiteboardexample.R
import im.zego.zegowhiteboard.ZegoWhiteboardConstants
import kotlinx.android.synthetic.main.layout_main_content.*
import kotlinx.android.synthetic.main.popwindow_select.view.*

/**
 * 笔画粗细选择 PopWindow
 */
class SelectOpModePopWindow(context: Context, var selectMode: Int) : BasePopWindow(
    context,
    contentView = LayoutInflater.from(context).inflate(R.layout.popwindow_select, null, false)
) {
    private var confirmListener: (Int,String) -> Unit = { _, _ -> }

   companion object{
       const val DRAW = ZegoWhiteboardConstants.ZegoWhiteboardOperationModeDraw
       const val DRAW_STRING = "绘制"
       const val SCROLL = ZegoWhiteboardConstants.ZegoWhiteboardOperationModeScroll
       const val SCROLL_STRING = "滚动"
       const val DRAW_SCALE = ZegoWhiteboardConstants.ZegoWhiteboardOperationModeDraw or
               ZegoWhiteboardConstants.ZegoWhiteboardOperationModeZoom
       const val DRAW_SCALE_STRING = "绘制｜缩放"
       const val SCROLL_SCALE = ZegoWhiteboardConstants.ZegoWhiteboardOperationModeScroll or
               ZegoWhiteboardConstants.ZegoWhiteboardOperationModeZoom
       const val SCROLL_SCALE_STRING =  "滚动｜缩放"
       const val NONE = ZegoWhiteboardConstants.ZegoWhiteboardOperationModeNone
       const val NONE_STRING = "禁用"
   }

    init {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        contentView.findViewById<TextView>(R.id.title).setText(R.string.draw_whiteboard_mode)
        val wheelView = contentView.findViewById<WheelView<String>>(R.id.type_list)
        wheelView.isResetSelectedPosition = false
        wheelView.data = listOf(DRAW_STRING, SCROLL_STRING, DRAW_SCALE_STRING, SCROLL_SCALE_STRING, NONE_STRING)
        val selectedPosition = when (selectMode) {
            DRAW -> {
                0
            }
            SCROLL -> {
                1
            }
            DRAW_SCALE -> {
                2
            }
            SCROLL_SCALE -> {
                3
            }
            else -> {
                4
            }
        }
        wheelView.selectedItemPosition = selectedPosition
        wheelView.setOnItemSelectedListener { wheelView, data, position ->
            selectMode = when (position) {
                0 -> {
                    DRAW
                }
                1 -> {
                    SCROLL
                }
                2 -> {
                    DRAW_SCALE
                }
                3 -> {
                    SCROLL_SCALE
                }
                else -> {
                    NONE
                }
            }
        }

        contentView.cancel.setOnClickListener {
            if (super.isShowing()) super.dismiss()
        }

        contentView.confirm.setOnClickListener {
            val string =  when (selectMode) {
                DRAW -> {
                    DRAW_STRING
                }
                SCROLL -> {
                    SCROLL_STRING
                }
                DRAW_SCALE -> {
                    DRAW_SCALE_STRING
                }
                SCROLL_SCALE -> {
                    SCROLL_SCALE_STRING
                }
                else -> {
                    NONE_STRING
                }
            }
            confirmListener.invoke(selectMode,string)
            if (super.isShowing()) super.dismiss()
        }
    }

    fun show(anchor: View) {
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        selectMode = 6
        super.showAtLocation(anchor, Gravity.BOTTOM,0,0)
    }

    fun setOnConfirmClickListener(listener: (Int,String) -> Unit) {
        this.confirmListener = listener
    }
}
