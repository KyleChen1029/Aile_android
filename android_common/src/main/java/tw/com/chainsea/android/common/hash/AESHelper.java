package tw.com.chainsea.android.common.hash;

import static tw.com.chainsea.android.common.CommonLib.getAppContext;

import android.annotation.SuppressLint;
import android.util.Base64;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import tw.com.chainsea.android.common.BuildConfig;
import tw.com.chainsea.android.common.R;

/**
 * current by evan on 12/14/20
 *
 * @author Evan Wang
 * @date 12/14/20
 */
public class AESHelper {
    private static final String ALGORITHM_AES = "AES";
    private static final String UTF_8 = "UTF-8";
    private static final String EBC_PKCS7_PADDING = "AES/ECB/PKCS7Padding";
    private static final String ECB_PKCS5_PADDING = "AES/ECB/PKCS5Padding";
    private static final String apiIdPart3 = "6188";

    @SuppressLint("GetInstance")
    public static String encryptBase64(String input) {
        try {
            Cipher cipher = Cipher.getInstance(EBC_PKCS7_PADDING);
            byte[] keyBytes = getApiKey().getBytes(StandardCharsets.UTF_8);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, ALGORITHM_AES));
            byte[] doFinal = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(doFinal, Base64.NO_WRAP);
//            return URLEncoder.encode(Base64.encodeToString(doFinal, Base64.NO_WRAP), UTF_8);
        } catch (Exception ignored) {
            return null;
        }
    }

    @SuppressLint("GetInstance")
    public static String decryptBase64(String input) {
        try {
            byte[] decodeInput = Base64.decode(input, Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance(EBC_PKCS7_PADDING);
            byte[] keyBytes = getApiKey().getBytes(StandardCharsets.UTF_8);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, ALGORITHM_AES));
            byte[] doFinal = cipher.doFinal(decodeInput);
            return new String(doFinal);
        } catch (Exception ignored) {
            return "";
        }
    }

    /*
     * encrypt
     * key length == 16
     *
     * @param sSrc
     * @param sKey
     * @return
     * @throws Exception
     */
//    public static String encrypt(String sSrc, String sKey) throws Exception {
//        if (sKey == null) {
//            return null;
//        }
//
//        // Determine whether the Key is 16 bits
//        if (sKey.length() != 16) {
//            return null;
//        }
//        byte[] raw = sKey.getBytes(StandardCharsets.UTF_8);
//        SecretKeySpec skeySpec = new SecretKeySpec(raw, ALGORITHM_AES);
//        Cipher cipher = Cipher.getInstance(EBC_PKCS7_PADDING);
//
//        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
//        byte[] encrypted = cipher.doFinal(sSrc.getBytes(StandardCharsets.UTF_8));
//        String encodeSrc = Base64.encodeToString(encrypted, Base64.NO_WRAP);
//        return URLEncoder.encode(encodeSrc, StandardCharsets.UTF_8);
//    }


    /**
     * decrypt
     *
     * @param sSrc
     * @param sKey
     * @return
     * @throws Exception
     */
    @SuppressLint("GetInstance")
    public static String decrypt(String sSrc, String sKey) throws Exception {
        try {
            if (sKey == null) {
                return null;
            }

            // Determine whether the Key is 16 bits
            if (sKey.length() != 16) {
                return null;
            }
            byte[] raw = sKey.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, ALGORITHM_AES);
            Cipher cipher = Cipher.getInstance(EBC_PKCS7_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
//            byte[] decodeArray = URLDecoder.decode(sSrc, UTF_8).getBytes(UTF_8);
            byte[] encrypted = Base64.decode(sSrc, Base64.NO_WRAP);//先用base64解密
            try {
                byte[] original = cipher.doFinal(encrypted);
                return new String(original, StandardCharsets.UTF_8);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    private static String getApiKey() {
        return BuildConfig.API_ID + getAppContext().getString(R.string.part2) + apiIdPart3;
    }

}
