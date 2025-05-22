package tw.com.chainsea.android.common.setting;

import android.content.ComponentName;
import android.content.Intent;
import android.provider.Settings;

/**
 * current by evan on 11/27/20
 *
 * @author Evan Wang
 * @date 11/27/20
 */
public class SettingScreenHelper {


    protected static void _showSettingScreen(String intentStr, ASettingCallBack callBack) {
        try {
            Intent intent = new Intent(intentStr);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callBack.callback(intent);
        } catch (Exception e) {
            callBack.error(e.getMessage());
        }
    }

    public static void showSettingScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.SETTINGS", callBack);
    }

    public static void showAPNScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.APN_SETTINGS", callBack);
    }

    public static void showLocationScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.LOCATION_SOURCE_SETTINGS", callBack);
    }

    public static void showSecurityScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.SECURITY_SETTINGS", callBack);
    }

    public static void showWifiScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.WIFI_SETTINGS", callBack);
    }

    public static void showDateScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.DATE_SETTINGS", callBack);
    }

    public static void showSoundScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.SOUND_SETTINGS", callBack);
    }

    public static void showDisplayScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.DISPLAY_SETTINGS", callBack);
    }

    public static void showApplicationScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.APPLICATION_SETTINGS", callBack);
    }

    public static void showNetworkSettingScreen(ASettingCallBack callBack) {
        showDataRoamingScreen(callBack);
    }

    public static void showNetworkOperatorScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.NETWORK_OPERATOR_SETTINGS", callBack);
    }

    public static void showDataRoamingScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.DATA_ROAMING_SETTINGS", callBack);
    }

    public static void showDataMobileScreen(ASettingCallBack callBack) {
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);//android.provider.Settings.ACTION_SETTINGS //Intent.ACTION_MAIN
        intent.setClassName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        callBack.callback(intent);
    }

    public static void showNotificationScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.NOTIFICATION_SETTINGS", callBack);
    }

    public static void showBatterySaverScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.BATTERY_SAVER_SETTINGS", callBack);
    }

    public static void showNfcScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.NFC_SETTINGS", callBack);
    }

    public static void showInternalStorageScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.INTERNAL_STORAGE_SETTINGS", callBack);
    }

    public static void showDictionarySettingScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.USER_DICTIONARY_SETTINGS", callBack);
    }

    public static void showManageApplicationsScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.MANAGE_APPLICATIONS_SETTINGS", callBack);
    }

    public static void showManageAllApplicationsScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS", callBack);
    }

    public static void showMemoryCardScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.MEMORY_CARD_SETTINGS", callBack);
    }

//    public static void showAirPlaneScreen(ASettingCallBack callBack) {
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN){
//            if (Reference.getSystemOptions().BRAND.equalsIgnoreCase("Lenovo")) {
//                showSettingScreen(callBack);
//            } else {
//                _showSettingScreen("android.settings.WIRELESS_SETTINGS", callBack);
//            }
//        } else {
//            _showSettingScreen("android.settings.AIRPLANE_MODE_SETTINGS", callBack);
//        }
//    }

    public static void showWirelessScreen(ASettingCallBack callBack) {
        _showSettingScreen("android.settings.WIRELESS_SETTINGS", callBack);
    }

    public static void showWifiScreenSafe(ASettingCallBack callBack) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callBack.callback(intent);
//            Reference.getAppContext().startActivity(intent);
        } catch (Exception e) {
            callBack.error(e.getMessage());
        }
    }
}
