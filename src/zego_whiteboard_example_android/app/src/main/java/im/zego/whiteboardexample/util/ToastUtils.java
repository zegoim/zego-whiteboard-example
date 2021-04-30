package im.zego.whiteboardexample.util;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;

import im.zego.whiteboardexample.R;

public final class ToastUtils {

    private ToastUtils() {
    }

    private static Toast mCenterToast;
    private static Context appContext;

    private static Toast getToast(Context context, int duration) {
        Toast toast;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            // 25 以后，系统做了限制，如果反复点击的都是同一个，会导致后面的暂时显示不出来
            toast = new Toast(context.getApplicationContext());
            toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            // 25 以前，toast太频繁会排队一直显示不消失
            // 显示在中间的toast不能覆盖掉显示在上面的toast
            if (mCenterToast == null) {
                mCenterToast = new Toast(context.getApplicationContext());
                mCenterToast.setGravity(Gravity.CENTER, 0, 0);
            }
            toast = mCenterToast;
        }
        //设置Toast显示位置，居中，向 X、Y轴偏移量均为0
        View view = LayoutInflater.from(context).inflate(R.layout.custom_toast, null);
        //设置显示时长
        toast.setDuration(duration);
        toast.setView(view);
        return toast;
    }

    private static void showCenterToastInner(Context context, String msg, int duration) {
        Toast toast = getToast(context, duration);
        //获取自定义视图
        TextView tvMessage = toast.getView().findViewById(R.id.tv_msg_toast);
        //设置文本
        tvMessage.setText(msg);
        //显示
        toast.show();
    }

    private static void showCenterToastInner(Context context, String msg) {
        showCenterToastInner(context, msg, Toast.LENGTH_SHORT);
    }

    public static void setAppContext(Context context) {
        appContext = context;
    }

    public static void showCenterToast(String string) {
        showCenterToastInner(appContext, string);
    }

    public static void showCenterToast(@StringRes int stringID) {
        showCenterToastInner(appContext, appContext.getString(stringID));
    }

    public static void showCenterToast(@StringRes int stringID, int duration) {
        showCenterToastInner(appContext, appContext.getString(stringID), duration);
    }

    public static void showCenterToast(@StringRes int stringID, Object... formatArgs) {
        showCenterToastInner(appContext, appContext.getString(stringID, formatArgs));
    }

}
