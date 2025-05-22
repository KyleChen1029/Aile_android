package tw.com.chainsea.chat.util;

import com.google.common.base.Strings;

import tw.com.chainsea.android.common.log.CELog;

public class NameKit {
    private final String[] colors = {
        "#1ABC9C",
        "#3498DB",
        "#6699CC",
        "#F1C40F",
        "#8E44AD",
        "#B45B3E",
        "#E74C3C",
        "#D35400",
        "#479AC7",
        "#2C3E50",
        "#7F8C8D",
        "#336699",
        "#66CCCC",
        "#00B271"
    };

    private int toAscii(String input) {
        if (input == null || input.isEmpty()) return 0;
        int result = 0;
        for (int i = 0; i < input.length(); i++) {
            result += input.charAt(i);
        }
        return result;
    }

    public String getBackgroundColor(String input) {
        return colors[toAscii(input) % colors.length];
    }

    public String getAvatarName(String input) {
        String returnValue;
        if (!Strings.isNullOrEmpty(input)) {
            try {
                String[] split = input.trim().toUpperCase().split("[^\\u4e00-\\u9fa5a-zA-Z0-9]+");
                if (split.length > 1) {
                    if (!split[0].isEmpty() && !split[1].isEmpty()) {
                        returnValue = String.valueOf(split[0].charAt(0)) + split[1].charAt(0);
                    } else {
                        for (String s : split) {
                            if (s.length() > 1) {
                                returnValue = s.substring(0, 2);
                            }
                        }
                    }
                } else if (split[0].length() > 1) {
                    if (split[0].contains("NULL")) {
                        returnValue = "未知";
                    }
                    returnValue = String.valueOf(split[0].charAt(0)) + split[0].charAt(1);
                }
                returnValue = split[0];
            } catch (Exception ignored) {
                CELog.e("input = " + input);
                returnValue = "";
            }
            return returnValue;
        } else
            return "";
    }
}
