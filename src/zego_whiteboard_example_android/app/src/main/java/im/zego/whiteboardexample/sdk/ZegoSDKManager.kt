package im.zego.whiteboardexample.sdk

import android.app.Application
import im.zego.whiteboardexample.sdk.docs.DocsViewSDKManager
import im.zego.whiteboardexample.sdk.rtc.VideoSDKManager
import im.zego.whiteboardexample.sdk.whiteboard.WhiteboardSDKManager
import im.zego.whiteboardexample.util.AppLogger
import im.zego.whiteboardexample.util.SharedPreferencesUtil

/**
 * 对 express、白板 SDK、DocsView SDK 的封装
 */
object ZegoSDKManager {

    private const val TAG = "ZegoSDKManager"

    var whiteboardNameIndex = 1

    fun initSDKEnvironment(application: Application, sdkInitCallback: SDKInitCallback) {
        initTestEnv()
        // 该处需要先初始化 VideoSDKManager 再初始化 WhiteboardSDKManager，顺序不能乱
        VideoSDKManager.init(application, object : SDKInitCallback {
            override fun onInit(success: Boolean) {
                if (success) {
                    WhiteboardSDKManager.init(application, object : SDKInitCallback {
                        override fun onInit(success: Boolean) {
                            if (success) {
                                notifyInitResult(sdkInitCallback)
                            } else {
                                sdkInitCallback.onInit(false)
                            }
                        }
                    })
                } else {
                    sdkInitCallback.onInit(false)
                }
            }
        })

        DocsViewSDKManager.init(application, object : SDKInitCallback {
            override fun onInit(success: Boolean) {
                if (success) {
                    notifyInitResult(sdkInitCallback)
                } else {
                    sdkInitCallback.onInit(false)
                }
            }
        })
    }

    fun unInitSDKEnvironment() {
        AppLogger.i(TAG, "unInitSDKEnvironment() called")
        VideoSDKManager.unInitSDK()
        WhiteboardSDKManager.unInitSDK()
        DocsViewSDKManager.unInitSDK()
    }

    private fun initTestEnv() {
        // 测试版本，首次安装应用。设置各环境的初始值
        if (!SharedPreferencesUtil.containsVideoSDKTestEnvSp()) {
            SharedPreferencesUtil.setVideoSDKTestEnv(true)
        }
        if (!SharedPreferencesUtil.containsDocsViewTestEnvSp()) {
            SharedPreferencesUtil.setDocsViewTestEnv(true)
        }
        if (!SharedPreferencesUtil.containsNextStepFlipPageSp()) {
            SharedPreferencesUtil.setNextStepFlipPage(true)
        }
    }

    /**
     * 用于判断是否所有初始化(liveroom/express、白板 SDK、DocsView SDK)都完成了
     */
    fun notifyInitResult(SDKInitCallback: SDKInitCallback) {
        if (VideoSDKManager.initRoomResult != null && DocsViewSDKManager.initDocsResult != null && WhiteboardSDKManager.initWhiteboardResult != null) {
            SDKInitCallback.onInit(VideoSDKManager.initRoomResult as Boolean && DocsViewSDKManager.initDocsResult as Boolean && WhiteboardSDKManager.initWhiteboardResult as Boolean)
        }
    }

}