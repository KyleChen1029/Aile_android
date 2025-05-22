package tw.com.chainsea.chat.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.chat.lib.Tools
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ImageUtil(
    private val context: Context
) {
    /**
     * 壓縮圖片
     * @param inputPath 原始圖片的路徑
     * @param maxSizeKB 要壓縮的最大大小
     * 先將圖片尺寸縮小 寬固定 1024
     * 再用二分法去找尋離最大大小最接近的壓縮率
     * @return outputFilePath 壓縮後的圖片路徑
     * */
    suspend fun compressImageFile(
        inputPath: String,
        maxSizeKB: Int = 512
    ): String? =
        withContext(Dispatchers.IO) {
            val originalFile = File(inputPath)
            if (!originalFile.exists()) return@withContext null

            if (originalFile.length() <= maxSizeKB * 1024L) {
                // 若檔案小於等於最大大小，則直接返回原檔案
                return@withContext originalFile.absolutePath
            }

            val originBitmap = BitmapFactory.decodeFile(inputPath) ?: return@withContext null
            // 先壓縮尺寸
            val scaledBitmap = getScaledBitmap(originBitmap)
            var bestQuality = 100
            if (scaledBitmap.allocationByteCount <= maxSizeKB * 1024L) {
                return@withContext saveScaledBitmapToCache(scaledBitmap, bestQuality)
            }

            // 如果還是超過指定大小 繼續壓縮畫質
            bestQuality = getBestQuality(scaledBitmap, maxSizeKB)

            // 以最適合的壓縮率輸出最終圖片
            val outputFilePath = getLowerQualityBitmap(scaledBitmap, bestQuality)

            return@withContext outputFilePath
        }

    /**
     * 壓縮尺寸
     * @param bitmap 需要壓縮的 bitmap
     * */
    private suspend fun getScaledBitmap(bitmap: Bitmap): Bitmap =
        withContext(Dispatchers.IO) {
            if (bitmap.width <= 1024) return@withContext bitmap
            val targetHeight = (bitmap.height.toFloat() / bitmap.width.toFloat() * 1024).toInt()
            return@withContext Bitmap.createScaledBitmap(bitmap, 1024, targetHeight, false)
        }

    /**
     * 壓縮畫質
     * @param bitmap 需要壓縮的 bitmap
     * @param quality 畫質 1~100
     * */
    private suspend fun getLowerQualityBitmap(
        bitmap: Bitmap,
        quality: Int
    ): String =
        withContext(Dispatchers.IO) {
            ByteArrayOutputStream().use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                return@withContext saveScaledBitmapToCache(bitmap, quality)
            }
        }

    /**
     * 取得最接近指定大小下的畫質
     * @param scaledBitmap 需要壓縮的 bitmap
     * @param maxSizeKB 指定的大小
     * @return 取得最接近的畫質
     * */
    private suspend fun getBestQuality(
        scaledBitmap: Bitmap,
        maxSizeKB: Int
    ): Int =
        withContext(Dispatchers.IO) {
            var minQuality = 1
            var maxQuality = 100
            var bestQuality = 100
            val maxSizeBytes = maxSizeKB * 1024L

            while (minQuality <= maxQuality) {
                val midQuality = (minQuality + maxQuality) / 2
                val byteArray = bitmapToByteArray(scaledBitmap, Bitmap.CompressFormat.JPEG, midQuality)

                if (byteArray.size > maxSizeBytes) {
                    maxQuality = midQuality - 1
                } else {
                    bestQuality = midQuality
                    minQuality = midQuality + 1
                }
            }
            return@withContext bestQuality
        }

    /**
     * 將 bitmap 轉成 byte array
     * @param bitmap 需要轉換的 bitmap
     * @param format 轉換後的格式
     * @param quality 畫質
     * @return ByteArray
     * */
    private suspend fun bitmapToByteArray(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        quality: Int
    ): ByteArray =
        withContext(Dispatchers.IO) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(format, quality, stream)
            return@withContext stream.toByteArray()
        }

    /***
     * 儲存壓縮完的圖片到 cache
     * @param bitmap 壓縮完的 bitmap
     * @param quality 畫質
     * @return 壓縮完的圖片路徑
     */
    private suspend fun saveScaledBitmapToCache(
        bitmap: Bitmap,
        quality: Int
    ): String =
        withContext(Dispatchers.IO) {
            val outputDir = context.cacheDir
            val outputName = Tools.createName("uid_photo", MessageType.IMAGE)
            val outputFile = File(outputDir, outputName)
            FileOutputStream(outputFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream) // 壓縮並寫入檔案
            }
            return@withContext outputFile.absolutePath
        }
}
