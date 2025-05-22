package tw.com.chainsea.ce.sdk.http.ce.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import tw.com.chainsea.ce.sdk.http.ce.model.Item;
import tw.com.chainsea.ce.sdk.http.ce.model.Result;

public class UserEmployeeListRsp implements Serializable {
    private boolean hasNextPage;
    @SerializedName("items")
    private List<Item> items;
    @SerializedName("_header_")
    private Result result;
    private long totalNumber;
    private long refreshTime;

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public long getTotalNumber() {
        return totalNumber;
    }

    public void setTotalNumber(long totalNumber) {
        this.totalNumber = totalNumber;
    }

    public long getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(long refreshTime) {
        this.refreshTime = refreshTime;
    }
}
