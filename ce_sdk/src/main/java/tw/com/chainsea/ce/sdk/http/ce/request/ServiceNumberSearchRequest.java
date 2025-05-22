package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import com.google.common.collect.Lists;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.response.base.ResponseBean;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 12/29/20
 *
 * @author Evan Wang
 * date 12/29/20
 */
public class ServiceNumberSearchRequest extends NewRequestBase {
    private ApiListener<Resp> listener;

    public ServiceNumberSearchRequest(Context ctx, ApiListener<Resp> listener) {
        super(ctx, ApiPath.serviceNumberSearch);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            Resp resp = JsonHelper.getInstance().from(s, Resp.class);
            this.listener.onSuccess(resp);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e(code + " " + errorMessage);
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public static class Resp extends ResponseBean implements Serializable {
        private static final long serialVersionUID = -71885443068624269L;
        private List<Item> items = Lists.newArrayList();

        @Override
        public void close() throws Exception {

        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

        public static class Item implements Serializable {
            private static final long serialVersionUID = 471479428894410344L;
            private String serviceNumberId;
            private String description;
            private String serviceNumberAvatarId;
            private String name;
            private String roomId;
            private boolean isSubscribe;

            public static long getSerialVersionUID() {
                return serialVersionUID;
            }

            public String getServiceNumberId() {
                return serviceNumberId;
            }

            public void setServiceNumberId(String serviceNumberId) {
                this.serviceNumberId = serviceNumberId;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public String getServiceNumberAvatarId() {
                return serviceNumberAvatarId;
            }

            public void setServiceNumberAvatarId(String serviceNumberAvatarId) {
                this.serviceNumberAvatarId = serviceNumberAvatarId;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getRoomId() {
                return roomId;
            }

            public void setRoomId(String roomId) {
                this.roomId = roomId;
            }

            public boolean isSubscribe() {
                return isSubscribe;
            }

            public void setSubscribe(boolean subscribe) {
                isSubscribe = subscribe;
            }
        }
    }

}
