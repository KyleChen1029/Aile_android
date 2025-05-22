package tw.com.chainsea.chat.service.fcm

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.system.ThreadExecutorHelper
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.ce.ApiManager
import java.lang.ref.WeakReference

object FCMTokenManager {
    /**
     * get google FCM tokenId tasker & push tokenId to Ap Server
     * failure retry count = 10
     */
    private const val MAX_RETRY_COUNT = 10
    private var contextRef: WeakReference<Context>? = null

    @Volatile
    private var fcmTokenRun: Runnable? = null

    fun refreshFCMTokenIdToRemote(context: Context) {
        // 使用 ApplicationContext 避免 Activity Context 造成的洩漏
        val applicationContext = context.applicationContext
        contextRef = WeakReference(applicationContext)

        if (fcmTokenRun != null) {
            ThreadExecutorHelper.getHandlerExecutor().remove(fcmTokenRun)
        }

        fcmTokenRun =
            object : Runnable {
                private var isDone = false
                private var tryCount = 0

                override fun run() {
                    val context1 = contextRef?.get()
                    if (context1 == null) {
                        // Context 已被回收，停止執行
                        cleanup()
                        return
                    }

                    FirebaseMessaging
                        .getInstance()
                        .token
                        .addOnCompleteListener { task: Task<String?> ->
                            if (!task.isSuccessful) {
                                handleFailure(task.exception)
                            } else {
                                handleSuccess(context, task.result)
                            }
                            handleCompletion()
                        }
                }

                fun handleFailure(exception: Exception?) {
                    CELog.i("FCM TokenId: is not Successful")
                    CELog.e(exception!!.message, exception)
                    if (tryCount > MAX_RETRY_COUNT) {
                        isDone = true
                    }
                }

                fun handleSuccess(
                    context: Context?,
                    fcmTokenId: String?
                ) {
                    CELog.i(String.format("FCM TokenId: %s", fcmTokenId))
                    val osType = TokenPref.getInstance(context).osType
                    val deviceType = TokenPref.getInstance(context).deviceType
                    isDone = true
                    ApiManager.doUpdateFcmToken(context, deviceType, osType, fcmTokenId, null)
                }

                fun handleCompletion() {
                    if (isDone) {
                        cleanup()
                    } else {
                        tryCount++
                        CELog.i("FCM TokenId: is not Successful And reTryCount : $tryCount")
                        ThreadExecutorHelper.getHandlerExecutor().execute(fcmTokenRun, 1000L)
                    }
                }
            }

        ThreadExecutorHelper.getHandlerExecutor().execute(fcmTokenRun)
    }

    private fun cleanup() {
        if (fcmTokenRun != null) {
            ThreadExecutorHelper.getHandlerExecutor().remove(fcmTokenRun)
            fcmTokenRun = null
        }
        if (contextRef != null) {
            contextRef!!.clear()
            contextRef = null
        }
    }

    // 在適當的地方（如 Application 或 Service 被銷毀時）調用
    fun release() {
        cleanup()
    }
}
