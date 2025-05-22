package tw.com.chainsea.chat.aiff;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.request.FromAppointRequest;
import tw.com.chainsea.chat.App;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.DialogAiffBinding;
import tw.com.chainsea.chat.databinding.DialogGetLocationPermissionHintBinding;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.ui.adapter.entity.RichMenuInfo;
import tw.com.chainsea.chat.ui.utils.permissionUtils.XPermissionUtils;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.custom.view.OnBridgeCallback;

public class AiffDialog extends AppCompatActivity implements View.OnClickListener, OnBridgeCallback, OnBridgeOpenRoom {
    private DialogAiffBinding binding;
    private final String TAG = AiffDialog.class.getSimpleName();
    private AiffSupport support;

    private String chatRoomId;
    private String displayType;
    private double dialogWidth;
    private double dialogHeight;
    private String channel = "";
    private String metaData = "";
    private static final int REQUEST_LOCATION_PERMISSION = 100;
    private WindowManager windowManager;
    private DisplayMetrics displayMetrics;
    private ValueCallback<Uri[]> mUploadMessage = null;
    private ActivityResultLauncher<Intent> uploadFileARL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.INSTANCE.setTheme(AiffDialog.this);
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        binding = DataBindingUtil.setContentView(this, R.layout.dialog_aiff);
        chatRoomId = getIntent().getStringExtra(AiffKey.ROOM_ID);
        initListener();
        init();
        setTitle(getIntent().getStringExtra(AiffKey.TITLE));
        setDisplayType(getIntent().getStringExtra(AiffKey.DISPLAY_TYPE));

        Type typeToken = new TypeToken<List<RichMenuInfo>>(){}.getType();
        List<RichMenuInfo> aiffInfoList = JsonHelper.getInstance().from(getIntent().getStringExtra(AiffKey.AIFF_INFO_LIST), typeToken);
        if (aiffInfoList != null) {
            initList(aiffInfoList);
        } else {
            initView();
            initAPPointData();
        }

        switch (displayType) {
            case AiffKey.ALL:
                setLayout(getPhoneWidth(), getPhoneHeight() * 0.9);
                break;
            case AiffKey.HALF:
                setLayout(getPhoneWidth(), (double) getPhoneHeight() / 2);
                break;
        }

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
            getWindow().getAttributes().gravity = Gravity.BOTTOM;
            getWindow().setLayout((int) dialogWidth, (int) dialogHeight);
            getWindow().setWindowAnimations(R.style.ios_bottom_dialog_anim);
        }
        uploadFileARL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->  {

        });
    }

    private void init() {
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        dialogWidth = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogHeight = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    private void initList(List<RichMenuInfo> richMenuInfoList) {
        AiffListAdapter aiffRichMenuAdapter = new AiffListAdapter(chatRoomId);
        aiffRichMenuAdapter.setData(richMenuInfoList);
        binding.webView.setVisibility(View.GONE);
        binding.rvAiffList.setVisibility(View.VISIBLE);

        binding.rvAiffList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAiffList.setAdapter(aiffRichMenuAdapter);
    }

    private void initListener() {
        binding.ivAiffClose.setOnClickListener(this);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {
        binding.webView.setVisibility(View.VISIBLE);
        binding.rvAiffList.setVisibility(View.GONE);
        setUrl(getIntent().getStringExtra(AiffKey.URL));

        support = new AiffSupport(this, chatRoomId);
        support.setBridgeOpenRoom(this);
        binding.webView.getSettings().setSupportZoom(true);
        binding.webView.getSettings().setDisplayZoomControls(true);
        binding.webView.getSettings().setBuiltInZoomControls(true);
        binding.webView.getSettings().setGeolocationEnabled(true);
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.setScrollContainer(true);
        binding.webView.setHorizontalScrollBarEnabled(true);
        binding.webView.setVerticalScrollBarEnabled(true);
        binding.webView.setBridgeCallBack(this);
        binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String message = String.format("message: %s - line %s: %s", consoleMessage.message(), consoleMessage.lineNumber(), consoleMessage.sourceId());
                if (Objects.requireNonNull(consoleMessage.messageLevel()) == ConsoleMessage.MessageLevel.ERROR) {
                    logErrorMessage(message);
                } else {
                    logInfoMessage(message);
                }
                return true;
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                final String[] requestedResources = request.getResources();
                for (String r : requestedResources) {
                    if (r.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                        request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                        break;
                    }
                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mUploadMessage = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                uploadFileARL.launch(intent);
                return true;
            }
        });
    }

    private void initAPPointData(){
        ApiManager.doFromAppoint(this, chatRoomId, new ApiListener<>() {
            @Override
            public void onSuccess(FromAppointRequest.Resp resp) {
                if (resp == null) return;
                try{
                    JSONObject respJsonObject = new JSONObject(resp.toString());
                    support.otherFroms = respJsonObject.getJSONArray("otherFroms");
                    support.status = respJsonObject.getString("status");
                    support.lastFrom = respJsonObject.getString("lastFrom");

                } catch (Exception ignored) { }
            }

            @Override
            public void onFailed(String errorMessage) {
                CELog.d("獲取資料 /openapi/from/appoint 失敗 errorMsg::" + errorMessage);
            }
        });
    }


    public void setUrl(String url) {
        binding.webView.loadUrl(url);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ivAiffClose) closeAiff();
//        switch (v.getId()) {
//            case R.id.ivAiffClose:
//                closeAiff();
//                break;
//        }
    }

    @Override
    public void onCallBack(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            String channel = jsonObject.getString(AiffKey.CHANNEL);
            StringBuilder methodData = new StringBuilder();
            if (channel.equals(AiffKey.INIT)) {
                channel = AiffKey.INIT_LAPP;
            }

            Iterator<String> keys = jsonObject.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                if (!key.equals(AiffKey.CHANNEL)) {
                    methodData.append(jsonObject.getString(key));
                }
            }

            if (AiffKey.GET_CURRENT_LOCATION.equals(channel)) {
                this.channel = channel;
                this.metaData = methodData.toString();
            }
            binding.webView.callJSFunction(support.callJsFunction(channel, methodData.toString(), jsonObject));
        } catch (Throwable t) {
            Log.e(TAG, "Error:" +t.getMessage());
            Log.e(TAG, "Could not parse malformed JSON: \"" + data + "\"");
        }
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    private int getPhoneWidth() {
        return displayMetrics.widthPixels;
    }

    private int getPhoneHeight() {
        return displayMetrics.heightPixels;
    }

    @Override
    public void open(String roomId) {
        if (!Strings.isNullOrEmpty(roomId)) {
            ActivityTransitionsControl.navigateToChat(this, roomId, (intent, s) -> {
                IntentUtil.INSTANCE.start(this, intent);
                this.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
            });
        }
    }

    @Override
    public void close() {
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_CLOSE_OLD_ROOM));
        finish();
    }

    @Override
    public void openExternalWindow(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(url));
        IntentUtil.INSTANCE.start(App.getContext(), intent);
    }

    @Override
    public void openInternalAiffWindow(String url) {
        String roomId = UserPref.getInstance(App.getContext()).getCurrentRoomId();
        AiffManager aiffManager = new AiffManager(App.getContext(), roomId);
        aiffManager.addAiffWebView(url);
    }

    @Override
    public void quote(String quote) {
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.QUOTE_MESSAGE, quote));
        closeAiff();
    }

    @Override
    public void quoteAndSend(String type, String quote) {
        String ignoreCase = type.toLowerCase();
        switch (ignoreCase) {
            case "text":
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.QUOTE_AND_SEND_MESSAGE, quote));
                break;
            case "template":
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.QUOTE_AND_SEND_TEMPLATE_MESSAGE, quote));
                break;
            case "image":
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.QUOTE_AND_SEND_IMAGE_MESSAGE, quote));
                break;
        }
        closeAiff();
    }

    public void setTitle(String title) {
        binding.tvAiffTitle.setText(title);
    }

    private void setLayout(double w, double h) {
        dialogHeight = h;
        dialogWidth = w;
        if(getWindow() != null) {
            getWindow().getAttributes().gravity = Gravity.BOTTOM;
            getWindow().setLayout((int) dialogWidth, (int) dialogHeight);
        }

        if (checkHasNavigationBar() < 50) {
            binding.viewSpace.setVisibility(View.VISIBLE);
        }
    }

    private void closeAiff() {
        finish();
        overridePendingTransition(R.anim.ios_dialog_enter, R.anim.ios_dialog_exit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.webView.removeJSBridge();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            getLocation();
        }
    }

    private void getLocation() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String deniedData = support.getLocationPermissionDeniedData(channel, metaData);
                binding.webView.callJSFunction(deniedData);
                return;
            }
            Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            String grantedData = support.getLocationPermissionGrantedData(channel, metaData, gpsLocation);
            binding.webView.callJSFunction(grantedData);
        } catch (Exception ignored) {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                closeAiff();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private int checkHasNavigationBar() {
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        return realHeight - displayHeight;
    }

    private void showGetLocationHintDialog() {
        String deniedData = support.getLocationPermissionDeniedData(channel, metaData);
        binding.webView.callJSFunction(deniedData);
        Dialog dialog = new Dialog(this);
        DialogGetLocationPermissionHintBinding dialogGetLocationPermissionHintBinding = DialogGetLocationPermissionHintBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogGetLocationPermissionHintBinding.getRoot());

        dialogGetLocationPermissionHintBinding.tvCancel.setOnClickListener(v -> dialog.dismiss());

        dialogGetLocationPermissionHintBinding.tvSubmit.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            IntentUtil.INSTANCE.start(this, intent);
            dialog.dismiss();
        });

        dialog.show();
        dialogGetLocationPermissionHintBinding.getRoot().getLayoutParams().width = (int)(getPhoneWidth() * 0.8);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    private void logInfoMessage(String message) {
        Log.i(TAG, message);
    }

    private void logErrorMessage(String message) {
        Log.e(TAG, message);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(EventMsg eventMsg) {
        switch (eventMsg.getCode()) {

            case MsgConstant.AIFF_REQUEST_PERMISSION:
                if (XPermissionUtils.hasAlwaysDeniedPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showGetLocationHintDialog();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                }
                break;

            case MsgConstant.AIFF_ON_LOCATION_GET:
                if (isFinishing()) {
                    support.removeLocationListener();
                } else {
                    String data = eventMsg.getData().toString();
                    binding.webView.callJSFunction(data);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case Activity.RESULT_OK:
                ArrayList<Uri> result = new ArrayList<>();
                if (data != null) {
                    if (data.getData() != null) {
                        result.add(data.getData());
                    }

                    if (data.getClipData() != null) {
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            result.add(data.getClipData().getItemAt(i).getUri());
                        }
                    }
                }

                mUploadMessage.onReceiveValue(result.toArray(new Uri[0]));
                break;
            case Activity.RESULT_CANCELED:
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(null);
                }
                mUploadMessage = null;
                break;
        }
    }

    //
//    public int getNavigationBarHeight(Activity activity) {
//        int result = 0;
//        int resourceId = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
//        if (resourceId > 0) {
//            result = activity.getResources().getDimensionPixelSize(resourceId);
//        }
//        return result;
//    }
}
