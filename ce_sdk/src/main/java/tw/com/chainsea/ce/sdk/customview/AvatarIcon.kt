package tw.com.chainsea.ce.sdk.customview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.ui.UiHelper
import tw.com.chainsea.ce.sdk.R
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant
import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomMemberResponse
import tw.com.chainsea.ce.sdk.socket.cp.CpSocket
import tw.com.chainsea.ce.sdk.util.BitmapUtil
import tw.com.chainsea.ce.sdk.util.BitmapUtil.toSecureHashName
import tw.com.chainsea.custom.view.image.CircleImageView
import java.io.File
import java.util.Locale
import java.util.concurrent.CompletableFuture

/**
 * 用戶大頭貼
 * 原本是用 TextView 和 ImageView 兩個元件判斷說要顯示哪個
 * 現整合在一起，對於控制和顯示上會比較統一
 * */
class AvatarIcon : CircleImageView {
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

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
    private val avatarWidth = 118
    private val avatarHeight = 118

    // cp 團隊 Options
    private val tenantOptions =
        RequestOptions()
            .placeholder(R.drawable.invalid_name)
            .error(R.drawable.invalid_name)
            .fitCenter()

    fun loadTenantAvatarIcon(currentTenant: RelationTenant) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                currentTenant.avatarId?.let {
                    val hashName = toSecureHashName(currentTenant.tenantId + it)
                    val tenantAvatarPic = "${context.applicationContext.filesDir.absolutePath}/avatars/$hashName.jpg"
                    val pic = File(tenantAvatarPic)
                    if (pic.exists()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Glide
                                .with(this@AvatarIcon)
                                .load(pic)
                                .apply(tenantOptions)
                                .into(this@AvatarIcon)
                        }
                        Log.d("Kyle111", "load tenant avatar ok, name=${pic.name}")
                        return@launch
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            val bitmap = getCpAvatarUrl(it).await()
                            bitmap?.let { b ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    Glide
                                        .with(this@AvatarIcon)
                                        .load(b)
                                        .apply(tenantOptions)
                                        .into(this@AvatarIcon)
                                }
                                BitmapUtil.saveAvatarFromBitmap(context.applicationContext, b, hashName)
                            } ?: run {
                                CoroutineScope(Dispatchers.Main).launch {
                                    setImageResource(R.drawable.invalid_name)
                                }
                            }
                        }
                    }
                } ?: run {
                    CoroutineScope(Dispatchers.Main).launch {
                        setImageResource(R.drawable.invalid_name)
                    }
                }
            } catch (ignored: Exception) {
            }
        }

    private fun getCpAvatarUrl(avatarId: String): Deferred<Bitmap?> =
        CoroutineScope(Dispatchers.IO).async {
            val avatarUrl = CpSocket.BASE_URL + "/openapi/base/avatar/view?args=%7B%22id%22:%22" + avatarId + "%22,%20%22size%22:%22s%22%7D"
            try {
                Glide
                    .with(context.applicationContext)
                    .asBitmap()
                    .load(avatarUrl)
                    .error(R.drawable.invalid_name)
                    .submit()
                    .get()
            } catch (e: Exception) {
                null
            }
        }

    /**
     * 顯示大頭貼
     *
     * @param avatarId avatarId
     * @param userName userName
     * @param uniqueId uniqueId
     * @param width width
     * @param height height
     * */
    fun loadAvatarIcon(
        avatarId: String? = "",
        userName: String = "",
        uniqueId: String?
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            if (avatarId.isNullOrEmpty()) {
                setTextAvatar(userName, uniqueId)
            } else {
                if (uniqueId.isNullOrEmpty()) return@launch
                val hashName = toSecureHashName(avatarId + uniqueId)
                val avatarPic = "${context.applicationContext.filesDir.absolutePath}/avatars/$hashName.jpg"
                val pic = File(avatarPic)
                if (pic.exists()) {
                    withContext(Dispatchers.Main) {
                        Picasso
                            .get()
                            .load(pic)
                            .error(R.drawable.custom_default_avatar)
                            .into(this@AvatarIcon)
                    }
                    Log.d("Kyle111", "load avatar ok, name=${pic.name}")
                } else {
                    val userAvatar = getBitmapFromUrl(avatarId).await()
                    userAvatar?.let {
                        withContext(Dispatchers.Main) {
                            Glide
                                .with(context.applicationContext)
                                .load(it)
                                .error(R.drawable.custom_default_avatar)
                                .into(this@AvatarIcon)
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            BitmapUtil.saveAvatarFromBitmap(context.applicationContext, it, toSecureHashName(avatarId + uniqueId))
                        }
                    } ?: run {
                        setTextAvatar(userName, uniqueId)
                    }
                }
            }
        } catch (ignored: Exception) {
        }
    }

    private fun setTextAvatar(
        userName: String,
        uniqueId: String?
    ) = CoroutineScope(Dispatchers.Main).launch {
        try {
            if (uniqueId.isNullOrEmpty()) return@launch
            val hashName = toSecureHashName(uniqueId + userName)
            val avatarPic = "${context.applicationContext.filesDir.absolutePath}/avatars/$hashName.jpg"
            val file = File(avatarPic)
            if (file.exists()) {
                Picasso
                    .get()
                    .load(file)
                    .error(R.drawable.custom_default_avatar)
                    .into(this@AvatarIcon)
                Log.d("Kyle111", "load text avatar ok, name=${file.name}")
            } else {
                val textBitmap = getTextAvatar(userName).await()
                textBitmap?.let {
                    Glide
                        .with(context.applicationContext)
                        .load(it)
                        .error(R.drawable.custom_default_avatar)
                        .into(this@AvatarIcon)
                    CoroutineScope(Dispatchers.IO).launch {
                        BitmapUtil.saveAvatarFromBitmap(context.applicationContext, it, hashName)
                    }
                } ?: run { Picasso.get().load(R.drawable.custom_default_avatar).into(this@AvatarIcon) }
            }
            // 查看文字頭圖是否有 cache
//        BitmapCacheUtil.getCache(userName)?.let {
//            post {
//                this@AvatarIcon.setImageBitmap(BitmapCacheUtil.getCache(userName))
//            }
//        } ?: run {
//            getTextAvatar(userName)?.let {
//                BitmapCacheUtil.putCache(userName, it)
//
//                post {
//                    this@AvatarIcon.setImageBitmap(it)
//                    //this@AvatarIcon.setImageURI()
//                }
//            }
//        }
        } catch (ignored: Exception) {
        }
    }

    // 從 Url 獲取圖片
    private fun getBitmapFromUrl(avatarId: String): Deferred<Bitmap?> =
        CoroutineScope(Dispatchers.IO).async {
            val avatarUrl = if (avatarId.startsWith("http")) avatarId else getCeAvatarUrl(avatarId)
            try {
                Glide
                    .with(context.applicationContext)
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
        chatRoomMember: List<ChatRoomMemberResponse>?,
        roomId: String? = null
    ) = CoroutineScope(Dispatchers.IO).launch {
        loadMultiAvatarIcon(chatRoomMember?.take(4)?.map { it.memberId } ?: listOf(), roomId)
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

    // 多人聊天室頭圖
    fun loadMultiAvatarIcon(
        chatRoomMemberIds: List<String>?,
        roomId: String? = null
    ) = CoroutineScope(Dispatchers.Main).launch {
        if (roomId.isNullOrEmpty()) {
            // 針對建立多人聊天室使用，不儲存
            createNewMultiAvatarIcon(chatRoomMemberIds, null)
        } else {
            // 已建立的多人聊天室組合頭圖
            var avatarIconName = roomId
            chatRoomMemberIds?.forEach { id ->
                val userProfile = DBManager.getInstance().queryUser(id) ?: return@forEach
                var avatarId = ""
                userProfile.avatarId?.let {
                    avatarId = it
                }
                avatarIconName += (userProfile.nickName + avatarId)
            }
            val hashName = toSecureHashName(avatarIconName)
            val avatarPic = File("${context.applicationContext.filesDir.absolutePath}/avatars/$hashName.jpg")
            if (avatarPic.exists()) {
                CoroutineScope(Dispatchers.Main).launch {
                    Picasso
                        .get()
                        .load(avatarPic)
                        .error(R.drawable.custom_default_avatar)
                        .into(this@AvatarIcon)
                }
                Log.d("Kyle111", "load multi avatar done, name=$hashName")
            } else {
                createNewMultiAvatarIcon(chatRoomMemberIds, roomId, hashName, isSaved = true)
            }
        }
    }

    /**
     * 組合多人聊天室頭圖icon名稱 roomId + 每個成員nickName + 每個成員avatarId
     * 若avatarId為空，則 ""
     * 用來判定當某個成員名稱或 avatarId 有改變，則組合新頭圖
     */
    private fun createNewMultiAvatarIcon(
        chatRoomMemberIds: List<String>?,
        roomId: String?,
        fileName: String? = null,
        isSaved: Boolean = false
    ) = CoroutineScope(Dispatchers.IO).launch {
        val copyChatRoomMemberIds = chatRoomMemberIds?.toMutableList()
        // val avatarIds = getChatRoomMemberAvatarIds(chatRoomMemberIds)
        post {
            CoroutineScope(Dispatchers.IO).launch {
                copyChatRoomMemberIds?.let {
                    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(output)
                    // 依照人數判斷要畫哪種的大頭貼
                    when (it.size) {
                        1 -> {
                            drawOneAvatar(it, roomId, fileName, isSaved)
                        }

                        2 -> {
                            drawTwoAvatar(canvas, output, it, roomId, fileName, isSaved)
                        }

                        3 -> {
                            drawThreeAvatar(canvas, output, it, roomId, fileName, isSaved)
                        }

                        4 -> {
                            drawFourAvatar(canvas, output, it, roomId, fileName, isSaved)
                        }

                        else -> {
                            drawFourAvatar(canvas, output, it, roomId, fileName, isSaved)
                        }
                    }
                }
            }
        }
    }

    private fun drawOneAvatar(
        chatRoomMemberIds: List<String>,
        roomId: String?,
        fileName: String? = null,
        isSaved: Boolean
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val userProfile = DBManager.getInstance().queryUser(chatRoomMemberIds[0])
            // 文字頭圖
            val textIcon = getTextAvatar(userProfile.nickName).await()
            // 使用者上傳頭圖
            var bitmapIcon: Bitmap? = null
            userProfile.avatarId?.let {
                bitmapIcon = getBitmapFromUrl(it).await()
            }
            // 判斷是要用哪種頭圖
            val targetBitmap = bitmapIcon ?: textIcon
//            BitmapCacheUtil.putCache(avatarIds.hashCode().toString(), targetBitmap)
            withContext(Dispatchers.Main) {
                Glide
                    .with(context.applicationContext)
                    .load(targetBitmap)
                    .error(R.drawable.custom_default_avatar)
                    .into(this@AvatarIcon)
            }
            if (isSaved && !roomId.isNullOrEmpty()) {
                targetBitmap?.let {
                    BitmapUtil.saveAvatarFromBitmap(context.applicationContext, it, fileName ?: "")
                }
                Log.d("Kyle111", "drawOneAvatar save multi avatar done, name=$fileName")
            }
        } catch (ignored: Exception) {
        }
    }

    // 兩個頭貼拼接
    private fun drawTwoAvatar(
        canvas: Canvas,
        output: Bitmap,
        chatRoomMemberIds: List<String>,
        roomId: String?,
        fileName: String? = null,
        isSaved: Boolean
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                chatRoomMemberIds.forEachIndexed { index, id ->
                    val userProfile = DBManager.getInstance().queryUser(id)
                    // 文字頭圖
                    val textIcon = getTextAvatar(userProfile.nickName).await()
                    // 使用者上傳頭圖
                    var bitmapIcon: Bitmap? = null
                    userProfile.avatarId?.let {
                        bitmapIcon = getBitmapFromUrl(it).await()
                    }
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
//            BitmapCacheUtil.putCache(avatarIds.hashCode().toString(), output)
                withContext(Dispatchers.Main) {
                    Glide
                        .with(context.applicationContext)
                        .load(output)
                        .error(R.drawable.custom_default_avatar)
                        .into(this@AvatarIcon)
                }
                if (isSaved && !roomId.isNullOrEmpty()) {
                    Log.d("Kyle111", "drawTwoAvatar save multi avatar done, name=$fileName")
                    BitmapUtil.saveAvatarFromBitmap(
                        context.applicationContext,
                        output,
                        fileName ?: ""
                    )
                }
            } catch (ignored: Exception) {
            }
        }
    }

    // 三個頭圖拼接
    private fun drawThreeAvatar(
        canvas: Canvas,
        output: Bitmap,
        chatRoomMemberIds: List<String>,
        roomId: String?,
        fileName: String? = null,
        isSaved: Boolean
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                chatRoomMemberIds.forEachIndexed { index, id ->
                    val userProfile = DBManager.getInstance().queryUser(id)
                    userProfile?.let {
                        // 文字頭圖
                        val textIcon = getTextAvatar(it.nickName).await()
                        // 使用者上傳頭圖
                        var bitmapIcon: Bitmap? = null
                        userProfile.avatarId?.let { avatar ->
                            bitmapIcon = getBitmapFromUrl(avatar).await()
                        }
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
//            BitmapCacheUtil.putCache(avatarIds.hashCode().toString(), output)
                withContext(Dispatchers.Main) {
                    Glide
                        .with(context.applicationContext)
                        .load(output)
                        .error(R.drawable.custom_default_avatar)
                        .into(this@AvatarIcon)
                }
                if (isSaved && !roomId.isNullOrEmpty()) {
                    Log.d("Kyle111", "drawThreeAvatar save multi avatar done, name=$fileName")
                    BitmapUtil.saveAvatarFromBitmap(context.applicationContext, output, fileName ?: "")
                }
            } catch (ignored: Exception) {
            }
        }
    }

    // 四個或以上的圖頭拼接
    private fun drawFourAvatar(
        canvas: Canvas,
        output: Bitmap,
        chatRoomMemberIds: List<String>,
        roomId: String?,
        fileName: String? = null,
        isSaved: Boolean
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                chatRoomMemberIds.forEachIndexed { index, id ->
                    val userProfile = DBManager.getInstance().queryUser(id) ?: return@forEachIndexed
                    // 文字頭圖
                    val textIcon = getTextAvatar(userProfile.nickName).await()
                    // 使用者上傳頭圖
                    var bitmapIcon: Bitmap? = null
                    userProfile.avatarId?.let {
                        bitmapIcon = getBitmapFromUrl(it).await()
                    }
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
//            BitmapCacheUtil.putCache(avatarIds.hashCode().toString(), output)
                withContext(Dispatchers.Main) {
                    Glide
                        .with(context.applicationContext)
                        .load(output)
                        .error(R.drawable.custom_default_avatar)
                        .into(this@AvatarIcon)
                }
                if (isSaved && !roomId.isNullOrEmpty()) {
                    Log.d("Kyle111", "drawFourAvatar save multi avatar done, name=$fileName")
                    BitmapUtil.saveAvatarFromBitmap(context.applicationContext, output, fileName ?: "")
                }
            } catch (ignored: Exception) {
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
        val rectF = RectF(0f, 0f, (width / 2).toFloat(), (height).toFloat())
        canvas.drawBitmap(corpBitmap, null, rectF, Paint())
    }

    // 繪製右半圓
    private fun drawRightHalfCircle(
        canvas: Canvas,
        bitmap: Bitmap
    ) {
        val corpBitmap =
            Bitmap.createBitmap(bitmap, bitmap.width / 4, 0, bitmap.width / 2, bitmap.height)
        val rectF = RectF((width / 2).toFloat(), 0f, (width).toFloat(), (height).toFloat())
        canvas.drawBitmap(corpBitmap, null, rectF, Paint())
    }

    // 繪製左上四分之一圓
    private fun drawLeftTopQuarter(
        canvas: Canvas,
        bitmap: Bitmap
    ) {
        val rectF = RectF(0f, 0f, (width / 2).toFloat(), (height / 2).toFloat())
        canvas.drawBitmap(bitmap, null, rectF, Paint())
    }

    // 繪製右上四分之一圓
    private fun drawRightTopQuarter(
        canvas: Canvas,
        bitmap: Bitmap
    ) {
        val rectF = RectF((width / 2).toFloat(), 0f, (width).toFloat(), (height / 2).toFloat())
        canvas.drawBitmap(bitmap, null, rectF, Paint())
    }

    // 繪製右下四分之一圓
    private fun drawRightBottomQuarter(
        canvas: Canvas,
        bitmap: Bitmap
    ) {
        val rectF =
            RectF(
                (width / 2).toFloat(),
                (height / 2).toFloat(),
                (width).toFloat(),
                (height).toFloat()
            )
        canvas.drawBitmap(bitmap, null, rectF, Paint())
    }

    // 繪製左下四分之一圓
    private fun drawLeftBottomQuarter(
        canvas: Canvas,
        bitmap: Bitmap
    ) {
        val rectF = RectF(0f, (height / 2).toFloat(), (width / 2).toFloat(), (height).toFloat())
        canvas.drawBitmap(bitmap, null, rectF, Paint())
    }

    // 取得文字頭圖
    private fun getTextAvatar(userName: String): Deferred<Bitmap?> =
        CoroutineScope(Dispatchers.IO).async {
            try {
                val shortName = getAvatarName(userName)
                val paint = getPaint(shortName)
                val bitmap = Bitmap.createBitmap(avatarWidth, avatarHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                // 先畫背景
                canvas.drawColor(getBackgroundColor(shortName))
                // 在畫中間文字
                paint.color = Color.WHITE
                val xPos = canvas.width / 2
                val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2)
                canvas.drawText(shortName, xPos.toFloat(), yPos, paint)
                return@async bitmap
            } catch (e: Exception) {
                Log.e("Kyle111", "getTextAvatar load text avatar failure ${e.message}")
                return@async null
            }
        }

    fun getTextAvatarFromJava(userName: String): CompletableFuture<Bitmap?> = GlobalScope.future { getTextAvatar(userName).await() }

    private fun getPaint(userName: String): Paint {
        val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
        paintText.color = getBackgroundColor(userName)
        paintText.textSize = UiHelper.dp2px(context, 18f)
        paintText.textAlign = Paint.Align.CENTER
        return paintText
    }

    private fun getCeAvatarUrl(avatarId: String): String =
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
}
