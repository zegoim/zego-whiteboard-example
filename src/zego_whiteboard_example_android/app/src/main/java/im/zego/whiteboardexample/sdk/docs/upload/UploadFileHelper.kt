package im.zego.whiteboardexample.sdk.docs.upload

import android.app.Activity
import android.content.Intent
import android.util.Log
import im.zego.zegodocs.ZegoDocsViewConstants
import im.zego.zegodocs.ZegoDocsViewManager
import im.zego.whiteboardexample.tool.PermissionHelper
import im.zego.zegodocs.IZegoDocsViewUploadListener
import im.zego.zegodocs.ZegoDocsViewCustomH5Config

/**
 * 上传、取消上传
 */
class UploadFileHelper {
    companion object {
        private const val TAG = "UploadHelper"
        const val REQUEST_CODE_UPLOAD_FILE = 10000
        const val REQUEST_CODE_UPLOAD_H5 = 10001
        private var mRenderType: Int = ZegoDocsViewConstants.ZegoDocsViewRenderTypeVector
        private var mH5Config: ZegoDocsViewCustomH5Config = ZegoDocsViewCustomH5Config()
        private var seq: Int = 0

        object MimeType {
            const val PPT = "application/vnd.ms-powerpoint"
            const val PPTX =
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            const val DOC = "application/msword"
            const val DOCX =
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            const val XLS = "application/vnd.ms-excel"
            const val XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            const val PDF = "application/pdf"
            const val TXT = "text/plain"
            const val JPG = "image/jpeg"
            const val JPEG = "image/jpeg"
            const val PNG = "image/png"
            const val BMP = "image/bmp"
            const val XMSBMP = "image/x-ms-bmp"
            const val WBMP = "image/vnd.wap.wbmp"
            const val HEIC = "image/heic"
        }

        fun uploadFile(activity: Activity, renderType: Int) {
            PermissionHelper.onReadSDCardPermissionGranted(activity) { grant ->
                if (grant) {
                    val uploadIntent = Intent().also {
                        it.action = Intent.ACTION_OPEN_DOCUMENT
                        it.flags =
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        it.addCategory(Intent.CATEGORY_OPENABLE)
                        it.type = "*/*"
                    }
                    mRenderType = renderType
                    activity.startActivityForResult(uploadIntent, REQUEST_CODE_UPLOAD_FILE)
                }
            }
        }

        fun uploadH5File(activity: Activity, h5Config: ZegoDocsViewCustomH5Config) {
            PermissionHelper.onReadSDCardPermissionGranted(activity) { grant ->
                if (grant) {
                    val uploadIntent = Intent().also {
                        it.action = Intent.ACTION_OPEN_DOCUMENT
                        it.flags =
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        it.addCategory(Intent.CATEGORY_OPENABLE)
                        it.type = "*/*"
                    }
                    mH5Config = h5Config
                    activity.startActivityForResult(uploadIntent, REQUEST_CODE_UPLOAD_H5)
                }
            }
        }

        fun onActivityResult(
            context: Activity, requestCode: Int, resultCode: Int, data: Intent?,
            uploadResult: (Int, Int, String?, Float) -> Unit
        ) {
            val isUploadAction =
                (requestCode == REQUEST_CODE_UPLOAD_FILE || requestCode == REQUEST_CODE_UPLOAD_H5)
            if (resultCode == Activity.RESULT_OK && isUploadAction) {
                val fileUri = data?.data
                Log.d(TAG, "fileUri  = $fileUri ")
                if (fileUri != null) {
                    val contentResolver = context.contentResolver;
                    try {
                        contentResolver.takePersistableUriPermission(
                            fileUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: SecurityException) {
                        Log.e(TAG, "FAILED TO TAKE PERMISSION")
                    }
                    val path = FileUtil.getPath(context, fileUri)
                    val isH5File = requestCode == REQUEST_CODE_UPLOAD_H5
                    if (path != null) {
                        uploadFileInner(context, path, isH5File, uploadResult)
                    } else {
                        Log.e(TAG, "url is NULL")
                    }
                }
            }
        }

        private fun uploadFileInner(
            context: Activity,
            filePath: String,
            isH5File: Boolean,
            uploadResult: (errorCode: Int, state: Int, fileID: String?, uploadPercent: Float) -> Unit
        ) {
            val listener = IZegoDocsViewUploadListener { state, errorCode, infoMap ->
                Log.d(
                    TAG,
                    "uploadFileInner() called with: state = $state, errorCode = $errorCode, infoMap = $infoMap"
                )
                val sequence = infoMap[ZegoDocsViewConstants.REQUEST_SEQ]
                if (sequence != null) {
                    seq = sequence as Int
                }
                when {
                    errorCode != ZegoDocsViewConstants.ZegoDocsViewSuccess -> {
                        uploadResult(errorCode, state, null, 0f)
                    }
                    state == ZegoDocsViewConstants.ZegoDocsViewUploadStateUpload -> {
                        val uploadPercent =
                            infoMap[ZegoDocsViewConstants.UPLOAD_PERCENT] as Float * 100

                        uploadResult(errorCode, state, null, uploadPercent)
                    }
                    state == ZegoDocsViewConstants.ZegoDocsViewUploadStateConvert -> {
                        val fileID = infoMap[ZegoDocsViewConstants.UPLOAD_FILEID] as String
                        uploadResult(errorCode, state, fileID, 100f)
                    }
                }
            }
            if (isH5File) {
                ZegoDocsViewManager.getInstance().uploadH5File(filePath, mH5Config, listener)
            } else {
                ZegoDocsViewManager.getInstance().uploadFile(filePath, mRenderType, listener)
            }

        }

        fun cancelUploadFile(uploadResult: (Int) -> Unit) {
            if (seq != 0) {
                ZegoDocsViewManager.getInstance().cancelUploadFile(seq) { errorCode ->
                    uploadResult(errorCode)
                }
            }
        }

    }
}