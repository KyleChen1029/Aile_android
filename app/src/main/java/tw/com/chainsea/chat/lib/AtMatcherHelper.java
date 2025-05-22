package tw.com.chainsea.chat.lib;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.text.KeyWordHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.TargetType;
import tw.com.chainsea.ce.sdk.bean.msg.content.MentionContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent;
import tw.com.chainsea.chat.App;
import tw.com.chainsea.chat.messagekit.main.viewholder.AtMessageView;
import tw.com.chainsea.chat.util.TextViewHelper;
import tw.com.chainsea.chat.util.UrlTextUtil;

/**
 * current by evan on 2019-11-28
 */
public class AtMatcherHelper {


    public static class AtSpanClick extends ClickableSpan {

        private String id;
        private int color;
        private AtMessageView.OnAtClickSpanClickListener listener;

        public AtSpanClick(String id, String name, int color, AtMessageView.OnAtClickSpanClickListener listener) {
            this.id = id;
            this.color = color;
            this.listener = listener;
        }

        @Override
        public void onClick(@NonNull View widget) {
            if (this.listener != null) {
                listener.atClick(this.id);
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(this.color);
        }
    }


    public static SpannableString setLeftImageAndHighLightAt(Context context, String text, @DrawableRes int id, LinkedList<UserProfileEntity> users, @ColorInt int color) {
        users.add(0, UserProfileEntity.Build()
            .id("00000000-0000-0000-0000-000000000000")
            .avatarId("ALL")
            .alias("ALL")
            .userType(UserType.EMPLOYEE)
            .nickName("ALL")
            .build()
        );
        Collections.sort(users, (o1, o2) -> {
            String o1Name = !Strings.isNullOrEmpty(o1.getAlias()) ? o1.getAlias() : o1.getNickName();
            String o2Name = !Strings.isNullOrEmpty(o2.getAlias()) ? o2.getAlias() : o2.getNickName();
            return ComparisonChain.start()
                .compare(o2Name, o1Name)
                .compare(o2Name.length(), o1Name.length() + 0.5)
                .result();
        });
        SpannableString spannableString = new SpannableString((id != 0 && id != -1 ? "  " : "") + text);
        Drawable rightDrawable = null;
        if (id != 0 && id != -1) {
            rightDrawable = AppCompatResources.getDrawable(context, id);
            rightDrawable.setBounds(0, 0, rightDrawable.getIntrinsicWidth(), rightDrawable.getIntrinsicHeight());
        }


        for (UserProfileEntity profile : users) {
            String name = (!Strings.isNullOrEmpty(profile.getAlias()) ? profile.getAlias() : profile.getNickName());
            String quote = Pattern.quote("@" + name + " ");
            String wordReg = "(?i)" + quote;//用(?i)来忽略大小写
            Matcher m = Pattern.compile(wordReg).matcher(text);

            while (m.find()) {
                int start = m.start();
                int end = m.end();

                if (id != 0 && id != -1) {
                    start += 2;
                    end += 2;
                }
                spannableString.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }


        spannableString.setSpan(new TextViewHelper.TextImageSpan(rightDrawable), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

//        String quote = Pattern.quote("" + keyword);
//        String wordReg = "(?i)" + quote;  //用(?i)来忽略大小写
//        Pattern p = Pattern.compile(wordReg);
//        Matcher m = p.matcher(spannableString);
//        while (m.find()) {
//            int start = m.start();
//            int end = m.end();
//            spannableString.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
        return spannableString;
    }


    public static SpannableStringBuilder matcherAtUsers(String prefix, List<MentionContent> ceMentions, Map<String, String> membersTable) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (MentionContent m : ceMentions) {
            TargetType targetType = m.getObjectType();
            switch (targetType) {
                case USER:
                    for (String id : m.getUserIds()) {
                        if (membersTable.get(id) != null) {
                            // To highlight
                            builder.append(matcherAtUser(0xFF4A90E2, prefix + membersTable.get(id) + " "));
                        } else {
                            // To highlight
                            builder.append(matcherAtUser(0xFF000000, prefix + "未知 "));
                        }
                    }
                    break;
                case ALL:
                    // To highlight
                    builder.append(matcherAtUser(0xFF4A90E2, prefix + "ALL "));
                    break;
                default:
            }

            MessageType type = m.getType();
            if (Objects.requireNonNull(type) == MessageType.TEXT) {
                TextContent atText = JsonHelper.getInstance().from(m.getContent(), TextContent.class);
                builder.append(atText.getText());
            }
        }
        return builder;
    }

    /**
     * Add highlight click event
     */
    public static SpannableStringBuilder matcherAtUsers(String prefix, List<MentionContent> ceMentions, Map<String, String> membersTable, AtMessageView.OnAtClickSpanClickListener<String> listener) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (MentionContent m : ceMentions) {
            TargetType targetType = m.getObjectType();
            if (targetType != null) {
                switch (targetType) {
                    case USER:
                        for (String id : m.getUserIds()) {
                            if (membersTable.get(id) != null) {
                                builder.append(matcherAtUser(0xFF4A90E2, id, prefix + membersTable.get(id) + " ", listener)); // To highlight
                            } else {
                                builder.append(matcherAtUser(0xFF000000, prefix + "未知 ")); // 要高亮
                            }
                        }
                        break;
                    case ALL:
                        builder.append(matcherAtUser(0xFF4A90E2, prefix + "ALL ")); // 要高亮
                        break;
                    default:
                }
                MessageType type = m.getType();
                if (Objects.requireNonNull(type) == MessageType.TEXT) {
                    TextContent atText = JsonHelper.getInstance().from(m.getContent(), TextContent.class);
                    builder.append(atText.getText());
                }
            }
        }
        return builder;
    }

    public static SpannableStringBuilder matcherAtUsersWithKeyword(List<MentionContent> ceMentions, Map<String, String> membersTable, String keyword) {
        return matcherAtUsersWithKeyword(ceMentions, membersTable, keyword, null);
    }

    public static SpannableStringBuilder matcherAtUsersWithKeyword(List<MentionContent> ceMentions, Map<String, String> membersTable, String keyword, AtMessageView.OnAtClickSpanClickListener<String> listener) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (MentionContent m : ceMentions) {
            TargetType targetType = m.getObjectType();
            if (targetType == null) {
                continue;
            }
            switch (targetType) {
                case USER:
                    for (String id : m.getUserIds()) {
                        if (membersTable.get(id) != null) {
                            builder.append(matcherAtUser(0xFF4A90E2, id, "@" + membersTable.get(id) + " ", listener)); // To highlight
                        } else {
                            builder.append(matcherAtUser(0xFF000000, "@" + "未知 ")); // 要高亮
                        }
                    }
                    break;
                case ALL:
                    builder.append(matcherAtUser(0xFF4A90E2, "@" + "ALL ")); // 要高亮
                    break;
                case UNDEF:
                default:
            }

            MessageType type = m.getType();
            if (Objects.requireNonNull(type) == MessageType.TEXT) {
                TextContent atText = JsonHelper.getInstance().from(m.getContent(), TextContent.class);
                String url = extractLinks(atText.getText());
                UrlTextUtil urlTextUtil = new UrlTextUtil();
                if (keyword.isEmpty()) {
                    if (urlTextUtil.isUrlFormat(atText.getText())) {
                        builder.append(urlTextUtil.getUrlSpannableString(App.getContext(), atText.getText()));
                    } else
                        builder.append(atText.getText());
                } else {
                    if (urlTextUtil.isUrlFormat(atText.getText())) {
                        builder.append(
                            urlTextUtil.getUrlSpannableString(
                                App.getContext(),
                                KeyWordHelper.matcherSearchKeyWordForURL(-0xfc7, atText.getText(), keyword)
                            )
                        );
                    } else {
                        SpannableString s = new SpannableString(atText.getText());
                        String quote = Pattern.quote("" + keyword);
                        String wordReg = "(?i)" + quote;  // Use (?i) to ignore case
                        Pattern p = Pattern.compile(wordReg);
                        Matcher matcher = p.matcher(s);

                        if (matcher.find()) {
                            int start = matcher.start();
                            int end = matcher.end();
                            Spannable keywordSpan = new SpannableString(atText.getText());
                            keywordSpan.setSpan(new BackgroundColorSpan(-0xfc7), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            builder.append(keywordSpan);
                        }
                    }
                }
            }
        }

        return builder;
    }

    private static String extractLinks(String text) {
        Matcher matcher = Patterns.WEB_URL.matcher(text);
        while (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    public static Spannable matcherAtUser(int color, String text) {
        Spannable s = new SpannableString(text);
        s.setSpan(new ForegroundColorSpan(color), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }

    public static Spannable matcherAtUser(int color, String id, String name, AtMessageView.OnAtClickSpanClickListener<String> listener) {
        Spannable s = new SpannableString(name);
        s.setSpan(new AtSpanClick(id, name, color, listener), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }
}
