package im.zego.whiteboardexample.sdk.rtc

  //replace_with_content_begin
import android.app.Application
import android.util.Log
import im.zego.whiteboardexample.AppConstants.RTC_LOG_SIZE
import im.zego.whiteboardexample.AppConstants.RTC_LOG_SUBFOLDER
import im.zego.zegoexpress.ZegoExpressEngine
import im.zego.zegoexpress.callback.IZegoEventHandler
import im.zego.zegoexpress.constants.ZegoRoomState
import im.zego.zegoexpress.constants.ZegoScenario
import im.zego.whiteboardexample.sdk.SDKInitCallback
import im.zego.zegoexpress.entity.*
import org.json.JSONObject
import java.io.File

/**
 * 不要写 双斜线 注释，编译脚本处理的时候会删掉所有的 双斜线
 */
internal class ZegoExpressWrapper : IZegoVideoSDKProxy {

    private val TAG = "ZegoExpressWrapper"
    private lateinit var expressEngine: ZegoExpressEngine
    private var zegoRoomStateListener: IZegoRoomStateListener? = null
    private var isLoginRoom = false
    private var loginResult: (Int) -> Unit = {}

    override fun initSDK(
        application: Application, appID: Long, appSign: String,
        sdkInitCallback: SDKInitCallback
    ) {
        Log.d(TAG, "init ZegoExpressEngine, version:${ZegoExpressEngine.getVersion()}")
        val config = ZegoEngineConfig()
        val zegoLogConfig = ZegoLogConfig()
        zegoLogConfig.logPath = application.getExternalFilesDir(null)!!.absolutePath + File.separator + RTC_LOG_SUBFOLDER
        zegoLogConfig.logSize = RTC_LOG_SIZE
        ZegoExpressEngine.setEngineConfig(config)
        ZegoExpressEngine.setLogConfig(zegoLogConfig)
        val profile = ZegoEngineProfile()
        profile.appID = appID
        profile.appSign = appSign
        profile.scenario = ZegoScenario.COMMUNICATION
        profile.application = application
        val engine = ZegoExpressEngine.createEngine(profile, null)
        if (engine == null) {
            sdkInitCallback.onInit(false)
            return
        }

        expressEngine = engine
        expressEngine.setEventHandler(object : IZegoEventHandler() {
            override fun onRoomStateUpdate(
                roomID: String,
                state: ZegoRoomState,
                errorCode: Int,
                extendedData: JSONObject
            ) {
                Log.d(TAG, "onRoomStateUpdate:state :${state},errorCode:${errorCode}")
                when (state) {
                    ZegoRoomState.DISCONNECTED -> {
                        if (isLoginRoom) {
                            loginResult.invoke(errorCode)
                            isLoginRoom = false
                        } else {
                            zegoRoomStateListener?.onDisconnect(errorCode, roomID)
                        }
                    }
                    ZegoRoomState.CONNECTED -> {
                        if (isLoginRoom) {
                            loginResult.invoke(errorCode)
                            isLoginRoom = false
                        } else {
                            zegoRoomStateListener?.onConnected(errorCode, roomID)
                        }
                    }
                    ZegoRoomState.CONNECTING -> {
                        zegoRoomStateListener?.connecting(errorCode, roomID)
                    }
                }
            }
        })

        sdkInitCallback.onInit(true)
    }

    override fun setZegoRoomCallback(stateCallback: IZegoRoomStateListener?) {
        this.zegoRoomStateListener = stateCallback
    }

    override fun unInitSDK() {
        ZegoExpressEngine.destroyEngine(null)
    }

    override fun loginRoom(
        userID: String,
        userName: String,
        roomID: String,
        function: (Int) -> Unit
    ) {
        val user = ZegoUser(userID, userName)
        val roomConfig = ZegoRoomConfig()
        roomConfig.isUserStatusNotify = true
        expressEngine.loginRoom(roomID, user, roomConfig)
        this.loginResult = function
        isLoginRoom = true
    }

    override fun logoutRoom(roomID: String) {
        expressEngine.logoutRoom(roomID)
        isLoginRoom = false
    }

    override fun getVersion(): String {
        return ZegoExpressEngine.getVersion()
    }

    override fun uploadLog() {
        expressEngine.uploadLog()
    }
}
  //replace_with_content_end
