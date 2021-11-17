package im.zego.whiteboardexample.sdk.rtc

import android.app.Application
import im.zego.whiteboardexample.AuthConstants
import im.zego.zegowhiteboard.ZegoWhiteboardManager
import im.zego.whiteboardexample.R
import im.zego.whiteboardexample.sdk.SDKInitCallback
import im.zego.whiteboardexample.util.AppLogger
import im.zego.whiteboardexample.util.SharedPreferencesUtil
import im.zego.whiteboardexample.util.ToastUtils


/**
 * 房间服务 SDK 管理
 */
object VideoSDKManager {

    // 未进入房间
    const val OUTSIDE = 0

    // 进入房间中
    const val PENDING_ENTER = 1

    // 已进入房间
    const val ENTERED = 2
    private val TAG = javaClass.simpleName
    private var roomID = ""
    private var mUserID = 0L

    // 房间状态
    private var state = OUTSIDE

    // ZegoSDK 实现代理（liveroom/express）
    private var zegoVideoSDKProxy: IZegoVideoSDKProxy = ZegoExpressWrapper()  //replace_with_content

    // 房间状态监听
    private var zegoRoomStateListener: IZegoRoomStateListener? = null

    // 用于判断是否成功初始化房间服务
    var initRoomResult: Boolean? = null

    fun init(application: Application, sdkInitCallback: SDKInitCallback) {
        AppLogger.i(
            TAG, "init initRoomSDK isVideoSDKTest, version:" + zegoVideoSDKProxy.getVersion()
        )
        zegoVideoSDKProxy.initSDK(application, AuthConstants.APP_ID, AuthConstants.APP_SIGN,
            object : SDKInitCallback {
                override fun onInit(success: Boolean) {
                    AppLogger.i(TAG, "init zegoLiveRoomSDK result:$success")
                    initRoomResult = success
                    sdkInitCallback.onInit(success)
                }
            })
    }

    private fun registerCallback() {
        zegoVideoSDKProxy.setZegoRoomCallback(object : IZegoRoomStateListener {
            override fun onConnected(errorCode: Int, roomID: String) {
                AppLogger.d(TAG, "onReconnect:errorCode:${errorCode}")
                zegoRoomStateListener?.onConnected(errorCode, roomID)
            }

            override fun onDisconnect(errorCode: Int, roomID: String) {
                AppLogger.d(TAG, "onDisconnect:errorCode:${errorCode}")
                zegoRoomStateListener?.onDisconnect(errorCode, roomID)
            }

            override fun connecting(errorCode: Int, roomID: String) {
                AppLogger.d(TAG, "onTempBroken:errorCode:${errorCode}")
                zegoRoomStateListener?.connecting(errorCode, roomID)
            }
        })
    }

    fun setRoomStateListener(listener: IZegoRoomStateListener?) {
        zegoRoomStateListener = listener
    }

    fun loginRoom(
        roomID: String,
        userName: String,
        function: (Int) -> Unit
    ) {
        val userID = generateUserId()
        mUserID = userID
        AppLogger.i(
            TAG,
            "enterRoom() called with: userID = [$userID], userName = [$userName], roomID = [$roomID] state = $state"
        )

        // 若状态为已登录则不再重复登录
        if (state != OUTSIDE) {
            return
        }

        registerCallback()
        // 进入中
        state = PENDING_ENTER

        // 开始进入房间
        zegoVideoSDKProxy.loginRoom(userID.toString(), userName, roomID)
        { errorCode: Int ->
            AppLogger.i(TAG, "loginRoom:$errorCode")
            when (errorCode) {
                0 -> {
                    // 已进入房间
                    state = ENTERED
                    this.roomID = roomID
                }
                else -> {
                    ToastUtils.showCenterToast(R.string.join_other, errorCode)
                    // 未进入房间
                    state = OUTSIDE
                    exitRoom()
                }
            }
            function.invoke(errorCode)
        }
    }

    /**
     * @return 返回 userId 由客户端生成，统一规则：毫秒级时间戳。注意：理论上还是会存在冲突的。
     */
    private fun generateUserId(): Long {
        val userId = System.currentTimeMillis()
        AppLogger.i(TAG, "generateUserId() myUserId: $userId")
        return userId
    }

    fun getUserID():Long{
        return mUserID
    }

    fun exitRoom() {
        ZegoWhiteboardManager.getInstance().clear()
        zegoVideoSDKProxy.logoutRoom(roomID)
        state = OUTSIDE
        this.roomID = ""
        zegoRoomStateListener = null
        zegoVideoSDKProxy.setZegoRoomCallback(null)
    }

    fun uploadLog() {
        zegoVideoSDKProxy.uploadLog()
    }

    fun unInitSDK() {
        initRoomResult = null
        zegoVideoSDKProxy.unInitSDK()
    }

    fun getVersion(): String {
        return zegoVideoSDKProxy.getVersion()
    }
}
