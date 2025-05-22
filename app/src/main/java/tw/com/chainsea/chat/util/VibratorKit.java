package tw.com.chainsea.chat.util;

import static tw.com.chainsea.chat.App.getContext;

import android.app.Service;
import android.os.VibrationEffect;
import android.os.Vibrator;

import java.util.Arrays;

public class VibratorKit {
    private static Vibrator vibrator;
    private static final long[] doubleClickPattern = {10, 50, 50, 50};
    private static final long[] longClickPattern = {10, 50};

    public static void longClick() {
        vibrate(longClickPattern);
    }

    public static void doubleClick() {
        vibrate(doubleClickPattern);
    }

    public static void vibrate(final long[] pattern) {
        if (vibrator == null) {
            vibrator = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
        }
        //SDK26後才能用
        int[] amplitudes = new int[pattern.length];
        Arrays.fill(amplitudes, 100);
        VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, amplitudes, -1);
        vibrator.vibrate(vibrationEffect);
    }
}
