package tw.com.chainsea.chat.messagekit.child.viewholder;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewbinding.ViewBinding;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import java.util.Objects;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.animator.AnimatorHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.messagekit.lib.MessageDomino;
import tw.com.chainsea.chat.messagekit.main.viewholder.listener.OnMessageItemEvanListener;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.chat.util.TimeUtil;
import tw.com.chainsea.custom.view.image.CircleImageView;

/**
 * Create by evan on 1/22/21
 *
 * @author Evan Wang
 * @date 1/22/21
 */
public abstract class ChildMessageBubbleView<C extends IMessageContent> extends ChildMessageViewBase<ChatRoomEntity, MessageEntity> {
    private MessageEntity message;
    //    private ChannelType msgFrom;
    protected String userId;
    protected boolean isGreenTheme;

    // 取得訊息內容 layout.xml
    abstract protected int getContentResId();

    // 訊息UI內容 findViewById
    abstract protected void bindView(View itemView);

    // 綁定內容 View
    abstract protected void bindContentView(C c);

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

    protected tw.com.chainsea.chat.databinding.ItemMsgBubbleBinding itemMsgBubbleBinding;

    public ChildMessageBubbleView(@NonNull ViewBinding binding) {
        super(binding);
        this.isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
        this.itemMsgBubbleBinding = (tw.com.chainsea.chat.databinding.ItemMsgBubbleBinding) binding;
        this.userId = TokenPref.getInstance(getContext()).getUserId();
        initListener();
    }

    private void initListener() {
        itemMsgBubbleBinding.llMsgBubbleRoot.setOnClickListener(this::doItemClickAction);
        itemMsgBubbleBinding.msgError.setOnClickListener(this::doRetryMessageAction);
        itemMsgBubbleBinding.checkBox.setOnCheckedChangeListener(this::doMessageSelectedAction);
    }

//    void doNameClickAction(View view) {
//        if (itemMsgBubbleBinding.checkBox.getVisibility() == View.VISIBLE) {
//            boolean isCheck = itemMsgBubbleBinding.checkBox.isChecked();
//            itemMsgBubbleBinding.checkBox.setChecked(!isCheck);
//        } else {
//            if (onMessageControlEventListener != null) {
//                onMessageControlEventListener.onSendNameClick(message.getSenderId());
//            }
//        }
//    }

    /**
     * 如果展開 checkBox 點擊整個item 連動 checkBox.checked()
     */
    public void doItemClickAction(View view) {
        if (itemMsgBubbleBinding.checkBox.getVisibility() == View.VISIBLE) {
            boolean isCheck = itemMsgBubbleBinding.checkBox.isChecked();
            itemMsgBubbleBinding.checkBox.setChecked(!isCheck);
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
        View.inflate(getContext(), getContentResId(), itemMsgBubbleBinding.msgContainer);
        bindView(itemView);
        // EVAN_FLAG 2019-09-23 信息手勢操作事件，代改寫 「單點、雙點、長按、移動」

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void refresh(MessageEntity item, boolean isCache) {
        this.message = item;
//        this.userId = TokenSave.getInstance(getContext()).getUserId();

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
        setAvatarView();
        setContent();
        setAccountName();
//        if (session.getTodoOverviewType() == SessionType.GROUP || session.getTodoOverviewType() == SessionType.DISCUSS) {
//            setGroupStatus();
//        } else {
//            setSingleStatus(item);
//        }
        setFrom();
        setChannelIcon(this.message.getFrom());

        try {
            C c = (C) item.content();
            bindContentView(c);
//            bindContentView((T) this.message.getTodoOverviewType(),(C)this.message.content());
        } catch (Exception e) {
            CELog.e("Exception message" + this.message.getSendTime());
        }


        if (ChatRoomType.GROUP_or_DISCUSS.contains(this.b.getType())) {
            setGroupStatus();
        } else {
            setSingleStatus(item);
        }

        itemMsgBubbleBinding.msgContainer.setOnTouchListener(new OnMessageItemEvanListener<>(getContext(), true, getMessage()) {
            @Override
            public void onClick(View v, MessageEntity message) {
                if (!isClickEventEnable()) {
                    return;
                }

                // 如果展開 checkBox onClick 連動 checkBox.checked()
                if (itemMsgBubbleBinding.checkBox.getVisibility() == View.VISIBLE) {
                    boolean isCheck = itemMsgBubbleBinding.checkBox.isChecked();
                    itemMsgBubbleBinding.checkBox.setChecked(!isCheck);
                } else {
                    ChildMessageBubbleView.this.onClick(v, message);
                }
            }

            @Override
            public void onDoubleClick(View v, MessageEntity message) {
                if (!isClickEventEnable()) {
                    return;
                }
                // 如果展開 checkBox 不執行 onDoubleClick
                if (itemMsgBubbleBinding.checkBox.getVisibility() != View.VISIBLE) {
                    ChildMessageBubbleView.this.onDoubleClick(v, message);
                }
            }

            @Override
            public void onLongClick(View v, MessageEntity message) {
                if (!isClickEventEnable()) {
                    return;
                }
                // 如果展開 checkBox 不執行 onLongClick
                if (itemMsgBubbleBinding.checkBox.getVisibility() != View.VISIBLE) {
                    ChildMessageBubbleView.this.onLongClick(v, itemMsgBubbleBinding.msgContainer.getDownX(), itemMsgBubbleBinding.msgContainer.getDownY(), message);
                }
            }
        });
    }


    /**
     * 繪製渠道圖標
     */
    protected void setChannelIcon(ChannelType channelType) {
        itemMsgBubbleBinding.ivChannel.setVisibility(View.GONE);
        if (channelType != null) {
            itemMsgBubbleBinding.ivChannel.setVisibility(View.VISIBLE);
            switch (channelType) {
                case FB:
                    itemMsgBubbleBinding.ivChannel.setImageResource(R.drawable.ic_fb);
                    break;
                case LINE:
                    itemMsgBubbleBinding.ivChannel.setImageResource(R.drawable.ic_line);
                    break;
                case QBI:
                case AILE_WEB_CHAT:
                    itemMsgBubbleBinding.ivChannel.setImageResource(R.drawable.qbi_icon);
                    break;
                case WEICHAT:
                    itemMsgBubbleBinding.ivChannel.setImageResource(R.drawable.wechat_icon);
                    break;
                case IG:
                    itemMsgBubbleBinding.ivChannel.setImageResource(R.drawable.ic_ig);
                    break;
                case GOOGLE:
                    itemMsgBubbleBinding.ivChannel.setImageResource(R.drawable.ic_google_message);
                    break;
                default:
                    itemMsgBubbleBinding.ivChannel.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private void setFrom() {
        itemMsgBubbleBinding.from.setVisibility(View.GONE);
    }

    /**
     * 還原預設
     */
    private void defaultMode() {
        itemMsgBubbleBinding.checkBox.setVisibility(View.GONE);
        itemMsgBubbleBinding.maskLayer.setVisibility(View.GONE);
        itemMsgBubbleBinding.maskLayer.setOnClickListener(null);
        itemView.setBackgroundColor(Color.TRANSPARENT);
    }

    /**
     * 開啟多選模式
     */
    public void selectionMode() {
        if (Boolean.TRUE.equals(this.message.isShowChecked())) {
            itemMsgBubbleBinding.checkBox.setVisibility(View.VISIBLE);
        } else {
            itemMsgBubbleBinding.checkBox.setVisibility(View.GONE);
        }
        itemMsgBubbleBinding.checkBox.setChecked(Boolean.TRUE.equals(this.message.isDelete()));
    }


    /**
     * 開啟範圍選取模式
     */
    public void rangeSelectionMode() {
        itemView.setBackgroundColor(Color.WHITE);
        itemMsgBubbleBinding.maskLayer.setVisibility(View.VISIBLE);
        itemMsgBubbleBinding.maskLayer.setOnClickListener(v -> {
            if (onMessageControlEventListener != null) {
                onMessageControlEventListener.doRangeSelection(message);
            }
        });
        if (Boolean.TRUE.equals(this.message.isShowSelection())) {
            itemMsgBubbleBinding.maskLayer.setAlpha(0.0f);
        } else {
            itemMsgBubbleBinding.maskLayer.setAlpha(0.6f);
        }


//        itemView.setAlpha(0.68f);
//        itemView.setBackgroundColor(Color.BLACK);
//        if (this.message.isShowSelection()) {
//
//        } else {
//            itemView.setAlpha(0.7f);
//        }
    }


    public void setGroupStatus() {
        MessageStatus status = this.message.getStatus();
        itemMsgBubbleBinding.msgTimeTag.setCompoundDrawables(null, null, null, null);
        itemMsgBubbleBinding.readTag.removeAllViews();
        itemMsgBubbleBinding.readTag.setVisibility(View.GONE);
        switch (Objects.requireNonNull(status)) {
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
                    itemMsgBubbleBinding.readTag.setVisibility(View.VISIBLE);
                    assert message.getReadedNum() != null;
                    assert message.getReceivedNum() != null;
                    int readAmount = this.message.getReadedNum();
                    int receivedAmount = this.message.getReceivedNum();
//                    int sendAmount = this.message.getSendNum();
                    CELog.i(String.format("Received and read status :: sendNum:[%s], receivedNum:[%s], readNum:[%s], ", this.message.getSendNum(), this.message.getReceivedNum(), this.message.getReadedNum()));
                    int unReadAmount = receivedAmount - readAmount;
                    if (receivedAmount > 5) {
                        //大空心圆 数字mUnReadNum
                        if (unReadAmount > 0) {
                            itemMsgBubbleBinding.readTag.addView(getStateImageView(R.drawable.unread_mark, true));
                            itemMsgBubbleBinding.readTag.addView(getNumTextView(unReadAmount));
                        }
                        if (readAmount > 0) {
                            itemMsgBubbleBinding.readTag.addView(getStateImageView(R.drawable.read_mark, true));
                            itemMsgBubbleBinding.readTag.addView(getNumTextView(readAmount));
                        }
                    } else if (readAmount > 5) {
                        itemMsgBubbleBinding.readTag.addView(getStateImageView(R.drawable.read_mark, true));
                        itemMsgBubbleBinding.readTag.addView(getNumTextView(readAmount));
                    } else {
                        //小空心圆 图mUnReadNum
                        for (int i = 0; i < unReadAmount; i++) {
                            itemMsgBubbleBinding.readTag.addView(getStateImageView(R.drawable.unread_mark, false));
                        }
                        for (int i = 0; i < readAmount; i++) {
                            itemMsgBubbleBinding.readTag.addView(getStateImageView(R.drawable.read_mark, false));
                        }
                    }
                    itemMsgBubbleBinding.msgTimeTag.setText(TimeUtil.INSTANCE.getHHmm(this.message.getSendTime()));
                } else {
                    itemMsgBubbleBinding.readTag.removeAllViews();
                    itemMsgBubbleBinding.readTag.setVisibility(View.GONE);
                    itemMsgBubbleBinding.msgTimeTag.setText(TimeUtil.INSTANCE.getHHmm(this.message.getSendTime()));
                }
        }
    }


    private CircleImageView getStateImageView(int mark, boolean isBigPic) {
        CircleImageView circleImageView = new CircleImageView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        if (isBigPic) {
//            params.weight = 8;
//            params.height = 8;
            params.weight = UiHelper.dip2px(getContext(), 6);
            params.height = UiHelper.dip2px(getContext(), 6);
        } else {
//            params.weight = 5;
//            params.height = 5;
            params.weight = UiHelper.dip2px(getContext(), 5);
            params.height = UiHelper.dip2px(getContext(), 5);
        }
        circleImageView.setLayoutParams(params);
        circleImageView.setImageResource(mark);
        return circleImageView;
    }

    private TextView getNumTextView(int num) {
        TextView txtNum = new TextView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.rightMargin = 6;
        txtNum.setLayoutParams(params);
        txtNum.setText(num);
        txtNum.setTextSize(10);
        txtNum.setTextColor(ResourcesCompat.getColor(getContext().getResources(), R.color.black, null));
        return txtNum;
    }

    final protected void setTimeFrom(String from) {
        itemMsgBubbleBinding.msgTimeTag.setText(from);
    }

    final protected MessageEntity getMessage() {
        return this.message;
    }

    private void setAvatarView() {
        if (Strings.isNullOrEmpty(this.message.getAvatarId())) {
            String avatarId = UserProfileReference.findAvatarIdByUserId(null, this.message.getSenderId());
            this.message.setAvatarId(avatarId);
        }

        if (ChatRoomType.person.equals(this.b.getType())) {
            itemMsgBubbleBinding.avatarLeftL.setVisibility(isRightMessage() ? View.GONE : View.VISIBLE);
            itemMsgBubbleBinding.msgAvatarRight.setImageResource(R.drawable.res_self_message_icon_r);
            itemMsgBubbleBinding.msgAvatarLeft.setImageResource(R.drawable.res_self_message_icon_l);
            itemMsgBubbleBinding.msgAvatarLeft.setVisibility(isRightMessage() ? View.GONE : View.VISIBLE);
            itemMsgBubbleBinding.msgAvatarRight.setVisibility(isRightMessage() ? View.VISIBLE : View.GONE);
//            CircleImageView show = isRightMessage() ? avatarRightIV : avatarLeftIV;
//            show.setImageResource(isRightMessage() ? R.drawable.res_self_message_icon_r : R.drawable.res_self_message_icon_l);
            return;
        }
        itemMsgBubbleBinding.avatarLeftL.setVisibility(isRightMessage() ? View.GONE : View.VISIBLE);
        CircleImageView show = isRightMessage() ? itemMsgBubbleBinding.msgAvatarRight : itemMsgBubbleBinding.msgAvatarLeft;
        if (isAnonymous && !isRightMessage()) {
            String sendId = this.message.getSenderId();
            MessageDomino domino = getDomino(sendId);
            show.setImageResource(domino.getResId());
        } else {
            AvatarService.post(getContext(), !isRightMessage() && this.b.getType().equals(ChatRoomType.subscribe) ?
                this.b.getServiceNumberAvatarId() :
                this.message.getAvatarId(), PicSize.SMALL, show, R.drawable.custom_default_avatar);
        }


        if (!this.b.getType().equals(ChatRoomType.subscribe)) {
//        if (session.getTodoOverviewType() != SessionType.SERVICES && session.getTodoOverviewType() != SessionType.SUBSCRIBE) {
            show.setOnClickListener(v -> {
                // EVAN_FLAG 2019-10-18 當開啟多選模式，點擊頭像要連動 checkBox select
                if (itemMsgBubbleBinding.checkBox.getVisibility() == View.VISIBLE) {
                    boolean isCheck = itemMsgBubbleBinding.checkBox.isChecked();
                    itemMsgBubbleBinding.checkBox.setChecked(!isCheck);
                } else {
                    if (onMessageControlEventListener != null) {
                        onMessageControlEventListener.onAvatarClick(message.getSenderId());
                    }
                }
            });
        }


        try {
            show.setBorder(0, 0);
            for (UserProfileEntity a : this.b.getMembers()) {
                if (a.getId().equals(this.message.getSenderId()) && !"ALL".equals(a.getNickName())) {
                    if (!UserType.EMPLOYEE.equals(a.getUserType()) && !isAnonymous) {
                        show.setBorder(2, R.drawable.circle_session_employee_bg);
                        return;
                    }
                }
            }
        } catch (Exception ignored) {
        }

//        getMessage();


//        if (session.getTodoOverviewType() == SessionType.SUBSCRIBE) {
//            holder.rlLogo.setBackgroundResource(R.drawable.circle_session_subscribe_bg);
//            lettingAvatarSize(holder, 40);
//        }else {
//            List<UserProfileEntity> accounts = DBManager.getInstance().findMembersByRoomId(mSessionList.get(position).getId());
//            holder.rlLogo.setBackgroundResource(R.drawable.circle_session_employee_bg);
//            lettingAvatarSize(holder, 45);
//            boolean hasEmployees = true;
//            for (UserProfileEntity a : accounts) {
//                if (!Constant.EMPLOYEE.equals(a.getUserType())) {
//                    hasEmployees = false;
//                }
//            }
//            if (!hasEmployees){
//                lettingAvatarSize(holder, 40);
//            }
//        }

//        avatarLeftRL.setBackgroundResource(R.drawable.circle_session_subscribe_bg);
//        holder.itemView.findViewById(R.id.avatar_left_l).setBackgroundResource(R.drawable.circle_session_subscribe_bg);
//        lettingAvatarSize(show, 45);

    }

//    private void lettingAvatarSize(View view, int size) {
//        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//        layoutParams.width = UiHelper.dip2px(getContext(), size, 0.5f);
//        layoutParams.height = UiHelper.dip2px(getContext(), size, 0.5f);
//        view.setLayoutParams(layoutParams);
//    }


    protected void setAccountName() {
        String senderId = this.message.getSenderId();
        if (!TextUtils.isEmpty(senderId)) {
            if (senderId.equals(userId)) {
                itemMsgBubbleBinding.accountName.setVisibility(View.GONE);
            } else {
                itemMsgBubbleBinding.accountName.setVisibility(View.VISIBLE);
            }
            //  若打開匿名模式且是對方訊息
            itemMsgBubbleBinding.accountName.setText((isAnonymous && !isRightMessage()) ? getDomino(this.message.getSenderId()).getName() : this.message.getSenderName());
        }
    }

    protected abstract boolean showName();

    public void setSingleStatus(MessageEntity item) {
        MessageStatus status = item.getStatus();
        itemMsgBubbleBinding.msgTimeTag.setCompoundDrawables(null, null, null, null);
        itemMsgBubbleBinding.readTag.removeAllViews();
        itemMsgBubbleBinding.readTag.setVisibility(View.GONE);
        itemMsgBubbleBinding.msgProgress.setVisibility(View.GONE);


        switch (Objects.requireNonNull(status)) {
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
                assert this.message.getReadedNum() != null;
                if (this.message.getReadedNum() == 0) {
                    return;
                }
                setStatusView(View.GONE, View.VISIBLE, View.GONE);
                itemMsgBubbleBinding.readTag.setVisibility(View.VISIBLE);
                if (isRightMessage() && !MessageType.CALL.equals(this.message.getType())) {
                    itemMsgBubbleBinding.readTag.setVisibility(View.VISIBLE);
                    itemMsgBubbleBinding.readTag.removeAllViews();
                    itemMsgBubbleBinding.readTag.addView(getStateImageView(R.drawable.read_mark, false));
                }
//
//
//                if (isRightMessage() && userId.equals(this.message.getSenderId()) && !MessageType.CALL.equals(this.message.getType())) {
//                    readLL.setVisibility(View.VISIBLE);
//                    readAmount = this.message.getReadedNum();
//                    receivedAmount = this.message.getReceivedNum();
////                    int sendAmount = this.message.getSendNum();
//                    CELog.i(String.format("Received and read status :: sendNum:[%s], receivedNum:[%s], readNum:[%s], ", this.message.getSendNum(), this.message.getReceivedNum(), this.message.getReadedNum()));
//                    unReadAmount = receivedAmount - readAmount;
//                    if (receivedAmount > 5) {
//                        //大空心圆 数字mUnReadNum
//                        if (unReadAmount > 0) {
//                            readLL.addView(getStateImageView(R.drawable.unread_mark, true));
//                            readLL.addView(getNumTextView(unReadAmount));
//                        }
//                        if (readAmount > 0) {
//                            readLL.addView(getStateImageView(R.drawable.read_mark, true));
//                            readLL.addView(getNumTextView(readAmount));
//                        }
//                    } else if (readAmount > 5) {
//                        readLL.addView(getStateImageView(R.drawable.read_mark, true));
//                        readLL.addView(getNumTextView(readAmount));
//                    } else {
//                        //小空心圆 图mUnReadNum
//                        for (int i = 0; i < unReadAmount; i++) {
//                            readLL.addView(getStateImageView(R.drawable.unread_mark, false));
//                        }
//                        for (int i = 0; i < readAmount; i++) {
//                            readLL.addView(getStateImageView(R.drawable.read_mark, false));
//                        }
//                    }
//                    msgTimeTag.setText(TimeUtil.getHHmm(this.message.getSendTime()));
//                } else {
//                    readLL.removeAllViews();
//                    readLL.setVisibility(View.GONE);
//                    msgTimeTag.setText(TimeUtil.getHHmm(this.message.getSendTime()));
//                }
        }

//        switch (status) {
//
//            case SENDING:
//                setStatusView(View.VISIBLE, View.GONE, View.GONE);
//                break;
//            case IS_REMOTE:
//            case SUCCESS:
//                setStatusView(View.GONE, View.VISIBLE, View.GONE);
//                msgTimeTag.setText(TimeUtil.getHHmm(this.message.getSendTime()));
//                break;
//            case RECEIVED:
//                setStatusView(View.GONE, View.VISIBLE, View.GONE);
//                if (isRightMessage() && !MessageType.CALL.equals(this.message.getType())) {
//                    readLL.setVisibility(View.VISIBLE);
//                    readLL.removeAllViews();
//                    readLL.addView(getStateImageView(R.drawable.unread_mark, false));
//                }
//                msgTimeTag.setText(TimeUtil.getHHmm(this.message.getSendTime()));
//                break;
//            case READ:
//                setStatusView(View.GONE, View.VISIBLE, View.GONE);
//                msgTimeTag.setText(TimeUtil.getHHmm(this.message.getSendTime()));
//                if (this.message.getReadedNum() == 0) {
//                    return;
//                }
//                setStatusView(View.GONE, View.VISIBLE, View.GONE);
//                readLL.setVisibility(View.VISIBLE);
//                if (isRightMessage() && !MessageType.CALL.equals(this.message.getType())) {
//                    readLL.setVisibility(View.VISIBLE);
//                    readLL.removeAllViews();
//                    readLL.addView(getStateImageView(R.drawable.read_mark, false));
//                }
//                break;
//            case FAILED:
//            case UPDATE_ERROR:
//            case ERROR:
//                setStatusView(View.GONE, View.GONE, View.VISIBLE);
//                break;
//            default:
//                break;
//        }
    }

    private void setStatusView(int loading, int time, int error) {
        itemMsgBubbleBinding.msgProgress.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(userId) && !userId.equals(this.message.getSenderId())) {
            itemMsgBubbleBinding.msgError.setVisibility(View.GONE);
            itemMsgBubbleBinding.msgProgress.setVisibility(View.GONE);
        } else {
            itemMsgBubbleBinding.msgError.setVisibility(error);
            itemMsgBubbleBinding.msgProgress.setVisibility(loading);
        }
//        if (isRightMessage()) {
//            errorIV.setVisibility(error);
//            msgProgress.setVisibility(loading);
//        } else {
//            errorIV.setVisibility(View.GONE);
//            msgProgress.setVisibility(View.GONE);
//        }
        itemMsgBubbleBinding.msgTimeTag.setVisibility(time);
//        errorIV.setVisibility(error);
//        msgTimeTag.setVisibility(time);
////        tvTimeLeft.setVisibility(time);
////        tvTimeRight.setVisibility(View.GONE);
//        msgProgress.setVisibility(loading);

    }

    protected boolean isRightMessage() {
        if (ChatRoomType.person.equals(this.b.getType())) {
            return Sets.newHashSet("android", "ios").contains(this.message.getOsType());
        } else if (ChatRoomType.SERVICES_or_SUBSCRIBE.contains(this.b.getType()) && !userId.equals(this.b.getOwnerId())) {
            return !this.b.getOwnerId().equals(this.message.getSenderId());
        } else {
            return !TextUtils.isEmpty(userId) && userId.equals(this.message.getSenderId());
        }
    }

    protected final void setGravity(View view, int gravity) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.gravity = gravity;
    }

    protected int leftBackground() {
        return Color.TRANSPARENT;
//        return R.drawable.bubble_receive;
    }

    protected int rightBackground() {
        return Color.TRANSPARENT;
//        if (this.b.getType().equals(ChatRoomType.SERVICES) && !isSend()) {
//            return R.drawable.bubble_send_f;
//        } else {
//            return R.drawable.bubble_send;
//        }
    }

    public boolean isSend() {
        return userId.equals(this.message.getSenderId());
    }


    private void setContent() {
//        LinearLayout bodyContainer = (LinearLayout) view.findViewById(R.id.msg_body);
        int index = isRightMessage() ? 3 : 0;
        if (itemMsgBubbleBinding.msgBody.getChildAt(index) != itemMsgBubbleBinding.msgContainer) {
            itemMsgBubbleBinding.msgBody.removeView(itemMsgBubbleBinding.msgContainer);
            itemMsgBubbleBinding.msgBody.addView(itemMsgBubbleBinding.msgContainer, index);
        }

        if (isRightMessage()) {
            itemMsgBubbleBinding.msgTimeTag.setGravity(Gravity.END);
            itemMsgBubbleBinding.msgContainerLayout.setGravity(Gravity.END);
            itemMsgBubbleBinding.msgContainer.setBackgroundResource(rightBackground());
        } else {
            itemMsgBubbleBinding.msgTimeTag.setGravity(Gravity.START);
            itemMsgBubbleBinding.msgContainerLayout.setGravity(Gravity.START);
            itemMsgBubbleBinding.msgContainer.setBackgroundResource(leftBackground());
        }
    }


    protected String getUserId() {
        return this.userId;
    }
}
