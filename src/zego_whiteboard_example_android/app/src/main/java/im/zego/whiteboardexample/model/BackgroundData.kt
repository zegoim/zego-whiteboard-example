package im.zego.whiteboardexample.model

import im.zego.zegowhiteboard.ZegoWhiteboardViewImageFitMode

/**
 * 设置背景图片的信息
 *
 * @param url 背景图片的 URL
 * @param imageName 图片名称
 */
class BackgroundData (val url: String, var imageName: String) {
    override fun toString(): String {
        return imageName
    }
}