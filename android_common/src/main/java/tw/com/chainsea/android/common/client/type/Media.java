package tw.com.chainsea.android.common.client.type;

import okhttp3.MediaType;
public enum Media {

    OCTET_STREAM(MediaType.parse("application/octet-stream")),

    JSON_UTF8(MediaType.parse("application/json; charset=utf-8")),
    JSON(MediaType.parse("application/json")),
    TEXT_UTF8(MediaType.parse("text/plain;  charset=utf-8")),
    TEXT(MediaType.parse("text/plain")),
    XML_UTF8(MediaType.parse("application/xml; charset=utf-8")),
    XML(MediaType.parse("application/xml")),
    X_DOWNLOAD(MediaType.parse("application/x-download")),
    X_DOWNLOAD_UTF8(MediaType.parse("application/x-download; charset=UTF-8")),

    TEXT_XML_UTF8(MediaType.parse("text/xml; charset=utf-8")),
    TEXT_XML(MediaType.parse("text/xml")),

    HTML_UTF8(MediaType.parse("text/html; charset=utf-8")),
    HTML(MediaType.parse("text/html")),

    X_WWW_FORM_URLENCODE_UTF8(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8")),
    X_WWW_FORM_URLENCODE(MediaType.parse("application/x-www-form-urlencoded")),

    IMAGE_BMP(MediaType.parse("image/bmp")),
    IMAGE_GIF(MediaType.parse("image/gif")),
    IMAGE_ICON(MediaType.parse("image/vnd.microsoft.icon")),
    IMAGE_JPEG(MediaType.parse("image/jpeg")),
    IMAGE_PNG(MediaType.parse("image/png")),
    IMAGE_SVG(MediaType.parse("image/svg+xml")),
    IMAGE_TIFF(MediaType.parse("image/tiff")),
    IMAGE_WEDP(MediaType.parse("image/webp")),


    AUDIO_ACC(MediaType.parse("audio/aac")),
    AUDIO_MID(MediaType.parse("audio/midi")),
    AUDIO_MPEG(MediaType.parse("audio/mpeg")),
    AUDIO_OGA(MediaType.parse("audio/ogg")),
    AUDIO_WAV(MediaType.parse("audio/wav")),
    AUDIO_WEBM(MediaType.parse("audio/webm")),
    AUDIO_M4A(MediaType.parse("audio/x-m4a")),
//    AUDIO_ACC(MediaType.parse("audio/3gpp")),
//    AUDIO_ACC(MediaType.parse("audio/aac")),

    VIDEO_AVI(MediaType.parse("video/x-msvideo")),
    VIDEO_MP4(MediaType.parse("video/mp4")),
    VIDEO_MPEG(MediaType.parse("video/mpeg")),
    VIDEO_OGV(MediaType.parse("video/ogg")),
    VIDEO_WEBM(MediaType.parse("video/webm")),
    VIDEO_3GP(MediaType.parse("video/3gpp")),
    VIDEO_3G2(MediaType.parse("video/3gpp2"));

    private MediaType mediaType;

    public MediaType get() {
        return this.mediaType;
    }

    public static Media findByFileType(String filePath) {
        for (Media media : Media.values()) {
            String subtype = media.get().subtype();
            if (filePath.contains(media.get().subtype())) {
                return media;
            }
        }
        return Media.JSON_UTF8;
    }

    Media(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }
}
