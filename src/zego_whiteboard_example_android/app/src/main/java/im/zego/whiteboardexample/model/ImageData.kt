package im.zego.whiteboardexample.model

/**
 * 下载图片的信息
 *
 * @param url 下载图片的 URL
 * @param imageName 图片名称，通过截取最后一个 “/” 之后的全部
 */
class ImageData(val url: String, var imageName: String) {
    override fun toString(): String {
        return imageName
    }
}