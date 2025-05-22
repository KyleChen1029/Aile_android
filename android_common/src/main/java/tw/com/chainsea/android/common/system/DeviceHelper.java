package tw.com.chainsea.android.common.system;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;

import java.util.UUID;

import tw.com.chainsea.android.common.CommonLib;

/**
 * current by evan on 2020-03-04
 */
public class DeviceHelper {
    public static String getDeviceName(Context context) {
        return Settings.Global.getString(context.getContentResolver(), Settings.Global.DEVICE_NAME);
    }

    @SuppressLint("MissingPermission")
    public static void setDeviceName(Context context, String name) {
        Settings.Global.putString(context.getContentResolver(), Settings.Global.DEVICE_NAME, name);
        BluetoothAdapter.getDefaultAdapter().setName(name);
    }

    private final static String DEFAULT_UUID = "ffffffff-ffff-ffff-ffff-ffffffffffff";

    public static String getUUID() {
        SharedPreferences sharedPreferences = CommonLib.getAppContext().getSharedPreferences("COMMON", Context.MODE_PRIVATE);
        String uuid = sharedPreferences.getString("UUID", DEFAULT_UUID);
        if (DEFAULT_UUID.equals(uuid)) {
            uuid = UUID.randomUUID().toString();
            sharedPreferences.edit().putString("UUID", uuid).apply();
        }
        return uuid;
    }

    public static String getUniquePsuedoID() {
        String serial;

        String m_szDevIDShort = "35" +
            Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +

            Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +

            Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +

            Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +

            Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

            Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

            Build.USER.length() % 10; //13 位

        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
            // API>=9 use serial number
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            // serial Need an initialization
            serial = "serial"; // Random initialization
        }
        // 15-digit number pieced together using hardware information
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }
}
