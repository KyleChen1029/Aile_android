package tw.com.chainsea.ce.sdk.bean.broadcast;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;

import java.io.Serializable;

import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.database.DBContract;

/**
 * current by evan on 2020-07-31
 *
 * @author Evan Wang
 * date 2020-07-31
 */

public class TopicEntity implements Serializable {
    private static final long serialVersionUID = -584517726960490751L;
    String id;
    String avatarId;
    String description;
    String name;

    TopicEntity(String id, String avatarId, String description, String name) {
        this.id = id;
        this.avatarId = avatarId;
        this.description = description;
        this.name = name;
    }

    public static TopicEntityBuilder Build() {
        return new TopicEntityBuilder();
    }

    public String getId() {
        return id;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public boolean isHardCode() {
        return "00000000-0000-0000-0000-000000000000".equals(this.id);
    }

    public static TopicEntity newHardCode() {
        return TopicEntity.Build().id("00000000-0000-0000-0000-000000000000").build();
    }

    public int getWeights() {
        if (Strings.isNullOrEmpty(name)) {
            return 101;
        }
        switch (name) {
            case "全部訂閱":
                return 0;
            case "外部訂閱":
                return 1;
            case "內部訂閱":
                return 2;
            default:
                return 100;
        }
    }

    public static TopicEntity.TopicEntityBuilder formatByCursor(Cursor cursor) {
        return TopicEntity.Build()
            .id(Tools.getDbString(cursor, DBContract.BroadcastTopicEntry._ID))
            .avatarId(Tools.getDbString(cursor, DBContract.BroadcastTopicEntry.COLUMN_AVATAR_ID))
            .name(Tools.getDbString(cursor, DBContract.BroadcastTopicEntry.COLUMN_NAME))
            .description(Tools.getDbString(cursor, DBContract.BroadcastTopicEntry.COLUMN_DESCRIPTION));
    }

    public static ContentValues getContentValues(TopicEntity entity) {
        ContentValues values = new ContentValues();
        values.put(DBContract.BroadcastTopicEntry._ID, entity.getId());
        values.put(DBContract.BroadcastTopicEntry.COLUMN_AVATAR_ID, entity.getAvatarId());
        values.put(DBContract.BroadcastTopicEntry.COLUMN_NAME, entity.getName());
        values.put(DBContract.BroadcastTopicEntry.COLUMN_DESCRIPTION, entity.getDescription());
        return values;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        TopicEntity other = (TopicEntity) obj;
        if (this.id == null || other.getId() == null) {
            return false;
        } else return this.id.equals(other.getId());
    }

    public TopicEntityBuilder toBuilder() {
        return new TopicEntityBuilder().id(this.id).avatarId(this.avatarId).description(this.description).name(this.name);
    }

    public static class TopicEntityBuilder {
        private String id;
        private String avatarId;
        private String description;
        private String name;

        TopicEntityBuilder() {
        }

        public TopicEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public TopicEntityBuilder avatarId(String avatarId) {
            this.avatarId = avatarId;
            return this;
        }

        public TopicEntityBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TopicEntityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TopicEntity build() {
            return new TopicEntity(id, avatarId, description, name);
        }

        @NonNull
        public String toString() {
            return "TopicEntity.TopicEntityBuilder(id=" + this.id + ", avatarId=" + this.avatarId + ", description=" + this.description + ", name=" + this.name + ")";
        }
    }
}
