package tw.com.chainsea.android.common.hash;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMacHelper {
    private static final String ALGORITHM_AES = "HmacSHA256"; //or "HmacSHA1", "HmacSHA512"
    private static final byte[] bytes = "7706618877066188".getBytes(StandardCharsets.UTF_8);

    public static String encryptHmac256Base64(String input) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM_AES);
            mac.init(new SecretKeySpec(bytes, ALGORITHM_AES));
            byte[] encrypted = mac.doFinal(input.getBytes());
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static char[] byteToHex(byte[] bytes) {
        final char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0, v; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return hexChars;
    }
}
