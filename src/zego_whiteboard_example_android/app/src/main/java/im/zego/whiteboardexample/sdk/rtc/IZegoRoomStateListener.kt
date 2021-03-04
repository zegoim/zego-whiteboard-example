package im.zego.whiteboardexample.sdk.rtc

/**
 * Created by yuxing_zhong on 2020/12/1
 */
interface IZegoRoomStateListener {
    fun onConnected(errorCode: Int, roomID: String)
    fun onDisconnect(errorCode: Int, roomID: String)
    fun connecting(errorCode: Int, roomID: String)
}