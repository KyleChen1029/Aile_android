package tw.com.chainsea.ce.sdk.http.ce.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import tw.com.chainsea.ce.sdk.bean.CustomerEntity;
import tw.com.chainsea.ce.sdk.http.ce.model.Result;

public class SyncContactRsp {
    private List<CustomerEntity> items;
    private int count;
    private boolean hasNextPage;
    @SerializedName("_header_")
    private Result result;
    private long refreshTime;

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public List<CustomerEntity> getItems() {
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
