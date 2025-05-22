package tw.com.chainsea.android.common.text;

import android.content.Context;

import com.google.common.base.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * current by evan on 2020-07-16
 *
 * @author Evan Wang
 * @date 2020-07-16
 */
public class StringHelper {

    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}");

    public static CharSequence getString(String str, String def) {
        return Strings.isNullOrEmpty(str) ? def == null ? "" : def : str;
    }

    public static StringBuilder autoNewLine(Context context, int... resIds) {
        StringBuilder builder = new StringBuilder();
        for (int res : resIds) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(context.getString(res));
        }
        return builder;
    }

    public static StringBuilder autoNewSpace(Context context, int... resIds) {
        StringBuilder builder = new StringBuilder();
        for (int res : resIds) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(context.getString(res));
        }
        return builder;
    }

    public static StringBuilder autoNewSpace(String... strs) {
        StringBuilder builder = new StringBuilder();
        for (String res : strs) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(res);
        }
        return builder;
    }

    public static int getAsciiCount(String str) {
        int asciiCount = 0;

        for (int i = 0; i < str.length(); i++) {
            asciiCount += (int) str.charAt(i);
        }
        return asciiCount << 10;
    }

    /**
     * 0 == default
     * 1 == toUpperCase()
     * 2 == toLowerCase()
     *
     * @param str
     * @param def
     * @return
     */
    public static String getEnd(String str, String def, int cas) {
        if (str == null || str.isEmpty()) {
            return def == null ? "" : def;
        }
        String end = str.substring(str.length() - 1);
        return cas == 1 ? end.toUpperCase() : cas == 2 ? end.toLowerCase() : end;
    }

    public static boolean isValidUUID(String uuid) {
        if (Strings.isNullOrEmpty(uuid)) {
            return false;
        }
        Matcher matcher = UUID_PATTERN.matcher(uuid);
        return matcher.matches();
    }

}
