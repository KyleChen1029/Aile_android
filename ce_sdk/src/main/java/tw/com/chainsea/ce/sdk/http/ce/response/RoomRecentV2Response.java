package tw.com.chainsea.ce.sdk.http.ce.response;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import tw.com.chainsea.ce.sdk.http.ce.model.Result;
import tw.com.chainsea.ce.sdk.http.ce.model.RoomRecentItem;

public class RoomRecentV2Response implements Serializable {
    private boolean hasNextPage;
    private long firstRefreshTime;
    private long lastRefreshTime;

    private long refreshTime;
    private int count;
    private List<RoomRecentItem> items;
    @SerializedName("_header_")
    private Result result;

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public long getFirstRefreshTime() {
        return firstRefreshTime;
    }

    public long getLastRefreshTime() {
        return lastRefreshTime;
    }

    public long getRefreshTime() {
        return refreshTime;
    }

    public int getCount() {
        return count;
    }

    public List<RoomRecentItem> getItems() {
        return items;
    }

    public Result getResult() {
        return result;
    }
}