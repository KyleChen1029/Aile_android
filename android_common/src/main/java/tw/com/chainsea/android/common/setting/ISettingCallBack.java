package tw.com.chainsea.android.common.setting;

import android.content.Intent;

/**
 * current by evan on 11/27/20
 *
 * @author Evan Wang
 * @date 11/27/20
 */
public interface ISettingCallBack<T extends Intent> {

    void callback(T t);

    void error(String errorMessage);
}
