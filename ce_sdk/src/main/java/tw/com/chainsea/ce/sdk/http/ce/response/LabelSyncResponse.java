package tw.com.chainsea.ce.sdk.http.ce.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import tw.com.chainsea.ce.sdk.bean.label.Label;
import tw.com.chainsea.ce.sdk.http.ce.model.Result;

public class LabelSyncResponse implements Serializable {
    private long refreshTime; //下一页的起始时间点
    private int count; //抓取到的数据笔数
    private boolean hasNextPage; //是否有下一页
    private List<Label> items;
    @SerializedName("_header_")
    private Result result;

    public long getRefreshTime() {
        return refreshTime;
    }

    public int getCount() {
        return count;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public List<Label> getItems() {
        return items;
    }

    public Result getResult() {
        return result;
    }
}
