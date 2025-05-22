package tw.com.chainsea.chat.view.chart;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import tw.com.chainsea.chat.R;

public class DrawChartGraphActivity extends AppCompatActivity {


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_chart_graph);

        WebView webview = findViewById(R.id.webview);
        final WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setUseWideViewPort(true);
        webview.setWebChromeClient(new WebChromeClient());
        webview.setInitialScale(1);

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // after the HTML page loads, run JS to initialize graph
//                int[] dataset = new int[]{5, 10, 15, 20, 35};
//                String text = Arrays.toString(dataset);

//                webview.loadUrl("javascript:loadPieChart(" + text + ", " + (webview.getHeight()) + ", " + (webview.getWidth()) + ")");
            }
        });

        // Load base html from the assets directory

        webview.loadUrl("https://observablehq.com/@d3/zoomable-sunburst");
//        webview.loadUrl("file:///android_asset/html/chart.html");
    }
}
