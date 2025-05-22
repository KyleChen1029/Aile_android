package tw.com.chainsea.android.common.setting;

import android.content.Intent;

/**
 * current by evan on 11/27/20
 *
 * @author Evan Wang
 * @date 11/27/20
 */
public abstract class ASettingCallBack<T extends Intent> implements ISettingCallBack<T> {

    @Override
    public void error(String errorMessage) {

    }
}
