package tw.com.chainsea.chat.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ActivityEcologyBinding
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.custom.view.progress.IosProgressBar

class EcologyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEcologyBinding
    private lateinit var iosProgressBar: IosProgressBar

    companion object {
        const val LINE_APP = "jp.naver.line.android"
        const val PLAY_STORE = "https://play.google.com/store/apps/details"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEcologyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bindUrl = intent.getStringExtra(BundleKey.BIND_URL.key())
        bindUrl?.let { url ->
            iosProgressBar =
                IosProgressBar.show(
                    this@EcologyActivity,
                    getString(R.string.wording_loading),
                    true,
                    false
                ) { }
            binding.webView.apply {
                requestFocus()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                webViewClient =
                    EcologyWebViewClient(
                        this@EcologyActivity,
                        iosProgressBar
                    )
                loadUrl(url)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}

class EcologyWebViewClient internal constructor(
    private val context: EcologyActivity,
    private var iosProgressBar: IosProgressBar
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        var url = request?.url.toString()
        return if (url.startsWith("http") || url.startsWith("https")) {
            view?.loadUrl(url)
            true
        } else {
            try {
                if (url.startsWith("intent")) {
                    url = url.replace("intent", "https")
                }
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    startActivity(
                        context,
                        this,
                        null
                    )
                    context.finish()
                }
                true
            } catch (e: Exception) {
                ToastUtils.showToast(context, "not installed Line")
                context.finish()
                false
            }
        }
    }

    override fun onPageFinished(
        view: WebView?,
        url: String?
    ) {
        super.onPageFinished(view, url)
        if (iosProgressBar.isShowing) {
            iosProgressBar.dismiss()
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (iosProgressBar.isShowing) {
            iosProgressBar.dismiss()
        }
    }
}
