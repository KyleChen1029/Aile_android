package tw.com.chainsea.ce.sdk.http.ce.response;

import java.util.List;

import tw.com.chainsea.ce.sdk.bean.ServiceNum;

public class SyncSubscribeServicenumberRsp {
    private List<ServiceNum> items;
    private int count;

    public List<ServiceNum> getItems() {
        return items;
    }

    public int getCount() {
        return count;
    }
}
