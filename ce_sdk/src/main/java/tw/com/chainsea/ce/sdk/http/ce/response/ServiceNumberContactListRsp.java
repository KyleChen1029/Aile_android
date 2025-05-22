package tw.com.chainsea.ce.sdk.http.ce.response;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import tw.com.chainsea.ce.sdk.bean.CustomerEntity;
import tw.com.chainsea.ce.sdk.http.ce.model.Result;

public class ServiceNumberContactListRsp implements Serializable {
    private boolean hasNextPage;
    private final List<CustomerEntity> items = Lists.newArrayList();
    private long refreshTime;
    @SerializedName("_header_")
    private Result result;


    public List<CustomerEntity> getItems() {
        return items;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public long getRefreshTime() {
        return refreshTime;
    }

    public Result getResult() {
        return result;
    }
}
