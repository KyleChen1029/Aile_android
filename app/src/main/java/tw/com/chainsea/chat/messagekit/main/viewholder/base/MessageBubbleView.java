package tw.com.chainsea.chat.messagekit.main.viewholder.base;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import java.lang.ref.WeakReference;
import java.util.Objects;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.animator.AnimatorHelper;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.FacebookTag;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.customview.AvatarIcon;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.http.ce.model.User;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.messagekit.SwipeMenuLayout;
import tw.com.chainsea.chat.messagekit.lib.MessageDomino;
import tw.com.chainsea.chat.messagekit.main.viewholder.listener.OnMessageItemEvanListener;
import tw.com.chainsea.chat.util.AvatarKit;
import tw.com.chainsea.chat.util.NoDoubleClickListener;
import tw.com.chainsea.chat.util.TextViewHelper;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.chat.util.TimeUtil;
import tw.com.chainsea.custom.view.image.CircleImageView;
import tw.com.chainsea.custom.view.layout.CoordinateFrameLayout;

/**
 * refactor by evan on 2019-10-17
 */
// EVAN_REFACTOR: 2019-10-18
//  信息文字過長導致UI遮蓋時間UI，
//  文字訊息 AutoLink 導致點擊事件被佔據，
//  信息點擊事件，單擊、雙擊、長按，並增加震動反饋，
//  當多選模式，雙擊與長按禁用，單擊信息或item本身都連動checkBox select，
//  當多選模式，點擊頭像禁用且連動checkBox select，
public abstract class MessageBubbleView<C extends IMessageContent> extends MessageViewBase {
    private static final String TAG = MessageBubbleView.class.getSimpleName();
    private final AvatarKit avatarKit = new AvatarKit();

    private MessageEntity message;
    protected String userId;

    protected LinearLayout rootLayout;
    WeakReference<AvatarIcon> avatarLeftIV;
    WeakReference<AvatarIcon> avatarRightIV;
    ImageView progressLoadingIV;
    protected TextView timeTV;
    ImageView errorIV;
    CoordinateFrameLayout containerCFL;
    public CheckBox checkBox;
    TextView fromTV;
    LinearLayout containerLL;
    TextView nameTV;
    LinearLayout readLL;
    protected WeakReference<LinearLayout> bodyContainer;
    WeakReference<ViewGroup> avatarLeftRL;
    WeakReference<ImageView> channelIV;
    protected LinearLayout maskLayerLL;
    ImageView facebookIcon;

    protected WeakReference<SwipeMenuLayout> mSwipeMenuLayout;
    protected boolean isGreenTheme;

    // 取得訊息內容 layout.xml
    abstract protected int getContentResId();

    // 綁定內容 View
    abstract protected void bindContentView(C c);

    abstract protected View getChildView();

    // 單點擊事件
    public void onClick(View v, MessageEntity message) {
        if (onMessageControlEventListener != null) {
            onMessageControlEventListener.onItemClick(message);
        }
    }

    // 雙點事件
    public abstract void onDoubleClick(View v, MessageEntity message);

    // 長按事件
    public abstract void onLongClick(View v, float x, float y, MessageEntity message);


    public MessageBubbleView(@NonNull ViewBinding binding) {
        super(binding.getRoot());
        this.isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
        findRootView(binding.getRoot());
        this.userId = TokenPref.getInstance(itemView.getContext()).getUserId();
        checkBox.setButtonDrawable(this.isGreenTheme ? R.drawable.green_gray_checkbox_style : R.drawable.default_checkbox_style);
        initListener();
    }

    private void findRootView(View view) {
        rootLayout = view.findViewById(R.id.ll_msg_bubble_root);
        avatarLeftIV = new WeakReference<>(view.findViewById(R.id.msg_avatar_left));
        avatarRightIV = new WeakReference<>(view.findViewById(R.id.msg_avatar_right));
        progressLoadingIV = view.findViewById(R.id.msg_progress);
        errorIV = view.findViewById(R.id.msg_error);
        containerCFL = view.findViewById(R.id.msg_container);
        checkBox = view.findViewById(R.id.check_box);
        fromTV = view.findViewById(R.id.from);
        containerLL = view.findViewById(R.id.msg_container_layout);
        nameTV = view.findViewById(R.id.account_name);
        bodyContainer = new WeakReference<>(view.findViewById(R.id.msg_body));
        avatarLeftRL = new WeakReference<>(view.findViewById(R.id.avatar_left_l));
        channelIV = new WeakReference<>(view.findViewById(R.id.iv_channel));
        maskLayerLL = view.findViewById(R.id.mask_layer);
        mSwipeMenuLayout = new WeakReference<>(view.findViewById(R.id.layout_swip));
        facebookIcon = view.findViewById(R.id.iv_facebook_icon);
        progressLoadingIV.setImageResource(this.isGreenTheme ? R.drawable.ic_sending_green : R.drawable.ic_sending);
    }


    private void initListener() {
        if (nameTV != null) {
            nameTV.setOnClickListener(this::doNameClickAction);
        }
        if (rootLayout != null) {
            rootLayout.setOnClickListener(this::doItemClickAction);
        }
        if (errorIV != null) {
            errorIV.setOnClickListener(this::doRetryMessageAction);
        }
        if (checkBox != null) {
            checkBox.setOnCheckedChangeListener(this::doMessageSelectedAction);
        }


        // 外部進線的聊天室無法使用滑動回覆，是另一個 xml 故用 swipeMenuLayout 是否 null 來判斷
        // 卡片訊息也無法回覆，用 flag 判斷
        if (mSwipeMenuLayout.get() != null) {
            // 滑動回覆
            mSwipeMenuLayout.get().setOnExpandListener(() -> {
                if (mOnMessageSlideReply.get() != null) {
                    mOnMessageSlideReply.get().onMessageSlideReply(message);
                    mSwipeMenuLayout.get().quickClose();
                }
            });
        }
    }

    @Override
    public MessageViewBase setMessageReplyEnable(boolean enable) {
        if (mSwipeMenuLayout.get() != null) {
            mSwipeMenuLayout.get().post(() -> {
                mSwipeMenuLayout.get().setSwipeEnable(false);
            });
        }
        return this;
    }

    void doNameClickAction(View view) {
        if (checkBox.getVisibility() == View.VISIBLE) {
            boolean isCheck = checkBox.isChecked();
            checkBox.setChecked(!isCheck);
        } else {
            if (onMessageControlEventListener != null) {
                onMessageControlEventListener.onSendNameClick(message.getSenderId());
            }
        }
    }

    /**
     * 如果展開 checkBox 點擊整個item 連動 checkBox.checked()
     */
    public void doItemClickAction(View view) {
        if (checkBox.getVisibility() == View.VISIBLE) {
            boolean isCheck = checkBox.isChecked();
            checkBox.setChecked(!isCheck);
        }
    }

    /**
     * 訊息重送點擊事件
     */
    public void doRetryMessageAction(View view) {
        if (onMessageControlEventListener != null) {
            onMessageControlEventListener.retry(message);
        }
    }

    /**
     * 左邊check box 勾選事件
     */
    void doMessageSelectedAction(CompoundButton button, boolean b) {
        if (message != null) {
            message.setDelete(b);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void findViews(View itemView) {
        containerCFL.addView(getChildView());
//        View.inflate(itemView.getContext(), getContentResId(), containerCFL);
//        bindView(itemView);
    }

    @Override
    public void setTimeLayout(View view, MessageEntity message) {
        if (message != null) {
            if (message.getType() == MessageType.TEMPLATE) {
                readLL = view.findViewById(R.id.ll_template_read_tag);
                timeTV = view.findViewById(R.id.tv_template_msg_time_tag);
            } else {
                readLL = view.findViewById(R.id.read_tag);
                timeTV = view.findViewById(R.id.msg_time_tag);
            }
            if (readLL == null) {
                readLL = rootLayout.findViewById(R.id.ll_template_read_tag);

            }
            if (timeTV == null) {
                timeTV = rootLayout.findViewById(R.id.tv_template_msg_time_tag);
            }
            readLL.setVisibility(View.VISIBLE);
            if (message.getFrom() == ChannelType.FB && message.getType() == MessageType.TEMPLATE) {
                LinearLayout layout = view.findViewById(R.id.ll_status);
                if (layout != null) {
                    layout.setVisibility(View.GONE);
                } else {
                    layout = rootLayout.findViewById(R.id.ll_status);
                    layout.setVisibility(View.GONE);
                }
                timeTV.setVisibility(View.GONE);

            } else {
                timeTV.setVisibility(View.VISIBLE);
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void refresh(MessageEntity item, boolean isCache) {
        this.message = item;

        switch (super.mode) {
            case DEFAULT:
                defaultMode();
                break;
            case RANGE_SELECTION:
                rangeSelectionMode();
                break;
            case SELECTION:
                selectionMode();
                break;
        }

        super.itemView.setTag(Boolean.TRUE.equals(item.isAnimator()) ? "Animator" : null);

        // EVAN_FLAG 2020-07-02 (1.12.0) 搜索功能增強，當聚焦到所選的結果，以搖動方式提醒
        if (Boolean.TRUE.equals(item.isAnimator())) {
            AnimatorHelper.shakeAnimation(super.itemView, true, 200L, (animator, status) -> {
                if (status.equals(AnimatorHelper.Status.END)) {
                    item.setAnimator(false);
                }
            });
        }
        setAvatarView(isCache);
        setContent(item);
        setAccountName();
        setFrom();
        setChannelIcon(this.message.getFrom());

        try {
            C c = (C) item.content();
            bindContentView(c);
        } catch (Exception e) {
            CELog.e("Exception message" + this.message.getSendTime() + "");
        }

        if (ChatRoomType.GROUP_or_DISCUSS.contains(this.chatRoomEntity.getType())) {
            setGroupStatus();
        } else {
            setSingleStatus(item);
        }

        containerCFL.setOnTouchListener(new OnMessageItemEvanListener<MessageEntity>(containerCFL.getContext(), true, getMessage()) {
            @Override
            public void onClick(View v, MessageEntity message) {
                if (!isClickEventEnable()) {
                    return;
                }

                // 如果展開 checkBox onClick 連動 checkBox.checked()
                if (checkBox.getVisibility() == View.VISIBLE) {
                    boolean isCheck = checkBox.isChecked();
                    checkBox.setChecked(!isCheck);
                } else {
                    MessageBubbleView.this.onClick(v, message);
                }
            }

            @Override
            public void onDoubleClick(View v, MessageEntity message) {
                if (!isClickEventEnable()) {
                    return;
                }
                // 如果展開 checkBox 不執行 onDoubleClick
                if (checkBox.getVisibility() != View.VISIBLE) {
                    MessageBubbleView.this.onDoubleClick(v, message);
                }
            }

            @Override
            public void onLongClick(View v, MessageEntity message) {
                if (!isClickEventEnable()) {
                    return;
                }
                // 如果展開 checkBox 不執行 onLongClick
                if (checkBox.getVisibility() != View.VISIBLE) {
                    MessageBubbleView.this.onLongClick(v, containerCFL.getDownX(), containerCFL.getDownY(), message);
                }
            }
        });
    }


    /**
     * 繪製渠道圖標
     */
    protected void setChannelIcon(ChannelType channelType) {
        channelIV.get().setVisibility(View.GONE);
        if (channelType != null) {
            channelIV.get().setVisibility(View.VISIBLE);
            switch (channelType) {
                case FB:
                    channelIV.get().setImageResource(R.drawable.ic_fb);
                    break;
                case LINE:
                    channelIV.get().setImageResource(R.drawable.ic_line);
                    break;
                case QBI:
                case AILE_WEB_CHAT:
                    channelIV.get().setImageResource(R.drawable.qbi_icon);
                    break;
                case WEICHAT:
                    channelIV.get().setImageResource(R.drawable.wechat_icon);
                    break;
                case IG:
                    channelIV.get().setImageResource(R.drawable.ic_ig);
                    break;
                case GOOGLE:
                    channelIV.get().setImageResource(R.drawable.ic_google_message);
                    break;
                default:
                    channelIV.get().setVisibility(View.GONE);
                    break;
            }
        }
    }

    private void setFrom() {
        fromTV.setVisibility(View.GONE);
    }

    /**
     * 還原預設
     */
    private void defaultMode() {
        checkBox.setVisibility(View.GONE);
        maskLayerLL.setVisibility(View.GONE);
        maskLayerLL.setOnClickListener(null);
        if (mSwipeMenuLayout.get() != null) {
            mSwipeMenuLayout.get().setSwipeEnable(true);
        }
        itemView.setBackgroundColor(Color.TRANSPARENT);
    }

    /**
     * 開啟多選模式
     */
    public void selectionMode() {
        if (this.message.isShowChecked()) {
            checkBox.setVisibility(View.VISIBLE);
        } else {
            checkBox.setVisibility(View.GONE);
        }
        if (mSwipeMenuLayout.get() != null) {
            mSwipeMenuLayout.get().setSwipeEnable(false);
        }
        checkBox.setChecked(this.message.isDelete());
    }

    /**
     * 開啟範圍選取模式
     */
    public void rangeSelectionMode() {
        itemView.setBackgroundColor(Color.WHITE);
        if (mSwipeMenuLayout.get() != null) {
            mSwipeMenuLayout.get().setSwipeEnable(false);
        }
        maskLayerLL.setVisibility(View.VISIBLE);
        maskLayerLL.setOnClickListener(v -> {
            if (onMessageControlEventListener != null) {
                onMessageControlEventListener.doRangeSelection(message);
            }
        });
        if (this.message.isShowSelection()) {
            maskLayerLL.setAlpha(0.0f);
        } else {
            maskLayerLL.setAlpha(0.6f);
        }
    }

    private CircleImageView getStateImageView(int mark, boolean isBigPic) {
        CircleImageView circleImageView = new CircleImageView(itemView.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        if (isBigPic) {
            params.weight = UiHelper.dip2px(itemView.getContext(), 6);
            params.height = UiHelper.dip2px(itemView.getContext(), 6);
        } else {
            params.weight = UiHelper.dip2px(itemView.getContext(), 5);
            params.height = UiHelper.dip2px(itemView.getContext(), 5);
        }
        circleImageView.setLayoutParams(params);
        circleImageView.setImageResource(mark);
        return circleImageView;
    }

    private TextView getNumTextView(int num) {
        TextView txtNum = new TextView(itemView.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.rightMargin = 6;
        txtNum.setLayoutParams(params);
        txtNum.setText(String.valueOf(num));
        txtNum.setTextSize(10);
        txtNum.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
        return txtNum;
    }

    final protected void setTimeFrom(String from) {
        timeTV.setText(from);
    }

    final protected MessageEntity getMessage() {
        return this.message;
    }

    private void setAvatarView(boolean isCache) {
        if (Strings.isNullOrEmpty(this.message.getAvatarId())) {
            String avatarId = UserProfileReference.findAvatarIdByUserId(null, this.message.getSenderId());
            this.message.setAvatarId(avatarId);
        }

        avatarLeftRL.get().setVisibility(isRightMessage() ? View.GONE : View.VISIBLE);
        AvatarIcon show = isRightMessage() ? avatarRightIV.get() : avatarLeftIV.get();
        String sendId = this.message.getSenderId();

        if (ChatRoomType.person.equals(this.chatRoomEntity.getType())) {
            avatarLeftRL.get().setVisibility(isRightMessage() ? View.GONE : View.VISIBLE);
            avatarRightIV.get().setImageResource(isGreenTheme ? R.drawable.res_self_message_icon_r_green : R.drawable.res_self_message_icon_r);
            avatarLeftIV.get().setImageResource(isGreenTheme ? R.drawable.res_self_message_icon_l_green : R.drawable.res_self_message_icon_l);
            avatarLeftIV.get().setVisibility(isRightMessage() ? View.GONE : View.VISIBLE);
            avatarRightIV.get().setVisibility(isRightMessage() ? View.VISIBLE : View.GONE); //這裡
            return;
        } else if (ChatRoomType.system.equals(this.chatRoomEntity.getType())) {
            String systemUserId = TokenPref.getInstance(itemView.getContext()).getSystemUserId();
            if (!Strings.isNullOrEmpty(systemUserId)) {
                if (Objects.equals(systemUserId, sendId)) {
                    String systemUserAvatarId = TokenPref.getInstance(itemView.getContext()).getSystemUserAvatarId();
                    String systemUserName = TokenPref.getInstance(itemView.getContext()).getSystemUserName();
                    show.loadAvatarIcon(!Strings.isNullOrEmpty(systemUserAvatarId) ? systemUserAvatarId : message.getAvatarId(), !Strings.isNullOrEmpty(systemUserName) ? systemUserName : Strings.isNullOrEmpty(message.getSenderName()) ? "Unknown" : message.getSenderName(), systemUserId);
                }
            } else {
                show.loadAvatarIcon(message.getAvatarId(), message.getSenderName() == null ? "Unknown" : message.getSenderName(), message.getSenderId());
            }
        } else {
            if (isAnonymous && !isRightMessage()) {
                MessageDomino domino = getDomino(sendId);
                show.setImageResource(domino.getResId());
            } else {
                if (ChatRoomType.consultAi.equals(chatRoomEntity.getRoomType())) {
                    show.setImageResource(R.drawable.ic_ai_consultation);
                } else {
                    if (!isRightMessage()) {
                        String customerName = message.getSenderName();
                        String avatar = message.getAvatarId();
                        String systemId = TokenPref.getInstance(itemView.getContext()).getSystemUserId();
                        String systemAvatar = TokenPref.getInstance(itemView.getContext()).getSystemUserAvatarId();

                        user = DBManager.getInstance().queryFriend(message.getSenderId());
                        if (user != null) {
                            if (!Strings.isNullOrEmpty(user.getCustomerName())) {
                                customerName = user.getCustomerName();
                                avatar = user.getAvatarId();
                            } else if (!Strings.isNullOrEmpty(message.getSenderName())) {
                                customerName = message.getSenderName();
                                avatar = message.getAvatarId();
                            } else if (user.getAlias() != null && !user.getAlias().isEmpty()) {
                                customerName = user.getAlias();
                                avatar = user.getAvatarId();
                            } else if (user.getNickName() != null && !user.getNickName().isEmpty()) {
                                customerName = user.getNickName();
                            } else {
                                customerName = "Unknown";
                            }
                        } else if (systemId.equals(message.getSenderId())) {
                            avatar = systemAvatar;
                            customerName = message.getSenderName();
                        } else {
                            customerName = "Unknown";
                        }
                        show.loadAvatarIcon(avatar, customerName, message.getSenderId());
                    }
                }
            }
        }
        show.setOnClickListener(clickListener);

        try {
            show.setBorder(0, 0);
        } catch (Exception ignored) {
        }
    }

    private static String messageSenderId = "";
    private static UserProfileEntity user = null;

    @SuppressLint("SetTextI18n")
    protected void setAccountName() {
        String senderId = this.message.getSenderId();
        if (!TextUtils.isEmpty(senderId)) {
            if (isRightMessage() && message.getTag() != null && !message.getTag().isEmpty()) {
                FacebookTag facebookTag = JsonHelper.getInstance().from(message.getTag(), FacebookTag.class);
                String senderName = "";
                if (facebookTag.getData() != null && facebookTag.getData().getReplyType() != null) {
                    if (facebookTag.getData().getReplyType().equals("public")) {
                        // facebook 公開回覆
                        senderName = nameTV.getContext().getString(R.string.facebook_public_replied);
                    } else {
                        // facebook 私訊回覆
                        senderName = nameTV.getContext().getString(R.string.facebook_private_replied);
                    }
                    if (!isSend()) {
                        UserProfileEntity userProfile = DBManager.getInstance().queryFriend(message.getSenderId());
                        if (userProfile != null) {
                            senderName += " by " + userProfile.getNickName();
                        }
                    }
                    facebookIcon.setVisibility(View.VISIBLE);
                    nameTV.setText(senderName);
                    nameTV.setVisibility(View.VISIBLE);
                }
            } else if (senderId.equals(userId)) {
                nameTV.setVisibility(View.GONE);
            } else {
                nameTV.setVisibility(View.VISIBLE);
                //  若打開匿名模式且是對方訊息
                if ((isAnonymous && !isRightMessage())) {
                    nameTV.setText(getDomino(this.message.getSenderId()).getName());
                } else {
                    if (chatRoomEntity.getOwnerId().equals(senderId)) {
                        if (chatRoomEntity.getServiceNumberType() != null) {
                            if (ChatRoomType.services.equals(chatRoomEntity.getType()) && chatOwner != null) {
                                if (UserType.EMPLOYEE.equals(this.chatOwner.getUserType())) {
                                    nameTV.setText(this.message.getSenderName());
                                } else {
                                    if (!isRightMessage() && message.getFrom() == ChannelType.FB && message.getType() == MessageType.TEMPLATE) {
                                        nameTV.setText(message.getSenderName());
                                    } else {
                                        UserProfileEntity userProfile = DBManager.getInstance().queryFriend(message.getSenderId());
                                        String customerName = "";
                                        if (userProfile != null && userProfile.getAlias() != null && !userProfile.getAlias().isEmpty()) {
                                            customerName = userProfile.getAlias();
                                        } else if (userProfile != null && !Strings.isNullOrEmpty(userProfile.getCustomerName())) {
                                            customerName = userProfile.getCustomerName();
                                        } else {
                                            customerName = message.getSenderName();
                                        }
                                        nameTV.setText(TextViewHelper.setLeftImage(nameTV.getContext(),
                                            !Strings.isNullOrEmpty(customerName) ? customerName : "未知",
                                            UserType.CONTACT.equals(this.chatOwner.getUserType()) ? R.drawable.ic_customer_15dp : R.drawable.ic_visitor_15dp));
                                    }
                                }
                            } else {
                                if (!Objects.equals(message.getSenderId(), messageSenderId)) {
                                    messageSenderId = message.getSenderId();
                                    user = DBManager.getInstance().queryFriend(message.getSenderId());
                                    nameTV.setText(user != null && Objects.equals(user.getStatus(), User.Status.DISABLE) ? this.message.getSenderName() + itemView.getContext().getString(R.string.text_forbidden) : this.message.getSenderName());
                                } else {
                                    // 使用 sync/message api 會需要從DB撈取發送人的使用者名稱
                                    user = DBManager.getInstance().queryFriend(messageSenderId);
                                    if (user != null) {
                                        if (Objects.equals(user.getStatus(), User.Status.DISABLE)) {
                                            nameTV.setText(user.getNickName() + itemView.getContext().getString(R.string.text_forbidden));
                                        } else {
                                            nameTV.setText(user.getNickName());
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        switch (chatRoomEntity.getRoomType()) {
                            case subscribe:
                                switch (chatRoomEntity.getServiceNumberType()) {
                                    case BOSS:
//                                        nameTV.setText(TextViewHelper.setLeftImage(getContext(), this.message.getSenderName(), R.drawable.ic_icon_boss_15dp));
                                        nameTV.setText(this.message.getSenderName());
                                        break;
                                    case PROFESSIONAL:
//                                        nameTV.setText(TextViewHelper.setLeftImage(getContext(), this.message.getSenderName(), R.drawable.icon_service_number_blue_15dp));
                                        nameTV.setText(TextViewHelper.setLeftImage(nameTV.getContext(), this.message.getSenderName(), R.drawable.icon_subscribe_number_pink_15dp));
                                        break;
                                    case NONE:
                                    case NORMAL:
                                    default:
                                        nameTV.setText(TextViewHelper.setLeftImage(nameTV.getContext(), this.message.getSenderName(), R.drawable.icon_subscribe_number_pink_15dp));
                                }
                                break;
                            case service:
                            case services:
                            case bossOwner:
                            case bossServiceNumber:
                            case serviceINumberAsker:
                            case serviceINumberStaff:
                            case serviceONumberStaff:
                            case serviceONumberAsker:
                                if (ChatRoomType.consultAi.equals(chatRoomEntity.getRoomType())) {
                                    nameTV.setText(message.getSenderName());
                                } else {
                                    if (Objects.requireNonNull(message.getSenderName()).contains("Aile Chat") && chatRoomEntity.getType().equals(ChatRoomType.services)) {
                                        //user = DBManager.getInstance().queryFriend(chatRoomEntity.getServiceNumberAgentId());
//                                        nameTV.setText(user != null ? itemView.getContext().getString(R.string.text_aile_chat_title, user.getNickName(), chatRoomEntity.getServiceNumberName()) :
//                                                itemView.getContext().getString(R.string.text_aile_chat_title, "服務人員", chatRoomEntity.getServiceNumberName()));
                                        nameTV.setText(itemView.getContext().getString(R.string.text_aile_chat_title, chatRoomEntity.getName(), chatRoomEntity.getServiceNumberName()));
                                    } else {
                                        nameTV.setText(chatRoomEntity.getServiceNumberName() + " by " + this.message.getSenderName());
                                    }
                                }
                                break;
                            case friend:
                                user = DBManager.getInstance().queryFriend(message.getSenderId());
                                if (user != null) {
                                    String name = user.getNickName();
                                    if (Objects.equals(user.getStatus(), User.Status.DISABLE)) {
                                        name = user.getNickName() + itemView.getContext().getString(R.string.text_forbidden);
                                    }
                                    nameTV.setText(name);
                                }
                                break;
                            case provisional:
                                user = DBManager.getInstance().queryFriend(message.getSenderId());
                                StringBuilder stringBuilder = new StringBuilder();
                                if (user != null) {
                                    stringBuilder.append(chatRoomEntity.getName())
                                        .append(" @ ")
                                        .append(chatRoomEntity.getServiceNumberName())
                                        .append(" By ")
                                        .append(user.getNickName());
                                } else {
                                    stringBuilder.append(message.getSenderName());
                                }
                                nameTV.setText(stringBuilder.toString());
                                break;
                            default:
                                if (Objects.requireNonNull(message.getSenderName()).contains("Aile Chat") && chatRoomEntity.getType().equals(ChatRoomType.services)) {
//                                    user = DBManager.getInstance().queryFriend(chatRoomEntity.getServiceNumberAgentId());
//                                    nameTV.setText(user != null ? itemView.getContext().getString(R.string.text_aile_chat_title, user.getNickName(), chatRoomEntity.getServiceNumberName()) :
//                                            itemView.getContext().getString(R.string.text_aile_chat_title, "服務人員", chatRoomEntity.getServiceNumberName()));
                                    nameTV.setText(itemView.getContext().getString(R.string.text_aile_chat_title, chatRoomEntity.getName(), chatRoomEntity.getServiceNumberName()));
                                } else {
                                    if (!Objects.equals(message.getSenderId(), messageSenderId)) {
                                        messageSenderId = message.getSenderId();
                                        user = DBManager.getInstance().queryFriend(message.getSenderId());
                                    }
                                    if (chatRoomEntity.getType().equals(ChatRoomType.system)) {
                                        String systemUserId = TokenPref.getInstance(itemView.getContext()).getSystemUserId();
                                        if (!Strings.isNullOrEmpty(systemUserId)) {
                                            if (Objects.equals(systemUserId, message.getSenderId())) {
                                                String systemUserName = TokenPref.getInstance(itemView.getContext()).getSystemUserName();
                                                nameTV.setText(!Strings.isNullOrEmpty(systemUserName) ? systemUserName : message.getSenderName());
                                            } else
                                                nameTV.setText(this.message.getSenderName());
                                        } else
                                            nameTV.setText(this.message.getSenderName());
                                    } else
                                        nameTV.setText(user != null && Objects.equals(user.getStatus(), User.Status.DISABLE) ? this.message.getSenderName() + itemView.getContext().getString(R.string.text_forbidden) : this.message.getSenderName());
                                }
                        }
                    }
                }
            }
        }
    }

    protected abstract boolean showName();

    public void setGroupStatus() {
        MessageStatus status = this.message.getStatus();
        timeTV.setCompoundDrawables(null, null, null, null);
        readLL.removeAllViews();
        readLL.setVisibility(View.GONE);
        switch (status) {
            case SENDING:
                setStatusView(View.VISIBLE, View.GONE, View.GONE);
                break;
            case ERROR:
            case UPDATE_ERROR:
            case FAILED:
                setStatusView(View.GONE, View.GONE, View.VISIBLE);
                break;
            default:
                setStatusView(View.GONE, View.VISIBLE, View.GONE);
                if (isRightMessage() && userId.equals(this.message.getSenderId()) && !MessageType.CALL.equals(this.message.getType())) {
                    readLL.setVisibility(View.VISIBLE);
                    // 已讀數量
                    int readAmount = 0;
                    // 已接收數量
                    int receivedAmount = 0;
                    if (message != null) {
                        if (message.getReadedNum() != null) {
                            readAmount = message.getReadedNum();
                        }
                        if (message.getReceivedNum() != null) {
                            receivedAmount = message.getReceivedNum();
                        }
                    }


//                    int sendAmount = this.message.getSendNum();
                    CELog.i(String.format("Received and read status :: sendNum:[%s], receivedNum:[%s], readNum:[%s], ", this.message.getSendNum(), this.message.getReceivedNum(), this.message.getReadedNum()));
                    // 未讀數量
                    int unReadAmount = receivedAmount - readAmount;
                    if (receivedAmount > 5) {
                        //大空心圆 数字mUnReadNum
                        if (unReadAmount > 0) {
                            readLL.addView(getStateImageView(R.drawable.unread_mark, true));
                            readLL.addView(getNumTextView(unReadAmount));
                        }
                        if (readAmount > 0) {
                            readLL.addView(getStateImageView(R.drawable.read_mark, true));
                            readLL.addView(getNumTextView(readAmount));
                        }
                    } else if (readAmount > 5) {
                        readLL.addView(getStateImageView(R.drawable.read_mark, true));
                        readLL.addView(getNumTextView(readAmount));
                    } else {
                        //小空心圆 图mUnReadNum
                        for (int i = 0; i < unReadAmount; i++) {
                            readLL.addView(getStateImageView(R.drawable.unread_mark, false));
                        }
                        for (int i = 0; i < readAmount; i++) {
                            readLL.addView(getStateImageView(R.drawable.read_mark, false));
                        }
                    }
                    timeTV.setText(TimeUtil.INSTANCE.getHHmm(this.message.getSendTime()));
                } else {
                    readLL.removeAllViews();
                    readLL.setVisibility(View.GONE);
                    timeTV.setText(TimeUtil.INSTANCE.getHHmm(this.message.getSendTime()));
                }
        }
    }

    public void setSingleStatus(MessageEntity item) {
        MessageStatus status = item.getStatus();
        timeTV.setCompoundDrawables(null, null, null, null);
        readLL.removeAllViews();
        readLL.setVisibility(View.GONE);
        progressLoadingIV.setVisibility(View.GONE);
        if (status == null) return;
        switch (status) {
            case SENDING:
                setStatusView(View.VISIBLE, View.GONE, View.GONE);
                break;
            case ERROR:
            case UPDATE_ERROR:
            case FAILED:
                setStatusView(View.GONE, View.GONE, View.VISIBLE);
                break;
            default:
                timeTV.setText(TimeUtil.INSTANCE.getHHmm(this.message.getSendTime()));
                setStatusView(View.GONE, View.VISIBLE, View.GONE);
                if (this.message.getReadedNum() != null && message.getReadedNum() > 0) {
                    readLL.setVisibility(View.VISIBLE);
                    if (isRightMessage() && !MessageType.CALL.equals(this.message.getType())) {
                        readLL.setVisibility(View.VISIBLE);
                        readLL.removeAllViews();
                        readLL.addView(getStateImageView(R.drawable.read_mark, false));
                    }
                } else if (this.message.getReceivedNum() != null && this.message.getReceivedNum() > 0) {
                    readLL.setVisibility(View.VISIBLE);
                    if (isRightMessage() && !MessageType.CALL.equals(this.message.getType())) {
                        readLL.setVisibility(View.VISIBLE);
                        readLL.removeAllViews();
                        readLL.addView(getStateImageView(R.drawable.unread_mark, false));
                    }
                } else {
                    return;
                }
                break;
        }
    }

    private void setStatusView(int loading, int time, int error) {
        progressLoadingIV.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(userId) && !userId.equals(this.message.getSenderId())) {
            errorIV.setVisibility(View.GONE);
            progressLoadingIV.setVisibility(View.GONE);
        } else {
            errorIV.setVisibility(error);
            progressLoadingIV.setVisibility(loading);
        }
        timeTV.setVisibility(time);
    }

    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
        @Override
        protected void onNoDoubleClick(View v) {
            if (!ChatRoomType.subscribe.equals(chatRoomEntity.getType())) {
                // EVAN_FLAG 2019-10-18 當開啟多選模式，點擊頭像要連動 checkBox select
                if (checkBox.getVisibility() == View.VISIBLE) {
                    boolean isCheck = checkBox.isChecked();
                    checkBox.setChecked(!isCheck);
                } else {
                    if (onMessageControlEventListener != null) {
                        onMessageControlEventListener.onAvatarClick(message.getSenderId());
                    }
                }
            } else {
                // EVAN_FLAG 2019-10-18 當開啟多選模式，點擊頭像要連動 checkBox select
                if (checkBox.getVisibility() == View.VISIBLE) {
                    boolean isCheck = checkBox.isChecked();
                    checkBox.setChecked(!isCheck);
                } else {
                    if (onMessageControlEventListener != null) {
                        onMessageControlEventListener.onSubscribeAgentAvatarClick(message.getSenderId());
                    }
                }
            }
        }
    };

    protected boolean isRightMessage() {
        if (ChatRoomType.person.equals(this.chatRoomEntity.getType())) {
            return Sets.newHashSet("android", "ios").contains(this.message.getOsType());
        } else if (ChatRoomType.consultAi.equals(chatRoomEntity.getRoomType())) {
            return getUserId().equals(message.getSenderId());
        } else if (ChatRoomType.SERVICES_or_SUBSCRIBE.contains(this.chatRoomEntity.getType()) && !userId.equals(this.chatRoomEntity.getOwnerId())) {
            return !this.chatRoomEntity.getOwnerId().equals(this.message.getSenderId());
        } else {
            return !TextUtils.isEmpty(userId) && userId.equals(this.message.getSenderId());
        }
    }

    protected final void setGravity(View view, int gravity) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.gravity = gravity;
    }

    protected int leftBackground() {
        return R.drawable.bubble_receive;
    }

    protected int rightBackground() {
        if (this.chatRoomEntity.getType().equals(ChatRoomType.services) && !isSend()) {
            return R.drawable.bubble_send_f;
        } else {
            return R.drawable.bubble_send;
        }
    }

    public boolean isSend() {
        return userId.equals(this.message.getSenderId());
    }


    private void setContent(MessageEntity item) {
        int index = isRightMessage() ? 3 : 0;
        if (bodyContainer.get().getChildAt(index) != containerCFL) {
            bodyContainer.get().removeView(containerCFL);
            bodyContainer.get().addView(containerCFL, index);
        }

        if (timeTV == null || containerLL == null) {
            setTimeLayout(containerCFL, item);
        }

        if (isRightMessage()) {
            timeTV.setGravity(Gravity.END);
            containerLL.setGravity(Gravity.END);
            if (message.getType() != MessageType.TEMPLATE) {
                containerCFL.setBackgroundResource(rightBackground());
            } else {
                containerCFL.setBackground(null);
            }
        } else {
            timeTV.setGravity(Gravity.START);
            containerLL.setGravity(Gravity.START);
            if (message.getType() != MessageType.TEMPLATE) {
                containerCFL.setBackgroundResource(leftBackground());
            } else {
                containerCFL.setBackground(null);
                timeTV.setGravity(Gravity.END);
            }
        }
    }

    protected String getUserId() {
        return this.userId;
    }
}
