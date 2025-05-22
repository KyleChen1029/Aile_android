package tw.com.chainsea.ce.sdk.config;

import java.util.Locale;

/**
 * current by evan on 2020-08-12
 *
 * @author Evan Wang
 * date 2020-08-12
 */
public class AppConfig {
    public static final String osType = "android";
    public static final String LANGUAGE = getLanguage();
    public static final String AES_KEY = "7706618877066188";
    public static String tokenForNewAPI = "";

    private static String getLanguage(){
        String localLanguage = (Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry()).toLowerCase();
        switch (localLanguage) {
            case "zh-tw":
            case "zh-cn":
            case "en-us":
                return localLanguage;
            default:
                return "en-us";
        }
    }
}
