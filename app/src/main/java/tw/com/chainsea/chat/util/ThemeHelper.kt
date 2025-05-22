package tw.com.chainsea.chat.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.chat.R
import java.util.regex.Pattern

object ThemeHelper {

    private var themeType = ThemeType.Default
    enum class ThemeType {
        Default, Green
    }
    private var _isServiceRoomTheme: Boolean = false
    val isServiceRoomTheme: Boolean
        get() = _isServiceRoomTheme

    fun updateServiceChatRoomTheme(isServiceRoom: Boolean) {
        _isServiceRoomTheme = isServiceRoom
    }
    fun setTheme(activity: Activity) {
        when (TokenPref.getInstance(activity).themeItemInfo) {
            ThemeType.Default.name -> {
                activity.setTheme(R.style.Theme_Aile_Base)
                themeType = ThemeType.Default
            }
            ThemeType.Green.name -> {
                activity.setTheme(R.style.Theme_Aile_Green)
                themeType = ThemeType.Green
            }
        }
    }

    fun matcherSearchAllSentence(text: String, keyword: String): SpannableString {
        val color = if(isGreenTheme()) Color.parseColor("#06B4A5") else -0xdd531b
        val s = SpannableString(text)
        val quote = Pattern.quote("" + keyword)
        val wordReg = "(?i)$quote" // Use (?i) to ignore case
        val p = Pattern.compile(wordReg)
        val m = p.matcher(s)
        while (m.find()) {
            val start = m.start()
            val end = m.end()
            s.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return s
    }

    fun isGreenTheme() = themeType == ThemeType.Green
}