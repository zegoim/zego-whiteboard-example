package im.zego.whiteboardexample.sdk.docs

import android.app.Application
import android.os.Environment
import im.zego.whiteboardexample.VersionConstants
import im.zego.zegodocs.ZegoDocsViewConfig
import im.zego.zegodocs.ZegoDocsViewManager
import im.zego.whiteboardexample.constants.AppConstants
import im.zego.whiteboardexample.sdk.SDKInitCallback
import im.zego.whiteboardexample.util.Logger
import im.zego.whiteboardexample.util.SharedPreferencesUtil
import java.io.File

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
        Logger.i(TAG, "initDocSdk.... currentVersion:${ZegoDocsViewManager.getInstance().version}, supportVersion:${VersionConstants.DOCS_SDK}")

        // 设置appID, appSign, 是否测试环境 isTestEnv
        val docsViewEnvTest = SharedPreferencesUtil.isDocsViewTestEnv()
        Logger.i(TAG, "initDocSdk.... isDocsViewEnvTest:$docsViewEnvTest")
        val config = ZegoDocsViewConfig()
        config.appID = SharedPreferencesUtil.getAppID()
        config.appSign = SharedPreferencesUtil.getAppSign()
        config.isTestEnv = docsViewEnvTest

        // 设置存储路径
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            // 可选设置
            config.logFolder = application.getExternalFilesDir(null)!!.absolutePath + File.separator + AppConstants.LOG_SUBFOLDER
            config.dataFolder = application.getExternalFilesDir(null)!!.absolutePath + File.separator + "zegodocs" + File.separator + "data"
            config.cacheFolder = application.getExternalFilesDir(null)!!.absolutePath + File.separator + "zegodocs" + File.separator + "cache"
        }

        // 设置 PPT 步数模式
        val pptStepMode: String = if (SharedPreferencesUtil.isNextStepFlipPage()) {
            "1"
        } else {
            "2"
        }
        ZegoDocsViewManager.getInstance().setCustomizedConfig("pptStepMode", pptStepMode)

        // 初始化
        ZegoDocsViewManager.getInstance().init(application, config) { errorCode: Int ->
            Logger.i(TAG, "init docsView result:$errorCode")
            initDocsResult = errorCode == 0
            sdkInitCallback.onInit(errorCode == 0)
        }
    }

    fun unInitSDK() {
        initDocsResult = null
        ZegoDocsViewManager.getInstance().uninit()
    }

}