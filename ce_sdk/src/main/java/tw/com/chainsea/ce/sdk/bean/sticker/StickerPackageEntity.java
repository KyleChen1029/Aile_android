package tw.com.chainsea.ce.sdk.bean.sticker;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

import tw.com.chainsea.ce.sdk.bean.common.EnableType;
import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.database.DBContract;

/**
 * current by evan on 2020-09-15
 *
 * @author Evan Wang
 * @date 2020-09-15
 */
public class StickerPackageEntity implements Serializable, Comparable<StickerPackageEntity> {
    private static final long serialVersionUID = -6262926720025031625L;

    private String id;
    private String name;
    private String iconId;
    private String iconUrl;
    private long joinTime;


    //local
    private EmoticonType emoticonType = EmoticonType.STICKER;
    private StickerSelectAction action = StickerSelectAction.UNDEF; // select action
    private int count; // item total count
    private int columns; // Show the number of columns // columns
    private int row; // Display line number
    private List<StickerItemEntity> stickerItems = Lists.newArrayList();
    private EnableType enable = EnableType.Y;

    public StickerPackageEntity(String id, String name, String iconId, String iconUrl, long joinTime, EmoticonType emoticonType, StickerSelectAction action, int count, int columns, int row, List<StickerItemEntity> stickerItems, EnableType enable) {
        this.id = id;
        this.name = name;
        this.iconId = iconId;
        this.iconUrl = iconUrl;
        this.joinTime = joinTime;
        this.emoticonType = emoticonType;
        this.action = action;
        this.count = count;
        this.columns = columns;
        this.row = row;
        this.stickerItems = stickerItems;
        this.enable = enable;
    }

    public StickerPackageEntity() {
    }


    public static StickerPackageEntity.StickerPackageEntityBuilder formatByCursor(Cursor cursor, List<StickerItemEntity> stickerItems) {
        EnableType t = EnableType.valueOf(Tools.getDbString(cursor, DBContract.StickerPackageEntry.COLUMN_IS_ENABLE));
        return StickerPackageEntity.Build()
            .id(Tools.getDbString(cursor, DBContract.StickerPackageEntry._ID))
            .name(Tools.getDbString(cursor, DBContract.StickerPackageEntry.COLUMN_PACKAGE_NAME))
            .iconId(Tools.getDbString(cursor, DBContract.StickerPackageEntry.COLUMN_ICON_ID))
            .iconUrl(Tools.getDbString(cursor, DBContract.StickerPackageEntry.COLUMN_ICON_URL))
            .joinTime(Tools.getDbLong(cursor, DBContract.StickerPackageEntry.COLUMN_JOIN_TIME))

            .action(StickerSelectAction.valueOf(Tools.getDbString(cursor, DBContract.StickerPackageEntry.COLUMN_SELECT_ACTION)))
            .emoticonType(EmoticonType.valueOf(Tools.getDbString(cursor, DBContract.StickerPackageEntry.COLUMN_EMOTICON_TYPE)))

            .count(Tools.getDbInt(cursor, DBContract.StickerPackageEntry.COLUMN_ITEM_COUNT))
            .columns(Tools.getDbInt(cursor, DBContract.StickerPackageEntry.COLUMN_ITEM_LINE))
            .row(Tools.getDbInt(cursor, DBContract.StickerPackageEntry.COLUMN_ITEM_ROW))
            .stickerItems(stickerItems)
            .enable(EnableType.valueOf(Tools.getDbString(cursor, DBContract.StickerPackageEntry.COLUMN_IS_ENABLE)));
    }


    public static ContentValues getContentValues(StickerPackageEntity entity, long updateTime, EmoticonType type) {
        ContentValues values = new ContentValues();
        values.put(DBContract.StickerPackageEntry._ID, entity.getId());
        values.put(DBContract.StickerPackageEntry.COLUMN_PACKAGE_NAME, entity.getName());
        values.put(DBContract.StickerPackageEntry.COLUMN_ICON_ID, entity.getIconId());
        values.put(DBContract.StickerPackageEntry.COLUMN_ICON_URL, entity.getIconUrl());
        values.put(DBContract.StickerPackageEntry.COLUMN_JOIN_TIME, entity.getJoinTime());

        values.put(DBContract.StickerPackageEntry.COLUMN_UPDATE_TIME, updateTime);
        values.put(DBContract.StickerPackageEntry.COLUMN_EMOTICON_TYPE, type.name());

        values.put(DBContract.StickerPackageEntry.COLUMN_ITEM_COUNT, entity.getColumns() * entity.getRow());
        values.put(DBContract.StickerPackageEntry.COLUMN_ITEM_LINE, entity.getColumns());
        values.put(DBContract.StickerPackageEntry.COLUMN_ITEM_ROW, entity.getRow());
        values.put(DBContract.StickerPackageEntry.COLUMN_SELECT_ACTION, entity.getAction().name());
        values.put(DBContract.StickerPackageEntry.COLUMN_IS_ENABLE, entity.getEnable().name());

        return values;
    }

    private static EmoticonType $default$emoticonType() {
        return EmoticonType.STICKER;
    }

    private static StickerSelectAction $default$action() {
        return StickerSelectAction.UNDEF;
    }

    private static List<StickerItemEntity> $default$stickerItems() {
        return Lists.newArrayList();
    }

    private static EnableType $default$enable() {
        return EnableType.Y;
    }

    public static StickerPackageEntityBuilder Build() {
        return new StickerPackageEntityBuilder();
    }


    @Override
    public int compareTo(StickerPackageEntity o) {
        return ComparisonChain.start()
            .compare(getJoinTime(), o.getJoinTime())
            .result();
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getIconId() {
        return this.iconId;
    }

    public String getIconUrl() {
        return this.iconUrl;
    }

    public long getJoinTime() {
        return this.joinTime;
    }

    public EmoticonType getEmoticonType() {
        return this.emoticonType;
    }

    public StickerSelectAction getAction() {
        return this.action;
    }

    public int getCount() {
        return this.count;
    }

    public int getColumns() {
        return this.columns;
    }

    public int getRow() {
        return this.row;
    }

    public List<StickerItemEntity> getStickerItems() {
        return this.stickerItems;
    }

    public EnableType getEnable() {
        return this.enable;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIconId(String iconId) {
        this.iconId = iconId;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }

    public void setEmoticonType(EmoticonType emoticonType) {
        this.emoticonType = emoticonType;
    }

    public void setAction(StickerSelectAction action) {
        this.action = action;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setStickerItems(List<StickerItemEntity> stickerItems) {
        this.stickerItems = stickerItems;
    }

    public void setEnable(EnableType enable) {
        this.enable = enable;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof StickerPackageEntity)) return false;
        final StickerPackageEntity other = (StickerPackageEntity) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$iconId = this.getIconId();
        final Object other$iconId = other.getIconId();
        if (this$iconId == null ? other$iconId != null : !this$iconId.equals(other$iconId))
            return false;
        final Object this$iconUrl = this.getIconUrl();
        final Object other$iconUrl = other.getIconUrl();
        if (this$iconUrl == null ? other$iconUrl != null : !this$iconUrl.equals(other$iconUrl))
            return false;
        if (this.getJoinTime() != other.getJoinTime()) return false;
        final Object this$emoticonType = this.getEmoticonType();
        final Object other$emoticonType = other.getEmoticonType();
        if (this$emoticonType == null ? other$emoticonType != null : !this$emoticonType.equals(other$emoticonType))
            return false;
        final Object this$action = this.getAction();
        final Object other$action = other.getAction();
        if (this$action == null ? other$action != null : !this$action.equals(other$action))
            return false;
        if (this.getCount() != other.getCount()) return false;
        if (this.getColumns() != other.getColumns()) return false;
        if (this.getRow() != other.getRow()) return false;
        final Object this$stickerItems = this.getStickerItems();
        final Object other$stickerItems = other.getStickerItems();
        if (this$stickerItems == null ? other$stickerItems != null : !this$stickerItems.equals(other$stickerItems))
            return false;
        final Object this$enable = this.getEnable();
        final Object other$enable = other.getEnable();
        return this$enable == null ? other$enable == null : this$enable.equals(other$enable);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof StickerPackageEntity;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $iconId = this.getIconId();
        result = result * PRIME + ($iconId == null ? 43 : $iconId.hashCode());
        final Object $iconUrl = this.getIconUrl();
        result = result * PRIME + ($iconUrl == null ? 43 : $iconUrl.hashCode());
        final long $joinTime = this.getJoinTime();
        result = result * PRIME + (int) ($joinTime >>> 32 ^ $joinTime);
        final Object $emoticonType = this.getEmoticonType();
        result = result * PRIME + ($emoticonType == null ? 43 : $emoticonType.hashCode());
        final Object $action = this.getAction();
        result = result * PRIME + ($action == null ? 43 : $action.hashCode());
        result = result * PRIME + this.getCount();
        result = result * PRIME + this.getColumns();
        result = result * PRIME + this.getRow();
        final Object $stickerItems = this.getStickerItems();
        result = result * PRIME + ($stickerItems == null ? 43 : $stickerItems.hashCode());
        final Object $enable = this.getEnable();
        result = result * PRIME + ($enable == null ? 43 : $enable.hashCode());
        return result;
    }

    @NonNull
    public String toString() {
        return "StickerPackageEntity(id=" + this.getId() + ", name=" + this.getName() + ", iconId=" + this.getIconId() + ", iconUrl=" + this.getIconUrl() + ", joinTime=" + this.getJoinTime() + ", emoticonType=" + this.getEmoticonType() + ", action=" + this.getAction() + ", count=" + this.getCount() + ", columns=" + this.getColumns() + ", row=" + this.getRow() + ", stickerItems=" + this.getStickerItems() + ", enable=" + this.getEnable() + ")";
    }

    public StickerPackageEntityBuilder toBuilder() {
        return new StickerPackageEntityBuilder().id(this.id).name(this.name).iconId(this.iconId).iconUrl(this.iconUrl).joinTime(this.joinTime).emoticonType(this.emoticonType).action(this.action).count(this.count).columns(this.columns).row(this.row).stickerItems(this.stickerItems).enable(this.enable);
    }

    public static class StickerPackageEntityBuilder {
        private String id;
        private String name;
        private String iconId;
        private String iconUrl;
        private long joinTime;
        private EmoticonType emoticonType$value;
        private boolean emoticonType$set;
        private StickerSelectAction action$value;
        private boolean action$set;
        private int count;
        private int columns;
        private int row;
        private List<StickerItemEntity> stickerItems$value;
        private boolean stickerItems$set;
        private EnableType enable$value;
        private boolean enable$set;

        StickerPackageEntityBuilder() {
        }

        public StickerPackageEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public StickerPackageEntityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public StickerPackageEntityBuilder iconId(String iconId) {
            this.iconId = iconId;
            return this;
        }

        public StickerPackageEntityBuilder iconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        public StickerPackageEntityBuilder joinTime(long joinTime) {
            this.joinTime = joinTime;
            return this;
        }

        public StickerPackageEntityBuilder emoticonType(EmoticonType emoticonType) {
            this.emoticonType$value = emoticonType;
            this.emoticonType$set = true;
            return this;
        }

        public StickerPackageEntityBuilder action(StickerSelectAction action) {
            this.action$value = action;
            this.action$set = true;
            return this;
        }

        public StickerPackageEntityBuilder count(int count) {
            this.count = count;
            return this;
        }

        public StickerPackageEntityBuilder columns(int columns) {
            this.columns = columns;
            return this;
        }

        public StickerPackageEntityBuilder row(int row) {
            this.row = row;
            return this;
        }

        public StickerPackageEntityBuilder stickerItems(List<StickerItemEntity> stickerItems) {
            this.stickerItems$value = stickerItems;
            this.stickerItems$set = true;
            return this;
        }

        public StickerPackageEntityBuilder enable(EnableType enable) {
            this.enable$value = enable;
            this.enable$set = true;
            return this;
        }

        public StickerPackageEntity build() {
            EmoticonType emoticonType$value = this.emoticonType$value;
            if (!this.emoticonType$set) {
                emoticonType$value = StickerPackageEntity.$default$emoticonType();
            }
            StickerSelectAction action$value = this.action$value;
            if (!this.action$set) {
                action$value = StickerPackageEntity.$default$action();
            }
            List<StickerItemEntity> stickerItems$value = this.stickerItems$value;
            if (!this.stickerItems$set) {
                stickerItems$value = StickerPackageEntity.$default$stickerItems();
            }
            EnableType enable$value = this.enable$value;
            if (!this.enable$set) {
                enable$value = StickerPackageEntity.$default$enable();
            }
            return new StickerPackageEntity(id, name, iconId, iconUrl, joinTime, emoticonType$value, action$value, count, columns, row, stickerItems$value, enable$value);
        }

        @NonNull
        public String toString() {
            return "StickerPackageEntity.StickerPackageEntityBuilder(id=" + this.id + ", name=" + this.name + ", iconId=" + this.iconId + ", iconUrl=" + this.iconUrl + ", joinTime=" + this.joinTime + ", emoticonType$value=" + this.emoticonType$value + ", action$value=" + this.action$value + ", count=" + this.count + ", columns=" + this.columns + ", row=" + this.row + ", stickerItems$value=" + this.stickerItems$value + ", enable$value=" + this.enable$value + ")";
        }
    }
}
