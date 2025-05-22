package tw.com.chainsea.ce.sdk.http.ce.base;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * current by evan on 2020-06-12
 *
 * @author Evan Wang
 * date 2020-06-12
 */
public abstract class AvatarRequestBase extends NewRequestBase {

    public AvatarRequestBase(Context ctx, String path) {
        super(ctx, path);
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {

    }
}
