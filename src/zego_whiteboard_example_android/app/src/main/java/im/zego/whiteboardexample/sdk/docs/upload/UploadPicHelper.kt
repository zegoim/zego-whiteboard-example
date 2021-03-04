package im.zego.whiteboardexample.sdk.docs.upload

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.webkit.MimeTypeMap
import im.zego.whiteboardexample.tool.PermissionHelper
import im.zego.whiteboardexample.util.ToastUtils

class UploadPicHelper {

    companion object {
        private val TAG = "UploadPicHelper"
        var supportedImageExtension = arrayOf("jpeg", "jpg", "png", "svg")
        val REQUEST_CODE_FOR_CHOOSE_PICTURE = 123

        fun startChoosePicture(activity: Activity) {
//            PermissionHelper.onReadSDCardPermissionGranted(activity) {
//                val intent =
//                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//                intent.type = "image/*"
//                intent.putExtra(Intent.EXTRA_MIME_TYPES, getSupportedImageMimeTypes())
//                if (intent.resolveActivity(activity.packageManager) != null) {
//                    activity.startActivityForResult(intent, REQUEST_CODE_FOR_CHOOSE_PICTURE)
//                }
//            }
            PermissionHelper.onReadSDCardPermissionGranted(activity) { grant ->
                if (grant) {
                    val intent =
                        Intent().also {
                            it.action = Intent.ACTION_OPEN_DOCUMENT
                            it.flags =
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                            it.addCategory(Intent.CATEGORY_OPENABLE)
                            it.type = "image/*"
                            it.putExtra(Intent.EXTRA_MIME_TYPES, getSupportedImageMimeTypes())
                        }
                    if (intent.resolveActivity(activity.packageManager) != null) {
                        activity.startActivityForResult(intent, REQUEST_CODE_FOR_CHOOSE_PICTURE)
                    }
                }
            }
        }

        fun getSupportedImageMimeTypes(): Array<String?>? {
            val supportedImageMimeTypes =
                arrayOfNulls<String>(supportedImageExtension.size)
            for (i in supportedImageExtension.indices) {
                supportedImageMimeTypes[i] = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(supportedImageExtension.get(i))
            }
            return supportedImageMimeTypes
        }

        fun handleActivityResult(
            context: Activity,
            requestCode: Int,
            resultCode: Int,
            data: Intent?,
            uploadResult: (filePath: String) -> Unit
        ) {
            if (requestCode != REQUEST_CODE_FOR_CHOOSE_PICTURE) {
                return
            }
            // 取消上传
            if (resultCode != Activity.RESULT_OK) {
                ToastUtils.showCenterToast("取消了上传")
                return
            }
            val fileUri = data?.data
            if (fileUri != null) {
                val contentResolver = context.contentResolver
                try {
                    contentResolver.takePersistableUriPermission(
                        fileUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    Log.e(TAG, "FAILED TO TAKE PERMISSION")
                }
                uploadResult(FileUtil.getPath(context, fileUri))
            }
        }
    }
}