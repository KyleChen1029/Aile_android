package tw.com.chainsea.ce.sdk.bean.msg.content;

import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;

/**
 * current by evan on 2020-01-09
 */

public class VideoContent implements IMessageContent<MessageType> {
    private static final long serialVersionUID = 2902896040670330343L;

    int height;
    int width;
    long size;
    String url;
    String name;
    String android_local_path = "";
    double duration;

    // ui control
    private boolean isPlaying = false;
    private String progress;
    private boolean isDownload;
    private int thumbnailWidth = -1;
    private int thumbnailHeight = -1;
    private String thumbnailUrl = "";


    public VideoContent(int height, int width, String name, String android_local_path) {
        this.height = height;
        this.width = width;
        this.name = name;
        this.android_local_path = android_local_path;
    }

    public VideoContent(int height, int width, long size, String url, String name) {
        this.height = height;
        this.width = width;
        this.size = size;
        this.url = url;
        this.name = name;
    }

    public VideoContent(int height, int width, long size, String url, String name, String android_local_path, double duration, boolean isPlaying) {
        this.height = height;
        this.width = width;
        this.size = size;
        this.url = url;
        this.name = name;
        this.android_local_path = android_local_path;
        this.duration = duration;
        this.isPlaying = isPlaying;
        this.isDownload = false;
        this.thumbnailUrl = "";
        this.thumbnailWidth = -1;
        this.thumbnailHeight = -1;
    }

    @Override
    public MessageType getType() {
        return MessageType.VIDEO;
    }

    @Override
    public String toStringContent() {
        return JsonHelper.getInstance().toJson(this);
    }

    @Override
    public String simpleContent() {
        return "[影片]";
    }

    @Override
    public String getFilePath() {
        return android_local_path;
    }

    @Override
    public JSONObject getSendObj()  {
        return null;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAndroid_local_path() {
        return android_local_path;
    }
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    public int getThumbnailWidth() {
        return thumbnailWidth;
    }
    public int getThumbnailHeight() {
        return thumbnailHeight;
    }

    public void setAndroid_local_path(String android_local_path) {
        this.android_local_path = android_local_path;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }
    public boolean isDownload() {
        return isDownload;
    }

    public void setDownload(boolean download) {
        isDownload = download;
    }

    public void setThumbnailUrl(String url) {
        thumbnailUrl = url;
    }
    public void setThumbnailWidth(int width) {
        thumbnailWidth = width;
    }
    public void setThumbnailHeight(int height) {
        thumbnailHeight = height;
    }
}
