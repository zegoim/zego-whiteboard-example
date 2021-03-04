package im.zego.whiteboardexample.sdk.whiteboard

import android.content.Context
import im.zego.zegowhiteboard.ZegoWhiteboardConfig
import im.zego.zegowhiteboard.ZegoWhiteboardManager
import im.zego.zegowhiteboard.callback.IZegoWhiteboardGetListListener
import im.zego.zegowhiteboard.callback.IZegoWhiteboardManagerListener
import im.zego.whiteboardexample.constants.AppConstants
import im.zego.whiteboardexample.sdk.SDKInitCallback
import im.zego.whiteboardexample.util.Logger
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
        Logger.i(TAG, "initWhiteboardSDK....,version:" + ZegoWhiteboardManager.getInstance().version)

        val config = ZegoWhiteboardConfig()
        // 设置日志存储路径
        config.logPath =
            context.getExternalFilesDir(null)!!.absolutePath + File.separator + AppConstants.LOG_SUBFOLDER
        // 设置图片存储路径
        config.cacheFolder =
            context.getExternalFilesDir(null)!!.absolutePath + File.separator + AppConstants.IMAGE_SUBFOLDER
        ZegoWhiteboardManager.getInstance().setConfig(config)

        // 初始化
        ZegoWhiteboardManager.getInstance().init(context) { errorCode ->
            Logger.i(TAG, "init Whiteboard errorCode:$errorCode")
            initWhiteboardResult = errorCode == 0
            if (errorCode == 0) {
                if (SharedPreferencesUtil.isSystemTextStyle()) {
                    // 设置默认字体为系统
                    ZegoWhiteboardManager.getInstance().setCustomFontFromAsset("", "")
                } else {
                    // 设置默认字体为思源字体
                    ZegoWhiteboardManager.getInstance().setCustomFontFromAsset(
                        AppConstants.FONT_FAMILY_DEFAULT_PATH,
                        AppConstants.FONT_FAMILY_DEFAULT_PATH_BOLD
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