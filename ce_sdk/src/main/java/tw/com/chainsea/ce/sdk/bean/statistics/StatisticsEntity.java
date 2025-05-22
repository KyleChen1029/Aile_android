package tw.com.chainsea.ce.sdk.bean.statistics;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.google.common.collect.ComparisonChain;

import java.io.Serializable;

import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.database.DBContract;

/**
 * current by evan on 2020-09-18
 * <p>
 * public static final String COLUMN_RELATION_ID = "rel_id";
 * public static final String COLUMN_ASCRIPTION = "ascription";
 * public static final String COLUMN_ORIGINAL_CONTENT = "original_content";
 * public static final String COLUMN_TOTAL_ROW = "total_row";
 * public static final String COLUMN_SCORE = "score";
 * public static final String COLUMN_STAT_TYPE = "stat_type";
 * public static final String COLUMN_UPDATE_TIME = "update_time";
 *
 * @author Evan Wang
 * @date 2020-09-18
 */

public class StatisticsEntity implements Serializable, Comparable<StatisticsEntity> {
    private static final long serialVersionUID = 6723873609011701050L;

    private int _id;
    private String relId;
    private String ascription;
    private String originalContent;
    private int totalRow;
    private int rowCount;
    private String statType;
    private long startTime;
    private long endTime;
    private long updateTime;

    public StatisticsEntity(int _id, String relId, String ascription, String originalContent, int totalRow, int rowCount, String statType, long startTime, long endTime, long updateTime) {
        this._id = _id;
        this.relId = relId;
        this.ascription = ascription;
        this.originalContent = originalContent;
        this.totalRow = totalRow;
        this.rowCount = rowCount;
        this.statType = statType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.updateTime = updateTime;
    }

    public StatisticsEntity() {
    }


    public static StatisticsEntity.StatisticsEntityBuilder formatByCursor(Cursor cursor) {
        StatisticsEntity.StatisticsEntityBuilder build = StatisticsEntity.Build()
            ._id(Tools.getDbInt(cursor, DBContract.StatisticsEntry._ID))
            .relId(Tools.getDbString(cursor, DBContract.StatisticsEntry.COLUMN_RELATION_ID))
            .ascription(Tools.getDbString(cursor, DBContract.StatisticsEntry.COLUMN_ASCRIPTION))
            .originalContent(Tools.getDbString(cursor, DBContract.StatisticsEntry.COLUMN_ORIGINAL_CONTENT))
            .totalRow(Tools.getDbInt(cursor, DBContract.StatisticsEntry.COLUMN_TOTAL_ROW))
            .rowCount(Tools.getDbInt(cursor, DBContract.StatisticsEntry.COLUMN_ROW_COUNT))
            .statType(Tools.getDbString(cursor, DBContract.StatisticsEntry.COLUMN_STAT_TYPE))
            .startTime(Tools.getDbLong(cursor, DBContract.StatisticsEntry.COLUMN_START_TIME))
            .endTime(Tools.getDbLong(cursor, DBContract.StatisticsEntry.COLUMN_END_TIME))
            .updateTime(Tools.getDbLong(cursor, DBContract.StatisticsEntry.COLUMN_UPDATE_TIME));
        return build;
    }


    public static ContentValues getContentValues(StatisticsEntity entity, long updateTime) {
        ContentValues values = new ContentValues();
        values.put(DBContract.StatisticsEntry.COLUMN_RELATION_ID, entity.getRelId());
        values.put(DBContract.StatisticsEntry.COLUMN_ASCRIPTION, entity.getAscription());
        values.put(DBContract.StatisticsEntry.COLUMN_ORIGINAL_CONTENT, entity.getOriginalContent());
        values.put(DBContract.StatisticsEntry.COLUMN_TOTAL_ROW, entity.getTotalRow());
        values.put(DBContract.StatisticsEntry.COLUMN_ROW_COUNT, entity.getRowCount());
        values.put(DBContract.StatisticsEntry.COLUMN_STAT_TYPE, entity.getStatType());
        values.put(DBContract.StatisticsEntry.COLUMN_START_TIME, entity.getStartTime());
        values.put(DBContract.StatisticsEntry.COLUMN_END_TIME, entity.getEndTime());
        values.put(DBContract.StatisticsEntry.COLUMN_UPDATE_TIME, updateTime);
        return values;
    }

    public static StatisticsEntityBuilder Build() {
        return new StatisticsEntityBuilder();
    }


    public String uniqueId() {
        return this.relId + "_" + this.statType;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uniqueId() == null) ? 0 : uniqueId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatisticsEntity other = (StatisticsEntity) obj;
        if (this.uniqueId() == null || other.uniqueId() == null) {
            return false;
        } else return this.uniqueId().equals(other.uniqueId());
    }

    public int getWeights() {
        try {
            return ServiceNumberStatType.valueOf(this.statType).getIndex();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int compareTo(StatisticsEntity o) {
        return ComparisonChain.start()
            .compare(this.getWeights(), o.getWeights())
            .result();
    }

    public int get_id() {
        return this._id;
    }

    public String getRelId() {
        return this.relId;
    }

    public String getAscription() {
        return this.ascription;
    }

    public String getOriginalContent() {
        return this.originalContent;
    }

    public int getTotalRow() {
        return this.totalRow;
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public String getStatType() {
        return this.statType;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public long getUpdateTime() {
        return this.updateTime;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setRelId(String relId) {
        this.relId = relId;
    }

    public void setAscription(String ascription) {
        this.ascription = ascription;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public void setTotalRow(int totalRow) {
        this.totalRow = totalRow;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public void setStatType(String statType) {
        this.statType = statType;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @NonNull
    public String toString() {
        return "StatisticsEntity(_id=" + this.get_id() + ", relId=" + this.getRelId() + ", ascription=" + this.getAscription() + ", originalContent=" + this.getOriginalContent() + ", totalRow=" + this.getTotalRow() + ", rowCount=" + this.getRowCount() + ", statType=" + this.getStatType() + ", startTime=" + this.getStartTime() + ", endTime=" + this.getEndTime() + ", updateTime=" + this.getUpdateTime() + ")";
    }

    public StatisticsEntityBuilder toBuilder() {
        return new StatisticsEntityBuilder()._id(this._id).relId(this.relId).ascription(this.ascription).originalContent(this.originalContent).totalRow(this.totalRow).rowCount(this.rowCount).statType(this.statType).startTime(this.startTime).endTime(this.endTime).updateTime(this.updateTime);
    }

    public static class StatisticsEntityBuilder {
        private int _id;
        private String relId;
        private String ascription;
        private String originalContent;
        private int totalRow;
        private int rowCount;
        private String statType;
        private long startTime;
        private long endTime;
        private long updateTime;

        StatisticsEntityBuilder() {
        }

        public StatisticsEntityBuilder _id(int _id) {
            this._id = _id;
            return this;
        }

        public StatisticsEntityBuilder relId(String relId) {
            this.relId = relId;
            return this;
        }

        public StatisticsEntityBuilder ascription(String ascription) {
            this.ascription = ascription;
            return this;
        }

        public StatisticsEntityBuilder originalContent(String originalContent) {
            this.originalContent = originalContent;
            return this;
        }

        public StatisticsEntityBuilder totalRow(int totalRow) {
            this.totalRow = totalRow;
            return this;
        }

        public StatisticsEntityBuilder rowCount(int rowCount) {
            this.rowCount = rowCount;
            return this;
        }

        public StatisticsEntityBuilder statType(String statType) {
            this.statType = statType;
            return this;
        }

        public StatisticsEntityBuilder startTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public StatisticsEntityBuilder endTime(long endTime) {
            this.endTime = endTime;
            return this;
        }

        public StatisticsEntityBuilder updateTime(long updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public StatisticsEntity build() {
            return new StatisticsEntity(_id, relId, ascription, originalContent, totalRow, rowCount, statType, startTime, endTime, updateTime);
        }

        @NonNull
        public String toString() {
            return "StatisticsEntity.StatisticsEntityBuilder(_id=" + this._id + ", relId=" + this.relId + ", ascription=" + this.ascription + ", originalContent=" + this.originalContent + ", totalRow=" + this.totalRow + ", rowCount=" + this.rowCount + ", statType=" + this.statType + ", startTime=" + this.startTime + ", endTime=" + this.endTime + ", updateTime=" + this.updateTime + ")";
        }
    }
}
