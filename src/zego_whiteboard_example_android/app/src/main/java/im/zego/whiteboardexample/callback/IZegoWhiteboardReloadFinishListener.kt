package im.zego.whiteboardexample.callback

import im.zego.whiteboardexample.widget.whiteboard.ZegoWhiteboardViewHolder

interface IZegoWhiteboardReloadFinishListener {
    fun onReloadFinish(viewHolder:ZegoWhiteboardViewHolder)
}