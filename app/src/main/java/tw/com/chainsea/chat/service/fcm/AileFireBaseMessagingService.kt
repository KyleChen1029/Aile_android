package tw.com.chainsea.chat.service.fcm

import android.Manifest
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.database.sp.UserPref
import tw.com.chainsea.ce.sdk.http.ce.ApiManager
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.util.BadgeUtil
import tw.com.chainsea.chat.view.base.HomeActivity

class AileFireBaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        var message = ""
        var senderName = ""
        var title = ""
        var code = ""
        var type = ""
        var messageId = ""
        var roomId = ""
        var room = JSONObject()
        remoteMessage.data.let {
            Log.d("AileFireBaseMessagingService", "onMessageReceived data=${remoteMessage.data}")
            it.forEach { data ->
                when (data.key) {
                    "body" -> message = data.value
                    "title" -> title = data.value
                    "content" -> {
                        val value = JsonHelper.getInstance().toJsonObject(data.value)
                        val roomJson = value.optString("room")
                        code = value.optString("code")
                        messageId = value.optString("messageId")
                        type = value.optString("type")
                        senderName = value.optString("senderName")
                        if (roomJson.isNotEmpty()) {
                            room = JsonHelper.getInstance().toJsonObject(roomJson)
                            roomId = room.optString("roomId")
                        }
                        Log.d("AileFireBaseMessagingService", "onMessageReceived code = ${value.getString("code")}, message = ${value.optString("messageId")}, room_type = ${room.optString("type")}, room_id = ${room.optString("roomId")}")
                    }

                    else -> {}
                }
            }
            if (code == "Ce.Message.UnReceived" && !isAppInForeground()) {
                sendNotification(message, title, senderName, roomId, type, messageId)
                var badge = UserPref.getInstance(this).brand
                badge += 1
                UserPref.getInstance(this).brand = badge
                BadgeUtil().setCount(this, badge)
            }
        }
    }

    private fun sendNotification(
        message: String,
        title: String,
        senderName: String,
        roomId: String,
        type: String,
        messageId: String
    ) {
        val channelId = getString(R.string.app_name)

        val intent =
            Intent(this, HomeActivity::class.java)
                .apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
        intent.putExtras(
            bundleOf(
                BundleKey.EXTRA_SESSION_ID.key() to roomId,
                BundleKey.EXTRA_MESSAGE.key() to messageId,
                BundleKey.TYPE.key() to "FcmNotification"
            )
        )
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val channel =
            NotificationChannel(channelId, getString(R.string.text_notification_message), NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = type
            }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        val builder =
            NotificationCompat
                .Builder(this, channelId)
                .setSmallIcon(R.drawable.ce_notification_icon)
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        when (type) {
            "Image" -> {
                val bitmap =
                    try {
                        Glide
                            .with(baseContext)
                            .asBitmap()
                            .load(message)
                            .submit()
                            .get()
                    } catch (e: Exception) {
                        // 防止原圖片被刪除 顯示app icon
                        BitmapFactory.decodeResource(resources, R.drawable.icon_aile)
                    }
                builder.setContentText(getString(R.string.text_notification_format, senderName, ""))
                builder.setLargeIcon(bitmap)
            }

            else -> {
                builder.setContentText(getString(R.string.text_notification_format, senderName, message))
            }
        }

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@AileFireBaseMessagingService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        }
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    override fun onNewToken(token: String) {
        Log.d("AileFireBaseMessagingService", "onNewToken token $token")
        val osType = TokenPref.getInstance(application).osType
        val deviceType = TokenPref.getInstance(application).deviceType
        CoroutineScope(Dispatchers.IO).launch {
            ApiManager.doUpdateFcmToken(application, deviceType, osType, token, null)
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.d("AileFireBaseMessagingService", "onDeletedMessages")
    }

    override fun onMessageSent(msgId: String) {
        super.onMessageSent(msgId)
        Log.d("Kyle111", "onMessageSent msgId=$msgId")
    }

    override fun onSendError(
        msgId: String,
        exception: Exception
    ) {
        super.onSendError(msgId, exception)
        Log.d("Kyle111", "onSendError msgId=$msgId, exception=${exception.message}")
    }
}
