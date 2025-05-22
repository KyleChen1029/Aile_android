package tw.com.chainsea.ce.sdk.bean.label;

import java.io.Serializable;
import java.util.List;

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;

/**
 * Created by sunhui on 2018/3/5.
 */
public class Label implements Serializable {
    private String id; //标签ID
    private String name; //标签名称
    private List<String> userIds; //标签成员ID数组
    private List<UserProfileEntity> users;
    private long createTime; //标签创建时间
    private String ownerId; //创建者
    private boolean readOnly; //是否是我的收藏标签
    private boolean deleted; //是否已被删除

    public Label() {}

    public Label(String id, String name, List<String> userIds, long createTime, String ownerId, boolean readOnly, boolean deleted) {
        this.id = id;
        this.name = name;
        this.userIds = userIds;
        this.createTime = createTime;
        this.ownerId = ownerId;
        this.readOnly = readOnly;
        this.deleted = deleted;
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

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public List<UserProfileEntity> getUsers() {
        return users;
    }

    public void setUsers(List<UserProfileEntity> users) {
        this.users = users;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isAddHardCode() {
        return "00000000-0000-0000-0000-000000000000".equals(id) && "新增標籤".equals(name) && "為聯絡人建立標籤".equals(ownerId) ;
    }

    public static Label newAddCell() {
        Label label = new Label();
        label.setId("00000000-0000-0000-0000-000000000000");
        label.setName("新增標籤");
        label.setOwnerId("為聯絡人建立標籤");
        return label;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Label other = (Label) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equalsIgnoreCase(other.id))
            return false;
        if (name == null) {
            return other.name == null;
        } else return name.equalsIgnoreCase(other.name);
    }
}
