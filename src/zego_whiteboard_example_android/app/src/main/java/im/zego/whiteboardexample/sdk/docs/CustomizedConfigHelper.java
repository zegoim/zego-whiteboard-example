package im.zego.whiteboardexample.sdk.docs;

import androidx.annotation.NonNull;

import im.zego.whiteboardexample.util.AppLogger;
import im.zego.zegodocs.ZegoDocsViewManager;

public class CustomizedConfigHelper {
    private final static String TAG = "CustomizedConfigHelper";

    /**
     * 动态PPT当前页最后一步点击时，是否触发翻页
     * @param isNextStepFlipPage  点击下一步是否翻页
     */
    public static void updatePPTStepMode(boolean isNextStepFlipPage) {
        String pptStepMode = isNextStepFlipPage ? "1" : "2";
        AppLogger.i(TAG, "updatePPTStepMode:" + pptStepMode);
        ZegoDocsViewManager.getInstance().setCustomizedConfig("pptStepMode", pptStepMode);
    }

    /**
     * 更新文件缩略图转码后的清晰度
     * 选项值[type] :
     * 1 -> 普通
     * 2 -> 标清
     * 3 -> 高清
     */
    public static void updateThumbnailClarity(@NonNull String type) {
        AppLogger.i(TAG, "updateThumbnailClarity:" + type);
        ZegoDocsViewManager.getInstance().setCustomizedConfig("thumbnailMode", type);
    }
}
