package tw.com.chainsea.ce.sdk.http.ce.model;

import java.io.Serializable;

public class Scope implements Serializable{
    private String nickName; //渠道显示昵称
    private String avatarId; //渠道显示头像ID
    private String identifyBy; //渠道类型key
    private String identifyValue; //渠道scopeId
    private String tenantId; //租户ID

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public String getIdentifyBy() {
        return identifyBy;
    }

    public void setIdentifyBy(String identifyBy) {
        this.identifyBy = identifyBy;
    }

    public String getIdentifyValue() {
        return identifyValue;
    }

    public void setIdentifyValue(String identifyValue) {
        this.identifyValue = identifyValue;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
