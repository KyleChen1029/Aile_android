package tw.com.chainsea.custom.view;

import android.util.Log;
import android.webkit.JavascriptInterface;

public class JSBridge {
    private String TAG = JSBridge.class.getSimpleName();
    private OnBridgeCallback bridgeCallback;

    public JSBridge() {

    }

    public void setBridgeCallBack(OnBridgeCallback bridgeCallback) {
        this.bridgeCallback = bridgeCallback;
    }

    // 没有返回结果
    @JavascriptInterface
    public void onCall(String paramFromJS) {
        Log.d(TAG, "js onCall = " + paramFromJS);
    }

    // 有返回结果
    @JavascriptInterface
    public String onCallBack(String paramFromJS) {
        Log.d(TAG, "js onCallBack = " + paramFromJS);
        bridgeCallback.onCallBack(paramFromJS);
        return paramFromJS;
    }


}
