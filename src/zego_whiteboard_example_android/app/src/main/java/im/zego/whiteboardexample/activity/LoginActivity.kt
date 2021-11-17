package im.zego.whiteboardexample.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import im.zego.whiteboardexample.R
import im.zego.whiteboardexample.sdk.docs.DocsViewSDKManager
import im.zego.whiteboardexample.sdk.rtc.VideoSDKManager
import im.zego.whiteboardexample.sdk.whiteboard.WhiteboardSDKManager
import im.zego.whiteboardexample.util.*
import im.zego.whiteboardexample.widget.dialog.LoadingDialog
import im.zego.whiteboardexample.widget.dialog.dismissLoadingDialog
import im.zego.whiteboardexample.widget.dialog.showLoadingDialog
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseActivity() {

    private val TAG = "LoginActivity"
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loadingDialog = LoadingDialog(this, 0.1f)

        initView()
        initListener()
    }

    private fun initView() {
        val radius = dp2px(this, 27.5f)
        CONFERENCE_ID = SharedPreferencesUtil.getLastJoinID()

        // 房间ID RoomId
        join_room_id.setText(CONFERENCE_ID)
        join_room_id.setSelection(CONFERENCE_ID.length)
        join_room_id.background = getRoundRectDrawable("#f4f5f8", radius)
        // 房间名称 RoomName
        join_room_name.setText(SharedPreferencesUtil.getLastJoinName())
        join_room_name.filters = arrayOf(nameFilter)
        join_room_name.background = getRoundRectDrawable("#f4f5f8", radius)
        // 登陆
        join_entrance_main.background = ZegoUtil.getJoinBtnDrawable(this)
    }

    private fun initListener() {
        // 登陆按钮
        join_entrance_main.setOnClickListener {
            if (join_room_id.text.isEmpty() || join_room_name.text.isEmpty()) {
                ToastUtils.showCenterToast(R.string.join_input_null)
                return@setOnClickListener
            }

            when {
                VideoSDKManager.initRoomResult == null || VideoSDKManager.initRoomResult == false -> {
                    ToastUtils.showCenterToast(R.string.join_init_video_failed)
                }
                DocsViewSDKManager.initDocsResult == null || DocsViewSDKManager.initDocsResult == false -> {
                    ToastUtils.showCenterToast(R.string.join_init_docs_failed)
                }
                WhiteboardSDKManager.initWhiteboardResult == null || WhiteboardSDKManager.initWhiteboardResult == false -> {
                    ToastUtils.showCenterToast(R.string.join_init_wb_failed)
                }
                else -> {
                    login()
                }
            }
        }

        // 设置按钮
        join_setting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
    }

    /**
     * 登陆
     */
    private fun login() {
        showLoadingDialog(loadingDialog, "")
        VideoSDKManager.loginRoom(join_room_id.text.toString(), join_room_name.text.toString()) {
            Logger.i(TAG, "login, stateCode:$it")
            if (it == 0) {
                CONFERENCE_ID = join_room_id.text.toString()
                SharedPreferencesUtil.setLastJoinID(join_room_id.text.toString())
                SharedPreferencesUtil.setLastJoinName(join_room_name.text.toString())
                startActivity(Intent(this, MainActivity::class.java))
            }
            dismissLoadingDialog(loadingDialog)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }
}