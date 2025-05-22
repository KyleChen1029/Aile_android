package tw.com.chainsea.ce.sdk.socket.ce.listener;

import java.util.Arrays;
import java.util.Iterator;

import io.socket.emitter.Emitter;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-10-22
 *
 * @author Evan Wang
 * @date 2020-10-22
 */
public abstract class OnConnectListener implements Emitter.Listener {

    protected abstract void connect(String data);

    @Override
    public void call(Object... args) {
        ThreadExecutorHelper.getSocketExecutor().execute(() -> {
            Iterator iterator = Arrays.asList(args).iterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
//                connect("");
                CELog.i("connect::: ", JsonHelper.getInstance().toJson(obj));
            }
            connect("");
        });

    }
}
