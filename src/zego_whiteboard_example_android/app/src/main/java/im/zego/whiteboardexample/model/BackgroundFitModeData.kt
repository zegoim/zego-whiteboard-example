package im.zego.whiteboardexample.model

import im.zego.zegowhiteboard.ZegoWhiteboardViewImageFitMode

/**
 * 设置背景图片的显示模式
 *
 * @param mode 下载图片的 URL
 * @param modeName 模式名称
 */
class BackgroundFitModeData (val mode: ZegoWhiteboardViewImageFitMode, var modeName: String) {
    override fun toString(): String {
        return modeName
    }
}