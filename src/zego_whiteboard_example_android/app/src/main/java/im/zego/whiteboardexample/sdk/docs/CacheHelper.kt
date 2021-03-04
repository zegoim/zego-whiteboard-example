package im.zego.whiteboardexample.sdk.docs

import android.app.Activity
import im.zego.zegodocs.ZegoDocsViewConstants
import im.zego.zegodocs.ZegoDocsViewManager
import im.zego.whiteboardexample.tool.PermissionHelper
import im.zego.whiteboardexample.util.Logger

/**
 * 缓存、取消缓存
 */
class CacheHelper {
    companion object {
        private const val TAG = "CacheHelper"
        private var seq: Int = 0

        fun cacheFile(activity: Activity, fileID: String, cacheResult: (Int, Int, Float) -> Unit) {
            PermissionHelper.onReadSDCardPermissionGranted(activity) { grant ->
                if (grant) {
                    cacheFileInner(fileID, cacheResult)
                }
            }
        }

        private fun cacheFileInner(fileID: String, cacheResult: (errorCode: Int, state: Int, uploadPercent: Float) -> Unit) {
            Logger.i(TAG, "cacheFileInner() fileID:${fileID}")

            ZegoDocsViewManager.getInstance().cacheFile(fileID)
            { state, errorCode, infoMap ->
                seq = infoMap[ZegoDocsViewConstants.REQUEST_SEQ] as Int
                when {
                    errorCode != ZegoDocsViewConstants.ZegoDocsViewSuccess -> {
                        Logger.i(TAG, "cacheFile(fileID):${fileID}, state: $state, errorCode: $errorCode, seq: $seq")
                        cacheResult(errorCode, state, 0f)
                    }
                    state == ZegoDocsViewConstants.ZegoDocsViewCacheStateCaching -> {
                        val uploadPercent = infoMap[ZegoDocsViewConstants.UPLOAD_PERCENT] as Float * 100
                        cacheResult(errorCode, state, uploadPercent)
                    }
                    state == ZegoDocsViewConstants.ZegoDocsViewCacheStateCached -> {
                        Logger.i(TAG, "cacheFile(fileID):${fileID}, state: $state, errorCode: $errorCode, seq: $seq")
                        cacheResult(errorCode, state, 100f)
                    }
                }
            }
        }

        fun cancelCache(activity: Activity, cancelCacheResult: (Int) -> Unit) {
            PermissionHelper.onReadSDCardPermissionGranted(activity) { grant ->
                if (grant) {
                    cancelCacheInner(seq, cancelCacheResult)
                }
            }
        }


        private fun cancelCacheInner(seq: Int, cancelCacheResult: (Int) -> Unit) {
            Logger.i(TAG, "cancelCacheInner() seq:${seq}")
            ZegoDocsViewManager.getInstance().cancelCacheFile(seq) { errorCode ->

                Logger.i(TAG, "cancelCacheInner(seq):${seq},  errorCode: $errorCode")
                cancelCacheResult(errorCode)


            }
        }


        fun queryFileCached(activity: Activity, fileID: String, queryFileResult: (errorCode: Int, exist: Boolean) -> Unit) {
            PermissionHelper.onReadSDCardPermissionGranted(activity) { grant ->
                if (grant) {
                    queryFileCacheInner(fileID, queryFileResult)
                }
            }
        }

        private fun queryFileCacheInner(fileID: String, queryFileResult: (errorCode: Int, exist: Boolean) -> Unit) {
            Logger.i(TAG, "queryFileCacheInner() fileID:${fileID}")
            ZegoDocsViewManager.getInstance().queryFileCached(fileID) { errorCode, exist ->
                Logger.i(TAG, "queryFileCacheInner(fileID):${fileID}, exist: $exist, errorCode: $errorCode")
                queryFileResult(errorCode, exist)
            }
        }

    }
}