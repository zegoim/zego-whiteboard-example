package im.zego.whiteboardexample

import android.app.Application
import com.ytee.logutil.LogcatFileManager
import im.zego.whiteboardexample.sdk.SDKInitCallback
import im.zego.whiteboardexample.sdk.ZegoSDKManager
import im.zego.whiteboardexample.util.CrashHandler
import im.zego.whiteboardexample.util.Logger
import im.zego.whiteboardexample.util.SharedPreferencesUtil
import im.zego.whiteboardexample.util.ToastUtils

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
                Logger.i(TAG, "onInit() result: $success")
            }
        })
    }

}