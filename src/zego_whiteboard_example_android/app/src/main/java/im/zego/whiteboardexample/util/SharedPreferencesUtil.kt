package im.zego.whiteboardexample.util

import android.content.Context
import android.content.SharedPreferences
import im.zego.whiteboardexample.AuthConstants

class SharedPreferencesUtil {
    companion object {
        private lateinit var context: Context
        const val env = "ENV_SETTING"

        fun setApplicationContext(applicationContext: Context) {
            context = applicationContext
        }

        private fun getSharedPreferences(spFileName: String): SharedPreferences {
            return context.getSharedPreferences(spFileName, Context.MODE_PRIVATE)
        }

        @JvmStatic
        fun setLastJoinName(name: String) {
            val sharedPreferences = getSharedPreferences(env)
            sharedPreferences.edit().putString("lastJoinName", name).apply()
        }

        @JvmStatic
        fun getLastJoinName(): String {
            val sharedPreferences = getSharedPreferences(env)
            return sharedPreferences.getString("lastJoinName", "")!!
        }

        @JvmStatic
        fun setLastJoinID(roomID: String) {
            val sharedPreferences = getSharedPreferences(env)
            sharedPreferences.edit().putString("lastJoinID", roomID).apply()
        }

        @JvmStatic
        fun getLastJoinID(): String {
            val sharedPreferences = getSharedPreferences(env)
            return sharedPreferences.getString("lastJoinID", "")!!
        }

        @JvmStatic
        fun getProcessID(): Int {
            val sharedPreferences = getSharedPreferences(env)
            return sharedPreferences.getInt("processID", 0)
        }

        @JvmStatic
        fun setProcessID(processID: Int) {
            val sharedPreferences = getSharedPreferences(env)
            sharedPreferences.edit().putInt("processID", processID).apply()
        }

        @JvmStatic
        fun getDocsViewEnv(): String {
            val sharedPreferences = getSharedPreferences(env)
            return sharedPreferences.getString("docs_view_env", "1")!!
        }

        @JvmStatic
        fun containsNextStepFlipPageSp(): Boolean {
            val sharedPreferences = getSharedPreferences(env)
            return sharedPreferences.contains("next_step_flip_page")
        }

        @JvmStatic
        fun setNextStepFlipPage(isTestEnv: Boolean) {
            val sharedPreferences = getSharedPreferences(env)
            sharedPreferences.edit().putBoolean("next_step_flip_page", isTestEnv).apply()
        }

        @JvmStatic
        fun isNextStepFlipPage():Boolean{
            val sharedPreferences = getSharedPreferences(env)
            return sharedPreferences.getBoolean("next_step_flip_page", true)
        }

        @JvmStatic
        fun isSystemTextStyle():Boolean{
            val sharedPreferences = getSharedPreferences(env)
            return sharedPreferences.getBoolean("text_style", true)
        }

        /**
         * k-v:获取本地缓存的缩略图清晰度类型
         *
         * 没有对应的setter方法是因为：
         * 我们在[im.zego.whiteboardexample.activity.SettingActivity.SettingsFragment]中
         * 使用了[androidx.preference.ListPreference]控件。
         *
         * 该控件内部封装了SharedPreferences，当用户每次改变清晰度类型时，会自动设置到指定的k-v中去
         */
        @JvmStatic
        fun getThumbnailClarityType(): String {
            val sharedPreferences = getSharedPreferences(env)
            return sharedPreferences.getString("key_thumbnail_clarity", "1")!!
        }

        @JvmStatic
        fun setLoadVideoInPPT(loadVideo: Boolean) {
            val sharedPreferences = getSharedPreferences(env)
            sharedPreferences.edit().putBoolean("load_video", loadVideo).apply()
        }

        @JvmStatic
        fun containsLoadVideoInPPT():Boolean{
            val sharedPreferences = getSharedPreferences(env)
            return sharedPreferences.contains("load_video")
        }

        @JvmStatic
        fun isVideoLoadedInPPT():Boolean{
            val sharedPreferences = getSharedPreferences(env)
            return sharedPreferences.getBoolean("load_video", true)
        }
    }
}