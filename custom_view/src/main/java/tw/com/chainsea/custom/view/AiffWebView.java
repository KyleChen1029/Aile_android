package tw.com.chainsea.custom.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@SuppressLint("SetJavaScriptEnabled")
public class AiffWebView extends WebView {
    private JSBridge JSBridge;

    public AiffWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AiffWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public AiffWebView(Context context) {
        super(context);
        initView(context);
    }

    private void initWebViewSetting() {
        this.getSettings().setSupportZoom(true);
        this.getSettings().setDisplayZoomControls(true);
        this.getSettings().setBuiltInZoomControls(true);
        this.setScrollContainer(true);
        this.setVerticalScrollBarEnabled(true);
        this.setHorizontalScrollBarEnabled(true);
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setDomStorageEnabled(true);
        this.clearCache(true);
        this.clearHistory();
        this.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    private void initView(Context context) {
        initWebViewSetting();
        JSBridge = new JSBridge();
        this.addJavascriptInterface(JSBridge, "jsToAndroid");
        this.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                injectScriptString(view, "js/androidwebviewpreload.js");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(url));
                    context.startActivity(intent);
                    return true;
                } else if (url.startsWith("http:") || url.startsWith("https:")) {
                    webView.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
//                injectScriptString(view, "js/androidwebviewpreload.js");
            }
        });
    }

    private void injectScriptString(WebView view, String scriptFile) {
        try {
            StringBuilder sb = new StringBuilder();
            InputStream is = view.getContext().getAssets().open(scriptFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
                //readLine會自動把換行符號去掉，需補上才能正常顯示
                sb.append("\n");
            }
            br.close();
            view.evaluateJavascript(sb.toString(), null);
        } catch (IOException ignored) {
        }
    }

    public void callJSFunction(String method) {
        Log.d("AiffWebView", "function string = " + method);
        this.post(() -> evaluateJavascript(method, null));
    }

    public void setBridgeCallBack(OnBridgeCallback bridgeCallback) {
        JSBridge.setBridgeCallBack(bridgeCallback);
    }

    public void removeJSBridge() {
        this.removeJavascriptInterface("jsToAndroid");
    }
}
