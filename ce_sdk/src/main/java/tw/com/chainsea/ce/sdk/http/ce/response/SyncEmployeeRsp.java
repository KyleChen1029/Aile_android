package tw.com.chainsea.ce.sdk.http.ce.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.http.ce.model.Result;

public class SyncEmployeeRsp implements Serializable {
    private boolean hasNextPage;
    @SerializedName("items")
    private List<UserProfileEntity> items;
    @SerializedName("_header_")
    private Result result;
    private int count;
    private long refreshTime;

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public List<UserProfileEntity> getItems() {
        return items;
    }

    public Result getResult() {
        return result;
    }

    public int getCount() {
        return count;
    }

    public long getRefreshTime() {
        return refreshTime;
    }
}
