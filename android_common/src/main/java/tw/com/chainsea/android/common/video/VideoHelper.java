package tw.com.chainsea.android.common.video;

import static android.media.MediaMetadataRetriever.METADATA_KEY_DURATION;
import static android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT;
import static android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION;
import static android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

/**
 * current by evan on 12/23/20
 * Update by Evan.W on 2020/12/30.
 *
 * @author Evan Wang
 * @date 12/23/20
 */
public class VideoHelper {


    public static Bitmap findFrameAtTime(String url, long time) throws IOException {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(url, new HashMap<String, String>());
        Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(time); //unit in microsecond
        mediaMetadataRetriever.close();
        Log.i("", "");
        return bmFrame;
    }


    public static long findDuration(String url) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(url, new HashMap<>());
        String duration = retriever.extractMetadata(METADATA_KEY_DURATION); // The playback duration is in milliseconds
        return Long.valueOf(duration) / 1000;
    }


    public static int[] size(String url) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(url);
            String rotation = retriever.extractMetadata(METADATA_KEY_VIDEO_ROTATION);
            int width = 0;
            int height = 0;
            if ("0".equals(rotation) || "180".equals(rotation)) {
                width = Integer.valueOf(retriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH));
            } else {
                width = Integer.valueOf(retriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT));
            }
            if ("0".equals(rotation) || "180".equals(rotation)) {
                height = Integer.valueOf(retriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT));
            } else {
                height = Integer.valueOf(retriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH));
            }
            retriever.close();
            return new int[]{width, height};
        } catch (Exception ignored) {
            return new int[]{0, 0};
        }
    }
}
