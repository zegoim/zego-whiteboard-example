package im.zego.whiteboardexample.sdk.rtc

import android.app.Application
import im.zego.whiteboardexample.sdk.SDKInitCallback

/**
 * ZegoSDK 会有两种实现，liveroom 和 express
 */
interface IZegoVideoSDKProxy {

    /**
     * 初始化 SDK
     */
    fun initSDK(
        application: Application,
        appID: Long,
        appSign: String,
        testEnv: Boolean,
        sdkInitCallback: SDKInitCallback
    )

    /**
     * 反初始化 SDK
     */
    fun unInitSDK()

    /**
     * 登录房间
     */
    fun loginRoom(
        userID: String,
        userName: String,
        roomID: String,
        function: (Int) -> Unit
    )

    /**
     * 退出房间
     */
    fun logoutRoom(roomID: String)

    /**
     * 设置房间回调
     */
    fun setZegoRoomCallback(stateCallback: IZegoRoomStateListener?)

    /**
     * liveroom/express 的 SDK 版本号
     */
    fun getVersion(): String

    /**
     * 上传 liveroom/express 的日志
     */
    fun uploadLog()
}

