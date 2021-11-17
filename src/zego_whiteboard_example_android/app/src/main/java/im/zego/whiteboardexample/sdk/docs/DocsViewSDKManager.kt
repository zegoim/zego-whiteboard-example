package im.zego.whiteboardexample.sdk.docs

import android.app.Application
import android.os.Environment
import im.zego.whiteboardexample.AuthConstants
import im.zego.whiteboardexample.VersionConstants
import im.zego.whiteboardexample.sdk.SDKInitCallback
import im.zego.whiteboardexample.util.AppLogger
import im.zego.whiteboardexample.util.SharedPreferencesUtil
import im.zego.zegodocs.ZegoDocsViewConfig
import im.zego.zegodocs.ZegoDocsViewManager

/**
 * 文档服务 SDK 管理
 */
object DocsViewSDKManager {

    private const val TAG = "DocsViewSDKManager"

    // 用于判断是否成功初始化文档服务
    var initDocsResult: Boolean? = null

    /**
     * 初始化文档服务
     */
    fun init(application: Application, sdkInitCallback: SDKInitCallback) {

        val config = ZegoDocsViewConfig()
        config.appID = AuthConstants.APP_ID
        config.appSign = AuthConstants.APP_SIGN

        // 设置存储路径
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            // 如需调整日志默认存储路径，请移除下面这行的注释符号，并做修改，并和互动白板SDK保持同样的路径
            // config.logFolder = application.getExternalFilesDir(null)!!.absolutePath + File.separator + RTC_LOG_SUBFOLDER
        }

        // 设置 PPT 步数模式
        CustomizedConfigHelper.updatePPTStepMode(SharedPreferencesUtil.isNextStepFlipPage())

        // 设置 缩略图清晰度
        CustomizedConfigHelper.updateThumbnailClarity(SharedPreferencesUtil.getThumbnailClarityType())

        CustomizedConfigHelper.loadVideoInPPT(SharedPreferencesUtil.isVideoLoadedInPPT())

        // 初始化
        ZegoDocsViewManager.getInstance().init(application, config) { errorCode: Int ->
            AppLogger.i(TAG, "init docsView result:$errorCode")
            initDocsResult = errorCode == 0
            sdkInitCallback.onInit(errorCode == 0)
        }
    }

    fun unInitSDK() {
        initDocsResult = null
        ZegoDocsViewManager.getInstance().uninit()
    }

}