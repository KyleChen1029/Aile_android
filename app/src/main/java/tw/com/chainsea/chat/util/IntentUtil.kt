package tw.com.chainsea.chat.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import tw.com.chainsea.chat.config.BundleKey

object IntentUtil {
    private var clickTime = 0L
    private var isActivityStarted = false

    fun startIntent(
        context: Context,
        clazz: Class<*>,
        bundle: Bundle? = null
    ) {
        val intent = Intent(context, clazz)
        bundle?.let {
            intent.putExtras(it)
        }
        start(context, intent)
    }

    fun launchIntent(
        context: Context,
        clazz: Class<*>,
        launcher: ActivityResultLauncher<Intent>?,
        bundle: Bundle? = null
    ) {
        val intent = Intent(context, clazz)
        bundle?.let {
            intent.putExtras(it)
        }
        launcher?.launch(intent)
    }

    fun launchUrl(
        context: Context,
        url: String
    ) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = url.toUri()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        start(context, intent)
    }

    fun shareToLine(
        context: Context,
        url: String
    ) {
        val intent = Intent()
        intent.setAction(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(url))
        context.startActivity(intent)
    }

    fun shareText(
        context: Context,
        message: String,
        isFromFilterPage: Boolean = false
    ) {
        val updateText: String = message.replace("\\n", "\\\n").replace("\\", "")
        val shareInvitationCodeIntent = Intent()
        shareInvitationCodeIntent.setAction(Intent.ACTION_SEND)
        shareInvitationCodeIntent.setType("text/plain")
        shareInvitationCodeIntent.putExtra(Intent.EXTRA_TEXT, updateText)
        shareInvitationCodeIntent.putExtra(BundleKey.IS_FROM_FILTER.key(), isFromFilterPage)
        context.startActivity(Intent.createChooser(shareInvitationCodeIntent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun shareToSms(
        context: Context,
        number: String,
        content: String
    ) {
        val uri = Uri.parse("smsto:$number")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra("sms_body", content)
        context.startActivity(intent)
    }

    fun shareToMail(
        context: Context,
        address: String,
        title: String,
        content: String
    ) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.setData(Uri.parse("mailto:")) // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
        intent.putExtra(Intent.EXTRA_SUBJECT, title)
        intent.putExtra(Intent.EXTRA_TEXT, content)
        context.startActivity(intent)
    }

    fun start(
        context: Context,
        intent: Intent
    ) {
        if (!isActivityStarted) {
            isActivityStarted = true
            context.startActivity(intent)

            Handler(Looper.getMainLooper()).postDelayed({
                isActivityStarted = false
            }, 3000)
        }
    }

    fun resetClickTime() {
        isActivityStarted = false
    }
}
