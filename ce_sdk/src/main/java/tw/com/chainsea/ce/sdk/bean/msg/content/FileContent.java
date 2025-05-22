package tw.com.chainsea.ce.sdk.bean.msg.content;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;

/**
 * current by evan on 2020-01-08
 */
public class FileContent implements IMessageContent<MessageType> {
    private static final long serialVersionUID = -7388788291294458530L;

    String name;
    int size;
    String url;
    String android_local_path = "";
    @SerializedName("MD5")
    String md5;

    // ui control
    private String progress;
    private boolean isDownload;

    public FileContent(String name, String android_local_path) {
        this.name = name;
        this.android_local_path = android_local_path;
    }

    public FileContent(String name, int size, String url, String android_local_path) {
        this.name = name;
        this.size = size;
        this.url = url;
        this.android_local_path = android_local_path;
    }

    @Override
    public MessageType getType() {
        return MessageType.FILE;
    }

    @Override
    public String toStringContent() {
        return JsonHelper.getInstance().toJson(this);
    }

    @Override
    public String simpleContent() {
        return "[文件]";
    }

    @Override
    public String getFilePath() {
        return android_local_path;
    }

    @Override
    public JSONObject getSendObj()  {
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAndroid_local_path() {
        return android_local_path;
    }

    public void setAndroid_local_path(String android_local_path) {
        this.android_local_path = android_local_path;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
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
}
