package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType;
import tw.com.chainsea.ce.sdk.bean.response.base.ResponseBean;
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

/**
 * current by evan on 2019-11-14
 * Is there a dedicated service in the service number chat room?
 */
public class ServiceNumberChatroomAgentServicedRequest extends NewRequestBase {
    private final ApiListener<Resp> listener;

    public ServiceNumberChatroomAgentServicedRequest(Context ctx, ApiListener<Resp> listener) {
        super(ctx, "/" + ApiPath.serviceNumberChatRoomAgentServiced);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        Resp resp = JsonHelper.getInstance().from(s, Resp.class);

        if (this.listener != null && resp != null) {
            String roomId = mJSONObject.getString("roomId");
            resp.setRoomId(roomId);
            this.listener.onSuccess(resp);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public static class ConsultArray implements Serializable {
        private String consultId;

        public String getConsultId() {
            return consultId;
        }

        public void setConsultId(String consultId) {
            this.consultId = consultId;
        }
    }

    public static class Resp extends ResponseBean implements Serializable {
        private String roomId;
        private boolean serviceNumberOwnerStop;
        private String identityId;
        @SerializedName("serviceNumberStatus")
        private ServiceNumberStatus serviceNumberStatus;
        private String robotChatRecordLink;
        private boolean warned;
        @SerializedName(value = "serviceAgentId", alternate = {"serviceNumberAgentId"})
        private String serviceNumberAgentId = "";
        private long startTime;
        private boolean transferFlag;

        private boolean isSnatch;

        private Set<ChannelType> otherFroms = Sets.newHashSet();
        private ChannelType lastFrom;

        private List<ActiveServiceConsultArray> activeServiceConsultArray;

        private List<ConsultArray> aiConsultArray = new ArrayList<>();

        public Resp(String roomId, ServiceNumberStatus serviceNumberStatus) {
            this.roomId = roomId;
            this.serviceNumberStatus = serviceNumberStatus;
        }

        public Resp(String roomId, ServiceNumberStatus serviceNumberStatus, Set<ChannelType> otherFroms, ChannelType lastFrom) {
            this.roomId = roomId;
            this.serviceNumberStatus = serviceNumberStatus;
            this.otherFroms = otherFroms;
            this.lastFrom = lastFrom;
        }

        public Resp(ServiceNumberStatus serviceNumberStatus, Set<ChannelType> otherFroms, ChannelType lastFrom) {
            this.serviceNumberStatus = serviceNumberStatus;
            this.otherFroms = otherFroms;
            this.lastFrom = lastFrom;
        }

        @Override
        public void close() throws Exception {

        }

        public boolean isServiceNumberOwnerStop() {
            return serviceNumberOwnerStop;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public ServiceNumberStatus getServiceNumberStatus() {
            return serviceNumberStatus;
        }

        public void setServiceNumberStatus(ServiceNumberStatus serviceNumberStatus) {
            this.serviceNumberStatus = serviceNumberStatus;
        }

        public String getServiceNumberAgentId() {
            return serviceNumberAgentId;
        }

        public void setServiceNumberAgentId(String serviceNumberAgentId) {
            this.serviceNumberAgentId = serviceNumberAgentId;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public boolean isTransferFlag() {
            return transferFlag;
        }

        public void setTransferFlag(boolean transferFlag) {
            this.transferFlag = transferFlag;
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

        public String getRobotChatRecordLink() {
            return robotChatRecordLink;
        }

        public void setSnatch(boolean isSnatch){
            this.isSnatch = isSnatch;
        }

        public boolean istSnatch() {
            return isSnatch;
        }

        public String getIdentityId() {
            return identityId;
        }

        public void setIdentityId(String identityId) {
            this.identityId = identityId;
        }

        public List<ActiveServiceConsultArray> getActiveServiceConsultArray() {
            return activeServiceConsultArray;
        }

        public void setActiveServiceConsultArray(List<ActiveServiceConsultArray> activeServiceConsultArray) {
            this.activeServiceConsultArray = activeServiceConsultArray;
        }

        public List<ConsultArray> getAiConsultArray() {
            return aiConsultArray;
        }

        public void setAiConsultArray(
                List<ConsultArray> aiConsultArray) {
            this.aiConsultArray = aiConsultArray;
        }
    }

}
