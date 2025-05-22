package tw.com.chainsea.ce.sdk.bean.msg.content;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.Serial;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;

/**
 * current by evan on 2020-01-08
 */
public class ImageContent implements IMessageContent<MessageType> {
    @Serial
    private static final long serialVersionUID = -7819782595368658313L;

    int height;
    String name;
    String thumbnailUrl;
    int width;
    int thumbnailSize;
    int thumbnailHeight;
    int thumbnailWidth;
    String url;
    int size;
    @SerializedName("MD5")
    String MD5;


    // ui control
    private String progress;
    boolean failedToLoad = false;

    public ImageContent(int height, String name, String thumbnailUrl, int width, int thumbnailSize, int thumbnailHeight, int thumbnailWidth, String url, int size, String MD5, String progress, boolean failedToLoad) {
        this.height = height;
        this.name = name;
        this.thumbnailUrl = thumbnailUrl;
        this.width = width;
        this.thumbnailSize = thumbnailSize;
        this.thumbnailHeight = thumbnailHeight;
        this.thumbnailWidth = thumbnailWidth;
        this.url = url;
        this.size = size;
        this.MD5 = MD5;
        this.progress = progress;
        this.failedToLoad = failedToLoad;
    }

    public ImageContent() {
    }

    private static boolean $default$failedToLoad() {
        return false;
    }

    public static ImageContentBuilder Build() {
        return new ImageContentBuilder();
    }


    @Override
    public MessageType getType() {
        return MessageType.IMAGE;
    }

    @Override
    public String toStringContent() {
        return JsonHelper.getInstance().toJson(this);
    }

//    @Override
//    public ImageContent from(String content) {
//        return JsonHelper.getInstance().from(content, this.getClass());
//    }


    @Override
    public String simpleContent() {
        return "[圖片]";
    }


    @Override
    public String getFilePath() {
        return url;
    }

    @Override
    public JSONObject getSendObj() {
        return null;
    }

    public int getHeight() {
        return this.height;
    }

    public String getName() {
        return this.name;
    }

    public String getThumbnailUrl() {
        return this.thumbnailUrl;
    }

    public int getWidth() {
        return this.width;
    }

    public int getThumbnailSize() {
        return this.thumbnailSize;
    }

    public int getThumbnailHeight() {
        return this.thumbnailHeight;
    }

    public int getThumbnailWidth() {
        return this.thumbnailWidth;
    }

    public String getUrl() {
        return this.url;
    }

    public int getSize() {
        return this.size;
    }

    public String getMD5() {
        return this.MD5;
    }

    public String getProgress() {
        return this.progress;
    }

    public boolean isFailedToLoad() {
        return this.failedToLoad;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setThumbnailSize(int thumbnailSize) {
        this.thumbnailSize = thumbnailSize;
    }

    public void setThumbnailHeight(int thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSize(int size) {
        this.size = size;
    }

//    public void setMD5(String MD5) {
//        this.MD5 = MD5;
//    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public void setFailedToLoad(boolean failedToLoad) {
        this.failedToLoad = failedToLoad;
    }

    @NonNull
    public String toString() {
        return "ImageContent(height=" + this.getHeight() + ", name=" + this.getName() + ", thumbnailUrl=" + this.getThumbnailUrl() + ", width=" + this.getWidth() + ", thumbnailSize=" + this.getThumbnailSize() + ", thumbnailHeight=" + this.getThumbnailHeight() + ", thumbnailWidth=" + this.getThumbnailWidth() + ", url=" + this.getUrl() + ", size=" + this.getSize() + ", MD5=" + this.getMD5() + ", progress=" + this.getProgress() + ", failedToLoad=" + this.isFailedToLoad() + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ImageContent other)) return false;
        if (!other.canEqual( this)) return false;
        if (this.getHeight() != other.getHeight()) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$thumbnailUrl = this.getThumbnailUrl();
        final Object other$thumbnailUrl = other.getThumbnailUrl();
        if (this$thumbnailUrl == null ? other$thumbnailUrl != null : !this$thumbnailUrl.equals(other$thumbnailUrl))
            return false;
        if (this.getWidth() != other.getWidth()) return false;
        if (this.getThumbnailSize() != other.getThumbnailSize()) return false;
        if (this.getThumbnailHeight() != other.getThumbnailHeight()) return false;
        if (this.getThumbnailWidth() != other.getThumbnailWidth()) return false;
        final Object this$url = this.getUrl();
        final Object other$url = other.getUrl();
        if (this$url == null ? other$url != null : !this$url.equals(other$url)) return false;
        if (this.getSize() != other.getSize()) return false;
        final Object this$MD5 = this.getMD5();
        final Object other$MD5 = other.getMD5();
        if (this$MD5 == null ? other$MD5 != null : !this$MD5.equals(other$MD5)) return false;
        final Object this$progress = this.getProgress();
        final Object other$progress = other.getProgress();
        if (this$progress == null ? other$progress != null : !this$progress.equals(other$progress))
            return false;
        return this.isFailedToLoad() == other.isFailedToLoad();
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ImageContent;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getHeight();
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $thumbnailUrl = this.getThumbnailUrl();
        result = result * PRIME + ($thumbnailUrl == null ? 43 : $thumbnailUrl.hashCode());
        result = result * PRIME + this.getWidth();
        result = result * PRIME + this.getThumbnailSize();
        result = result * PRIME + this.getThumbnailHeight();
        result = result * PRIME + this.getThumbnailWidth();
        final Object $url = this.getUrl();
        result = result * PRIME + ($url == null ? 43 : $url.hashCode());
        result = result * PRIME + this.getSize();
        final Object $MD5 = this.getMD5();
        result = result * PRIME + ($MD5 == null ? 43 : $MD5.hashCode());
        final Object $progress = this.getProgress();
        result = result * PRIME + ($progress == null ? 43 : $progress.hashCode());
        result = result * PRIME + (this.isFailedToLoad() ? 79 : 97);
        return result;
    }

    public ImageContentBuilder toBuilder() {
        return new ImageContentBuilder().height(this.height).name(this.name).thumbnailUrl(this.thumbnailUrl).width(this.width).thumbnailSize(this.thumbnailSize).thumbnailHeight(this.thumbnailHeight).thumbnailWidth(this.thumbnailWidth).url(this.url).size(this.size).MD5(this.MD5).progress(this.progress).failedToLoad(this.failedToLoad);
    }

    public static class ImageContentBuilder {
        private int height;
        private String name;
        private String thumbnailUrl;
        private int width;
        private int thumbnailSize;
        private int thumbnailHeight;
        private int thumbnailWidth;
        private String url;
        private int size;
        private String MD5;
        private String progress;
        private boolean failedToLoad$value;
        private boolean failedToLoad$set;

        ImageContentBuilder() {
        }

        public ImageContentBuilder height(int height) {
            this.height = height;
            return this;
        }

        public ImageContentBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ImageContentBuilder thumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
            return this;
        }

        public ImageContentBuilder width(int width) {
            this.width = width;
            return this;
        }

        public ImageContentBuilder thumbnailSize(int thumbnailSize) {
            this.thumbnailSize = thumbnailSize;
            return this;
        }

        public ImageContentBuilder thumbnailHeight(int thumbnailHeight) {
            this.thumbnailHeight = thumbnailHeight;
            return this;
        }

        public ImageContentBuilder thumbnailWidth(int thumbnailWidth) {
            this.thumbnailWidth = thumbnailWidth;
            return this;
        }

        public ImageContentBuilder url(String url) {
            this.url = url;
            return this;
        }

        public ImageContentBuilder size(int size) {
            this.size = size;
            return this;
        }

        public ImageContentBuilder MD5(String MD5) {
            this.MD5 = MD5;
            return this;
        }

        public ImageContentBuilder progress(String progress) {
            this.progress = progress;
            return this;
        }

        public ImageContentBuilder failedToLoad(boolean failedToLoad) {
            this.failedToLoad$value = failedToLoad;
            this.failedToLoad$set = true;
            return this;
        }

        public ImageContent build() {
            boolean failedToLoad$value = this.failedToLoad$value;
            if (!this.failedToLoad$set) {
                failedToLoad$value = ImageContent.$default$failedToLoad();
            }
            return new ImageContent(height, name, thumbnailUrl, width, thumbnailSize, thumbnailHeight, thumbnailWidth, url, size, MD5, progress, failedToLoad$value);
        }

        @NonNull
        public String toString() {
            return "ImageContent.ImageContentBuilder(height=" + this.height + ", name=" + this.name + ", thumbnailUrl=" + this.thumbnailUrl + ", width=" + this.width + ", thumbnailSize=" + this.thumbnailSize + ", thumbnailHeight=" + this.thumbnailHeight + ", thumbnailWidth=" + this.thumbnailWidth + ", url=" + this.url + ", size=" + this.size + ", MD5=" + this.MD5 + ", progress=" + this.progress + ", failedToLoad$value=" + this.failedToLoad$value + ")";
        }
    }
}
