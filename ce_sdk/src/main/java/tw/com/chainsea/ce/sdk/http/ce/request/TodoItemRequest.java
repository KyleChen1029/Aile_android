package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;

/**
 * current by evan on 2020-07-14
 *
 * @author Evan Wang
 * @date 2020-07-14
 */
public class TodoItemRequest extends NewRequestBase {
    private ApiListener<TodoEntity> listener;

    public TodoItemRequest(Context ctx, ApiListener<TodoEntity> listener) {
        super(ctx, ApiPath.todoItem);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            TodoEntity entity = JsonHelper.getInstance().from(jsonObject.toString(), TodoEntity.class);
            this.listener.onSuccess(entity);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

}
