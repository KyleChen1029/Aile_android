package tw.com.chainsea.chat.keyboard;

import static androidx.core.content.ContextCompat.getColor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.common.base.Strings;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.multimedia.AMediaBean;
import tw.com.chainsea.android.common.multimedia.MultimediaHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.InputLogBean;
import tw.com.chainsea.ce.sdk.bean.InputLogType;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType;
import tw.com.chainsea.ce.sdk.bean.sticker.StickerItemEntity;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ViewKeyboardbarBinding;
import tw.com.chainsea.chat.keyboard.emoticon.NewEmoticonLayout;
import tw.com.chainsea.chat.keyboard.emoticon.view.ExtraLayout;
import tw.com.chainsea.chat.keyboard.emoticon.view.RecordLayout;
import tw.com.chainsea.chat.keyboard.utils.Utils;
import tw.com.chainsea.chat.keyboard.view.HadEditText;
import tw.com.chainsea.chat.keyboard.view.SoftHandleLayout;
import tw.com.chainsea.chat.messagekit.enums.RichMenuBottom;
import tw.com.chainsea.chat.messagekit.main.adapter.BottomRichMeunAdapter;
import tw.com.chainsea.chat.style.RoomThemeStyle;
import tw.com.chainsea.chat.ui.adapter.entity.RichMenuInfo;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.chat.widget.GridItemDecoration;
import tw.com.chainsea.chat.widget.photo.PhotoBean;

public class ChatKeyboardLayout extends SoftHandleLayout {
    private Context ctx;
    private ViewKeyboardbarBinding binding;
    // Marking function level up
    private OnMentionFeatureListener onMentionFeatureListener;
    private final boolean isLimitedOnlyText = false;
    private boolean isEnableSend = true;
    public int FUNC_RECORD_POS = 0;    //display medias area
    public int FUNC_ORDER_COUNT = 0;
    private int mInputAreaLineCount = 1;
    private static final int InitState = 0;
    private static final int SingleLineOpenState = 1;
    private static final int SingleLineCloseState = 2;
    private static final int TwoOrThreeLineState = 3;
    private static final int MultiLineHasExpandBtnState = 4;
    private int currentState = InitState;
    private String mPreEditContent = "";
    private boolean isSingleLine = false;
    public boolean isRecording = false;
    public boolean isExtraClick = false;
    private boolean isExpandInputArea = false;
    private RecordLayout recordLayout;
    private ExtraLayout extraLayout;
    private NewEmoticonLayout newEmoticonLayout;

    private boolean isRecordEnable = true;
    private boolean click = true;  //只在第一次点击表情时为true
    private int period;
    OnChatKeyBoardListener mOnChatKeyBoardListener;
    private RoomThemeStyle themeStyle = RoomThemeStyle.UNDEF;
    private boolean isFacebookChatRoom = false;
    private boolean isGreenTheme = false;
    private boolean isServiceRoomTheme = false;

    /**
     * 底部功能類型
     */
    public enum BottomFunType {
        FUN_FILE(0, "檔案選擇器"),
        FUN_EXTRA(1, ""),
        FUN_RECORD(2, "錄音機"),
        FUN_PHOTO(3, "圖片選擇器"),
        FUN_FACIAL(4, "表情選擇器"),
        FUN_CAMERA(5, "相機"),
        FUN_VIDEO(6, "錄影");
//        FUN_CONSULT(7, "諮詢");

        private int type;
        private String name;

        BottomFunType(int type, String name) {
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

    public ChatKeyboardLayout(Context context) {
        super(context, null);
        initView(context);
    }

    public ChatKeyboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChatKeyboardLayout);
        initView(context);
        typedArray.recycle();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView(Context context) {
        this.ctx = context;
        //在ViewBinding中使用custom view，綁定方法如下
        binding = ViewKeyboardbarBinding.inflate(LayoutInflater.from(context), this, true);
        isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
        isServiceRoomTheme = ThemeHelper.INSTANCE.isServiceRoomTheme();
//        EmoticonHandler.getInstance(context).loadEmoticonsToMemory();
        initRichMenuView(5);
        setAutoHeightLayoutView(binding.lyFootFunc);

//        binding.rightVerticalSendIV.setEnabled(false);
//        binding.horizontalSendIV.setEnabled(false);

        binding.inputHET.setOnTextChangedInterface(new HadEditText.OnTextChangedInterface() {
            @Override
            public void onBeforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onShowMention(LinkedList<UserProfileEntity> users, HadEditText editText, boolean isMultiSelect, String keyword) {
                showMention(editText, isMultiSelect, keyword);
            }

            @Override
            public void onHideMention(HadEditText editText, String keyword) {
                hideMention(editText, keyword);
            }

            @Override
            public void onNotifyMentionDataChanged(LinkedList<UserProfileEntity> users) {
                notifyMentionDataChanged(users);
            }

            @Override
            public void onSelectionChanged(int selStart, int selEnd) {
//                cancelRecord();
            }

            @Override
            public void onTextChanged(CharSequence arg0, boolean enableSend) {
                if (isRecordEnable) {
                    if (Strings.isNullOrEmpty(binding.inputHET.getText().toString())) {
                        binding.horizontalSendIV.setVisibility(GONE);
                        binding.horizontalRecordIV.setVisibility(VISIBLE);
                    } else {
                        binding.horizontalSendIV.setVisibility(VISIBLE);
                        binding.horizontalRecordIV.setVisibility(GONE);
                    }
                } else {
                    binding.horizontalSendIV.setVisibility(VISIBLE);
                    binding.horizontalRecordIV.setVisibility(GONE);
                }

//                if (TextUtils.isEmpty(arg0.toString())) {
//                    binding.horizontalSendIV.setEnabled(false);
//                } else {
//                    binding.horizontalSendIV.setEnabled(true);
//                }

                isEnableSend = enableSend;

                if (isSingleLine) {
                    int selection = getInputSelection();
                    binding.inputHET.setSingleLine(false);
                    isSingleLine = false;
                    updateStateByTouch();
                    setInputSelection(selection);
                    SystemClock.sleep(10);
                }

                int selection = getInputSelection();

                if (!mPreEditContent.equals(binding.inputHET.getText().toString()) && mInputAreaLineCount != binding.inputHET.getLineCount()) {

                    if (binding.inputHET.getLineCount() != 0) {
                        mInputAreaLineCount = binding.inputHET.getLineCount();
                    }

                    if (mInputAreaLineCount == 0) {
                        mInputAreaLineCount = 1;
                    }

                    switch (mInputAreaLineCount) {
                        case 1:
                            currentState = SingleLineCloseState;
                            updateUIByState();
                            break;
                        case 2:
                        case 3:
                            currentState = TwoOrThreeLineState;
                            updateUIByState();
                            break;
                        case 4:
                        default:
                            currentState = MultiLineHasExpandBtnState;
                            updateUIByState();
                            break;
                    }
                }
                setInputSelection(selection);
                mPreEditContent = binding.inputHET.getText().toString();

            }
        });
        binding.inputHET.requestFocusFromTouch();
        initListener();
    }

    private void initListener() {
        binding.verticalExpandIV.setOnClickListener(this::doExpandAction);
        binding.horizontalExpandIV.setOnClickListener(this::doExpandAction);
        binding.verticalMentionIV.setOnClickListener(this::doMentionAction);
        binding.horizontalMentionIV.setOnClickListener(this::doMentionAction);
        binding.horizontalCameraIV.setOnClickListener(this::doOpenMedia);
        binding.horizontalPicIV.setOnClickListener(this::doPicAction);
        binding.horizontalMoreIV.setOnClickListener(this::doMoreAction);
//        binding.horizontalVideoIV.setOnClickListener(this::doVideoAction);
        binding.facialIV.setOnClickListener(this::doFacialAction);
        binding.horizontalRecordIV.setOnClickListener(this::doRecordAction);
        binding.inputHET.setOnClickListener(this::doInputClickAction);
        binding.inputHET.setOnFocusChangeListener(this::doInputFocusChange);
        binding.inputHET.setOnEditorActionListener(this::doInputEditorAction);
        binding.horizontalSendIV.setOnClickListener(this::doSendAction);
//        binding.rightVerticalSendIV.setOnClickListener(this::doSendAction);
        binding.rightVerticalExpandIV.setOnClickListener(this::doRightExpandAction);
        binding.horizontalPlusIV.setOnClickListener(this::doExtraAction);
    }

    public void setChatRoomEntity(ChatRoomEntity entity, boolean isMentionMode, boolean isAddAll) {
        if (entity != null) {
            this.binding.inputHET.setChatRoomEntity(entity, isMentionMode, isAddAll);
            if (!Strings.isNullOrEmpty(entity.getBusinessId()) && !ChatRoomType.SERVICES_or_SUBSCRIBE.contains(entity.getType())) {
                themeStyle = RoomThemeStyle.BUSINESS;
            } else {
                String self = TokenPref.getInstance(getContext()).getUserId();
                if (ChatRoomType.services.equals(entity.getType())
                    && ServiceNumberType.BOSS.equals(entity.getServiceNumberType())
                    && self.equals(entity.getServiceNumberOwnerId())) {
                    themeStyle = RoomThemeStyle.DISCUSS;
                } else {
                    themeStyle = RoomThemeStyle.of(entity.getType().name());
                }
            }
        }
//        themeStyle();
    }

    public void setThemeStyle(RoomThemeStyle themeStyle) {
        this.themeStyle = themeStyle;
        themeStyle();
    }

    // Set a new theme style
    private void themeStyle() {
        int cameraResource = getCameraResource();
        int pictureResource = getPictureResource();
        int recorderResource = getRecorderResource();
        int plusResource = getPlusResource();
        binding.bottomRichMenuRV.setBackgroundColor(isGreenTheme && themeStyle != RoomThemeStyle.SERVICES ? getColor(mContext, R.color.color_015F57) : themeStyle.getMainColor());
        binding.keyboardToolbarCL.setBackgroundColor(isGreenTheme && themeStyle != RoomThemeStyle.SERVICES ? getColor(mContext, R.color.color_015F57) : themeStyle.getMainColor());
        binding.keyboardToolbarCL.setVisibility(VISIBLE); //聊天室初始化後再顯示鍵盤，避免閃退
        //if (RoomThemeStyle.SERVICES_or_BUSINESS_or_BROADCAST_or_SERVICE_MEMBER.contains(themeStyle)) {
        binding.horizontalCameraIV.setImageResource(cameraResource);
        binding.horizontalPicIV.setImageResource(pictureResource);
        binding.horizontalMoreIV.setImageResource(R.drawable.slector_pin_business);
        binding.horizontalExpandIV.setImageResource(R.drawable.slector_next_business);
        binding.verticalExpandIV.setImageResource(R.drawable.slector_next_business);
        //binding.horizontalVideoIV.setImageResource(R.drawable.slector_video_business);
        binding.horizontalSendIV.setImageResource(R.drawable.slector_send_business);
//            binding.rightVerticalSendIV.setImageResource(R.drawable.slector_send_business);
        binding.horizontalRecordIV.setImageResource(recorderResource);
        binding.horizontalPlusIV.setImageResource(plusResource);
        //}
    }

    private int getCameraResource() {
        int cameraResource;
        if (isGreenTheme) {
            if (themeStyle == RoomThemeStyle.SERVICES) {
                cameraResource = R.drawable.slector_camera_services_green;
            } else {
                cameraResource = R.drawable.slector_camera_business_green;
            }
        } else {
            if (themeStyle == RoomThemeStyle.SERVICES) {
                cameraResource = R.drawable.slector_camera_services_green;
            } else {
                cameraResource = R.drawable.slector_camera;
            }
        }
        return cameraResource;
    }

    private int getPictureResource() {
        int pictureResource;
        if (isGreenTheme) {
            if (themeStyle == RoomThemeStyle.SERVICES) {
                pictureResource = R.drawable.slector_pic_services_green;
            } else {
                pictureResource = R.drawable.slector_pic_business_green;
            }
        } else {
            if (themeStyle == RoomThemeStyle.SERVICES) {
                pictureResource = R.drawable.slector_pic_services_green;
            } else {
                pictureResource = R.drawable.slector_pic;
            }
        }
        return pictureResource;
    }

    private int getRecorderResource() {
        int recorderResource;
        if (isGreenTheme) {
            if (themeStyle == RoomThemeStyle.SERVICES) {
                recorderResource = R.drawable.slector_mic_services_green;
            } else {
                recorderResource = R.drawable.slector_mic_business_green;
            }
        } else {
            if (themeStyle == RoomThemeStyle.SERVICES) {
                recorderResource = R.drawable.slector_mic_services_green;
            } else {
                recorderResource = R.drawable.slector_mic;
            }
        }
        return recorderResource;
    }

    private int getPlusResource() {
        int plusResource;
        if (isGreenTheme) {
            if (themeStyle == RoomThemeStyle.SERVICES) {
                plusResource = R.drawable.selector_extra_services_green;
            } else {
                plusResource = R.drawable.selector_extra_green;
            }
        } else {
            if (themeStyle == RoomThemeStyle.SERVICES) {
                plusResource = R.drawable.selector_extra_services_green;
            } else {
                plusResource = R.drawable.selector_extra;
            }
        }
        return plusResource;
    }

    public View getRootView() {
        return binding.root;
    }

    private void updateStateByTouch() {
        if (currentState == InitState) {
            currentState = SingleLineCloseState;
            updateUIByState();
        } else if (currentState == SingleLineOpenState) {
            if (mInputAreaLineCount == 0 || mInputAreaLineCount == 1) {
                currentState = SingleLineCloseState;
            } else if (mInputAreaLineCount == 2 || mInputAreaLineCount == 3) {
                currentState = TwoOrThreeLineState;
            } else if (mInputAreaLineCount == 4) {
                currentState = MultiLineHasExpandBtnState;
            } else {
                currentState = MultiLineHasExpandBtnState;
            }
            binding.inputHET.setSingleLine(false);
            isSingleLine = false;
            updateUIByState();
        }
    }

    private void updateUIByState() {
        if (isExpandInputArea) {
            return;
        }
        switch (currentState) {
            case InitState:
            case SingleLineOpenState:
                int selection = getInputSelection();
                currentState = SingleLineOpenState;
                binding.inputHET.setSingleLine(true);
                isSingleLine = true;
                isExpandInputArea = false;
                setInputSelection(selection);
                binding.horizontalCameraIV.setVisibility(VISIBLE);
                if (!isFacebookChatRoom) {
                    binding.horizontalVideoIV.setVisibility(GONE);
                }
                binding.horizontalPicIV.setVisibility(VISIBLE);
                binding.horizontalPlusIV.setVisibility(VISIBLE);
                binding.horizontalMoreIV.setVisibility(GONE);
                binding.horizontalExpandIV.setVisibility(GONE);
                binding.rightVerticalExpandIV.setVisibility(GONE);
                break;
            case SingleLineCloseState:
            case TwoOrThreeLineState:
                binding.horizontalCameraIV.setVisibility(GONE);
                binding.horizontalVideoIV.setVisibility(GONE);
                binding.horizontalPicIV.setVisibility(GONE);
                binding.horizontalMoreIV.setVisibility(GONE);
                binding.horizontalExpandIV.setVisibility(VISIBLE);
                binding.rightVerticalExpandIV.setVisibility(GONE);
                binding.horizontalPlusIV.setVisibility(GONE);
                if (RoomThemeStyle.SERVICES.equals(themeStyle)) {
                    binding.horizontalConsultIV.setVisibility(View.GONE);
                }

                if (isRecordEnable) {
                    if (Strings.isNullOrEmpty(binding.inputHET.getText().toString())) {
                        binding.horizontalSendIV.setVisibility(GONE);
                        binding.horizontalRecordIV.setVisibility(VISIBLE);
                    } else {
                        binding.horizontalSendIV.setVisibility(VISIBLE);
                        binding.horizontalRecordIV.setVisibility(GONE);
                    }
                }
                break;
            case MultiLineHasExpandBtnState:
                binding.horizontalCameraIV.setVisibility(GONE);
                binding.horizontalVideoIV.setVisibility(GONE);
                binding.horizontalPicIV.setVisibility(GONE);
                binding.horizontalMoreIV.setVisibility(GONE);
                binding.horizontalExpandIV.setVisibility(VISIBLE);
                binding.horizontalPlusIV.setVisibility(GONE);
                binding.rightVerticalExpandIV.setVisibility(VISIBLE);
                break;
        }
        closeInputArea(false);
    }

    // EVAN_FLAG 2019-12-26 (1.9.0) 展開輸入框
    private void expandInputArea() {
        isExpandInputArea = true;
        expandKeyboardLayout(true);
        binding.rightVerticalExpandIV.setImageResource(R.drawable.collapse_white);
    }

    // EVAN_FLAG 2019-12-26 (1.9.0) 收起輸入框
    private void closeInputArea(boolean isOpenKeyBoard) {
        isExpandInputArea = false;
        binding.inputHET.setMaxLines(4);
        expandKeyboardLayout(false);
        if (isOpenKeyBoard) {
            openSoftKeyboard(binding.inputHET);
        }
        binding.rightVerticalExpandIV.setImageResource(R.drawable.expand_white);
    }

    /**
     * EVAN_FLAG 2020-01-06 (1.9.0) 解決展開輸入框導致輸入文字被覆蓋問題。
     */
    private void expandKeyboardLayout(boolean isExpand) {
        int reSize = -1;

        ConstraintLayout provisionalMemberList = null;
        if (mContext != null && mContext instanceof Activity) {
            provisionalMemberList = ((Activity) mContext).findViewById(R.id.scope_provisional_member_list);
        }

        if (isExpand) {
            binding.inputHET.setMaxLines(Integer.MAX_VALUE);
            Rect r = new Rect();
            binding.root.getWindowVisibleDisplayFrame(r);
            int mH = UiHelper.dip2px(getContext(), 44);
            reSize = r.height() - mH;
            // 這邊處理如果有臨時成員列表，展開的話縮小的 icon 會被擋到 故另做處理
            if (provisionalMemberList != null && provisionalMemberList.getVisibility() == View.VISIBLE) {
                reSize = reSize - provisionalMemberList.getHeight();
            }
        }
        ConstraintLayout.LayoutParams inputParams = (ConstraintLayout.LayoutParams) binding.inputHET.getLayoutParams();
        inputParams.height = isExpand ? ConstraintLayout.LayoutParams.MATCH_PARENT : ConstraintLayout.LayoutParams.WRAP_CONTENT;
        RelativeLayout.LayoutParams rootParams = (LayoutParams) binding.root.getLayoutParams();
        rootParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);

        rootParams.height = isExpand ? reSize : LayoutParams.WRAP_CONTENT;
        rootParams.width = LayoutParams.MATCH_PARENT;

        // 這邊處理如果有臨時成員列表，展開的話縮小的 icon 會被擋到 故另做處理
        if (isExpand) {
            rootParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            if (provisionalMemberList != null && provisionalMemberList.getVisibility() == View.VISIBLE) {
                rootParams.setMargins(0, provisionalMemberList.getHeight(), 0, 0);
            }
        } else {
            rootParams.setMargins(0, 0, 0, 0);
        }

        ConstraintLayout.LayoutParams toolbarParams = (ConstraintLayout.LayoutParams) binding.keyboardToolbarCL.getLayoutParams();
        toolbarParams.height = isExpand ? reSize : ConstraintLayout.LayoutParams.WRAP_CONTENT;

        binding.inputHET.setLayoutParams(inputParams);
        binding.root.setLayoutParams(rootParams);

        binding.keyboardToolbarCL.setLayoutParams(toolbarParams);
    }


    @Override
    public void OnSoftKeyboardPop(int height) {
        if (mOnChatKeyBoardListener != null) {
            mOnChatKeyBoardListener.onSoftKeyboardStartOpened(height);
        }

        super.OnSoftKeyboardPop(height);
        if (mOnChatKeyBoardListener != null) {
            mOnChatKeyBoardListener.onSoftKeyboardEndOpened(height);
        }
        if (isExpandInputArea) {
            expandKeyboardLayout(true);
        }
    }


    // EVAN_FLAG 1/21/21 (1.15.1 需要合併)
    @Override
    public void OnSoftKeyboardClose() {
        super.OnSoftKeyboardClose();
        if (mOnChatKeyBoardListener != null) {
            mOnChatKeyBoardListener.onSoftKeyboardClosed();
        }

        if (isExpandInputArea) {
            expandKeyboardLayout(true);
        }
    }


    /**
     * 左側收放更多功能鈕
     */
    void doExpandAction(View view) {
        int selection = getInputSelection();
        currentState = SingleLineOpenState;
        binding.inputHET.setSingleLine(true);
        setEditableState(false);

        isSingleLine = true;
        isExpandInputArea = false;
        updateUIByState();
        setInputSelection(selection);
    }

    /**
     * 左側@AT提及功能鈕
     */
    void doMentionAction(View view) {

    }

    void doOpenMedia(View view) {
        if (mOnChatKeyBoardListener != null) {
            mOnChatKeyBoardListener.onOpenCameraDialog();
        }
    }

    /**
     * 左側相機功能鈕
     */
    public void doCameraAction() {
        binding.lyFootFunc.removeAllViews();
        isRecording = false;
        isExtraClick = false;
        binding.facialIV.setTag(null);
        setIconState(BottomFunType.FUN_CAMERA);
        if (mOnChatKeyBoardListener != null) {
            mOnChatKeyBoardListener.onOpenCamera();
        }
    }

    /**
     * 左側擴充功能鈕
     */
    public void doExtraAction(View view) {
        if (isExtraClick) {
            binding.lyFootFunc.removeView(extraLayout);
            isExtraClick = false;
            clearIconState();
            return;
        }

        setBottomOpen(true);
        isRecording = false;
        binding.facialIV.setTag(null);
        binding.lyFootFunc.removeAllViews();
        setIconState(BottomFunType.FUN_EXTRA);

        changeHeight(-1);
        closeSoftKeyboard(binding.inputHET);
        hideAutoView();

        if (click) {
            OnSoftKeyboardPop(Utils.getDefKeyboardHeight(mContext));
            click = false;
        }
        if (mOnChatKeyBoardListener != null) {
            view.post(() -> mOnChatKeyBoardListener.onOpenExtraArea());
        }
        if (mOnChatKeyBoardListener != null) {
            view.post(() -> mOnChatKeyBoardListener.onOpenRecord());
        }
    }

    /**
     * 左側照片功能鈕
     */
    public void doPicAction(View view) {
        setBottomOpen(true);
        isRecording = false;
        isExtraClick = false;
        binding.lyFootFunc.removeAllViews();
        binding.facialIV.setTag(null);
        setIconState(BottomFunType.FUN_PHOTO);
        if (click) {
            OnSoftKeyboardPop(Utils.getDefKeyboardHeight(mContext));
            click = false;
        }
        changeHeight(-1);
        closeSoftKeyboard(binding.inputHET);
        hideAutoView();
        if (mOnChatKeyBoardListener != null) {
            binding.horizontalPicIV.post(() -> {
                mOnChatKeyBoardListener.onOpenPhotoSelector(false);
            });
        }
    }

    public void doChangePicSelectorAction() {
        binding.facialIV.setTag(null);
        setIconState(BottomFunType.FUN_PHOTO);
        closeSoftKeyboard(binding.inputHET);
        hideAutoView();
        if (mOnChatKeyBoardListener != null) {
            binding.horizontalPicIV.post(() -> mOnChatKeyBoardListener.onOpenPhotoSelector(true));
        }
    }

    /**
     * 左側多媒體功能按鈕
     */
    void doMoreAction(View view) {
        setBottomOpen(true);
        isRecording = false;
        isExtraClick = false;
        binding.lyFootFunc.removeAllViews();
        binding.facialIV.setTag(null);
        setIconState(BottomFunType.FUN_FILE);
        if (click) {
            OnSoftKeyboardPop(Utils.getDefKeyboardHeight(mContext));
            click = false;
        }
        changeHeight(-1);
        closeSoftKeyboard(binding.inputHET);
        hideAutoView();
        if (mOnChatKeyBoardListener != null) {
            view.post(() -> mOnChatKeyBoardListener.onOpenMultimediaSelector());
        }
    }

    /**
     * 左側錄影功能鈕
     */
    public void doVideoAction() {
        setBottomOpen(true);
        isRecording = false;
        isExtraClick = false;
        binding.lyFootFunc.removeAllViews();
        binding.facialIV.setTag(null);
        setIconState(BottomFunType.FUN_VIDEO);
        if (mOnChatKeyBoardListener != null) {
            mOnChatKeyBoardListener.onOpenVideo();
        }
    }

    /**
     * 左,右側表情功能鈕
     */
    void doFacialAction(View view) {
        setBottomOpen(false);
        isRecording = false;
        isExtraClick = false;
        if (view.getTag() != null) {
            view.setTag(null);
            openSoftKeyboard(binding.inputHET);
            binding.facialIV.setImageResource(R.drawable.slector_face);
            changeHeight(Utils.getDefKeyboardHeight(mContext));
            return;
        }

        view.setTag("open");
        setIconState(BottomFunType.FUN_FACIAL);

        int selection = getInputSelection();

        if (click) {
            OnSoftKeyboardPop(Utils.getDefKeyboardHeight(mContext));
            click = false;
        }

        closeSoftKeyboard(binding.inputHET);
        changeHeight(Utils.getDefKeyboardHeight(mContext));

        show(BottomFunType.FUN_FACIAL);

        updateStateByTouch();
        setInputSelection(selection);

        view.postDelayed(() -> {
            int keyboardHeight = Utils.getDefKeyboardHeight(mContext);
            showAutoView(keyboardHeight);
            if (mOnChatKeyBoardListener != null) {
                mOnChatKeyBoardListener.onOpenEmoticon();
            }
        }, 200L);

    }

    /**
     * 右側錄音功能鈕
     */
    void doRecordAction(View view) {
        if (isRecording) {
            binding.lyFootFunc.removeView(recordLayout);
            isRecording = false;
            clearIconState();
            return;
        }
        isExtraClick = false;
        setBottomOpen(false);
        binding.facialIV.setTag(null);
        binding.lyFootFunc.removeAllViews();
        setIconState(BottomFunType.FUN_RECORD);
        if (click) {
            OnSoftKeyboardPop(Utils.getDefKeyboardHeight(mContext));
            click = false;
        }

        recordClick();
        if (mOnChatKeyBoardListener != null) {
            view.post(() -> mOnChatKeyBoardListener.onOpenRecord());
        }
    }

    /**
     * 輸入框點擊事件
     */
    void doInputClickAction(View view) {
        setBottomOpen(false);
        isRecording = false;
        isExtraClick = false;
        if (mOnChatKeyBoardListener != null) {
            mOnChatKeyBoardListener.onInputClick();
        }
        binding.lyFootFunc.removeAllViews();
        binding.facialIV.setTag(null);
        binding.facialIV.setImageResource(R.drawable.slector_face);
        clearIconState();

        int selection = getInputSelection();

        if (!binding.inputHET.isFocused()) {
            binding.inputHET.setFocusableInTouchMode(true);
            binding.inputHET.requestFocus();
            InputMethodManager imm = (InputMethodManager) binding.inputHET.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(binding.inputHET, InputMethodManager.SHOW_IMPLICIT);
        }

        updateStateByTouch();
        setInputSelection(selection);
    }

    /**
     * 輸入框聚焦事件
     */
    void doInputFocusChange(View view, boolean isFocus) {
        setBottomOpen(false);
        if (mOnChatKeyBoardListener != null) {
            mOnChatKeyBoardListener.onInputClick();
        }
        if (isFocus) {
            binding.facialIV.setTag(null);
            binding.facialIV.setImageResource(R.drawable.slector_face);
            setEditableState(true);
            updateStateByTouch();
        } else {
            setEditableState(false);
        }
    }

    /**
     * 輸入框編輯事件
     */
    public boolean doInputEditorAction(TextView v, int actionId, KeyEvent event) {
        if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            binding.inputHET.clearMode();
        }
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            if (mOnChatKeyBoardListener != null) {
                binding.inputHET.clearMode();
                mOnChatKeyBoardListener.onSendBtnClick(binding.inputHET.getTextData(), isEnableSend);
            }
            return true;
        }
        return false;
    }


    /**
     * 右側送出訊息功能鈕
     */
    void doSendAction(View view) {
        setBottomOpen(false);
        if (mOnChatKeyBoardListener != null) {
            HadEditText.SendData sendData = binding.inputHET.getTextData();
            if (sendData.getContent().isEmpty()) {
                Toast.makeText(getContext(), getContext().getString(R.string.text_can_not_send_empty_message), Toast.LENGTH_SHORT).show();
                return;
            }
            mOnChatKeyBoardListener.onSendBtnClick(binding.inputHET.getTextData(), isEnableSend);
        }
    }

    /**
     * 右側展開輸入框功能鈕
     */
    void doRightExpandAction(View view) {
        setBottomOpen(false);
        if (isExpandInputArea) {
            isExpandInputArea = false;
            currentState = binding.inputHET.getLineCount();
            updateUIByState();
            isExpandInputArea = true;
            closeInputArea(true);
        } else {
            expandInputArea();
        }
    }

    private void setIconState(BottomFunType funType) {
        binding.horizontalCameraIV.setSelected(false);
        binding.horizontalVideoIV.setSelected(false);
        binding.horizontalPicIV.setSelected(false);
        binding.horizontalPlusIV.setSelected(false);
        binding.facialIV.setSelected(false);
        binding.facialIV.setImageResource(R.drawable.slector_face);
        binding.horizontalMoreIV.setSelected(false);
        binding.horizontalRecordIV.setSelected(false);
        binding.horizontalConsultIV.setSelected(false);
        switch (funType) {
            case FUN_RECORD:
                binding.horizontalRecordIV.setSelected(true);
                break;
            case FUN_FACIAL:
                binding.facialIV.setSelected(true);
                binding.facialIV.setImageResource(R.drawable.keyboard_gray);
                break;
            case FUN_PHOTO:
                binding.horizontalPicIV.setSelected(true);
                break;
            case FUN_FILE:
                binding.horizontalMoreIV.setSelected(true);
                break;
            case FUN_VIDEO:
                binding.horizontalVideoIV.setSelected(true);
                break;
            case FUN_CAMERA:
                binding.horizontalCameraIV.setSelected(true);
                break;
            case FUN_EXTRA:
                binding.horizontalPlusIV.setSelected(true);
                break;
//            case FUN_CONSULT:
//                binding.horizontalConsultIV.setSelected(true);
//                break;
        }
    }


    public void setRecordIconState() {
        binding.horizontalCameraIV.setSelected(false);
        binding.horizontalVideoIV.setSelected(false);
        binding.horizontalPicIV.setSelected(false);
        binding.horizontalConsultIV.setSelected(false);
        binding.facialIV.setSelected(false);
        binding.facialIV.setImageResource(R.drawable.slector_face);
        binding.horizontalMoreIV.setSelected(false);
        binding.horizontalPlusIV.setSelected(false);
    }

    public void clearIconState() {
        binding.horizontalCameraIV.setSelected(false);
        binding.horizontalVideoIV.setSelected(false);
        binding.horizontalPicIV.setSelected(false);
        binding.facialIV.setSelected(false);
        binding.facialIV.setImageResource(R.drawable.slector_face);
        binding.horizontalMoreIV.setSelected(false);
        binding.horizontalRecordIV.setSelected(false);
        binding.horizontalConsultIV.setSelected(false);
        binding.horizontalPlusIV.setSelected(false);
    }

    private void setInputSelection(int selection) {
        int length = binding.inputHET.getText().toString().length();
        binding.inputHET.setSelection(Math.min(length, selection));
    }

    private int getInputSelection() {
        return binding.inputHET.getSelectionStart();
    }

    /**
     * 影藏＠AT提到選單
     */
    public void hideMention(HadEditText editText, String keyword) {
        if (this.onMentionFeatureListener != null) {
            this.onMentionFeatureListener.onHideMention(editText, keyword);
        }
    }

    /**
     * 顯示＠AT提到選單
     */
    private void showMention(HadEditText editText, boolean isMultiSelect, String keyword) {
        if (this.onMentionFeatureListener != null) {
            this.onMentionFeatureListener.onShowMention(editText, isMultiSelect, keyword);
        }
    }

    /**
     * 通知更新＠AT資料
     */
    private void notifyMentionDataChanged(LinkedList<UserProfileEntity> users) {
        if (this.onMentionFeatureListener != null) {
            this.onMentionFeatureListener.onNotifyMentionDataChanged(users);
        }
    }

    public ChatKeyboardLayout setOnMentionFeatureListener(OnMentionFeatureListener onMentionFeatureListener) {
        this.onMentionFeatureListener = onMentionFeatureListener;
        return this;
    }

    public void appendMentionSelectById(String id) {
        if (binding.inputHET != null) {
            binding.inputHET.appendMentionSelectById(id);
        }
    }

    private void initRichMenuView(int count) {
        binding.bottomRichMenuRV.setBackgroundColor(getColor(mContext, isGreenTheme && isServiceRoomTheme ? R.color.color_6BC2BA : isGreenTheme ? R.color.color_015F57 : R.color.colorPrimary));
        binding.bottomRichMenuRV.setMaxHeight(0);
        binding.bottomRichMenuRV.setLayoutManager(new GridLayoutManager(getContext(), count));
        binding.bottomRichMenuRV.addItemDecoration(isGreenTheme && !isServiceRoomTheme ? new GridItemDecoration(Color.BLACK) : new GridItemDecoration(Color.WHITE));
        binding.bottomRichMenuRV.setItemAnimator(new DefaultItemAnimator());
        binding.bottomRichMenuRV.setHasFixedSize(true);
        binding.bottomRichMenuRV.measure(0, 0);
    }

    public void setOnItemClickListener(MessageEntity msg, List<RichMenuBottom> datas, List<RichMenuInfo> aiff, BottomRichMeunAdapter.OnItemClickListener onItemClickListener, BottomRichMeunAdapter.OnAiffItemClickListener onAiffItemClickListener) {
        // EVAN_FLAG 2019-12-24 (1.9.0) 因為調整標註功能層級需要在開啟進階選單時關閉標註功能
        hideMention(binding.inputHET, "");
        BottomRichMeunAdapter adapter = new BottomRichMeunAdapter();
        adapter.setData(msg);
        adapter.setDatas(datas);
        adapter.setAiffData(aiff);
        adapter.setOnItemClickListener(onItemClickListener);
        adapter.setOnAiffItemClickListener(onAiffItemClickListener);
        binding.bottomRichMenuRV.setAdapter(adapter);
        binding.keyboardToolbarCL.setVisibility(GONE);
        binding.bottomRichMenuRV.setVisibility(VISIBLE);
        binding.inputHET.requestFocus();
    }

    public ChatKeyboardLayout setRichMenuGridCount(int count) {
        binding.bottomRichMenuRV.setLayoutManager(new GridLayoutManager(getContext(), count));
        return this;
    }

    public boolean isOpenFuncView() {
        if (binding.lyFootFunc != null && binding.lyFootFunc.isShown()) {
            binding.facialIV.setTag(null);
            hideAutoView();
            return true;
        }
        return false;
    }

    public View getRichMenuRecyclerView() {
        return this.binding.bottomRichMenuRV;
    }

    public void setIsBlock(boolean isBlock) {
        if (isBlock) {
            binding.llBarChatControl.setVisibility(VISIBLE);
        } else {
            binding.llBarChatControl.setVisibility(GONE);
        }
    }

    private void setEditableState(boolean b) {
        binding.inputHET.setFocusable(b);
        binding.inputHET.setFocusableInTouchMode(b);
        binding.inputHET.requestFocus();
        updateUIByState();
    }

    public HadEditText getInputHET() {
        return binding.inputHET;
    }

    public void clearInputArea() {
        binding.inputHET.setText("");

        if (isExpandInputArea) {
            closeInputArea(false);
        }

        currentState = SingleLineCloseState;
        updateUIByState();
    }

    public void setInputHETText(String content) {
        binding.inputHET.setReady(false).setText(content);
        binding.inputHET.setReady(true);
        binding.inputHET.setSelection(binding.inputHET.getText().length());
    }

    public void setUnfinishedEdited(InputLogBean bean) {
        binding.inputHET.setUsingAtInterFace(InputLogType.AT.equals(bean.getType()))
            .setReady(false).setText(bean.getText());

        binding.inputHET.setReady(true);
        binding.inputHET.setSelection(binding.inputHET.getText().length());
        binding.inputHET.setUsingAtInterFace(false);
    }


    public void hideKeyboard() {
        hideBottomPop();
        binding.keyboardToolbarCL.setVisibility(GONE);
        binding.bottomRichMenuRV.setVisibility(VISIBLE);
    }

    public void invisibleKeyboard() {
        hideBottomPop();
        binding.keyboardToolbarCL.setVisibility(View.INVISIBLE);
        binding.bottomRichMenuRV.setVisibility(View.GONE);
    }

    public void showKeyboard() {
        binding.keyboardToolbarCL.setVisibility(VISIBLE);
        binding.bottomRichMenuRV.setVisibility(GONE);
    }

    /**
     * hide keyboard or emoticon area or media area
     */
    public void hideBottomPop() {
        hideAutoView();
        closeSoftKeyboard(binding.inputHET);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (binding.lyFootFunc.isShown()) {
                binding.facialIV.setTag(null);
                hideAutoView();
                return true;
            } else {
                return super.dispatchKeyEvent(event);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void recordClick() {
        if (mOnChatKeyBoardListener != null) {
            mOnChatKeyBoardListener.onRecordingStartAction();
        }
    }

    // 隱藏左邊功能
    public void hideLeftFeature() {
        binding.leftHorizontalFeatureCL.setVisibility(View.GONE);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.inputHET.getLayoutParams();
        params.leftMargin = 20;
    }

    // 隱藏表情功能
    public void hideEmojiFeature() {
        binding.facialIV.setVisibility(View.GONE);
    }

    // 隱藏錄影功能
    public void hideRecordVideoFeature() {
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
            isFacebookChatRoom = true;
            if (binding.horizontalVideoIV.getVisibility() == View.GONE) return;
            binding.horizontalVideoIV.setVisibility(View.GONE);
        });
    }

    // 顯示錄影功能
    public void showRecordVideoFeature() {
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
            isFacebookChatRoom = false;
            if (binding.horizontalVideoIV.getVisibility() == View.VISIBLE) return;
            binding.horizontalVideoIV.setVisibility(View.VISIBLE);
        });
    }

    public void setRecordEnable(boolean isEnable) {
        isRecordEnable = isEnable;
        if (!isEnable) {
            binding.horizontalSendIV.setSelected(true);
            binding.horizontalSendIV.setVisibility(VISIBLE);
            binding.horizontalRecordIV.setVisibility(GONE);
        }
    }

    /**
     * 錄音機
     */
    public void recorder() {
        recordLayout = new RecordLayout(this.ctx, (path, time) -> {
            if (mOnChatKeyBoardListener != null) {
                mOnChatKeyBoardListener.onRecordingSendAction(path, time);
            }
            return null;
        });
        FUNC_RECORD_POS = FUNC_ORDER_COUNT;
        ++FUNC_ORDER_COUNT;
    }


    public void show(BottomFunType type) {
        binding.lyFootFunc.removeAllViews();
        boolean status = true;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        switch (type) {
            case FUN_FILE:
//                binding.lyFootFunc.addView(mediaLayout, params);
//                mediaSelector2Layout
//                binding.lyFootFunc.addView(mediaSelector2Layout, params);
                break;
            case FUN_PHOTO:
//                binding.lyFootFunc.addView(photoLayout, params);
                break;
            case FUN_FACIAL:
                if (newEmoticonLayout == null) {
                    newEmoticonLayout = new NewEmoticonLayout(this.ctx);
                }

                newEmoticonLayout.setOnEmoticonSelectListener(new NewEmoticonLayout.OnEmoticonSelectListener<StickerItemEntity>() {
                    @Override
                    public void onEmoticonSelect(StickerItemEntity entity) {
                        binding.inputHET.requestFocus();
                        int index = binding.inputHET.getSelectionStart();
                        Editable editable = binding.inputHET.getEditableText();
                        if (index < 0) {
                            editable.append(entity.getName());
                        } else {
                            editable.insert(index, entity.getName());
                        }
                    }

                    @Override
                    public void onStickerSelect(StickerItemEntity entity, Drawable drawable) {
                        if (mOnChatKeyBoardListener != null) {
                            mOnChatKeyBoardListener.onStickerClicked(entity.getId(), entity.getStickerPackageId());
                        }
                    }
                });

                binding.lyFootFunc.addView(newEmoticonLayout, params);
                break;
            case FUN_RECORD:
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                binding.lyFootFunc.addView(recordLayout, params);
                break;
            case FUN_EXTRA:
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                binding.lyFootFunc.addView(extraLayout, params);
                break;
            default:
                status = false;
                break;

        }
        binding.lyFootFunc.setVisibility(status ? VISIBLE : GONE);
    }


    public void setOnKeyBoardBarListener(OnChatKeyBoardListener mOnChatKeyBoardListener) {
        this.mOnChatKeyBoardListener = mOnChatKeyBoardListener;
    }

    public interface OnChatKeyBoardListener {
        void onSendBtnClick(HadEditText.SendData sendData, boolean enableSend);

        void onRecordingSendAction(String path, final int duration);

        void onRecordingStartAction();

        void onUserDefEmoticonClicked(String tag, String uri);

        void onStickerClicked(String stickerId, String packageId);

        void onOpenCamera();

        void onOpenVideo();

        void onOpenFolders();

        void onOpenGallery();

        void onOpenPhotoSelector(boolean isChange);

        void onOpenConsult();

        void toMediaSelectorPreview(boolean isOriginal, String type, String current, TreeMap<String, String> data, int position);

        void onOpenMultimediaSelector();

        void onOpenRecord();

        void onOpenEmoticon();

//        void onPicSelected(List<PhotoBean> list);

        void onMediaSelector(MultimediaHelper.Type type, List<AMediaBean> list, boolean isOriginal);

        void onSoftKeyboardStartOpened(int keyboardHeightInPx);

        void onSoftKeyboardEndOpened(int keyboardHeightInPx);

        void onInputClick();

        void onSoftKeyboardClosed();

        void onOpenExtraArea();

        void onBusinessCardSend();

        void onBusinessMemberCardSend();

        void onOpenCameraDialog();

        void onSlideUpSendImage(MultimediaHelper.Type type, List<AMediaBean> list, boolean isOriginal);
    }

    public void setSelect(BottomFunType type, boolean selected) {
        if (Objects.requireNonNull(type) == BottomFunType.FUN_RECORD) {
            binding.horizontalRecordIV.setSelected(selected);
            startRecord();
        }
    }

    public void doInitExtraArea(boolean isBusinessCardSetting, ChatRoomEntity entity, boolean isProvisionMember, boolean isBossServiceNumberOwner) {
        extraLayout = new ExtraLayout(this.ctx, isBusinessCardSetting, entity, isProvisionMember, isBossServiceNumberOwner, () -> {
            //電子名片
            if (mOnChatKeyBoardListener != null) {
                mOnChatKeyBoardListener.onBusinessCardSend();
            }
            return null;
        }, () -> {
            //附件
            if (mOnChatKeyBoardListener != null) {
                doMoreAction(binding.horizontalMoreIV);
            }
            return null;
        }, () -> {
            //企業會員卡片
            if (mOnChatKeyBoardListener != null) {
                mOnChatKeyBoardListener.onBusinessMemberCardSend();
            }
            return null;
        });
        binding.horizontalPlusIV.setSelected(true);
        closeSoftKeyboard(binding.inputHET);
        show(BottomFunType.FUN_EXTRA);
        isExtraClick = true;
    }

    public void startRecord() {
        binding.horizontalRecordIV.setSelected(true);
        closeSoftKeyboard(binding.inputHET);
        show(BottomFunType.FUN_RECORD);
        isRecording = true;
    }

//    public void onUpdateProfile(String roomId) {
//        if (this.binding.inputHET != null) {
//            this.binding.inputHET.makeUpMemberList(true, roomId);
//        }
//    }

    public void setChatDisable(String reason) {
        binding.llBarChatControl.setVisibility(VISIBLE);
        binding.btnChatDisableReason.setText(reason);
    }

    public void setChatEnable() {
        binding.llBarChatControl.setVisibility(GONE);
    }

    public void setInputHint(CharSequence hint) {

        SpannableString span = new SpannableString(hint);
        span.setSpan(new RelativeSizeSpan(0.9f), 0, hint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.inputHET.setHint(span);
    }


    public void handleEvent(EventMsg eventMsg) {
        if (eventMsg.getCode() == MsgConstant.NOTICE_REFRESH_MENTION_DATA) {
            binding.inputHET.refreshMemberList();
        }
    }

    public Group getMetaOverTimeView() {
        return binding.metaOvertimeGroup;
    }

    public TextView getMetaOverTimeTextView() {
        return binding.tvMetaOvertime;
    }

    public void setFacebookOverTimeViewClickListener(OnClickListener onClickListener) {
        binding.tvMetaOvertime.setOnClickListener(onClickListener);
    }


    // EVAN_FLAG 2019-12-23 (1.9.0) 標註功能層級上提
    public interface OnMentionFeatureListener {
        void onShowMention(HadEditText editText, boolean isMultiSelect, String keyword);

        void onHideMention(HadEditText editText, String keyword);

        void onNotifyMentionDataChanged(LinkedList<UserProfileEntity> users);
    }

    public void setKeyboardDisabled(boolean isDisabled, String tipMessage) {
        binding.scopeMessageBossRoom.setVisibility(isDisabled ? View.VISIBLE : View.GONE);
        binding.tvMessage.setText(tipMessage);
        binding.scopeMessageBossRoom.setOnClickListener(v -> { /* do nothing */});
        if (isDisabled) binding.inputHET.clearFocus();
    }
}
