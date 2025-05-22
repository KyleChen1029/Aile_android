package tw.com.chainsea.android.common.text;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class KeyWordHelper {
    public static Spanned matcherSearchTitle(String name, String keyword) {
        /*Spanned temp = null;
        if (name != null && name.contains(keyword)) {
            int index = name.indexOf(keyword);
            int len = keyword.length();
            temp = Html.fromHtml(name.substring(0, index)
                    + "<font color=#FF0000>"
                    + name.substring(index, index + len) + "</font>"
                    + name.substring(index + len, name.length()));
        }*/
        String content;
        if (name.length() > 20) {
            int index = name.indexOf(keyword);
            if (index > 10) {
                content = "..." + name.substring(index - 10, name.length());
            } else {
                content = name;
            }
        } else {
            content = name;
        }
        StringBuffer sb = new StringBuffer();
        String quote = Pattern.quote(keyword);
        String wordReg = "(?i)" + quote; // Use (?i) to ignore case
        Matcher matcher = Pattern.compile(wordReg).matcher(content);
        while (matcher.find()) {
            // This ensures that the case of the original text has not changed
            matcher.appendReplacement(sb, "<font color=#4A90E2>" + matcher.group() + "</font>");
        }
        matcher.appendTail(sb);
        Spanned temp = Html.fromHtml(String.valueOf(sb));
        content = sb.toString();
        return temp;
    }

    /**
     * Multiple keywords highlight and change color
     *
     * @param color   Changing color value
     * @param text    Text
     * @param keyword Keyword array in text
     * @return
     */
    public static SpannableString matcherSearchTitle(int color, String text, String keyword) {
        if (text.length() > 20) {
            int index = text.indexOf(keyword);
            if (index > 10) {
                text = "..." + text.substring(index - 3);
            }
        }
        SpannableString s = new SpannableString(text);
        String quote = Pattern.quote("" + keyword);
        String wordReg = "(?i)" + quote;  // Use (?i) to ignore case
        Pattern p = Pattern.compile(wordReg);
        Matcher m = p.matcher(s);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            s.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return s;
    }

//    public static SpannableString matcherSearchAllSentence(int color, String text, String keyword) {
//        SpannableString s = new SpannableString(text);
//        String quote = Pattern.quote("" + keyword);
//        String wordReg = "(?i)" + quote;  // Use (?i) to ignore case
//        Pattern p = Pattern.compile(wordReg);
//        Matcher m = p.matcher(s);
//        while (m.find()) {
//            int start = m.start();
//            int end = m.end();
//            s.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
//        return s;
//    }

    public static SpannableString highlightKeywords(int color, String text, String... keys) {
        SpannableString s = new SpannableString(text);
        for (String key : keys) {
            String quote = Pattern.quote("" + key);
            String wordReg = "(?i)" + quote;  // Use (?i) to ignore case
            Pattern p = Pattern.compile(wordReg);
            Matcher m = p.matcher(s);

            while (m.find()) {
                int start = m.start();
                int end = m.end();
                s.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            s.setSpan(new ForegroundColorSpan(Color.WHITE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return s;
    }

    public static SpannableString matcherSearchBackground(int color, String text, String keyword) {
        if (text == null) return new SpannableString("");
        if (keyword == null) return new SpannableString(text);

        if (text.length() > 20) {
            int index = text.indexOf(keyword);
            if (index > 10) {
                text = "..." + text.substring(index - 3, text.length());
            }
        }
        SpannableString s = new SpannableString(text);
        String quote = Pattern.quote("" + keyword);
        String wordReg = "(?i)" + quote;  // Use (?i) to ignore case
        Pattern p = Pattern.compile(wordReg);
        Matcher m = p.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();
            s.setSpan(new BackgroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            s.setSpan(new ForegroundColorSpan(Color.WHITE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return s;
    }

    public static SpannableString matcherSearchKeyWordForURL(int color, String text, String keyword) {
        if (text == null) return new SpannableString("");
        if (keyword == null) return new SpannableString(text);

        SpannableString s = new SpannableString(text);
        String quote = Pattern.quote("" + keyword);
        String wordReg = "(?i)" + quote;  // Use (?i) to ignore case
        Pattern p = Pattern.compile(wordReg);
        Matcher m = p.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();
            s.setSpan(new BackgroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return s;
    }

    public static Spannable matcherKey(int color, String text) {
        Spannable s = new SpannableString(text);
        s.setSpan(new ForegroundColorSpan(color), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }

    public static Spannable matcherKeys(int color, String text, String keyword, View.OnClickListener listener) {
        SpannableString s = new SpannableString(text);
        String quote = Pattern.quote("" + keyword);
        String wordReg = "(?i)" + quote;  // Use (?i) to ignore case
        Pattern p = Pattern.compile(wordReg);
        Matcher m = p.matcher(s);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            String group = m.group();
            s.setSpan(new SpanClick(group, color, listener), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return s;
    }

    public static class SpanClick extends ClickableSpan {

        private String text;
        private int color;
        private View.OnClickListener listener;

        public SpanClick(String t, int color, View.OnClickListener listener) {
            this.text = t;
            this.color = color;
            this.listener = listener;
        }

        @Override
        public void onClick(@NonNull View widget) {
            if (this.listener != null) {
                listener.onClick(widget);
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(this.color);
        }
    }

}
