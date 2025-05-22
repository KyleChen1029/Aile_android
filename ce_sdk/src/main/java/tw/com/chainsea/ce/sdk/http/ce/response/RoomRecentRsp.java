package tw.com.chainsea.ce.sdk.http.ce.response;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.http.ce.model.Result;

public class RoomRecentRsp implements Serializable {
    private Boolean hasNextPage;
    private final List<ChatRoomEntity> items = Lists.newArrayList();
    private long refreshTime;
    @SerializedName("_header_")
    private Result result;

    public Boolean getHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(Boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public List<ChatRoomEntity> getItems() {
        return items;
    }

    public long getRefreshTime() {
        return refreshTime;
    }

    public Result getResult() {
        return result;
    }
}