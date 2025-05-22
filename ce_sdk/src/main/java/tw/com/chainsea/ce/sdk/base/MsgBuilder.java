package tw.com.chainsea.ce.sdk.base;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.bean.msg.MessageType;

/**
 * Created by 90Chris on 2016/6/8.
 */
public class MsgBuilder {
    private MessageType mType;
    private String mContent = null;
    private String mName = null;
    private String mTemplateId = null;
    private String mTitle = null;
    private String mText = null;
    private String mImageUrl = null;
    private String mAndroidLocalPath = null;
    private int mSize = -1;
    private int mThumbnailSize = -1;
    private int mThumbnailWidth = -1;
    private int mThumbnailHeight = -1;
    private int mWidth = -1;
    private int mHeight = -1;
    private String mUrl = null;
    private String mThumbnailUrl = null;
    private double mDuration = -1;
    private String mId;
    private String md5;

    private String mPackageId;


    public MsgBuilder(MessageType mType) {
        this.mType = mType;
    }

    // text message, the content param is String, not json
    public MsgBuilder content(String content) {
        mContent = content;
        return this;
    }

    public MsgBuilder name(String name) {
        mName = name;
        return this;
    }

    public MsgBuilder size(int size) {
        mSize = size;
        return this;
    }

    public MsgBuilder md5 (String md5) {
        this.md5 = md5;
        return this;
    }

    public MsgBuilder thumbnailSize(int thumbnailSize) {
        mThumbnailSize = thumbnailSize;
        return this;
    }

    public MsgBuilder width(int width) {
        mWidth = width;
        return this;
    }

    public MsgBuilder height(int height) {
        mHeight = height;
        return this;
    }

    public MsgBuilder url(String url) {
        mUrl = url;
        return this;
    }

    public MsgBuilder thumbnailUrl(String url) {
        mThumbnailUrl = url;
        return this;
    }

    public MsgBuilder thumbnailWidth(int width) {
        mThumbnailWidth = width;
        return this;
    }

    public MsgBuilder thumbnailHeight(int height) {
        mThumbnailHeight = height;
        return this;
    }

    public MsgBuilder duration(double duration) {
        mDuration = duration;
        return this;
    }

    public MsgBuilder id(String id) {
        mId = id;
        return this;
    }

    public MsgBuilder packageId(String packageId) {
        mPackageId = packageId;
        return this;
    }


    public MsgBuilder templateId(String templateId) {
        mTemplateId = templateId;
        return this;
    }

    public MsgBuilder title(String title) {
        mTitle = title;
        return this;
    }

    public MsgBuilder text(String text) {
        mText = text;
        return this;
    }

    public MsgBuilder imageUrl(String imageUrl) {
        mImageUrl = imageUrl;
        return this;
    }

    public MsgBuilder androidLocalPath(String androidLocalPath) {
        mAndroidLocalPath = androidLocalPath;
        return this;
    }

    public JSONObject build() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", mType.getValue());
        if (mContent != null) {
            jsonObject.put("content", mContent);
        } else {
            JSONObject content = new JSONObject();
            if (mName != null) {
                content.put("name", mName);
            }
            if (mSize >= 0) {
                content.put("size", mSize);
            }
            if (mThumbnailSize >= 0) {
                content.put("thumbnailSize", mThumbnailSize);
            }
            if (mWidth >= 0) {
                content.put("width", mWidth);
            }
            if (mHeight >= 0) {
                content.put("height", mHeight);
            }
            if (mUrl != null) {
                content.put("url", mUrl);
            }
            if (mThumbnailUrl != null) {
                content.put("thumbnailUrl", mThumbnailUrl);
            }
            if (mThumbnailWidth > 0) {
                content.put("thumbnailWidth", mThumbnailWidth);
            }

            if (mThumbnailHeight > 0) {
                content.put("thumbnailHeight", mThumbnailHeight);
            }

            if (mDuration >= 0) {
                content.put("duration", mDuration);
            }
            if (mId != null) {
                content.put("id", mId);
            }
            if (mTitle != null) {
                content.put("title", mTitle);
            }
            if (mTemplateId != null) {
                content.put("templateId", mTemplateId);
            }
            if (mText != null) {
                content.put("text", mText);
            }
            if (mAndroidLocalPath != null) {
                content.put("android_local_path", mAndroidLocalPath);
            }
            if (mImageUrl != null) {
                content.put("imageUrl", mImageUrl);
            }
            if (md5 != null) {
                content.put("MD5", md5);
            }


            if (mPackageId != null) {
                content.put("packageId", mPackageId);
            }
            jsonObject.put("content", content.toString()); //为了存到mongodb，这里把json对象改成String
        }
        return jsonObject;
    }
}
