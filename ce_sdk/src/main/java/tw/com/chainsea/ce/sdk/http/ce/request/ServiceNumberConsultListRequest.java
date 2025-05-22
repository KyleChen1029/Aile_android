package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import com.google.common.collect.Lists;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.response.base.ResponseBean;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;

/**
 * Create by Evan.W on 2021/01/21.
 *
 * @author Evan Wang
 * date 1/21/21
 */
public class ServiceNumberConsultListRequest extends NewRequestBase {
    private ApiListener<Resp> listener;

    public ServiceNumberConsultListRequest(Context ctx, ApiListener<Resp> listener) {
        super(ctx, "/" + ApiPath.serviceNumberConsultList);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        try (Resp resp = JsonHelper.getInstance().from(s, Resp.class)) {
            if (this.listener != null) {
                this.listener.onSuccess(resp);
            }
        }catch (Exception e) {
            failed(ErrCode.JSON_PARSE_FAILED, e.getMessage());
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public static class Resp extends ResponseBean implements Serializable {
        private static final long serialVersionUID = 6815839207046252691L;
        private int count;
        private List<ServiceNumberEntity> items = Lists.newArrayList();

        @Override
        public void close() throws Exception {

        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<ServiceNumberEntity> getItems() {
            return items;
        }

        public void setItems(List<ServiceNumberEntity> items) {
            this.items = items;
        }
    }
}
