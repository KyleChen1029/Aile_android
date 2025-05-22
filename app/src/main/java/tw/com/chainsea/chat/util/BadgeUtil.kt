package tw.com.chainsea.chat.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import me.leolin.shortcutbadger.ShortcutBadger
import tw.com.chainsea.android.common.R
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.chat.refactor.welcomePage.WelcomeActivity
import kotlin.math.min

class BadgeUtil {
    companion object {
        private const val BADGE_CHANNEL_ID = "badge"
    }

    fun setCount(
        context: Context,
        count: Int
    ) {
        if (count <= 0) return

        // 使用 runCatching 處理可能的異常
        runCatching {
            createNotificationChannel(context)
            sendAppBadge(context, count)
            ShortcutBadger.applyCount(context, min(count, 999))
            CELog.i("計算未讀數： $count")
        }.onFailure { e ->
            CELog.e("設置未讀數失敗: ${e.message}")
        }
    }

    private fun createNotificationChannel(context: Context) {
        (
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as? NotificationManager
        )?.let { manager ->
            NotificationChannel(
                BADGE_CHANNEL_ID,
                context.getString(R.string.text_notification_badge),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(true)
                enableLights(false)
                enableVibration(false)
                vibrationPattern = longArrayOf(0)
                setSound(null, null)
                manager.createNotificationChannel(this)
            }
        }
    }

    private fun sendAppBadge(
        context: Context,
        count: Int
    ) {
        (
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as? NotificationManager
        )?.let { manager ->
            val notification =
                NotificationCompat
                    .Builder(context, BADGE_CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("您有${count}條未讀訊息")
                    .setContentIntent(getPendingIntent(context))
                    .setSmallIcon(R.drawable.ce_notification_icon)
                    .setChannelId(BADGE_CHANNEL_ID)
                    .setAutoCancel(true)
                    .setNumber(count)
                    .setSound(null)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .build()

            manager.notify(0, notification)
        }
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent =
            Intent(context, WelcomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
