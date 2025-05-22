package tw.com.chainsea.ce.sdk.bean.sticker;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.google.common.collect.ComparisonChain;

import java.io.Serializable;

import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.database.DBContract;

/**
 * current by evan on 2020-09-15
 *
 * @author Evan Wang
 * date 2020-09-15
 */
public class StickerItemEntity implements Serializable, Comparable<StickerItemEntity> {
    private static final long serialVersionUID = 6481046860263824975L;

    private String id;
    private String stickerPackageId;
    private String name;
    private String displayName;
    private int index;
    private String keywords;
    private String pictureId;
    private String pictureUrl;
    private String thumbnailPictureId;
    private String thumbnailPictureUrl;

    private long updateTime;

    StickerItemEntity(String id, String stickerPackageId, String name, String displayName, int index, String keywords, String pictureId, String pictureUrl, String thumbnailPictureId, String thumbnailPictureUrl, long updateTime) {
        this.id = id;
        this.stickerPackageId = stickerPackageId;
        this.name = name;
        this.displayName = displayName;
        this.index = index;
        this.keywords = keywords;
        this.pictureId = pictureId;
        this.pictureUrl = pictureUrl;
        this.thumbnailPictureId = thumbnailPictureId;
        this.thumbnailPictureUrl = thumbnailPictureUrl;
        this.updateTime = updateTime;
    }
//    @Builder.Default
//    private EnableType enable = EnableType.Y;


    public static StickerItemEntity formatByCursor(Cursor cursor) {
        return StickerItemEntity.Build()
            .id(Tools.getDbString(cursor, DBContract.StickerItemEntry._ID))
            .stickerPackageId(Tools.getDbString(cursor, DBContract.StickerItemEntry.COLUMN_STICKER_PACKAGE_ID))
            .name(Tools.getDbString(cursor, DBContract.StickerItemEntry.COLUMN_NAME))
            .displayName(Tools.getDbString(cursor, DBContract.StickerItemEntry.COLUMN_DISPLAY_NAME))
            .index(Tools.getDbInt(cursor, DBContract.StickerItemEntry.COLUMN_SORT_INDEX))
            .keywords(Tools.getDbString(cursor, DBContract.StickerItemEntry.COLUMN_KEYWORDS))
            .pictureId(Tools.getDbString(cursor, DBContract.StickerItemEntry.COLUMN_PICTURE_ID))
            .pictureUrl(Tools.getDbString(cursor, DBContract.StickerItemEntry.COLUMN_PICTURE_URL))
            .thumbnailPictureId(Tools.getDbString(cursor, DBContract.StickerItemEntry.COLUMN_THUMBNAIL_PICTURE_ID))
            .thumbnailPictureUrl(Tools.getDbString(cursor, DBContract.StickerItemEntry.COLUMN_THUMBNAIL_PICTURE_URL))
            .updateTime(Tools.getDbLong(cursor, DBContract.StickerItemEntry.COLUMN_UPDATE_TIME))
//                .enable(EnableType.valueOf(Tools.getDbString(cursor, DBContract.StickerItemEntry.COLUMN_IS_ENABLE)))
            .build();
    }

    public static ContentValues getContentValues(StickerItemEntity entity, long updateTime) {
        ContentValues values = new ContentValues();
        values.put(DBContract.StickerItemEntry._ID, entity.getId());
        values.put(DBContract.StickerItemEntry.COLUMN_STICKER_PACKAGE_ID, entity.getStickerPackageId());
        values.put(DBContract.StickerItemEntry.COLUMN_NAME, entity.getName());
        values.put(DBContract.StickerItemEntry.COLUMN_DISPLAY_NAME, entity.getDisplayName());
        values.put(DBContract.StickerItemEntry.COLUMN_SORT_INDEX, entity.getIndex());
        values.put(DBContract.StickerItemEntry.COLUMN_KEYWORDS, entity.getKeywords());
        values.put(DBContract.StickerItemEntry.COLUMN_PICTURE_ID, entity.getPictureId());
        values.put(DBContract.StickerItemEntry.COLUMN_PICTURE_URL, entity.getPictureUrl());
        values.put(DBContract.StickerItemEntry.COLUMN_THUMBNAIL_PICTURE_ID, entity.getThumbnailPictureId());
        values.put(DBContract.StickerItemEntry.COLUMN_THUMBNAIL_PICTURE_URL, entity.getThumbnailPictureUrl());

        values.put(DBContract.StickerItemEntry.COLUMN_UPDATE_TIME, updateTime);
//        values.put(DBContract.StickerItemEntry.COLUMN_IS_ENABLE, entity.getEnable().name());

        return values;
    }

    public static StickerItemEntityBuilder Build() {
        return new StickerItemEntityBuilder();
    }

    @Override
    public int compareTo(StickerItemEntity o) {
        return ComparisonChain.start()
            .compare(o.getIndex(), getIndex())
            .result();
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStickerPackageId() {
        return stickerPackageId;
    }

    public void setStickerPackageId(String stickerPackageId) {
        this.stickerPackageId = stickerPackageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getPictureId() {
        return pictureId;
    }

    public void setPictureId(String pictureId) {
        this.pictureId = pictureId;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getThumbnailPictureId() {
        return thumbnailPictureId;
    }

    public void setThumbnailPictureId(String thumbnailPictureId) {
        this.thumbnailPictureId = thumbnailPictureId;
    }

    public String getThumbnailPictureUrl() {
        return thumbnailPictureUrl;
    }

    public void setThumbnailPictureUrl(String thumbnailPictureUrl) {
        this.thumbnailPictureUrl = thumbnailPictureUrl;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public StickerItemEntityBuilder toBuilder() {
        return new StickerItemEntityBuilder().id(this.id).stickerPackageId(this.stickerPackageId).name(this.name).displayName(this.displayName).index(this.index).keywords(this.keywords).pictureId(this.pictureId).pictureUrl(this.pictureUrl).thumbnailPictureId(this.thumbnailPictureId).thumbnailPictureUrl(this.thumbnailPictureUrl).updateTime(this.updateTime);
    }

    public static class StickerItemEntityBuilder {
        private String id;
        private String stickerPackageId;
        private String name;
        private String displayName;
        private int index;
        private String keywords;
        private String pictureId;
        private String pictureUrl;
        private String thumbnailPictureId;
        private String thumbnailPictureUrl;
        private long updateTime;

        StickerItemEntityBuilder() {
        }

        public StickerItemEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public StickerItemEntityBuilder stickerPackageId(String stickerPackageId) {
            this.stickerPackageId = stickerPackageId;
            return this;
        }

        public StickerItemEntityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public StickerItemEntityBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public StickerItemEntityBuilder index(int index) {
            this.index = index;
            return this;
        }

        public StickerItemEntityBuilder keywords(String keywords) {
            this.keywords = keywords;
            return this;
        }

        public StickerItemEntityBuilder pictureId(String pictureId) {
            this.pictureId = pictureId;
            return this;
        }

        public StickerItemEntityBuilder pictureUrl(String pictureUrl) {
            this.pictureUrl = pictureUrl;
            return this;
        }

        public StickerItemEntityBuilder thumbnailPictureId(String thumbnailPictureId) {
            this.thumbnailPictureId = thumbnailPictureId;
            return this;
        }

        public StickerItemEntityBuilder thumbnailPictureUrl(String thumbnailPictureUrl) {
            this.thumbnailPictureUrl = thumbnailPictureUrl;
            return this;
        }

        public StickerItemEntityBuilder updateTime(long updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public StickerItemEntity build() {
            return new StickerItemEntity(id, stickerPackageId, name, displayName, index, keywords, pictureId, pictureUrl, thumbnailPictureId, thumbnailPictureUrl, updateTime);
        }

        @NonNull
        public String toString() {
            return "StickerItemEntity.StickerItemEntityBuilder(id=" + this.id + ", stickerPackageId=" + this.stickerPackageId + ", name=" + this.name + ", displayName=" + this.displayName + ", index=" + this.index + ", keywords=" + this.keywords + ", pictureId=" + this.pictureId + ", pictureUrl=" + this.pictureUrl + ", thumbnailPictureId=" + this.thumbnailPictureId + ", thumbnailPictureUrl=" + this.thumbnailPictureUrl + ", updateTime=" + this.updateTime + ")";
        }
    }

//    public EnableType getEnable() {
//        return enable;
//    }
//
//    public void setEnable(EnableType enable) {
//        this.enable = enable;
//    }
}
