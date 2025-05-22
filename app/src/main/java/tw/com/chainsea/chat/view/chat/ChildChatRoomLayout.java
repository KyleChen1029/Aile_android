//package tw.com.chainsea.chat.view.chat;
//
//import android.content.Context;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.recyclerview.widget.DefaultItemAnimator;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.ImageView;
//
//import com.google.common.collect.Iterables;
//import com.google.common.collect.Lists;
//
//import java.lang.ref.WeakReference;
//import java.text.SimpleDateFormat;
//import java.util.Collections;
//import java.util.List;
//import java.util.Locale;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//import tw.com.chainsea.android.common.json.JsonHelper;
//import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
//import tw.com.chainsea.android.common.ui.UiHelper;
//import tw.com.chainsea.ce.sdk.bean.MsgStatusBean;
//import tw.com.aile.sdk.bean.message.MessageEntity;
//import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
//import tw.com.chainsea.ce.sdk.bean.msg.SourceType;
//import tw.com.chainsea.ce.sdk.bean.msg.Tools;
//import tw.com.chainsea.ce.sdk.bean.msg.content.UndefContent;
//import tw.com.chainsea.ce.sdk.bean.parameter.Sort;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
//import tw.com.chainsea.ce.sdk.cache.ChatMemberCacheService;
//import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
//import tw.com.chainsea.ce.sdk.database.sp.UserPref;
//import tw.com.chainsea.ce.sdk.event.EventMsg;
//import tw.com.chainsea.ce.sdk.event.MsgConstant;
//import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
//import tw.com.chainsea.ce.sdk.reference.MessageReference;
//import tw.com.chainsea.ce.sdk.service.ChatMessageService;
//import tw.com.chainsea.ce.sdk.service.ChatRoomService;
//import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
//import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.messagekit.ChildMessageRecyclerView;
//import tw.com.chainsea.chat.util.TimeUtil;
//import tw.com.chainsea.chat.view.chat.adapter.ChildChatMembersAdapter;
//import tw.com.chainsea.android.common.log.CELog;
//
///**
// * Create by evan on 1/22/21
// *
// * @author Evan Wang
// * @date 1/22/21
// */
//public class ChildChatRoomLayout extends ConstraintLayout {
//
//    private WeakReference<Context> weakReference;
//
//    @BindView(R.id.iv_close)
//    ImageView ivClose;
//    @BindView(R.id.iv_expand)
//    ImageView ivExpand;
//    @BindView(R.id.rv_members)
//    RecyclerView rvMembers;
//    @BindView(R.id.cmrv_messages)
//    ChildMessageRecyclerView childMessageRecyclerView;
//
//    int excludeHeight;
//    double heightSpec = 0.66;
//
//    // tools
//    private String currentDate;
//    private static SimpleDateFormat messageTimeLineFormat = new SimpleDateFormat("MMMdd日(EEE)", Locale.TAIWAN);
//
//    // bind data
//    private String selfId;
//    private String roomId;
//    private ChatRoomEntity entity;
//    private List<MessageEntity> messageEntities = Lists.newArrayList();
//
//    public ChildChatRoomLayout(Context context) {
//        super(context);
//        init(context);
//    }
//
//    public ChildChatRoomLayout(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        init(context);
//    }
//
//    public ChildChatRoomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        init(context);
//    }
//
//    private void init(Context context) {
//        setClickable(false);
//        this.weakReference = new WeakReference<Context>(context);
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View root = inflater.inflate(R.layout.layout_child_chat_room, this);
//        ButterKnife.bind(this, root);
//        this.excludeHeight = UiHelper.dip2px(this.weakReference.get(), 44);
//        this.selfId = TokenPref.getInstance(context).getUserId();
//    }
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//    }
//
//    private void measureHeight() {
//        ConstraintLayout.LayoutParams params = (LayoutParams) getLayoutParams();
//        if (this.heightSpec == 1.0) {
//            params.topToTop = LayoutParams.PARENT_ID;
//            params.height = LayoutParams.MATCH_PARENT;
//        } else {
//            params.topToTop = LayoutParams.UNSET;
//            params.height = (int) ((UiHelper.getDisplayHeight(this.weakReference.get()) - this.excludeHeight) * this.heightSpec);
//        }
//        invalidate();
//        requestLayout();
//    }
//
//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom);
//    }
//
//    @Override
//    public void requestLayout() {
//        super.requestLayout();
//    }
//
//    @Deprecated
//    public void open(String roomId) {
//        this.roomId = roomId;
//        this.messageEntities.clear();
//        boolean hasData = ChatRoomReference.getInstance().hasLocalData(roomId);
//        if (hasData) {
//            open(ChatRoomReference.getInstance().findById2( this.selfId, roomId, true, true, true, true, true));
//        } else {
//            ChatRoomService.getInstance().getChatRoomItem(this.weakReference.get(), this.selfId, roomId, RefreshSource.ALL, new ServiceCallBack<ChatRoomEntity, RefreshSource>() {
//                @Override
//                public void complete(ChatRoomEntity entity, RefreshSource refreshSource) {
//                    setTag(roomId);
//                    open(entity);
//                }
//
//                @Override
//                public void error(String message) {
//                    setTag(null);
//                }
//            });
//        }
//    }
//
//    @Deprecated
//    public void open(ChatRoomEntity entity) {
//        this.entity = entity;
//        UserPref.getInstance(weakReference.get()).setCurrentConsultationRoomId(entity.getId());
//        if (this.entity.getUnReadNum() > 0) {
//            ChatMessageService.doMessageReadAllByRoomId(weakReference.get(), entity, entity.getUnReadNum(), Lists.newArrayList(), true);
//        }
//        this.childMessageRecyclerView.setContainer(this.messageEntities, this.entity);
//        setVisibility(View.VISIBLE);
//        rvMembers.setVisibility(View.GONE);
//        if (0 == 1) {
//            rvMembers.setVisibility(View.VISIBLE);
//            initMembers();
//        }
//
//        this.heightSpec = 0.66;
//        this.currentDate = "";
//        measureHeight();
//        List<MessageEntity> localEntities = MessageReference.findByRoomId(this.entity.getId());
//        Collections.sort(localEntities);
//        String lastMessageId = "";
//        if (!localEntities.isEmpty()) {
//            lastMessageId = Iterables.getLast(localEntities).getId();
//        }
//        for (MessageEntity msg : localEntities) {
//            displayMessage(true, this.roomId, msg);
//        }
//        this.childMessageRecyclerView.refreshData();
//        perfectlyMessageData(this.roomId, lastMessageId);
//    }
//
//    private void perfectlyMessageData(String roomId, String lastMessageId) {
//        ChatMessageService.getMessageEntities(this.weakReference.get(), roomId, lastMessageId, Sort.ALL, new ServiceCallBack<List<MessageEntity>, RefreshSource>() {
//            @Override
//            public void complete(List<MessageEntity> messageEntities, RefreshSource refreshSource) {
//                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//                    for (MessageEntity msg : messageEntities) {
//                        displayMessage(true, roomId, msg);
//                    }
//                });
//            }
//
//            @Override
//            public void error(String message) {
//
//            }
//        });
//    }
//
//    private void initMembers() {
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.weakReference.get(), LinearLayoutManager.HORIZONTAL, false);
//        this.rvMembers.setLayoutManager(linearLayoutManager);
//        this.rvMembers.setItemAnimator(new DefaultItemAnimator());
//
//        this.rvMembers.setHasFixedSize(true);
//        ChildChatMembersAdapter adapter = new ChildChatMembersAdapter();
//        adapter.setData(ChatMemberCacheService.getChatMember(this.roomId)).refreshData();
//
//        this.rvMembers.setAdapter(adapter);
//        this.rvMembers.measure(0, 0);
//    }
//
//    public String getRoomId() {
//        return this.roomId;
//    }
//
//    public void displayMessage(boolean isNew, String roomId, MessageEntity entity) {
//        if (this.roomId.equals(roomId)) {
//            String date = messageTimeLineFormat.format(entity.getSendTime());
//            if (currentDate != null && this.messageEntities != null) {
//                if (this.messageEntities.size() == 0) {
//                    currentDate = messageTimeLineFormat.format(System.currentTimeMillis());
//                } else {
//                    currentDate = messageTimeLineFormat.format(this.messageEntities.get(this.messageEntities.size() - 1).getSendTime());
//                }
//            }
//            if (!date.equals(currentDate) || this.messageEntities.size() == 0) {
//                currentDate = date;
//                long dayBegin = TimeUtil.getDayBegin(entity.getSendTime());
//
//                MessageEntity timeMessage = new MessageEntity.Builder()
//                        .id(Tools.generateTimeMessageId(dayBegin))
//                        .sendTime(dayBegin)
//                        .roomId(this.roomId)
//                        .status(MessageStatus.SUCCESS)
//                        .sourceType(SourceType.SYSTEM)
//                        .content(new UndefContent("TIME_LINE").toStringContent())
//                        .sendTime(dayBegin)
//                        .build();
//                if (!this.messageEntities.contains(timeMessage)) {
//                    this.messageEntities.add(timeMessage);
//                }
//            }
//
//            // 去重複邏輯
//            if (this.messageEntities.contains(entity)) {
//                int index = this.messageEntities.indexOf(entity);
//                this.messageEntities.remove(index);
//            }
//
//            this.messageEntities.remove(entity);
//            this.messageEntities.add(entity);
//
//            this.childMessageRecyclerView.refreshData();
//
//            if (isNew) {
//                this.childMessageRecyclerView.refreshToBottom(true);
//            }
//        }
//    }
//
//    public void onSoftKeyboardOpened(int height) {
//        this.heightSpec = 1.0;
//        measureHeight();
//    }
//
//    public void onSoftKeyboardClosed(int height) {
//        this.heightSpec = 0.66;
//        measureHeight();
//    }
//
//    @Override
//    public void setVisibility(int visibility) {
//        super.setVisibility(visibility);
//        if (visibility == View.GONE || visibility == View.INVISIBLE) {
//            UserPref.getInstance(weakReference.get()).setCurrentConsultationRoomId("");
//        }
//    }
//
//
//    /**
//     * Global Handle Notify
//     */
//    public void handleEvent(EventMsg eventMsg) {
//        switch (eventMsg.getCode()) {
//            case MsgConstant.NOTICE_APPEND_CONSULTATION_NEW_MESSAGE_IDS:
//                CELog.d("");
//                List<String> appendNewMessageIds = JsonHelper.getInstance().fromToList(eventMsg.getString(), String[].class);
//                if (entity != null) {
//                    List<MessageEntity> localEntities = MessageReference.findByIdsAndRoomId(null, appendNewMessageIds.toArray(new String[appendNewMessageIds.size()]), entity.getId());
//                    for (MessageEntity messageEntity : localEntities) {
//                        displayMessage(true, entity.getId(), messageEntity);
//                    }
//                }
//                break;
//            case MsgConstant.MSG_STATUS_FILTER:
//                CELog.e("");
//                MsgStatusBean mMsgStatusBean = (MsgStatusBean) eventMsg.getData();
//                String messageId = mMsgStatusBean.getMessageId();
//                int sendNum = mMsgStatusBean.getSendNum();
//                long sendTime = mMsgStatusBean.getSendTime();
//                if (sendTime <= 0) {
//                    sendTime = System.currentTimeMillis();
//                }
//
//                if (sendNum < 0) {
////                    chatRoom.setFailNum(chatRoom.getFailNum() + 1);
//                }
//
//                int index = this.messageEntities.indexOf(new MessageEntity.Builder().id(messageId).build());
//                if (index < 0) {
//                    return;
//                }
//
//                MessageEntity message = this.messageEntities.get(index);
//                if (sendNum < 0) {
//                    message.setStatus(MessageStatus.FAILED);
//                } else {
//                    message.setStatus(MessageStatus.SUCCESS);
//                }
//                message.setSendNum(sendNum);
//                message.setSendTime(sendTime);
//                displayMessage(false, this.roomId, message);
//                break;
//        }
//    }
//
//
//
//    /* ↓↓↓↓ -------- ------- ---------- ------- ----- binding event ↓↓↓↓↓↓ */
//
//    @OnClick(R.id.iv_close)
//    public void doCloseAction(View view) {
//        setVisibility(View.GONE);
//    }
//
//    @OnClick(R.id.iv_expand)
//    public void doExpandAction(View view) {
//        this.heightSpec = this.heightSpec == 1.0 ? 0.66 : 1.0;
//        measureHeight();
//    }
//
//}
