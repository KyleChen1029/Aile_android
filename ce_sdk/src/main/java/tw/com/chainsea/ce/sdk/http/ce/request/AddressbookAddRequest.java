package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-11-12
 *
 * @author Evan Wang
 * date 2020-11-12
 */
public class AddressbookAddRequest extends NewRequestBase {
    private final ApiListener<Set<String>> listener;

    public AddressbookAddRequest(Context ctx, ApiListener<Set<String>> listener) {
        super(ctx, "/" + ApiPath.addressBookAdd);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        JSONArray jsonArray = jsonObject.getJSONArray("roomIds");
        Set<String> roomIds = JsonHelper.getInstance().fromToSet(jsonArray.toString(), String[].class);
        if (this.listener != null) {
            this.listener.onSuccess(roomIds);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e(errorMessage);
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

}
