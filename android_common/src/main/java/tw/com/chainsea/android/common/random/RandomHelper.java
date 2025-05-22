package tw.com.chainsea.android.common.random;

import android.util.Log;

import androidx.core.graphics.ColorUtils;

import java.security.SecureRandom;

/**
 * current by evan on 2020/5/6
 *
 * @author Evan Wang
 * @date 2020/5/6
 */
public class RandomHelper {


    public static int randomColor() {
        SecureRandom random = new SecureRandom();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        Log.i("", "" + ColorUtils.blendARGB(r, g, b));
        return ColorUtils.blendARGB(r, g, b);
    }
}
