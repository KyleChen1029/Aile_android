package tw.com.chainsea.chat.keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

import tw.com.chainsea.android.common.event.KeyboardHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.InputLogBean;
import tw.com.chainsea.ce.sdk.bean.InputLogType;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.style.RoomThemeStyle;
import tw.com.chainsea.chat.keyboard.listener.OnNewKeyboardListener;
import tw.com.chainsea.chat.messagekit.enums.RichMenuBottom;
import tw.com.chainsea.chat.messagekit.main.adapter.BottomRichMeunAdapter;
import tw.com.chainsea.chat.widget.GridItemDecoration;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-07-08
 *
 * @author Evan Wang
 * date 2020-07-08
 */
public class NewKeyboardLayout extends ConstraintLayout implements View.OnFocusChangeListener, TextWatcher, View.OnClickListener, TextView.OnEditorActionListener {

    NewKeyboardView kView;
//    Handler uiHandler = new Handler(Looper.getMainLooper());

    OnNewKeyboardListener onNewKeyboardListener;

    // bind data
    ChatRoomEntity entity;

    Set<NewKeyboardFun> blacklist = Sets.newHashSet();

    /**
     * 底部功能類型
     */
    public enum NewKeyboardFun {
        FUN_MEDIA(0, "檔案選擇器"),
        FUN_RECORD(2, "錄音機"),
        FUN_PHOTO(3, "圖片選擇器"),
        FUN_FACIAL(4, "表情選擇器"),
        FUN_CAMERA(5, "相機"),
        FUN_VIDEO(6, "錄影");

        private int type;
        private String name;

        NewKeyboardFun(int type, String name) {
            this.type = type;
            this.name = name;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public NewKeyboardLayout(Context context) {
        super(context);
        init(context);
    }

    public NewKeyboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NewKeyboardLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.new_keyboard_layout, this);
        kView = NewKeyboardView.bindView(root);
        listener();
//        kView.etInput.findFocus();
    }


    private void listener() {
        // 左邊功能
        kView.ivCamera.setOnClickListener(this);
        kView.ivPic.setOnClickListener(this);
        kView.ivVideo.setOnClickListener(this);
        kView.ivMedia.setOnClickListener(this);
        kView.ivFunExpand.setOnClickListener(this);

        // 輸入區
        kView.etInput.setOnClickListener(this);
        kView.etInput.setOnFocusChangeListener(this);
        kView.etInput.addTextChangedListener(this);

        kView.etInput.setOnEditorActionListener(this);
        kView.etInput.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            CELog.d("source:: " + source + " , dest:: " + dest);
            return null;
        }});

        kView.ivFacial.setOnClickListener(this);

        // 右邊功能
        kView.ivSend.setOnClickListener(this);
        kView.ivInputExpand.setOnClickListener(this);


    }

    public void setOnNewKeyboardListener(OnNewKeyboardListener onNewKeyboardListener) {
        this.onNewKeyboardListener = onNewKeyboardListener;
    }

    /**
     * 綁定聊天室資料
     */
    public void bind(ChatRoomEntity entity) {
        this.entity = entity;
    }


    /**
     * 功能黑名單管理
     */
    public void setBlacklist(Set<NewKeyboardFun> blacklist) {
        this.blacklist = blacklist;
        for (NewKeyboardFun fun : this.blacklist) {
            switch (fun) {
                case FUN_RECORD:
                    kView.ivSend.setImageResource(R.drawable.slector_send);
                    break;
                case FUN_FACIAL:
                    kView.ivFacial.setVisibility(GONE);
                    break;
                case FUN_PHOTO:
                    kView.ivPic.setVisibility(GONE);
                    break;
                case FUN_VIDEO:
                    kView.ivVideo.setVisibility(GONE);
                    break;
                case FUN_CAMERA:
                    kView.ivCamera.setVisibility(GONE);
                    break;
                case FUN_MEDIA:
                    kView.ivMedia.setVisibility(GONE);
                    break;
            }
        }
    }

    public void addBlacklist(NewKeyboardFun fun) {
        this.blacklist.add(fun);
    }

    public void removeBlacklist(NewKeyboardFun fun) {
        this.blacklist.remove(fun);
    }

    /**
     * 刷新 Keyboard 狀態
     * <p>1、開啟AT功能</>
     * <p>2、外部渠道禁止輸入，輸入區遮罩</>
     * <p>3、輸入區樣式風格</p>
     */
    public void refresh() {
        themeStyle(getThemeStyle(entity));
    }

    private RoomThemeStyle getThemeStyle(ChatRoomEntity entity) {
        RoomThemeStyle themeStyle = RoomThemeStyle.UNDEF;
        if (!Strings.isNullOrEmpty(entity.getBusinessId()) && !ChatRoomType.SERVICES_or_SUBSCRIBE.contains(entity.getType())) {
            themeStyle = RoomThemeStyle.BUSINESS;
        } else {
            themeStyle = RoomThemeStyle.of(entity.getType().name());
        }
        return themeStyle;
    }

    /**
     * 輸入區樣式設定
     */
    private void themeStyle(RoomThemeStyle themeStyle) {
        kView.root.setBackgroundColor(themeStyle.getKeyboardColor());
        int charCount = kView.etInput.length();

        if (RoomThemeStyle.SERVICES_or_BUSINESS_or_BROADCAST_or_SERVICE_MEMBER.contains(themeStyle)) {
            kView.ivCamera.setImageResource(R.drawable.slector_camera_business);
            kView.ivPic.setImageResource(R.drawable.slector_pic_business);
            kView.ivVideo.setImageResource(R.drawable.slector_video_business);
            kView.ivMedia.setImageResource(R.drawable.slector_pin_business);
            kView.ivFunExpand.setImageResource(R.drawable.slector_next_business);

            if (!this.blacklist.contains(NewKeyboardFun.FUN_RECORD)) {
                kView.ivSend.setImageResource(charCount > 0 ? R.drawable.slector_send_business : R.drawable.slector_mic_business);
            }
        } else {
            kView.ivCamera.setImageResource(R.drawable.slector_camera);
            kView.ivPic.setImageResource(R.drawable.slector_pic);
            kView.ivVideo.setImageResource(R.drawable.slector_video);
            kView.ivMedia.setImageResource(R.drawable.slector_pin);
            kView.ivFunExpand.setImageResource(R.drawable.slector_next);
            if (!this.blacklist.contains(NewKeyboardFun.FUN_RECORD)) {
                kView.ivSend.setImageResource(charCount > 0 ? R.drawable.slector_send : R.drawable.slector_mic);
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        kView.ivFacial.setTag(null);
        kView.ivFacial.setSelected(false);
        setFunctionBtnSelectStatus(null);
        if (onNewKeyboardListener != null) {
            onNewKeyboardListener.onCloseFun(null);
        }
        setLeftFunStatus(hasFocus);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        CELog.e("");
    }

    @Override
    public void afterTextChanged(Editable s) {
        setRightFunStatus(s.length());
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
            int lineCount = kView.etInput.getLineCount();
            setInputLineCountStatus(lineCount);
        });
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            onSendClick(v);
            return true;
        }
        return false;
    }

    public void setText(CharSequence text, boolean isSelectEnd) {
        kView.etInput.setText(text);
        if (isSelectEnd) {
            kView.etInput.setSelection(text.length());
        }
    }


    public void append(CharSequence text, boolean isSelectEnd) {
        Editable editable = kView.etInput.getEditableText();
        int index = kView.etInput.getSelectionStart();
        if (index < 0) {
            editable.append(text);
        } else {
            editable.insert(index, text);
        }
    }

    /**
     * 左邊功能狀態
     */
    private void setLeftFunStatus(boolean hasFocus) {
        kView.ivCamera.setVisibility(hasFocus || this.blacklist.contains(NewKeyboardFun.FUN_CAMERA) ? View.GONE : View.VISIBLE);
        kView.ivPic.setVisibility(hasFocus || this.blacklist.contains(NewKeyboardFun.FUN_PHOTO) ? View.GONE : View.VISIBLE);
        kView.ivVideo.setVisibility(hasFocus || this.blacklist.contains(NewKeyboardFun.FUN_VIDEO) ? View.GONE : View.VISIBLE);
        kView.ivMedia.setVisibility(hasFocus || this.blacklist.contains(NewKeyboardFun.FUN_MEDIA) ? View.GONE : View.VISIBLE);
        kView.ivFunExpand.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
        kView.llLeftFunBox.setOrientation(hasFocus ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
    }

    /**
     * 右邊功能狀態
     * 字節判斷
     */
    private void setRightFunStatus(int charCount) {
        RoomThemeStyle themeStyle;
        if (!Strings.isNullOrEmpty(entity.getBusinessId()) && !ChatRoomType.SERVICES_or_SUBSCRIBE.contains(entity.getType())) {
            themeStyle = RoomThemeStyle.BUSINESS;
        } else {
            themeStyle = RoomThemeStyle.of(entity.getType().name());
        }

        if (!this.blacklist.contains(NewKeyboardFun.FUN_RECORD)) {
            if (RoomThemeStyle.SERVICES_or_BUSINESS_or_BROADCAST_or_SERVICE_MEMBER.contains(themeStyle)) {
                kView.ivSend.setImageResource(charCount > 0 ? R.drawable.slector_send_business : R.drawable.slector_mic_business);
            } else {
                kView.ivSend.setImageResource(charCount > 0 ? R.drawable.slector_send : R.drawable.slector_mic);
            }
        }

    }


    /**
     * 輸入區行列狀態監聽
     */
    private void setInputLineCountStatus(int lineCount) {
        kView.ivInputExpand.setVisibility(lineCount >= 4 ? View.VISIBLE : View.GONE);
    }


    /**
     * 相機按鈕點擊
     */
    public void onCameraClick(View view) {
        kView.ivFacial.setTag(null);
        kView.ivFacial.setSelected(false);
        setFunctionBtnSelectStatus(NewKeyboardFun.FUN_CAMERA);
        hideKeyboard();
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
            onNewKeyboardListener.onOpenCameraFun();
            setFunctionBtnUnSelect(NewKeyboardFun.FUN_CAMERA);
            onNewKeyboardListener.onCloseFun(null);
        });
    }


    /**
     * 相簿按鈕點擊
     */
    public void onPicClick(View view) {
        kView.ivFacial.setTag(null);
        kView.ivFacial.setSelected(false);
        setFunctionBtnSelectStatus(NewKeyboardFun.FUN_PHOTO);
        hideKeyboard();
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
            onNewKeyboardListener.onCloseFun(null);
            onNewKeyboardListener.onOpenPhotoSelectorFun(false);
        });
    }


    /**
     * 錄影按鈕點擊
     */
    public void onVideoClick(View view) {
        kView.ivFacial.setTag(null);
        kView.ivFacial.setSelected(false);
        setFunctionBtnSelectStatus(NewKeyboardFun.FUN_VIDEO);
        hideKeyboard();
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
            onNewKeyboardListener.onOpenVideoFun();
            setFunctionBtnUnSelect(NewKeyboardFun.FUN_VIDEO);
            onNewKeyboardListener.onCloseFun(null);
        });
    }

    /**
     * 多媒體按鈕點擊
     */
    public void onMediaClick(View view) {
        kView.ivFacial.setTag(null);
        kView.ivFacial.setSelected(false);
        setFunctionBtnSelectStatus(NewKeyboardFun.FUN_MEDIA);
        hideKeyboard();
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
            onNewKeyboardListener.onCloseFun(null);
            onNewKeyboardListener.onOpenMediaSelectorFun();
        });
    }

    /**
     * 展開功能按鈕點擊
     */
    public void onFunctionExpandClick(View view) {
        int select = kView.etInput.getSelectionStart();
        select = select == 0 ? kView.etInput.length() : select;
        kView.ivInputExpand.setSelected(false);
        expandInputArea(false);
        kView.etInput.setSingleLine(true);
        setLeftFunStatus(false);
        setInputSelection(select);
    }

    /**
     * 輸入框區域點擊
     */
    public void onInputAreaClick(View view) {
        kView.ivFacial.setTag(null);
        kView.ivFacial.setSelected(false);
        if (onNewKeyboardListener != null) {
            onNewKeyboardListener.onCloseFun(null);
        }
        int select = kView.etInput.getSelectionStart();
        select = select == 0 ? kView.etInput.length() : select;
        setFunctionBtnSelectStatus(null);
        kView.etInput.setSingleLine(false);
        kView.etInput.setMaxLines(kView.ivInputExpand.isSelected() ? Integer.MAX_VALUE : 4);
        // 收起左偏功能
        setLeftFunStatus(true);
        // 關閉全部底部功能
        setInputSelection(select);
    }


    /**
     * 表情按鈕點擊
     */
    public void onFacialClick(View view) {
        if (!kView.ivFacial.isSelected()) {
            hideKeyboard();
            setFunctionBtnSelectStatus(NewKeyboardFun.FUN_FACIAL);
            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> onNewKeyboardListener.onOpenFacialFun());
            setLeftFunStatus(true);
        } else {
            setFunctionBtnSelectStatus(null);
            openKeyboard();
            onNewKeyboardListener.onCloseFun(null);
        }
    }

    /**
     * 送出按鈕點擊
     */
    public void onSendClick(View view) {
        if (kView.etInput.length() > 0) {
            kView.ivFacial.setTag(null);
            kView.ivFacial.setSelected(false);
        }
        if (onNewKeyboardListener != null) {
            onNewKeyboardListener.onSendAction(kView.etInput.getTextData(), false);
        }
    }

    /**
     * 錄音按鈕點擊
     */
    public void onRecordClick(View view) {
        kView.ivFacial.setTag(null);
        kView.ivFacial.setSelected(false);
        setFunctionBtnSelectStatus(NewKeyboardFun.FUN_RECORD);
        hideKeyboard();
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> onNewKeyboardListener.onOpenRecordFun());
    }

    /**
     * 展開數入區域按鈕點擊
     */
    public void onInputExpandClick(View view) {
        expandInputArea(!view.isSelected());
        view.setSelected(!view.isSelected());
    }


    /**
     * 收合輸入區行為
     */
    private void expandInputArea(boolean isExpand) {
        if (isExpand) {
//            kView.etInput.setSingleLine(false);
            kView.etInput.setMaxLines(Integer.MAX_VALUE);
        } else {
//            kView.etInput.setSingleLine(true);
            kView.etInput.setMaxLines(4);
        }
        if (kView.root.getId() == R.id.nkl_input) {
            CELog.e("");
        }


        CELog.d(kView.root.getLayoutParams().toString());
        CELog.d(kView.etInput.getLayoutParams().toString());
//        R.id.cl_toolbar
        //R.id.rv_message_list
        ConstraintLayout.LayoutParams inputParams = (ConstraintLayout.LayoutParams) kView.etInput.getLayoutParams();
        inputParams.height = isExpand ? ConstraintLayout.LayoutParams.PARENT_ID : ConstraintLayout.LayoutParams.WRAP_CONTENT;

        ConstraintLayout.LayoutParams rootParams = (ConstraintLayout.LayoutParams) kView.root.getLayoutParams();
        rootParams.height = isExpand ? ConstraintLayout.LayoutParams.PARENT_ID : ConstraintLayout.LayoutParams.WRAP_CONTENT;
        rootParams.width = ConstraintLayout.LayoutParams.PARENT_ID;


        rootParams.topToTop = isExpand ? ConstraintLayout.LayoutParams.PARENT_ID : ConstraintLayout.LayoutParams.UNSET;
        rootParams.topToBottom = isExpand ? ConstraintLayout.LayoutParams.UNSET : R.id.x_refresh_layout;

        rootParams.bottomToTop = R.id.fun_media;
        rootParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        rootParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;

        kView.etInput.setLayoutParams(inputParams);
        kView.root.setLayoutParams(rootParams);
//        kView.clArea.setLayoutParams(toolbarParams);
    }

    public void openKeyboard() {
        KeyboardHelper.open(kView.etInput);
    }

    public void hideKeyboard() {
        KeyboardHelper.hide(kView.etInput);
    }

    public void clearFocus() {
        kView.etInput.clearFocus();
        KeyboardHelper.hide(kView.etInput);
    }

    /**
     * 設定游標只是器位置
     */
    private synchronized void setInputSelection(int select) {
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> kView.etInput.setSelection(select));
    }


    public void setFunctionBtnUnSelect(NewKeyboardFun type) {
        if (type == null) {
            return;
        }
        switch (type) {
            case FUN_CAMERA:
                kView.ivCamera.setSelected(false);
                break;
            case FUN_PHOTO:
                kView.ivPic.setSelected(false);
                break;
            case FUN_VIDEO:
                kView.ivVideo.setSelected(false);
                break;
            case FUN_MEDIA:
                kView.ivMedia.setSelected(false);
                break;
            case FUN_FACIAL:
                kView.ivFacial.setSelected(false);
                break;
            case FUN_RECORD:
                kView.ivSend.setSelected(false);
                break;
        }
    }

    public void setFunctionBtnSelectStatus(NewKeyboardFun type) {

        kView.ivCamera.setSelected(false);
        kView.ivPic.setSelected(false);
        kView.ivVideo.setSelected(false);
        kView.ivMedia.setSelected(false);

        kView.ivFacial.setSelected(false);

        // 當初現錄音樣式
        kView.ivSend.setSelected(false);
        if (type == null) {
            return;
        }

        switch (type) {
            case FUN_CAMERA:
                kView.ivCamera.setSelected(true);
                break;
            case FUN_PHOTO:
                kView.ivPic.setSelected(true);
                break;
            case FUN_VIDEO:
                kView.ivVideo.setSelected(true);
                break;
            case FUN_MEDIA:
                kView.ivMedia.setSelected(true);
                break;
            case FUN_FACIAL:
                kView.ivFacial.setSelected(true);
                break;
            case FUN_RECORD:
                kView.ivSend.setSelected(true);
                break;
        }
    }


    /**
     * Get the unfinished editing content (objectification)
     */
    public InputLogBean getUnfinishedEditBean() {
        String text = kView.etInput.getText().toString();
        InputLogBean.InputLogBeanBuilder builder = InputLogBean.Build().text(text);
        return builder.type(InputLogType.TEXT).build();
    }

    public void setUnfinishedEdited(InputLogBean bean) {
        try {
            kView.etInput.setText(bean.getText());
//        kView.etInput.setReady(true);
            kView.etInput.setSelection(kView.etInput.getText().length());
            kView.etInput.setUsingAtInterFace(false);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public NewKeyboardLayout setRichMenuGridCount(int count) {
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), count);
        kView.mhrvRichMenu.setLayoutManager(new GridLayoutManager(getContext(), count));
        return this;
    }

    public void hideRichMenu() {
        kView.mhrvRichMenu.setVisibility(GONE);
    }

    public void setOnItemClickListener(MessageEntity msg, List<RichMenuBottom> datas, BottomRichMeunAdapter.OnItemClickListener onItemClickListener) {
        // Because adjusting the labeling function level requires turning off the labeling function when opening the advanced menu
//            hideMention(inputHET, "");
        // 關閉鍵盤
        hideKeyboard();
        // 關閉鍵盤功能
        onNewKeyboardListener.onCloseFun(null);


        kView.mhrvRichMenu.addItemDecoration(new GridItemDecoration(Color.WHITE));
        kView.mhrvRichMenu.setBackgroundColor(getThemeStyle(entity).getMainColor());
        kView.mhrvRichMenu.setMaxHeight(0);
        kView.mhrvRichMenu.measure(0, 0);

//        this.mOnItemClickListener = onItemClickListener;
//        initRichMenuView(2);
        BottomRichMeunAdapter adapter = new BottomRichMeunAdapter();
        adapter.setData(msg);
        adapter.setDatas(datas);
        adapter.setOnItemClickListener(onItemClickListener);
        kView.mhrvRichMenu.setAdapter(adapter);
//        showPopupWindow(longCLickArray,root,onItemClickListener);
//        keyboardToolbarCL.setVisibility(GONE);
        kView.mhrvRichMenu.setVisibility(VISIBLE);

//            inputHET.requestFocus();
    }


    @Override
    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_camera:
                onCameraClick(v);
                break;
            case R.id.iv_pic:
                onPicClick(v);
                break;
            case R.id.iv_video:
                onVideoClick(v);
                break;
            case R.id.iv_media:
                onMediaClick(v);
                break;
            case R.id.iv_fun_expand:
                onFunctionExpandClick(v);
                break;
            case R.id.et_input:
                onInputAreaClick(v);
                break;
            case R.id.iv_facial:
                onFacialClick(v);
                break;
            case R.id.iv_send:
                if (this.blacklist.contains(NewKeyboardFun.FUN_RECORD)) {
                    onSendClick(v);
                } else {
                    if (kView.etInput.length() > 0) {
                        onSendClick(v);
                    } else {
                        onRecordClick(v);
                    }
                }
                break;
            case R.id.iv_input_expand:
                onInputExpandClick(v);
                break;
        }
    }
}
