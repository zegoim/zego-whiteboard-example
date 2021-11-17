package im.zego.whiteboardexample.sdk.whiteboard

import android.content.Context
import android.os.Environment
import im.zego.whiteboardexample.AppConstants.*
import im.zego.whiteboardexample.VersionConstants
import im.zego.zegowhiteboard.ZegoWhiteboardConfig
import im.zego.zegowhiteboard.ZegoWhiteboardManager
import im.zego.zegowhiteboard.callback.IZegoWhiteboardGetListListener
import im.zego.zegowhiteboard.callback.IZegoWhiteboardManagerListener
import im.zego.whiteboardexample.sdk.SDKInitCallback
import im.zego.whiteboardexample.util.AppLogger
import im.zego.whiteboardexample.util.SharedPreferencesUtil
import java.io.File

/**
 * 白板服务 SDK 管理
 */
object WhiteboardSDKManager {

    private const val TAG = "WhiteboardSDKManager"

    // 用于判断是否成功初始化白板服务
    var initWhiteboardResult: Boolean? = null

    fun init(context: Context, sdkInitCallback: SDKInitCallback) {
        val config = ZegoWhiteboardConfig()
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            // 如需调整日志默认存储路径，请移除下面这行的注释符号，并做修改
            // config.logPath = context.getExternalFilesDir(null)!!.absolutePath + File.separator + RTC_LOG_SUBFOLDER
        }

        ZegoWhiteboardManager.getInstance().setConfig(config)

        // 初始化
        ZegoWhiteboardManager.getInstance().init(context) { errorCode ->
            AppLogger.i(TAG, "init Whiteboard errorCode:$errorCode")
            initWhiteboardResult = errorCode == 0
            if (errorCode == 0) {
                if (SharedPreferencesUtil.isSystemTextStyle()) {
                    // 设置默认字体为系统
                    ZegoWhiteboardManager.getInstance().setCustomFontFromAsset("", "")
                } else {
                    // 设置默认字体为思源字体
                    ZegoWhiteboardManager.getInstance().setCustomFontFromAsset(
                        RECOMMEND_REGULAR_FONT_PATH,
                        RECOMMEND_BOLD_FONT_PATH
                    )
                }
            }
            initWhiteboardResult = errorCode == 0
            sdkInitCallback.onInit(errorCode == 0)
        }
    }

    fun unInitSDK() {
        initWhiteboardResult = null
        ZegoWhiteboardManager.getInstance().uninit()
    }

    fun setWhiteboardCountListener(listener: IZegoWhiteboardManagerListener) {
        ZegoWhiteboardManager.getInstance().setWhiteboardManagerListener(listener)
    }

    fun getWhiteboardViewList(listListener: IZegoWhiteboardGetListListener) {
        ZegoWhiteboardManager.getInstance().getWhiteboardViewList(listListener)
    }
}