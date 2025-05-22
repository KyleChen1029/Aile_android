package tw.com.chainsea.ce.sdk.lib

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tw.com.chainsea.android.common.log.CELog
import java.io.File
import java.util.concurrent.TimeUnit

object BitmapCacheUtil {

    private val diskCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES)
        .build(object : CacheLoader<String, Bitmap?>() {
            override fun load(key: String): Bitmap? {
                val path = Environment.getDataDirectory()
                    .toString() + "/user/0/tw.com.chainsea.chat/cache/temp/avatar/"
                val composeFile = File(path + key)
                return if (composeFile.exists()) {
                    BitmapFactory.decodeFile(composeFile.path)
                } else null
            }
        })

    suspend fun getCache(path: String): Bitmap? = withContext(Dispatchers.IO) {
        return@withContext try {
            val bitmap = diskCache[path]
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap
            } else {
                diskCache.invalidate(path)
                null
            }
        } catch (e: Exception) {
//            CELog.w(e.message)
            null
        }
    }

    fun putCache(path: String, bitmap: Bitmap?) {
        bitmap?.let {
            if (!it.isRecycled) {
                val newBitmap =
                    Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, false)
                diskCache.put(path, newBitmap)
            }
        }
    }

    fun clearAllCache() {
        diskCache.cleanUp()
    }
}