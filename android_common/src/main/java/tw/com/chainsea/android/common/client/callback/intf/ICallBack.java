package tw.com.chainsea.android.common.client.callback.intf;

import okhttp3.Callback;
import okhttp3.Response;

/**
 * implements ICallBack and overwrite your success logic in response(String resp)
 *
 * @author Evan Wang
 * @version 0.0.1
 * @since 0.0.1
 */
public interface ICallBack extends Callback {
    /**
     * Http Request complete response callback method
     *
     * @param resp
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    void response(Response resp);


    void progress(float progress, long total);
}
