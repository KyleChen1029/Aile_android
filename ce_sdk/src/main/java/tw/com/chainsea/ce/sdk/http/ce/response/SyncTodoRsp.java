package tw.com.chainsea.ce.sdk.http.ce.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity;
import tw.com.chainsea.ce.sdk.http.ce.model.Result;

public class SyncTodoRsp implements Serializable {
    private boolean hasNextPage;
    @SerializedName("items")
    private List<TodoEntity> items;
    @SerializedName("_header_")
    private Result result;
    private int count;
    private long refreshTime;

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public List<TodoEntity> getItems() {
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
