package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Set;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType;
import tw.com.chainsea.ce.sdk.bean.response.base.ResponseBean;
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;

public class FromAppointRequest extends NewRequestBase {
    private ApiListener<Resp> listener;

    public FromAppointRequest(Context ctx, ApiListener<Resp> listener) {
        super(ctx, "/" + ApiPath.fromAppoint);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        try {
            Resp responseVo = JsonHelper.getInstance().from(s, Resp.class);
            if (responseVo == null || (responseVo.getLastFrom() == null && responseVo.getOtherFroms().isEmpty())) {
                this.listener.onSuccess(null);
            } else {
                String roomId = mJSONObject.getString("roomId");
                responseVo.setRoomId(roomId);
                this.listener.onSuccess(responseVo);
            }
        } catch (Exception e) {
            this.listener.onSuccess(null);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public static class Resp extends ResponseBean implements Serializable {
        private static final long serialVersionUID = 1436953131974855525L;
        String roomId;
        @SerializedName("status")
        ServiceNumberStatus status = ServiceNumberStatus.OFF_LINE;
        ChannelType lastFrom;
        Set<ChannelType> otherFroms = Sets.newHashSet();

        boolean lastMessageTimeOut = false;

        @Override
        public void close() throws Exception {

        }

        public Resp(ServiceNumberStatus status, ChannelType lastFrom, Set<ChannelType> otherFroms) {
            this.status = status;
            this.lastFrom = lastFrom;
            this.otherFroms = otherFroms;
        }

        public boolean isLastMessageTimeOut() {
            return lastMessageTimeOut;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public ServiceNumberStatus getStatus() {
            return status;
        }

        public void setStatus(ServiceNumberStatus status) {
            this.status = status;
        }

        public Set<ChannelType> getOtherFroms() {
            return otherFroms;
        }

        public void setOtherFroms(Set<ChannelType> otherFroms) {
            this.otherFroms = otherFroms;
        }

        public ChannelType getLastFrom() {
            return lastFrom;
        }

        public void setLastFrom(ChannelType lastFrom) {
            this.lastFrom = lastFrom;
        }
    }
}
