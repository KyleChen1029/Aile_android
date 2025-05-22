package tw.com.chainsea.android.common.permission.callback;


/**
 * Get system service callback letter
 *
 * @param <SERVICE>
 * @author Evan Wang
 * @version 0.0.1
 * @since 0.0.1
 */

public interface ServiceCallBack<SERVICE> {
    void request(SERVICE service);
}
