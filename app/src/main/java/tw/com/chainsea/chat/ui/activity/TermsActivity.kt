@file:Suppress("ktlint:standard:no-wildcard-imports")

package tw.com.chainsea.chat.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ActivityTermsBinding
import tw.com.chainsea.custom.view.progress.IosProgressBar

class TermsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTermsBinding
    private lateinit var iosProgressBar: IosProgressBar

    companion object {
        const val TERMS_URL = "https://www.aile.cloud/law/ecosystem.html"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsBinding.inflate(layoutInflater)
        iosProgressBar =
            IosProgressBar.show(
                this@TermsActivity,
                getString(R.string.wording_loading),
                true,
                false
            ) { }

        setContentView(binding.root)

        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            webViewClient = TermsWebViewClient(iosProgressBar)
            setOnScrollChangeListener { _, _, scrollY, _, _ ->
                val contentHeight = this.contentHeight * this.scale
                val viewHeight = this.height.toFloat()
                if (scrollY >= (contentHeight - viewHeight).toInt() - 100) {
                    binding.btnAgree.apply {
                        background = AppCompatResources.getDrawable(this@TermsActivity, R.drawable.bg_terms_btn_radius_8_00a0e9_enable)
                        isEnabled = true
                    }
                }
            }
            loadUrl(TERMS_URL)
        }

        binding.scopeClose.setOnClickListener {
            finish()
        }
        binding.btnAgree.setOnClickListener {
            // UserPref.getInstance(this).setUserTermsAgreement()
            val intent = Intent()
            intent.putExtra("isAgree", true)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}

class TermsWebViewClient internal constructor(
    private var iosProgressBar: IosProgressBar
) : WebViewClient() {
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
