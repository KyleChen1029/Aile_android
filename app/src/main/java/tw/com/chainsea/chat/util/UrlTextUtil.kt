package tw.com.chainsea.chat.util

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.text.util.Linkify
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import tw.com.chainsea.chat.R
import java.util.regex.Pattern

class UrlTextUtil {
    fun getUrlSpannableString(
        tv: TextView,
        text: CharSequence
    ): SpannableStringBuilder {
        val spannable: Spannable = SpannableString(text)

        val urlPattern = (
            "((http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?+-=\\\\.&]*|" +
                "(www\\.|WWW\\.)+[\\w\\d:#@%/;$()~_?+-=\\\\.&]*)"
        )
        val p: Pattern = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE)
        Linkify.addLinks(spannable, p, "http://")

        val urlPattern2 = "((https):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?+-=\\\\.&]*)"
        val p2: Pattern = Pattern.compile(urlPattern2, Pattern.CASE_INSENSITIVE)
        Linkify.addLinks(spannable, p2, "https://")

        val end = text.length
        val urlSpans = spannable.getSpans(0, end, URLSpan::class.java)
        if (urlSpans.isEmpty()) {
            return SpannableStringBuilder(text)
        }

        val spannableStringBuilder = SpannableStringBuilder(text)
        Linkify.addLinks(spannableStringBuilder, Linkify.EMAIL_ADDRESSES)

        for (uri in urlSpans) {
            val url = uri.url
            if (url.indexOf("http://") == 0 || url.indexOf("https://") == 0) {
                val customUrlSpan = CustomUrlSpan(tv.context, url)
                spannableStringBuilder.setSpan(
                    customUrlSpan,
                    spannable.getSpanStart(uri),
                    spannable.getSpanEnd(uri),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
            }
        }
        return spannableStringBuilder
    }

//    fun getUrlSpannableString(
//        context: Context,
//        text: CharSequence
//    ): SpannableStringBuilder {
//        val spannable: Spannable = SpannableString(text)
//
//        val urlPattern = (
//            "((http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?+-=\\\\.&]*|" +
//                "(www\\.|WWW\\.)+[\\w\\d:#@%/;$()~_?+-=\\\\.&]*)"
//            )
//        val p: Pattern = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE)
//        Linkify.addLinks(spannable, p, "http://")
//
//        val urlPattern2 = "((https):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?+-=\\\\.&]*)"
//        val p2: Pattern = Pattern.compile(urlPattern2, Pattern.CASE_INSENSITIVE)
//        Linkify.addLinks(spannable, p2, "https://")
//
//        val end = text.length
//        val urlSpans = spannable.getSpans(0, end, URLSpan::class.java)
//        if (urlSpans.isEmpty()) {
//            return SpannableStringBuilder(text)
//        }
//
//        val spannableStringBuilder = SpannableStringBuilder(text)
//        Linkify.addLinks(spannableStringBuilder, Linkify.EMAIL_ADDRESSES)
//
//        for (uri in urlSpans) {
//            val url = uri.url
//            if (url.indexOf("http://") == 0 || url.indexOf("https://") == 0) {
//                val customUrlSpan = CustomUrlSpan(context, url)
//                spannableStringBuilder.setSpan(
//                    customUrlSpan,
//                    spannable.getSpanStart(uri),
//                    spannable.getSpanEnd(uri),
//                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
//                )
//            }
//        }
//        return spannableStringBuilder
//    }

    fun getUrlSpannableString(
        context: Context,
        text: CharSequence
    ): SpannableStringBuilder {
        val spannableStringBuilder = SpannableStringBuilder(text)

        // 處理 HTTP 和 HTTPS URL
        val urlPattern =
            Pattern.compile(
                "((https?|ftp):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?+-=\\\\.&]*|" +
                    "(www\\.|WWW\\.)[\\w\\d:#@%/;$()~_?+-=\\\\.&]*\\.[a-zA-Z]{2,})",
                Pattern.CASE_INSENSITIVE
            )

        // 為 URL 添加鏈接
        Linkify.addLinks(spannableStringBuilder, urlPattern, null)

        // 定義更嚴格的電子郵件模式，要求郵件地址前後有空格或在文本邊界
        val emailPattern =
            Pattern.compile(
                "\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b",
                Pattern.CASE_INSENSITIVE
            )
        Linkify.addLinks(spannableStringBuilder, emailPattern, "mailto:")

        // 獲取所有 URLSpan
        val end = text.length
        val urlSpans = spannableStringBuilder.getSpans(0, end, URLSpan::class.java)

        // 處理每個 URLSpan
        for (uri in urlSpans) {
            val url = uri.url
            val spanStart = spannableStringBuilder.getSpanStart(uri)
            val spanEnd = spannableStringBuilder.getSpanEnd(uri)

            // 檢查是否為有效的 URL 或電子郵件
            if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://") || url.startsWith("mailto:")) {
                // 確認郵件地址不是 "@xxx" 這樣的格式
                if (url.startsWith("mailto:@") || url == "mailto:") {
                    // 移除無效的電子郵件鏈接
                    spannableStringBuilder.removeSpan(uri)
                } else {
                    // 替換為自定義的 URLSpan
                    val customUrlSpan = CustomUrlSpan(context, url)
                    spannableStringBuilder.setSpan(
                        customUrlSpan,
                        spanStart,
                        spanEnd,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                    // 移除原始的 URLSpan
                    spannableStringBuilder.removeSpan(uri)
                }
            } else {
                // 移除不符合條件的 URLSpan
                spannableStringBuilder.removeSpan(uri)
            }
        }

        return spannableStringBuilder
    }

    fun isUrlFormat(content: String): Boolean {
        val urlPattern =
            Pattern.compile(
                "((https?|ftp):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?+-=\\\\.&]*" + // http://, https://, ftp://
                    "|(www\\.|WWW\\.)[\\w\\d:#@%/;$()~_?+-=\\\\.&]*\\.[a-zA-Z]{2,})" // www.example.com
            )
        val matcher = urlPattern.matcher(content)
        return matcher.find()
    }

    class CustomUrlSpan(
        private val context: Context,
        private val url: String
    ) : ClickableSpan() {
        override fun onClick(widget: View) {
            IntentUtil.launchUrl(context, url)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            // 設置超連結的文字顏色
            ds.color = ContextCompat.getColor(context, R.color.link_color)
        }
    }
}
