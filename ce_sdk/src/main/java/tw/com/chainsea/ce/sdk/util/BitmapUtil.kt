package tw.com.chainsea.ce.sdk.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.ui.UiHelper
import tw.com.chainsea.ce.sdk.R
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomMemberResponse
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Locale

object BitmapUtil {
    private val colors =
        arrayOf(
            "#1ABC9C",
            "#3498DB",
            "#6699CC",
            "#F1C40F",
            "#8E44AD",
            "#B45B3E",
            "#E74C3C",
            "#D35400",
            "#479AC7",
            "#2C3E50",
            "#7F8C8D",
            "#336699",
            "#66CCCC",
            "#00B271"
        )
    private const val WIDTH = 118
    private const val HEIGHT = 118

    fun saveAvatarFromBitmap(
        mContext: Context,
        bitmap: Bitmap,
        filename: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (filename.isEmpty()) return@launch
        val myDir = File("${mContext.filesDir.absolutePath}/avatars/")
        if (!myDir.exists()) {
            myDir.mkdirs()
        }
        val file = File(myDir, "$filename.jpg")
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
            Log.d("Kyle111", "save avatar done, name=$filename")
        } catch (e: IOException) {
            Log.e("Kyle111", "saveAvatarFromBitmap error=${e.message}")
        }
    }

    // 從 Url 獲取圖片
    private fun getBitmapFromUrl(
        context: Context,
        avatarId: String
    ): Deferred<Bitmap?> =
        CoroutineScope(Dispatchers.IO).async {
            val avatarUrl = if (avatarId.startsWith("http")) avatarId else getCeAvatarUrl(context, avatarId)
            try {
                Glide
                    .with(context)
                    .asBitmap()
                    .load(avatarUrl)
                    .error(R.drawable.custom_default_avatar)
                    .submit()
                    .get()
            } catch (e: Exception) {
                null
            }
        }

    fun getChatRoomMemberIdsAndLoadMultiAvatarIcon(
        context: Context,
        chatRoomMember: List<ChatRoomMemberResponse>?,
        roomId: String? = null
    ) = CoroutineScope(Dispatchers.IO).launch {
        loadMultiAvatarIcon(context, chatRoomMember?.take(4)?.map { it.memberId } ?: listOf(), roomId)
    }

    // 多人聊天室頭圖
    private fun loadMultiAvatarIcon(
        context: Context,
        chatRoomMemberIds: List<String>?,
        roomId: String? = null
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (roomId.isNullOrEmpty()) {
            // 針對建立多人聊天室使用，不儲存
            createNewMultiAvatarIcon(context, chatRoomMemberIds, null)
        } else {
            // 已建立的多人聊天室組合頭圖
            var avatarIconName = roomId

            chatRoomMemberIds?.forEach { id ->
                val userProfile = DBManager.getInstance().queryUser(id)
                userProfile?.let {
                    val nickName = it.nickName
                    var avatarId = ""
                    it.avatarId?.let { id ->
                        avatarId = id
                    }
                    avatarIconName += (nickName + avatarId)
                }
            }

            val avatarPic = File("${context.filesDir.absolutePath}/avatars/$avatarIconName.jpg")
            if (!avatarPic.exists()) {
                createNewMultiAvatarIcon(context, chatRoomMemberIds, roomId, isSaved = true)
            }
        }
    }

    private suspend fun getChatRoomMemberAvatarIds(ids: List<String>?): List<String> =
        withContext(Dispatchers.IO) {
            ids?.take(4)?.map { id ->
                val userProfile = DBManager.getInstance().queryUser(id)
                userProfile?.let {
                    if (it.avatarId.isNullOrEmpty()) {
                        it.nickName
                    } else {
                        it.avatarId
                    }
                } ?: ""
            } ?: emptyList()
        }

    /**
     * 組合多人聊天室頭圖icon名稱 roomId + 每個成員nickName + 每個成員avatarId
     * 若avatarId為空，則 ""
     * 用來判定當某個成員名稱或 avatarId 有改變，則組合新頭圖
     */
    private fun createNewMultiAvatarIcon(
        context: Context,
        chatRoomMemberIds: List<String>?,
        roomId: String?,
        isSaved: Boolean = false
    ) = CoroutineScope(Dispatchers.IO).launch {
        val copyChatRoomMemberIds = chatRoomMemberIds?.toMutableList()
        val avatarIds = getChatRoomMemberAvatarIds(chatRoomMemberIds)
        copyChatRoomMemberIds?.let {
            val output = Bitmap.createBitmap(45, 45, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            // 依照人數判斷要畫哪種的大頭貼
            when (it.size) {
                1 -> {
                    drawOneAvatar(context, it, avatarIds, roomId, isSaved)
                }

                2 -> {
                    drawTwoAvatar(context, canvas, output, it, avatarIds, roomId, isSaved)
                }

                3 -> {
                    drawThreeAvatar(context, canvas, output, it, avatarIds, roomId, isSaved)
                }

                4 -> {
                    drawFourAvatar(context, canvas, output, it, avatarIds, roomId, isSaved)
                }

                else -> {
                    drawFourAvatar(context, canvas, output, it, avatarIds, roomId, isSaved)
                }
            }
        }
    }

    private fun drawOneAvatar(
        context: Context,
        chatRoomMemberIds: List<String>,
        avatarIds: List<String>,
        roomId: String?,
        isSaved: Boolean
    ) = CoroutineScope(Dispatchers.IO).launch {
        val userProfile = DBManager.getInstance().queryUser(chatRoomMemberIds[0])
        userProfile?.let {
            val nickName = it.nickName
            // 文字頭圖
            val textIcon = getTextAvatar(context, nickName)
            // 使用者上傳頭圖
            var bitmapIcon: Bitmap? = null
            var avatarId: String? = ""
            it.avatarId?.let { id ->
                avatarId = id
                bitmapIcon = getBitmapFromUrl(context, id).await()
            }
            // 判斷是要用哪種頭圖
            val targetBitmap = bitmapIcon ?: textIcon
            if (isSaved && !roomId.isNullOrEmpty()) {
                targetBitmap?.let { bitmap ->
                    saveAvatarFromBitmap(
                        context,
                        bitmap,
                        this@BitmapUtil.toSecureHashName(roomId + nickName + avatarId)
                    )
                }
            }
        }
    }

    // 兩個頭貼拼接
    private fun drawTwoAvatar(
        context: Context,
        canvas: Canvas,
        output: Bitmap,
        chatRoomMemberIds: List<String>,
        avatarIds: List<String>,
        roomId: String?,
        isSaved: Boolean
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            var avatarIconName = ""
            chatRoomMemberIds.forEachIndexed { index, id ->
                val userProfile = DBManager.getInstance().queryUser(id)
                userProfile?.let {
                    // 文字頭圖
                    val nickName = it.nickName
                    val textIcon = getTextAvatar(context, nickName)
                    // 使用者上傳頭圖
                    var bitmapIcon: Bitmap? = null
                    var avatarId: String? = ""
                    it.avatarId?.let { id ->
                        avatarId = id
                        bitmapIcon = getBitmapFromUrl(context, id).await()
                    }
                    avatarIconName += (nickName + avatarId)
                    // 判斷是要用哪種頭圖
                    val targetBitmap = bitmapIcon ?: textIcon

                    when (index) {
                        0 -> {
                            targetBitmap?.let {
                                drawLeftHalfCircle(canvas, it)
                            }
                        }

                        1 -> {
                            targetBitmap?.let {
                                drawRightHalfCircle(canvas, it)
                            }
                        }
                    }
                }
            }
            if (isSaved && !roomId.isNullOrEmpty()) {
                saveAvatarFromBitmap(
                    context,
                    output,
                    this@BitmapUtil.toSecureHashName(roomId + avatarIconName)
                )
            }
        }
    }

    // 三個頭圖拼接
    private fun drawThreeAvatar(
        context: Context,
        canvas: Canvas,
        output: Bitmap,
        chatRoomMemberIds: List<String>,
        avatarIds: List<String>,
        roomId: String?,
        isSaved: Boolean
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            var avatarIconName = ""
            chatRoomMemberIds.forEachIndexed { index, id ->
                val userProfile = DBManager.getInstance().queryUser(id)
                userProfile.let {
                    val nickName = it.nickName
                    // 文字頭圖
                    val textIcon = getTextAvatar(context, nickName)
                    // 使用者上傳頭圖
                    var bitmapIcon: Bitmap? = null
                    var avatarId: String? = ""
                    it.avatarId?.let { id ->
                        avatarId = id
                        bitmapIcon = getBitmapFromUrl(context, id).await()
                    }
                    avatarIconName += (nickName + avatarId)
                    // 判斷是要用哪種頭圖
                    val targetBitmap = bitmapIcon ?: textIcon
                    when (index) {
                        0 -> {
                            targetBitmap?.let {
                                drawLeftHalfCircle(canvas, it)
                            }
                        }

                        1 -> {
                            targetBitmap?.let {
                                drawRightTopQuarter(canvas, it)
                            }
                        }

                        2 -> {
                            targetBitmap?.let {
                                drawRightBottomQuarter(canvas, it)
                            }
                        }
                    }
                }
            }
            if (isSaved && !roomId.isNullOrEmpty()) {
                saveAvatarFromBitmap(
                    context,
                    output,
                    this@BitmapUtil.toSecureHashName(roomId + avatarIconName)
                )
            }
        }
    }

    // 四個或以上的圖頭拼接
    private fun drawFourAvatar(
        context: Context,
        canvas: Canvas,
        output: Bitmap,
        chatRoomMemberIds: List<String>,
        avatarIds: List<String>,
        roomId: String?,
        isSaved: Boolean
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            var avatarIconName = ""
            chatRoomMemberIds.forEachIndexed { index, id ->
                val userProfile = DBManager.getInstance().queryUser(id) ?: return@forEachIndexed
                val nickName = userProfile.nickName
                // 文字頭圖
                val textIcon = getTextAvatar(context, nickName)
                // 使用者上傳頭圖
                var bitmapIcon: Bitmap? = null
                var avatarId: String? = ""
                userProfile.avatarId?.let { id ->
                    avatarId = id
                    bitmapIcon = getBitmapFromUrl(context, id).await()
                }
                avatarIconName += (nickName + avatarId)
                // 判斷是要用哪種頭圖
                val targetBitmap = bitmapIcon ?: textIcon
                when (index) {
                    0 -> {
                        targetBitmap?.let {
                            drawLeftTopQuarter(canvas, it)
                        }
                    }

                    1 -> {
                        targetBitmap?.let {
                            drawRightTopQuarter(canvas, it)
                        }
                    }

                    2 -> {
                        targetBitmap?.let {
                            drawLeftBottomQuarter(canvas, it)
                        }
                    }

                    3 -> {
                        targetBitmap?.let {
                            drawRightBottomQuarter(canvas, it)
                        }
                    }

                    else -> {
                        return@forEachIndexed
                    }
                }
            }
            if (isSaved && !roomId.isNullOrEmpty()) {
                saveAvatarFromBitmap(
                    context,
                    output,
                    this@BitmapUtil.toSecureHashName(roomId + avatarIconName)
                )
            }
        }
    }

    // 繪製左半圓
    private fun drawLeftHalfCircle(
        canvas: Canvas,
        bitmap: Bitmap
    ) {
        val corpBitmap =
            Bitmap.createBitmap(bitmap, bitmap.width / 4, 0, bitmap.width / 2, bitmap.height)
        val rectF = RectF(0f, 0f, (WIDTH / 2).toFloat(), (HEIGHT).toFloat())
        canvas.drawBitmap(corpBitmap, null, rectF, Paint())
    }

    // 繪製右半圓
    private fun drawRightHalfCircle(
        canvas: Canvas,
        bitmap: Bitmap
    ) {
        val corpBitmap =
            Bitmap.createBitmap(bitmap, bitmap.width / 4, 0, bitmap.width / 2, bitmap.height)
        val rectF = RectF((WIDTH / 2).toFloat(), 0f, (WIDTH).toFloat(), (HEIGHT).toFloat())
        canvas.drawBitmap(corpBitmap, null, rectF, Paint())
    }

    // 繪製左上四分之一圓
    private fun drawLeftTopQuarter(
        canvas: Canvas,
        bitmap: Bitmap
    ) {
        val rectF = RectF(0f, 0f, (WIDTH / 2).toFloat(), (HEIGHT / 2).toFloat())
        canvas.drawBitmap(bitmap, null, rectF, Paint())
    }

    // 繪製右上四分之一圓
    private fun drawRightTopQuarter(
        canvas: Canvas,
        bitmap: Bitmap
    ) {
        val rectF = RectF((WIDTH / 2).toFloat(), 0f, (WIDTH).toFloat(), (HEIGHT / 2).toFloat())
        canvas.drawBitmap(bitmap, null, rectF, Paint())
    }

    // 繪製右下四分之一圓
    private fun drawRightBottomQuarter(
        canvas: Canvas,
        bitmap: Bitmap
    ) {
        val rectF =
            RectF(
                (WIDTH / 2).toFloat(),
                (HEIGHT / 2).toFloat(),
                (WIDTH).toFloat(),
                (HEIGHT).toFloat()
            )
        canvas.drawBitmap(bitmap, null, rectF, Paint())
    }

    // 繪製左下四分之一圓
    private fun drawLeftBottomQuarter(
        canvas: Canvas,
        bitmap: Bitmap
    ) {
        val rectF = RectF(0f, (HEIGHT / 2).toFloat(), (WIDTH / 2).toFloat(), (HEIGHT).toFloat())
        canvas.drawBitmap(bitmap, null, rectF, Paint())
    }

    // 取得文字頭圖
    private suspend fun getTextAvatar(
        context: Context,
        userName: String
    ): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                val shortName = getAvatarName(userName)
                val paint = getPaint(context, shortName)
                val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                // 先畫背景
                canvas.drawColor(getBackgroundColor(shortName))
                // 在畫中間文字
                paint.color = Color.WHITE
                val xPos = canvas.width / 2
                val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2)
                canvas.drawText(shortName, xPos.toFloat(), yPos, paint)
                return@withContext bitmap
            } catch (_: Exception) {
                return@withContext null
            }
        }

    private fun getPaint(
        context: Context,
        userName: String
    ): Paint {
        val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
        paintText.color = getBackgroundColor(userName)
        paintText.textSize = UiHelper.dp2px(context, 18f)
        paintText.textAlign = Paint.Align.CENTER
        return paintText
    }

    private fun getCeAvatarUrl(
        context: Context,
        avatarId: String
    ): String =
        TokenPref
            .getInstance(
                context
            ).currentTenantUrl + "/openapi/base/avatar/view?args=%7B%22id%22:%22" + avatarId + "%22,%20%22size%22:%22m%22%7D"

    private fun getBackgroundColor(input: String): Int = Color.parseColor(colors[toAscii(input) % colors.size])

    private fun toAscii(input: String): Int {
        if (input.isEmpty()) return 0
        var result = 0
        for (element in input) {
            result += element.code
        }
        return result
    }

    fun getAvatarName(input: String): String {
        return try {
            val split =
                input
                    .trim { it <= ' ' }
                    .uppercase(Locale.getDefault())
                    .split("[^\\u4e00-\\u9fa5a-zA-Z0-9]+".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            if (split.size > 1) {
                if (split[0].isNotEmpty() && split[1].isNotEmpty()) {
                    return split[0][0].toString() + split[1][0]
                } else {
                    for (s in split) {
                        if (s.length > 1) {
                            return s.substring(0, 2)
                        }
                    }
                }
            } else if (split[0].length > 1) {
                return if (split[0].contains("NULL")) {
                    "未知"
                } else {
                    split[0][0].toString() + split[0][1]
                }
            }
            split[0]
        } catch (e: java.lang.Exception) {
            CELog.e("input = $input")
            e.printStackTrace()
            ""
        }
    }

    fun toSecureHashName(originalName: String): String =
        try {
            // Use SHA-256 instead of MD5 (more secure)
            val digest = MessageDigest.getInstance("SHA-256")
            // Compute hash
            val hashedBytes = digest.digest(originalName.toByteArray(StandardCharsets.UTF_8))
            // Convert to hexadecimal
            hashedBytes.joinToString("") { "%02x".format(it) }
        } catch (e: NoSuchAlgorithmException) {
            // Fallback with more secure alternative
            originalName.hashCode().toString()
        }

    fun doHandleAiffImage(
        context: Context,
        url: String
    ): Deferred<Pair<String, Bitmap?>> =
        CoroutineScope(Dispatchers.IO).async {
            try {
                val bitmap =
                    Glide
                        .with(context)
                        .asBitmap()
                        .load(url)
                        .submit()
                        .get()
                val myDir = File("${context.filesDir.absolutePath}/temp/")
                if (!myDir.exists()) {
                    myDir.mkdirs()
                }
                val file = File(myDir, url.substring(url.lastIndexOf("/") + 1))
                if (file.exists()) file.delete()
                val out = FileOutputStream(file)
                val format =
                    if (url.substring(url.lastIndexOf(".") + 1) == "png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                bitmap.compress(format, 90, out)
                out.flush()
                out.close()
                Pair(file.absolutePath, bitmap)
            } catch (ignore: Exception) {
                Pair("", null)
            }
        }
}
