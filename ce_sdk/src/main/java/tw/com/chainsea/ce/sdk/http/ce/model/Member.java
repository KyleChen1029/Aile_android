package tw.com.chainsea.ce.sdk.http.ce.model;

import com.google.gson.annotations.SerializedName;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import org.json.JSONObject;

import java.io.Serializable;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.ServiceNumberPrivilege;

public class Member implements Serializable {
    @SerializedName(value = "id", alternate = {"memberId"})
    private String id; //成员用户ID
    @SerializedName("privilege")
    private ServiceNumberPrivilege privilege = ServiceNumberPrivilege.UNDEF; //成员权限，可为：Owner、Manager、Common
    private long joinTime;

    public long getJoinTime() {
        return joinTime;
    }

    public ServiceNumberPrivilege getPrivilege() {
        return privilege;
    }

    public void setPrivilege(ServiceNumberPrivilege privilege) {
        this.privilege = privilege;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        Member other = (Member) obj;
        if (this.id == null) {
            return other.getId() == null;
        } else return this.getId().equals(other.getId());
    }

    public static class MemberTypeAdapter {
        @ToJson
        String toJson(Member member) {
            return JsonHelper.getInstance().toJson(member);
        }

        @FromJson
        Member fromJson(Object object) {
            Member member = new Member();
            try {
                JSONObject jsonObject = new JSONObject(JsonHelper.getInstance().toJson(object));
                if (jsonObject.has("id")) {
                    member.setId(jsonObject.optString("id"));
                } else if (jsonObject.has("memberId")) {
                    member.setId(jsonObject.optString("memberId"));
                }
                member.setPrivilege(ServiceNumberPrivilege.of(jsonObject.optString("privilege")));
                return member;
            } catch (Exception e) {

            }

            return member;
        }
    }
}
