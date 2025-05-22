package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.bean.sticker.StickerItemEntity;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;

/**
 * current by evan on 2020-09-15
 *
 * @author Evan Wang
 * @date 2020-09-15
 */
public class StickerListRequest extends NewRequestBase {
    private ApiListener<List<StickerItemEntity>> listener;

    public StickerListRequest(Context ctx, ApiListener<List<StickerItemEntity>> listener) {
        super(ctx, ApiPath.stickerList);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (JsonHelper.getInstance().has(jsonObject, "items") && this.listener != null) {
            String jsonArray = jsonObject.getJSONArray("items").toString();
            List<StickerItemEntity> list = JsonHelper.getInstance().fromToList(jsonArray, StickerItemEntity[].class);
            this.listener.onSuccess(list);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

}
