package tw.com.chainsea.chat.lib;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {


    public static boolean checkMobile(String str) {
        Pattern pattern = Pattern.compile("^(\\+86)?((13[0-9])|(14[5,7])|(15[^4])|(17[0,3,5-8])|(18[0-9])|166|198|199)\\d{8}$|^(\\+886)?0*9[0-9]{8}$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }


    public static <D> boolean isEmpty(List<D> list) {
        return list == null || list.isEmpty();
    }
}
