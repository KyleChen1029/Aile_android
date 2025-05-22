package tw.com.chainsea.android.common.client.type;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import java.util.Set;

import okhttp3.MediaType;

/**
 * current by evan on 2019-10-30
 */
public enum FileMedia {

    BMP(new String[]{".bmp"}, Media.IMAGE_BMP),
    GIF(new String[]{".gif"}, Media.IMAGE_GIF),
    ICON(new String[]{".icon"}, Media.IMAGE_ICON),
    JPEG(new String[]{".jpeg", ".jpg"}, Media.IMAGE_JPEG),
    PNG(new String[]{".png"}, Media.IMAGE_PNG),
    SVG(new String[]{".svg"}, Media.IMAGE_SVG),
    TIFF(new String[]{".tiff", ".tif"}, Media.IMAGE_TIFF),
    WEDP(new String[]{".webp"}, Media.IMAGE_WEDP),


    ACC(new String[]{".acc"}, Media.AUDIO_ACC),
    MID(new String[]{".mid", ".midi"}, Media.AUDIO_MID),
    A_MPEG(new String[]{".mp3"}, Media.AUDIO_MPEG),
    OGA(new String[]{".oga"}, Media.AUDIO_OGA),
    WAV(new String[]{".wav"}, Media.AUDIO_WAV),
    A_WEBM(new String[]{".weba"}, Media.AUDIO_WEBM),
    A_MP4A(new String[]{"x-m4a"}, Media.AUDIO_M4A),
    MP4A(new String[]{".m4a"}, Media.AUDIO_M4A),


    AVI(new String[]{".avi"}, Media.VIDEO_AVI),
    V_MPEG(new String[]{".mpeg"}, Media.VIDEO_MPEG),
    OGV(new String[]{".ogv"}, Media.VIDEO_OGV),
    V_WEBM(new String[]{".webm"}, Media.VIDEO_WEBM),
    V_3GP(new String[]{".3gp"}, Media.VIDEO_3GP),
    V_3G2(new String[]{".3g2"}, Media.VIDEO_3G2),
    V_MP4(new String[]{".mp4"}, Media.VIDEO_MP4),

    DOWNLOAD(new String[]{".x-download"}, Media.X_DOWNLOAD);


    private String[] fileTypes;
    private Media media;

    FileMedia(String[] fileTypes, Media media) {
        this.fileTypes = fileTypes;
        this.media = media;
    }

    public static Set<FileMedia> IMAGE_TYPE_SET = Sets.newHashSet(BMP, GIF, ICON, JPEG, PNG, SVG, TIFF, WEDP);
    public static Set<FileMedia> VIDEO_TYPE_SET = Sets.newHashSet(AVI, V_MP4, V_MPEG, OGV, V_WEBM, V_3GP, V_3G2);

    public static boolean isImage(String contentType) {
        if (Strings.isNullOrEmpty(contentType)) {
            return false;
        }
        for (FileMedia value : IMAGE_TYPE_SET) {
            for (String type : value.fileTypes) {
                if (type.toUpperCase().contains(contentType.toUpperCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isVideo(String contentType) {
        if (Strings.isNullOrEmpty(contentType)) {
            return false;
        }
        for (FileMedia value : VIDEO_TYPE_SET) {
            for (String type : value.fileTypes) {
                if (type.toUpperCase().contains(contentType.toUpperCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isDownload(String contentType) {
        for (String type : DOWNLOAD.fileTypes) {
            if (type.toUpperCase().contains(contentType.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFileContentType(String contentType) {
        for (FileMedia value : values()) {
            for (String type : value.fileTypes) {
                if (type.toUpperCase().contains(contentType.toUpperCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static MediaType of(String fileTypes) {
        for (FileMedia value : values()) {
            for (String type : value.fileTypes) {
                if (type.toUpperCase().contains(fileTypes.toUpperCase())) {
                    return value.media.get();
                }
            }
        }
        return Media.IMAGE_JPEG.get();
    }
}
