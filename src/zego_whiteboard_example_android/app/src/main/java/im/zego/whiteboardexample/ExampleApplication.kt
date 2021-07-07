package im.zego.whiteboardexample

import android.app.Application
import android.widget.Toast
import com.ytee.logutil.LogcatFileManager
import im.zego.whiteboardexample.sdk.SDKInitCallback
import im.zego.whiteboardexample.sdk.ZegoSDKManager
import im.zego.whiteboardexample.util.*
import im.zego.zegodocs.ZegoDocsViewManager
import im.zego.zegowhiteboard.ZegoWhiteboardManager

class ExampleApplication : Application() {
    private val TAG = "ExampleApplication"

    override fun onCreate() {
        super.onCreate()

        LogcatFileManager.getInstance().startLogcatManager(this)
        SharedPreferencesUtil.setApplicationContext(this)
        SharedPreferencesUtil.setProcessID(android.os.Process.myPid())
        ToastUtils.setAppContext(this)
        CrashHandler.setAppContext(this)
        ZegoSDKManager.initSDKEnvironment(this, object : SDKInitCallback {
            override fun onInit(success: Boolean) {
                AppLogger.i(TAG, "onInit() result: $success")
            }
        })
        val isAppTooOldForDocs = ZegoUtil.isAppTooOld(ZegoDocsViewManager.getInstance().version, VersionConstants.DOCS_SDK)
        val isAppTooOldForWhiteboard = ZegoUtil.isAppTooOld(ZegoWhiteboardManager.getInstance().version, VersionConstants.WHITEBOARD_SDK)
        if (isAppTooOldForDocs || isAppTooOldForWhiteboard) {
            ToastUtils.showCenterToast(R.string.app_version_warning, Toast.LENGTH_LONG)
        }
    }

}