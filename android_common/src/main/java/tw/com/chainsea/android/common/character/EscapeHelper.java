package tw.com.chainsea.android.common.character;

public class EscapeHelper {

    public static String escapeKey(String key) {
        switch (key) {
            case "/":
                return "//";
            case "'":
                return "''";
            case "[":
                return "/[";
            case "]":
                return "/]";
            case "%":
                return "/%";
            case "&":
                return "/&";
            case "_":
                return "/_";
            case "(":
                return "/(";
            case ")":
                return "/)";
            default:
                return key;
        }
    }
}
