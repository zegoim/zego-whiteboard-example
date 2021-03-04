package im.zego.whiteboardexample.sdk.docs.upload

import android.app.Activity
import android.content.Intent
import android.util.Log
import im.zego.zegodocs.ZegoDocsViewConstants
import im.zego.zegodocs.ZegoDocsViewManager
import im.zego.whiteboardexample.tool.PermissionHelper

/**
 * 上传、取消上传
 */
class UploadFileHelper {
    companion object {
        private const val TAG = "UploadHelper"
        const val REQUEST_CODE_UPLOAD = 10000
        private var mRenderType: Int = ZegoDocsViewConstants.ZegoDocsViewRenderTypeVector
        private var seq: Int = 0

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
                    mRenderType = renderType;
                    activity.startActivityForResult(uploadIntent, REQUEST_CODE_UPLOAD)
                }
            }
        }

        fun onActivityResult(
            context: Activity, requestCode: Int, resultCode: Int, data: Intent?,
            uploadResult: (Int, Int, String?, Float) -> Void?
        ) {
            if (requestCode == REQUEST_CODE_UPLOAD && resultCode == Activity.RESULT_OK) {
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
                    uploadFileInner(context, FileUtil.getPath(context, fileUri), uploadResult as (Int, Int, String?, Float) -> Void)
                }
            }
        }

        private fun uploadFileInner(
            context: Activity,
            filePath: String,
            uploadResult: (errorCode: Int, state: Int, fileID: String?, uploadPercent: Float) -> Void
        ) {
            ZegoDocsViewManager.getInstance().uploadFile(filePath, mRenderType)
            { state, errorCode, infoMap ->
                seq = infoMap[ZegoDocsViewConstants.REQUEST_SEQ] as Int
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
        }

        fun cancelUploadFile(uploadResult: (Int) -> Unit) {
            ZegoDocsViewManager.getInstance()
                .cancelUploadFile(seq) { errorCode -> uploadResult(errorCode) }
        }

    }
}