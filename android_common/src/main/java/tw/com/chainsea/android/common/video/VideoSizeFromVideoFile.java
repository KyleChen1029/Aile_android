package tw.com.chainsea.android.common.video;

import static android.media.MediaMetadataRetriever.METADATA_KEY_BITRATE;
import static android.media.MediaMetadataRetriever.METADATA_KEY_DURATION;
import static android.media.MediaMetadataRetriever.METADATA_KEY_MIMETYPE;
import static android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT;
import static android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION;
import static android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH;

import android.media.MediaMetadataRetriever;

import java.io.File;

import tw.com.chainsea.android.common.file.FileHelper;


/**
 * current by evan on 2020-03-05
 */
public class VideoSizeFromVideoFile implements IVideoSize {

    private final String filePath;
    private final File file;

    public VideoSizeFromVideoFile(String filePath) {
        this.filePath = filePath;
        this.file = new File(filePath);
    }

    @Override
    public int width() {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            String rotation = retriever.extractMetadata(METADATA_KEY_VIDEO_ROTATION);
            if ("0".equals(rotation) || "180".equals(rotation)) {
                return Integer.parseInt(retriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH));
            } else {
                return Integer.parseInt(retriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT));
            }
        } catch (Exception ignored) {

            return 0;
        }
    }

    @Override
    public int height() {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            String rotation = retriever.extractMetadata(METADATA_KEY_VIDEO_ROTATION);
            if ("0".equals(rotation) || "180".equals(rotation)) {
                return Integer.parseInt(retriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT));
            } else {
                return Integer.parseInt(retriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH));
            }
        } catch (Exception ignored) {

            return 0;
        }
    }

    @Override
    public long size() {
        return this.file.length();
    }

    @Override
    public String name() {
        String type = FileHelper.getFileTyle(filePath);
        return String.format("%s_%s.%s", "video", System.currentTimeMillis(), type);
    }

    @Override
    public File file() {
        return this.file;
    }

    @Override
    public String path() {
        return this.filePath;
    }

    @Override
    public long duration() {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            String duration = retriever.extractMetadata(METADATA_KEY_DURATION); // The playback duration is in milliseconds
            String type = retriever.extractMetadata(METADATA_KEY_MIMETYPE);
            String bitrate = retriever.extractMetadata(METADATA_KEY_BITRATE);
            return Long.parseLong(duration) / 1000;
        } catch (Exception ignored) {

            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        return SizeEqualsAndHashCode.equals(this, o);

    }

    @Override
    public int hashCode() {
        return SizeEqualsAndHashCode.hashCode(this);
    }
}
