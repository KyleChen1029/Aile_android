package tw.com.chainsea.ce.sdk.bean.tenant;

import androidx.annotation.NonNull;

import com.google.common.collect.ComparisonChain;

import java.io.Serializable;

/**
 * current by evan on 12/14/20
 *
 * @author Evan Wang
 * date 12/14/20
 */
public class EnvironmentInfo implements Serializable, Comparable<EnvironmentInfo> {
    private static final long serialVersionUID = 2540967570994269620L;


    private String id;
    private String code;
    private String url;
    private String name;
    private String userId;
    private String accountNum;
    private String accountPsw;
    private long joinTime;

    public EnvironmentInfo(String id, String code, String url, String name, String userId, String accountNum, String accountPsw, long joinTime) {
        this.id = id;
        this.code = code;
        this.url = url;
        this.name = name;
        this.userId = userId;
        this.accountNum = accountNum;
        this.accountPsw = accountPsw;
        this.joinTime = joinTime;
    }

    @Override
    @NonNull
    public String toString() {
        return "EnvironmentInfo{" +
            "id='" + id + '\'' +
            ", code='" + code + '\'' +
            ", url='" + url + '\'' +
            ", name='" + name + '\'' +
            ", joinTime=" + joinTime +
            ", accountId='" + accountNum + '\'' +
            ", accountPsw='" + accountPsw + '\'' +
            '}';
    }

    @Override
    public int compareTo(@NonNull EnvironmentInfo o) {
        return ComparisonChain.start()
            .compare(o.getJoinTime(), this.getJoinTime())
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccountNum() {
        return accountNum;
    }

    public void setAccountNum(String accountNum) {
        this.accountNum = accountNum;
    }

    public String getAccountPsw() {
        return accountPsw;
    }

    public void setAccountPsw(String accountPsw) {
        this.accountPsw = accountPsw;
    }
}
