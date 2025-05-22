package tw.com.chainsea.ce.sdk.http.ce.request;

import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_TODO;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.response.SyncTodoRsp;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.reference.TodoReference;

public class SyncTodoRequest extends NewRequestBase {
    public static final int PAGE_SIZE = 100;
    private final Listener listener;
    private Context context;

    public SyncTodoRequest(Context ctx, Listener listener) {
        super(ctx, "/" + ApiPath.syncTodo);
        this.context = ctx;
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        SyncTodoRsp rsp = JsonHelper.getInstance().from(s, SyncTodoRsp.class);
        if(rsp.getItems() != null && !rsp.getItems().isEmpty()){
            TodoReference.save(null, rsp.getItems());
            DBManager.getInstance().updateOrInsertApiInfoField(SYNC_TODO, rsp.getRefreshTime());
            if(rsp.isHasNextPage()){
                new SyncTodoRequest(context, listener)
                        .setMainThreadEnable(false)
                        .request(new JSONObject()
                                .put("pageSize", PAGE_SIZE)
                                .put("refreshTime", rsp.getRefreshTime()));
            }else{
                if(listener != null) listener.onSuccess();
            }
        }else {
            if(listener != null) listener.onSuccess();
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public interface Listener {
        void onSuccess();
        void onFailed(String errorMessage);
    }
}
