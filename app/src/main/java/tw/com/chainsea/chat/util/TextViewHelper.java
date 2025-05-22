package tw.com.chainsea.chat.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomMemberResponse;


/**
 * current by evan on 2020/5/13
 *
 * @author Evan Wang
 * @date 2020/5/13
 */
public class TextViewHelper {
    /**
     *
     */
    public static SpannableString setLeftImage(Context context, String text, @DrawableRes int id) {
        SpannableString spannableString = new SpannableString(
            (id != 0 && id != -1 ? "  " : "") + text);
        Drawable rightDrawable = null;
        if (id != 0 && id != -1) {
            rightDrawable = AppCompatResources.getDrawable(context, id);
            rightDrawable.setBounds(0, 0, rightDrawable.getIntrinsicWidth(),
                rightDrawable.getIntrinsicHeight());
        }

        spannableString.setSpan(new TextImageSpan(rightDrawable), 0, 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public static SpannableString getTitleWithIcon(Context context, String title, int count, @DrawableRes int drawableId) {
        String newTitle = getTitle(title, count, false);
        return setLeftImage(context, newTitle, drawableId);
    }


    public static SpannableString setLeftAndRightImage(Context context, String text,
                                                       @DrawableRes int leftId, @DrawableRes int rightId) {
        SpannableString spannableString = new SpannableString(
            (leftId != 0 && leftId != -1 ? "  " : "") + text);
        Drawable rightDrawable = null;
        if (leftId != 0 && leftId != -1) {
            rightDrawable = AppCompatResources.getDrawable(context, leftId);
            rightDrawable.setBounds(0, 0, rightDrawable.getIntrinsicWidth(),
                rightDrawable.getIntrinsicHeight());
        }

        spannableString.setSpan(new TextImageSpan(rightDrawable), 0, 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }


    public static SpannableStringBuilder setLeftImage(Context context,
                                                      SpannableStringBuilder builder, @DrawableRes int id) {
//        SpannableString spannableString = new SpannableString((id != 0 && id != -1 ? "  " : "") + builder);
        Drawable rightDrawable = null;
        if (id != 0 && id != -1) {
            builder.insert(0, "  ");
            rightDrawable = AppCompatResources.getDrawable(context, id);
            rightDrawable.setBounds(0, 0, rightDrawable.getIntrinsicWidth(),
                rightDrawable.getIntrinsicHeight());
        }

        builder.setSpan(new TextImageSpan(rightDrawable), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    public static SpannableStringBuilder setLeftImage(Context context, CharSequence charSequence,
                                                      @DrawableRes int id) {
        SpannableStringBuilder builder = new SpannableStringBuilder(charSequence);
        return setLeftImage(context, builder, id);
    }


    public static SpannableString setLeftImageAndHighLight(Context context, String text,
                                                           @DrawableRes int id, String keyword, @ColorInt int color) {
        if (text.length() > 20) {
            int index = text.indexOf(keyword);
            if (index > 10) {
                text = "..." + text.substring(index - 3, text.length());
            }
        }

        SpannableString spannableString = new SpannableString(
            (id != 0 && id != -1 ? "  " : "") + text);
        Drawable rightDrawable = null;
        if (id != 0 && id != -1) {
            rightDrawable = AppCompatResources.getDrawable(context, id);
            rightDrawable.setBounds(0, 0, rightDrawable.getIntrinsicWidth(),
                rightDrawable.getIntrinsicHeight());
        }

        spannableString.setSpan(new TextImageSpan(rightDrawable), 0, 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        String quote = Pattern.quote(keyword);
        String wordReg = "(?i)" + quote;  //用(?i)来忽略大小写
        Pattern p = Pattern.compile(wordReg);
        Matcher m = p.matcher(spannableString);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            spannableString.setSpan(new ForegroundColorSpan(color), start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }


//    public static SpannableStringBuilder setLeftImage(Context context, SpannableString builder, @DrawableRes int id) {
////        SpannableString spannableString = new SpannableString((id != 0 && id != -1 ? "  " : "") + builder);
//        Drawable rightDrawable = null;
//        if (id != 0 && id != -1) {
//            builder.insert(0,  "  ");
//            rightDrawable = context.getDrawable(id);
//            rightDrawable.setBounds(0, 0, rightDrawable.getIntrinsicWidth(), rightDrawable.getIntrinsicHeight());
//        }
//
//        builder.setSpan(new TextImageSpan(rightDrawable), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        return builder;
//    }


    public static class TextImageSpan extends ImageSpan {
        public TextImageSpan(Context context, Bitmap bitmap) {
            super(context, bitmap);
        }

        public TextImageSpan(Context context, Bitmap bitmap, int verticalAlignment) {
            super(context, bitmap, verticalAlignment);
        }

        public TextImageSpan(Drawable drawable) {
            super(drawable);
        }
    }

    private static String getTitle(String title, int memberSize, boolean fromList) {
        String appendedText = " (" + memberSize + ")"; // 要添加的文本
        String resultText = "";
        int maxLength = fromList ? 23 : 16;
        // 如果原始文本的長度大於最大長度，則進行ellipsize處理
        if (getTextCount(title) > maxLength) {
            // 取得省略後的文本
            String ellipsizedText = substringByLength(title, maxLength);

            // 創建一個SpannableString對象並設定省略後的文本
            SpannableString spannableString = new SpannableString(ellipsizedText);

            resultText = TextUtils.concat(spannableString, "...", appendedText).toString();
        } else {
            resultText = title + appendedText;
        }
        return resultText;
    }

    public static void setGroupRoomTitle(TextView textView, String title, int memberSize,
                                         boolean fromList) {
        String groupTitle = getTitle(title, memberSize, fromList);
        textView.setText(setLeftImage(textView.getContext(), groupTitle,
            tw.com.chainsea.android.common.R.drawable.icon_group_chat_room));
    }

    public static void setDiscussTitle(TextView textView, String title, int memberSize,
                                       boolean fromList) {
        String discussTitle = getTitle(title, memberSize, fromList);
        textView.setText(discussTitle);
    }

    /**
     * 設置多人聊天室的 title
     * 用於聊天室列表
     */
    public static void setDiscussTitle(TextView textView, List<ChatRoomMemberResponse> memberList) {
        setDiscussTitle(textView, getCombineDiscussTitle(memberList), memberList.size(), false);
    }

    /**
     * 設置多人聊天室的 title
     * 用於聊天室內
     */
    public static String getDiscussTitle(List<ChatRoomMemberResponse> memberList) {
        if (memberList == null) return "N/A";
        return getTitle(getCombineDiscussTitle(memberList), memberList.size(), false);
    }

    /**
     * 取得多人聊天室組合 title
     *
     * @param memberList 聊天室的 chat member
     */
    private static String getCombineDiscussTitle(List<ChatRoomMemberResponse> memberList) {
        if (memberList != null) {
            StringBuilder title = new StringBuilder();
            List<ChatRoomMemberResponse> subList;
            if (memberList.size() >= 4) {
                subList = memberList.subList(0, 4);
            } else {
                subList = memberList;
            }

            for (int i = 0; i < subList.size(); i++) {
                UserProfileEntity userProfile = DBManager.getInstance().queryFriend(subList.get(i).getMemberId());
                if (userProfile != null) {
                    title.append(userProfile.getNickName());
                }

                if (i < subList.size() - 1) {
                    title.append(",");
                } else {
                    break;
                }
            }
            return title.toString();
        } else {
            return "N/A";
        }
    }

    //取得中文、英文、數字實際字數
    public static int getTextCount(String input) {
        Pattern chinesePattern = Pattern.compile("[\u4e00-\u9fa5]");
        Pattern englishPattern = Pattern.compile("[a-zA-Z]");
        Pattern numberPattern = Pattern.compile("\\d");

        int chineseCount = countMatches(chinesePattern, input);
        int englishCount = countMatches(englishPattern, input);
        int numberCount = countMatches(numberPattern, input);
        return chineseCount * 2 + englishCount + numberCount;
    }

    public static int countMatches(Pattern pattern, String string) {
        Matcher matcher = pattern.matcher(string);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    public static String substringByLength(String input, int length) {
        int count = 0;
        int endIndex = 0;
        for (char c : input.toCharArray()) {
            count += (c >= '\u0000' && c <= '\u007F') ? 1 : 2;
            if (count > length) break;
            endIndex++;
        }
        return input.substring(0, endIndex);
    }

    public static String getHandledTitle(String title, int memberSize, boolean fromList) {
        return getTitle(title, memberSize, fromList);
    }

    public static String getHandledTitleExcludeNumber(String title, boolean fromList) {
        return getTitleExcludeNumber(title, fromList);
    }

    private static String getTitleExcludeNumber(String title, boolean fromList) {
        String resultText = "";
        int maxLength = fromList ? 23 : 16;
        // 如果原始文本的長度大於最大長度，則進行ellipsize處理
        if (getTextCount(title) > maxLength) {
            // 取得省略後的文本
            String ellipsizedText = substringByLength(title, maxLength);

            // 創建一個SpannableString對象並設定省略後的文本
            SpannableString spannableString = new SpannableString(ellipsizedText);

            resultText = spannableString + "...";
        } else {
            resultText = title;
        }
        return resultText;
    }

    public static String substringWithinByteLimit(String s, int limit) {
        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.substring(0, i + 1).getBytes(StandardCharsets.UTF_8).length > limit) {
                return s.substring(0, i) + "...";
            }
        }
        return s;
    }
}
