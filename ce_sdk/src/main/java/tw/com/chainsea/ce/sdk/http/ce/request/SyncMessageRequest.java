package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.response.base.ResponseBean;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

/**
 * 串接 sync/message 之 api
 * Created by Ian on 2022/10/5.
 */
public class SyncMessageRequest extends NewRequestBase {
    private final ApiListener<Resp> listener;

    public SyncMessageRequest(Context ctx, ApiListener<Resp> listener) {
        super(ctx, ApiPath.syncMessage);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        try {
            Resp resp = JsonHelper.getInstance().from(s, Resp.class);
            if (resp != null && resp.items != null && !resp.items.isEmpty()) {
                String roomId = resp.roomId;
                Iterator<MessageEntity> iterator = resp.items.iterator();
                while (iterator.hasNext()) {
                    MessageEntity entity = iterator.next();
                    if (Strings.isNullOrEmpty(entity.getAvatarId())) {
                        throw new RuntimeException("avatar id is null ");
                    }
                    if (!roomId.equals(entity.getRoomId())) {
                        iterator.remove();
                    }
                }
                Collections.sort(resp.items);

                if (this.listener != null) {
                    this.listener.onSuccess(resp);
                }
            }
        } catch (Exception e) {
            failed(ErrCode.RESPONSE_PARAMETER_NOT_FOUND, e.getMessage());
        }

    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e(errorMessage, code);
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public static class Resp extends ResponseBean implements Serializable {
        private static final long serialVersionUID = -8435105573423615338L;
        private String roomId;
        private List<MessageEntity> items = Lists.newArrayList();
        private boolean hasNextPage;


        @Override
        public void close() throws Exception { }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public List<MessageEntity> getItems() {
            return items;
        }

        public void setItems(List<MessageEntity> items) {
            this.items = items;
        }

        public boolean isHasNextPage() {
            return hasNextPage;
        }

        public void setHasNextPage(boolean hasNextPage) {
            this.hasNextPage = hasNextPage;
        }


    }
}
