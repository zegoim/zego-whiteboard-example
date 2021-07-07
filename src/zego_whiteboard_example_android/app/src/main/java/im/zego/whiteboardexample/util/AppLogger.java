package im.zego.whiteboardexample.util;

import android.util.Log;

import im.zego.whiteboardexample.BuildConfig;


/**
 * 控制日志打印
 */

public class AppLogger {
    private static final String TAG_PREFIX = "ZEGO_";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    public static void d(String tag, String log) {
        if (DEBUG) {
            Log.d(wrapTag(tag), log);
        }
    }

    public static void i(String tag, String log) {
        if (DEBUG) {
            Log.i(wrapTag(tag), log);
        }
    }

    public static void w(String tag, String log) {
        if (DEBUG) {
            Log.w(wrapTag(tag), log);
        }
    }

    public static void e(String tag, String log) {
        if (DEBUG) {
            Log.e(wrapTag(tag), log);
        }
    }

    public static String wrapTag(String tag) {
        return TAG_PREFIX + tag;
    }

}
