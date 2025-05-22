package tw.com.chainsea.ce.sdk.bean;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;

/**
 * Created by sunhui on 2017/5/12.
 */
public class CrowdEntity implements Serializable, Cloneable {
    private String id;
    private String name;
    private String ownerId;
    private String avatarId;
    private boolean isCustomName;
    private String kind;
    private String avatarUrl;
    private List<UserProfileEntity> users;
    private long lastMessageTime;
    private long createTime;
    private List<UserProfileEntity> members;
    private long updateTime;
    private ChatRoomType type;
    private long saveTime; ////用來判斷儲存搜尋頭圖時間
    private boolean isSelected = false; //Adapter是否有被點選

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    private List<UserProfileEntity> memberArray;

    public CrowdEntity(String id, String name, String ownerId, String avatarId, boolean isCustomName, String kind, String avatarUrl, List<UserProfileEntity> users, ChatRoomType type) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.avatarId = avatarId;
        this.isCustomName = isCustomName;
        this.kind = kind;
        this.avatarUrl = avatarUrl;
        this.memberArray = users;
        this.type = type;
    }

    public CrowdEntity() {
    }

    public static CrowdEntityBuilder Build() {
        return new CrowdEntityBuilder();
    }

    public boolean isAddHardCode() {
        return "isAdd".equals(id) && "建立社團".equals(name) && "與好友建立社團".equals(ownerId);
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
        CrowdEntity other = (CrowdEntity) obj;
        if (this.id == null || other.getId() == null) {
            return false;
        } else if (this.isSelected != ((CrowdEntity) obj).isSelected) {
            return false;
        } else return this.id.equals(other.getId());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public boolean isCustomName() {
        return isCustomName;
    }

    public void setCustomName(boolean customName) {
        isCustomName = customName;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<UserProfileEntity> getMemberArray() {
        return memberArray;
    }

    public void setMemberArray(List<UserProfileEntity> memberArray) {
        this.memberArray = memberArray;
    }

    public long getCreateTime() {
        return createTime;
    }

    public List<UserProfileEntity> getMembers() {
        return members;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setSaveTime(long saveTime) {
        this.saveTime = saveTime;
    }

    public long getSaveTime() {
        return saveTime;
    }

    public ChatRoomType getType() {
        return type;
    }

    public void setType(ChatRoomType type) {
        this.type = type;
    }

    @Override
    @NonNull
    public CrowdEntity clone() {
        try {
            return (CrowdEntity) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public CrowdEntityBuilder toBuilder() {
        return new CrowdEntityBuilder().id(this.id).name(this.name).ownerId(this.ownerId).avatarId(this.avatarId).isCustomName(this.isCustomName).kind(this.kind).avatarUrl(this.avatarUrl).users(this.memberArray).type(this.type);
    }

    public static class CrowdEntityBuilder {
        private String id;
        private String name;
        private String ownerId;
        private String avatarId;
        private boolean isCustomName;
        private String kind;
        private String avatarUrl;
        private List<UserProfileEntity> users;
        private ChatRoomType type;

        CrowdEntityBuilder() {
        }

        public CrowdEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public CrowdEntityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CrowdEntityBuilder ownerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public CrowdEntityBuilder avatarId(String avatarId) {
            this.avatarId = avatarId;
            return this;
        }

        public CrowdEntityBuilder isCustomName(boolean isCustomName) {
            this.isCustomName = isCustomName;
            return this;
        }

        public CrowdEntityBuilder kind(String kind) {
            this.kind = kind;
            return this;
        }

        public CrowdEntityBuilder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public CrowdEntityBuilder users(List<UserProfileEntity> users) {
            this.users = users;
            return this;
        }

        public CrowdEntityBuilder type(ChatRoomType type) {
            this.type = type;
            return this;
        }

        public CrowdEntity build() {
            return new CrowdEntity(id, name, ownerId, avatarId, isCustomName, kind, avatarUrl, users, type);
        }

        @NonNull
        public String toString() {
            return "CrowdEntity.CrowdEntityBuilder(id=" + this.id + ", name=" + this.name + ", ownerId=" + this.ownerId + ", avatarId=" + this.avatarId + ", isCustomName=" + this.isCustomName + ", kind=" + this.kind + ", avatarUrl=" + this.avatarUrl + ", users=" + this.users + ", type=" + this.type + ")";
        }
    }
}
