package tw.com.chainsea.chat.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color.*
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import androidx.core.content.FileProvider
import com.luck.picture.lib.config.PictureMimeType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.lib.ToastUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class FileUtils {

    companion object {

        @JvmStatic
        fun fileToType(file: File?): String {
            if (file != null) {
                val name = file.name
                if (name.endsWith(".mp4") || name.endsWith(".avi")
                    || name.endsWith(".3gpp") || name.endsWith(".3gp") || name.startsWith(".mov")
                ) {
                    return "video/mp4"
                } else if (name.endsWith(".PNG") || name.endsWith(".png") || name.endsWith(".jpeg")
                    || name.endsWith(".gif") || name.endsWith(".GIF") || name.endsWith(".jpg")
                    || name.endsWith(".webp") || name.endsWith(".WEBP") || name.endsWith(".JPEG")
                    || name.endsWith(".bmp")
                ) {
                    return "image/jpeg"
                } else if (name.endsWith(".mp3") || name.endsWith(".amr")
                    || name.endsWith(".aac") || name.endsWith(".war")
                    || name.endsWith(".flac") || name.endsWith(".lamr")
                    || name.endsWith("m4a")
                ) {
                    return "audio/mpeg"
                }
            }
            return "image/jpeg"
        }

        @JvmStatic
        fun pictureToVideo(pictureType: String): String {
            if (!TextUtils.isEmpty(pictureType)) {
                if (pictureType.startsWith("video")) {
                    return PictureMimeType.MIME_TYPE_VIDEO
                } else if (pictureType.startsWith("audio")) {
                    return PictureMimeType.MIME_TYPE_AUDIO
                }
            }
            return PictureMimeType.MIME_TYPE_IMAGE
        }

        @JvmStatic
        fun modifyTextViewDrawable(v: TextView, drawable: Drawable, index: Int) {
            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
            //index 0:左 1：上 2：右 3：下
            if (index == 0) {
                v.setCompoundDrawables(drawable, null, null, null)
            } else if (index == 1) {
                v.setCompoundDrawables(null, drawable, null, null)
            } else if (index == 2) {
                v.setCompoundDrawables(null, null, drawable, null)
            } else {
                v.setCompoundDrawables(null, null, null, drawable)
            }
        }

        @JvmStatic
        fun createImageType(path: String?): String {
            path?.let {
                try {
                    if (!TextUtils.isEmpty(path)) {
                        val file = File(path)
                        val fileName = file.name
                        val last = fileName.lastIndexOf(".") + 1
                        val temp = fileName.substring(last, fileName.length)
                        return "image/$temp"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return "image/jpeg"
                }
            }

            return "image/jpeg"
        }

        @JvmStatic
        fun createVideoType(path: String?): String {
            path?.let {
                try {
                    if (!TextUtils.isEmpty(path)) {
                        val file = File(path)
                        val fileName = file.name
                        val last = fileName.lastIndexOf(".") + 1
                        val temp = fileName.substring(last, fileName.length)
                        return "video/$temp"
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    return "video/mp4"
                }
            }
            return "video/mp4"
        }
        @JvmStatic
        fun dip2px(context: Context, dpValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }

        @JvmStatic
        fun mergeQrCode(qrCode: Bitmap, fileName: String, context: Context, isShareQrCode: Boolean, hintText: String) = CoroutineScope(Dispatchers.IO).launch {
            val isSave = bitmapToFile(qrCode, fileName, context)
            if (isSave) {
                if (isShareQrCode) { //點擊分享
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.setType("image/*")
                    val photoUri = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".fileprovider",
                        File(
                            DownloadUtil.downloadImageDir,
                            fileName
                        )
                    )
                    shareIntent.putExtra(Intent.EXTRA_STREAM, photoUri)
                    context.startActivity(shareIntent)
                } else {
                    //點擊下載
                    CoroutineScope(Dispatchers.Main).launch {
                        ToastUtils.showToast(
                            context,
                            hintText
                        )
                    }
                }
            }
        }

        fun bitmapToFile(bitmap: Bitmap?, fileName: String, context: Context): Boolean {
            val file: File?
            val fos: OutputStream
            var saved = false
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val cursor = resolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Images.Media._ID),
                        "${MediaStore.Images.Media.DISPLAY_NAME}=?",
                        arrayOf(fileName),
                        null
                    )

                    cursor?.use {
                        if (it.moveToFirst()) {
                            // 如果存在相同名稱的檔案，則刪除該檔案
                            val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                            val deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                            resolver.delete(deleteUri, null, null)
                        }
                    }
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        ?: throw IOException("Failed to create new MediaStore record.")
                    resolver.openOutputStream(uri)?.use {
                        saved = bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it) == true
                    } ?: throw IOException("Failed to open output stream.")
                    fos = resolver.openOutputStream(uri)!!
                } else {
                    file = File(Environment.getExternalStorageDirectory().absolutePath + "/Pictures/" + fileName)
                    file.createNewFile()
                    // Convert bitmap to byte array
                    val bos = ByteArrayOutputStream()
                    val bitMapData = bos.toByteArray()
                    fos = FileOutputStream(file)
                    saved = bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos) == true
                    fos.write(bitMapData)

                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    val contentUri = Uri.fromFile(file)
                    mediaScanIntent.data = contentUri
                    context.sendBroadcast(mediaScanIntent)
                }
                fos.flush()
                fos.close()
                saved
            } catch (e: Exception) {
                Log.e("bitmapToFile", "bitmapToFile ${e.localizedMessage}")
                saved
            }
        }

        fun getCircularBitmap(bitmap: Bitmap): Bitmap {
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            val color = RED
            val paint = Paint().apply {
                isAntiAlias = true
                this.color = color
            }

            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            val rectF = RectF(rect)

            canvas.drawARGB(0, 0, 0, 0)
            canvas.drawOval(rectF, paint)

            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)

            return output
        }

    }
}