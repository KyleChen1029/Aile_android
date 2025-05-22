package tw.com.chainsea.android.common.voice;

import android.annotation.SuppressLint;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.File;

/**
 * current by evan on 2020-08-26
 *
 * @author Evan Wang
 * @date 2020-08-26
 */
public class VoiceHelper {


    /**
     * Take recording file length
     *
     * @param file
     * @return
     */
    public static double getVoiceDuration(File file) {
        return getVoiceDuration(file.getPath());
    }

    /**
     * Take recording file length
     *
     * @param path
     * @return
     */
    public static double getVoiceDuration(String path) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int millSecond = Integer.parseInt(durationStr);
        Log.e("getVoiceDuration", (millSecond / 60.0) + "");
        return millSecond / 1000.0d;
    }

    @SuppressLint("DefaultLocale")
    public static String strDuration(double duration) {
        int ms, s, m, h, d;
        double dec;
        double time = duration * 1.0f;

        time = (time / 1000.0);
        dec = time % 1;
        time = time - dec;
        ms = (int) (dec * 1000);

        time = (time / 60.0);
        dec = time % 1;
        time = time - dec;
        s = (int) (dec * 60);

        time = (time / 60.0);
        dec = time % 1;
        time = time - dec;
        m = (int) (dec * 60);

        time = (time / 24.0);
        dec = time % 1;
        time = time - dec;
        h = (int) (dec * 24);

        d = (int) time;

//        return (String.format("%02d:%02d", m, s));
        if (d > 0) {
            return (String.format("%d d - %02d:%02d:%02d", d, h, m, s));
        } else if (h > 0) {
            return (String.format("%02d:%02d:%02d", h, m, s));
        } else {
            return (String.format("%02d:%02d", m, s));
        }
    }

}
