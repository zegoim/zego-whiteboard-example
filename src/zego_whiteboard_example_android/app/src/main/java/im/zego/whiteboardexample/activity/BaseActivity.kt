package im.zego.whiteboardexample.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import im.zego.whiteboardexample.util.SharedPreferencesUtil
import im.zego.whiteboardexample.util.ZegoUtil

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        // 如果 app 后台被系统干掉了，那么。。。
        if (SharedPreferencesUtil.getProcessID() != android.os.Process.myPid()) {
            ZegoUtil.killSelfAndRestart(this, LoginActivity::class.java)
        }
    }
}