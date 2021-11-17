package im.zego.whiteboardexample.sdk.docs

import android.app.Activity
import im.zego.zegodocs.ZegoDocsViewConstants
import im.zego.zegodocs.ZegoDocsViewManager
import im.zego.whiteboardexample.tool.PermissionHelper
import im.zego.whiteboardexample.util.AppLogger

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
            AppLogger.i(TAG, "cacheFileInner() fileID:${fileID}")

            ZegoDocsViewManager.getInstance().cacheFile(fileID)
            { state, errorCode, infoMap ->
                seq = infoMap[ZegoDocsViewConstants.REQUEST_SEQ] as Int
                when {
                    errorCode != ZegoDocsViewConstants.ZegoDocsViewSuccess -> {
                        AppLogger.i(TAG, "cacheFile(fileID):${fileID},ZegoDocsViewSuccess state: $state, errorCode: $errorCode, seq: $seq, cachePercent: 0")
                        cacheResult(errorCode, state, 0f)
                    }
                    state == ZegoDocsViewConstants.ZegoDocsViewCacheStateCaching -> {
                        val cachePercent = infoMap[ZegoDocsViewConstants.CACHE_PERCENT] as Float * 100
                        cacheResult(errorCode, state, cachePercent)
                        AppLogger.i(TAG, "cacheFile(fileID):${fileID},ZegoDocsViewCacheStateCaching state: $state, errorCode: $errorCode, seq: $seq, cachePercent: $cachePercent")
                    }
                    state == ZegoDocsViewConstants.ZegoDocsViewCacheStateCached -> {
                        AppLogger.i(TAG, "cacheFile(fileID):${fileID},ZegoDocsViewCacheStateCached state: $state, errorCode: $errorCode, seq: $seq, cachePercent: 100")
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
            AppLogger.i(TAG, "cancelCacheInner() seq:${seq}")
            ZegoDocsViewManager.getInstance().cancelCacheFile(seq) { errorCode ->

                AppLogger.i(TAG, "cancelCacheInner(seq):${seq},  errorCode: $errorCode")
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
            AppLogger.i(TAG, "queryFileCacheInner() fileID:${fileID}")
            ZegoDocsViewManager.getInstance().queryFileCached(fileID) { errorCode, exist ->
                AppLogger.i(TAG, "queryFileCacheInner(fileID):${fileID}, exist: $exist, errorCode: $errorCode")
                queryFileResult(errorCode, exist)
            }
        }

        fun clearAllCached(activity: Activity) {
            PermissionHelper.onReadSDCardPermissionGranted(activity) { grant ->
                if (grant) {
                    ZegoDocsViewManager.getInstance().clearCacheFolder()
                }
            }
        }

    }
}