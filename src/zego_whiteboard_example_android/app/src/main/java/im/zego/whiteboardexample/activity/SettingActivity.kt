package im.zego.whiteboardexample.activity

import android.os.Build
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import im.zego.zegodocs.ZegoDocsViewManager
import im.zego.zegowhiteboard.ZegoWhiteboardManager
import im.zego.whiteboardexample.BuildConfig
import im.zego.whiteboardexample.R
import im.zego.whiteboardexample.sdk.docs.CustomizedConfigHelper
import im.zego.whiteboardexample.sdk.docs.DocsViewSDKManager
import im.zego.whiteboardexample.sdk.rtc.VideoSDKManager
import im.zego.whiteboardexample.util.*
import kotlinx.android.synthetic.main.activity_setting.*
import java.lang.StringBuilder

class SettingActivity : BaseActivity() {

    private val TAG = "SettingActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }

        initListener()

        video_version.text = "video: ${VideoSDKManager.getVersion()}"
        app_version.text = "app: ${BuildConfig.VERSION_NAME}"
        docs_version.text = "docs: ${ZegoDocsViewManager.getInstance().version}"
        whiteboard_version.text = "whiteboard: ${ZegoWhiteboardManager.getInstance().version}"
        abi.text = BuildConfig.abi_Filters

        val builder = StringBuilder("support_abi:")
        Build.SUPPORTED_ABIS.forEach {
            builder.append(" $it ")
        }
        support_abi.text = builder.toString()

        setting_upload_log.setOnClickListener {
            VideoSDKManager.uploadLog()
        }
    }

    private fun initListener() {
        // 返回上一页
        setting_back.setOnClickListener {
            onBackPressed()
        }

        // 保存
        setting_save.setOnClickListener {
            // 重启使环境生效
            val time = 2
            setting_save.postDelayed({
                ZegoUtil.killSelfAndRestart(this, LoginActivity::class.java)
            }, time * 1000L)
            ToastUtils.showCenterToast("${time}秒后重启应用")
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = SharedPreferencesUtil.env
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            // 房间服务环境
            findPreference<SwitchPreferenceCompat>("video_env")?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    true
                }
            // 文件服务环境
            findPreference<SwitchPreferenceCompat>("docs_view_env")?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    true
                }
            // 下一步触发下一页
            findPreference<SwitchPreferenceCompat>("next_step_flip_page")?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    CustomizedConfigHelper.updatePPTStepMode(newValue as Boolean)
                    true
                }
            CustomizedConfigHelper.updatePPTStepMode(SharedPreferencesUtil.isNextStepFlipPage())

            // 字体
            findPreference<SwitchPreferenceCompat>("text_style")?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    true
                }

            val listPreference = findPreference<ListPreference>("key_thumbnail_clarity")
            listPreference?.summary = listPreference?.entries?.get(
                listPreference.findIndexOfValue(SharedPreferencesUtil.getThumbnailClarityType())
            )
            CustomizedConfigHelper.updateThumbnailClarity(SharedPreferencesUtil.getThumbnailClarityType())

            listPreference?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    CustomizedConfigHelper.updateThumbnailClarity(newValue.toString())
                    if (preference is ListPreference) {
                        preference.summary = preference.entries[preference.findIndexOfValue(newValue.toString())]
                        preference.value = newValue.toString()
                    }
                    true
                }
        }
    }
}