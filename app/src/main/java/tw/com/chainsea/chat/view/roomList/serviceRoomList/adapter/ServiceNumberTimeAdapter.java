//package tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter;
//
//import android.content.Context;
//import android.text.SpannableStringBuilder;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//import androidx.databinding.DataBindingUtil;
//
//import com.google.common.base.Strings;
//import com.google.common.collect.ComparisonChain;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.common.collect.Sets;
//
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import tw.com.chainsea.chat.util.TextViewHelper;
//import tw.com.chainsea.ce.sdk.bean.InputLogBean;
//import tw.com.chainsea.ce.sdk.bean.PicSize;
//import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
//import tw.com.chainsea.ce.sdk.bean.msg.ChannelType;
//import tw.com.aile.sdk.bean.message.MessageEntity;
//import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
//import tw.com.chainsea.ce.sdk.bean.msg.SourceType;
//import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
//import tw.com.chainsea.ce.sdk.bean.room.ComponentRoomType;
//import tw.com.chainsea.ce.sdk.database.DBManager;
//import tw.com.chainsea.ce.sdk.service.AvatarService;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.base.Constant;
//import tw.com.chainsea.chat.databinding.ItemRoom4Binding;
//import tw.com.chainsea.chat.lib.AtMatcherHelper;
//import tw.com.chainsea.chat.util.TimeUtil;
//import tw.com.chainsea.chat.util.Unit;
//import tw.com.chainsea.chat.view.chatroom.adapter.listener.OnRoomItemClickListener;
//import tw.com.chainsea.chat.view.roomList.type.Menu;
//import tw.com.chainsea.custom.view.recyclerview.AnimationAdapter;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemNoSwipeViewHolder;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemSwipeWithActionWidthViewHolder;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension;
//import tw.com.chainsea.custom.view.recyclerview.lintener.OnSwipeMenuListener;
//
//
///**
// * Created by Evan Wang 2019/10/16
// */
//
//public class ServiceNumberTimeAdapter extends AnimationAdapter<ItemBaseViewHolder<ChatRoomEntity>> {
//    private List<ChatRoomEntity> metadata = Lists.newArrayList();
//    private final List<ChatRoomEntity> entities = Lists.newArrayList();
//    private boolean isComponent = false; // 是否組裝
//    private boolean isRoom = false;
//    private OnRoomItemClickListener<ChatRoomEntity> onRoomItemClickListener;
//    private ItemTouchHelperExtension itemTouchHelperExtension;
//    private OnSwipeMenuListener<ChatRoomEntity, Menu> onSwipeMenuListener;
//    private ItemRoom4Binding room4Binding;
//    private Set<String> expansionSet = Sets.newHashSet();
//    private Set<String> isSubNodeSet = Sets.newHashSet();
//    private Map<String, ChatRoomEntity> limitOpenDatas = Maps.newHashMap();
//
//    private int LIMIT_SUB_LENGTH = 3;
//
//    String userId = "";
//    Context context;
//
//    public ServiceNumberTimeAdapter() {
//        super.setAnimationEnable(false);
//    }
//
//    @NonNull
//    @Override
//    public ItemBaseViewHolder<ChatRoomEntity> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        context = parent.getContext();
//        ItemRoom4Binding room4Binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_room_4, parent, false);
//        return viewType == 1 ? new MultiItemViewHolder(room4Binding) : new SingleItemViewHolder(room4Binding);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ItemBaseViewHolder<ChatRoomEntity> holder, int position) {
//        ChatRoomEntity entity = this.entities.get(position);
//        holder.onBind(entity, 0, position);
//    }
//
//    @Override
//    public int getItemCount() {
//        return this.entities.size();
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        ChatRoomEntity entity = entities.get(position);
//        switch (entity.getComponentType()) {
//            case MULTI:
//                return 1;
//            case SINGLE:
//                if (entity.isSub() && entity.isAnimationEnable()) {
//                    return super.triggerType;
//                } else if (entity.isSub() && !entity.isAnimationEnable()) {
//                    return super.triggerType + 1;
//                } else {
//                    return 0;
//                }
//            default:
//                return 0;
//        }
//    }
//
//
//    /**
//     * 設置是否為跟目錄，一級目錄
//     *
//     * @param isRoom
//     * @return
//     */
//    public ServiceNumberTimeAdapter setIsRoom(boolean isRoom) {
//        this.isRoom = isRoom;
//        return this;
//    }
//
//    public ServiceNumberTimeAdapter setData(List<ChatRoomEntity> metadata, boolean isComponent) {
//        this.metadata = metadata;
//        this.isComponent = isComponent;
//        this.entities.clear();
//        this.entities.addAll(component());
//        return this;
//    }
//
//    /**
//     * 新增單筆資料
//     *
//     * @param entity
//     * @param isComponent
//     * @return
//     */
//    public ServiceNumberTimeAdapter addData(ChatRoomEntity entity, boolean isComponent) {
//        if (this.metadata.contains(entity)) {
//            this.metadata.remove(entity);
//        }
//
//        this.metadata.add(entity);
//        this.isComponent = isComponent;
//        this.entities.clear();
//        this.entities.addAll(component());
//        sort(this.entities);
//
//        return this;
//    }
//
//
//    /**
//     * 刪除單筆資料
//     *
//     * @param entity
//     * @param isComponent
//     * @return
//     */
////    public ServiceNumberTimeAdapter removeData(ChatRoomEntity entity, boolean isComponent) {
////        this.metadata.remove(entity);
////        this.isComponent = isComponent;
////        Collections.sort(this.metadata);
////        component();
////        return this;
////    }
//
//
//    /**
//     * 刷新資料
//     */
//    public void refreshData() {
//        this.expansionSet.clear();
//        this.isSubNodeSet.clear();
//        this.limitOpenDatas.clear();
//        this.entities.clear();
//        this.entities.addAll(component());
//        sort(this.entities);
//        notifyDataSetChanged();
//    }
//
//
//    /**
//     * 組
//     */
//    public List<ChatRoomEntity> component() {
//        Iterator<ChatRoomEntity> iterator = this.metadata.iterator();
//        List<ChatRoomEntity> entities = Lists.newArrayList();
//        flag:
//        while (iterator.hasNext()) {
//            ChatRoomEntity room = iterator.next();
//            if (this.isComponent) {
//                room.getComponentEntities().clear();
//                for (ChatRoomEntity entity : entities) {
//                    if (entity.isServiceListComponent(room)) {
//                        entity.componentAdd(room);
//                        continue flag;
//                    }
//                }
//            } else {
//                room.getComponentEntities().clear();
//            }
//            entities.add(room);
//        }
//
//        // 處理交換
//        List<ChatRoomEntity> exchangeEntities = Lists.newArrayList();
//        for (ChatRoomEntity entity : entities) {
//            exchangeEntities.add(exchangeEntity(entity));
//        }
//        return exchangeEntities;
//    }
//
//    /**
//     * 提出主要聊天室
//     */
//    private ChatRoomEntity exchangeEntity(ChatRoomEntity entity) {
////        if (Strings.isNullOrEmpty(entity.getBusinessId())) {
////            return entity;
////        }
//
//        boolean hasGeneral = false;
////        for (ChatRoomEntity component : entity.getComponentEntities()) {
////            if (!hasGeneral) {
////                hasGeneral = Strings.isNullOrEmpty(component.getBusinessId());
////            }
////        }
//
//        ChatRoomEntity general = null;
//        List<ChatRoomEntity> components = Lists.newArrayList();
//        if (hasGeneral) {
//            for (ChatRoomEntity component : entity.getComponentEntities()) {
//                if (!Strings.isNullOrEmpty(component.getBusinessId())) {
//                    components.add(component);
//                } else {
//                    general = component;
//                }
//            }
//
//            entity.getComponentEntities().clear();
//            components.add(entity);
//            general.setComponentEntities(components);
//        } else {
//            if (!entity.getComponentEntities().isEmpty()) {
//                general = ChatRoomEntity.Build()
//                        .id(entity.getServiceNumberId())
//                        .isHardCode(true)
//                        .avatarId(entity.getAvatarId())
//                        .serviceNumberAvatarId(entity.getServiceNumberAvatarId())
//                        .name(entity.getName())
//                        .memberIds(entity.getMemberIds())
//                        .members(entity.getMembers())
//                        .type(entity.getType())
//                        .serviceNumberName(entity.getServiceNumberName())
//                        .lastMessage(entity.getLastMessage())
//                        .unfinishedEdited(entity.getUnfinishedEdited())
//                        .unfinishedEditedTime(entity.getUnfinishedEditedTime())
//                        .failedMessage(entity.getFailedMessage())
//                        .ownerId(entity.getOwnerId())
//                        .updateTime(entity.getUpdateTime())
//                        .build();
//
//                general.setComponentEntities(Lists.newArrayList(entity.getComponentEntities()));
//                entity.getComponentEntities().clear();
//                general.getComponentEntities().add(entity);
//            }
//        }
//
//        if (general == null) {
//            general = entity;
//        }
//        computeNodeEnd(general);
//        sort(general.getComponentEntities());
//        return general;
//    }
//
//
//    public void sort(List<ChatRoomEntity> entities) {
//        Collections.sort(entities, (s1, s2) -> {
//            return ComparisonChain.start()
////                    .compareTrueFirst(s2.getLastMessage() != null, s1.getLastMessage() != null)
//                    .compare(s2.getUpdateTime(), s1.getUpdateTime()).result();
////            return Longs.compare(s2.getLastMessage().getSendTime(), s1.getLastMessage().getSendTime() +0.5);
//        });
//    }
//
//    /**
//     * 更新聊天室未完成編輯內容
//     *
//     * @param entity
//     * @return
//     */
//    public ServiceNumberTimeAdapter changeUnEditByRoomId(ChatRoomEntity entity) {
//        if (entity == null || this.entities == null || this.entities.isEmpty()) {
//            return this;
//        }
//        Iterator<ChatRoomEntity> iterator = this.entities.iterator();
//        while (iterator.hasNext()) {
//            ChatRoomEntity chat = iterator.next();
//            if (chat.getId().equals(entity.getId())) {
//                chat.setUnfinishedEditedTime(entity.getUnfinishedEditedTime());
//                chat.setUnfinishedEdited(entity.getUnfinishedEdited());
//            }
//        }
//        return this;
//    }
//
//
//    /**
//     * 計算節點結束位置
//     */
//    private void computeNodeEnd(ChatRoomEntity entity) {
//        entity.setSubEnd(false);
//        entity.setSubCenter(false);
//        entity.setSubTop(false);
//        entity.setSub(false);
//        if (ComponentRoomType.MULTI.equals(entity.getComponentType()) && !entity.getComponentEntities().isEmpty()) {
//            sort(entity.getComponentEntities());
//            for (ChatRoomEntity sub : entity.getComponentEntities()) {
//                sub.setSubTop(false);
//                sub.setSubCenter(false);
//                sub.setSubEnd(false);
//                sub.setSub(true);
//            }
//            entity.getComponentEntities().get(entity.getComponentEntities().size() - 1).setSubEnd(true);
//            entity.getComponentEntities().get(0).setSubTop(true);
//
//            // 找出第三筆位置
//            if (entity.getComponentEntities().size() > 3) {
//                entity.getComponentEntities().get(2).setSubCenter(true);
//            }
//        }
//    }
//
//    public ServiceNumberTimeAdapter setOnRoomItemClickListener(OnRoomItemClickListener<ChatRoomEntity> onRoomItemClickListener) {
//        this.onRoomItemClickListener = onRoomItemClickListener;
//        return this;
//    }
//
//    public ServiceNumberTimeAdapter setOnSwipeMenuListener(OnSwipeMenuListener<ChatRoomEntity, Menu> onSwipeMenuListener) {
//        this.onSwipeMenuListener = onSwipeMenuListener;
//        return this;
//    }
//
//    public ServiceNumberTimeAdapter setItemTouchHelperExtension(ItemTouchHelperExtension itemTouchHelperExtension) {
//        this.itemTouchHelperExtension = itemTouchHelperExtension;
//        return this;
//    }
//
//    @Override
//    public void executeAnimatorEnd(int position) {
//
//    }
//
//    public interface OnItemClickListener<S extends ChatRoomEntity> {
//        void onItemClick(S s);
//    }
//
//    /**
//     * 關閉群組聊天室
//     *
//     * @param entity
//     */
//    private void closeNodeTree(ChatRoomEntity entity, int position) {
//        expansionSet.remove(entity.serviceUniqueId());
//        limitOpenDatas.remove(entity.getMergeMemberId());
//        isSubNodeSet.removeAll(entity.getComponentIds());
//        entities.removeAll(entity.getComponentEntities());
//        notifyDataSetChanged();
//    }
//
//    /**
//     * 關閉所有群組聊天室
//     */
//    public void closeAllNodeTree() {
//        expansionSet.clear();
//        limitOpenDatas.clear();
//        isSubNodeSet.clear();
//
//        Iterator<ChatRoomEntity> iterator = entities.iterator();
//        while (iterator.hasNext()) {
//            if (iterator.next().isSub()) {
//                iterator.remove();
//            }
//        }
//        notifyDataSetChanged();
//    }
//
//    /**
//     * 打開群組聊天室
//     *
//     * @param entity
//     */
//    private void openNodeTree(ChatRoomEntity entity, int position, int itemSize) {
//        expansionSet.add(entity.serviceUniqueId());
//        isSubNodeSet.addAll(entity.getComponentIds());
//        int postProcessing = 0;
//        for (int i = 0; i < entity.getComponentEntities().size(); i++) {
//            if (i < itemSize) {
//                ChatRoomEntity sub = entity.getComponentEntities().get(i);
//                sub.setAnimationEnable(true);
//                entities.add(position + 1 + i, sub);
//            } else {
//                limitOpenDatas.put(entity.getMergeMemberId(), entity);
//            }
//        }
//        notifyDataSetChanged();
//    }
//
//    class MultiItemViewHolder extends ItemNoSwipeViewHolder<ChatRoomEntity> {
//
//        public MultiItemViewHolder(@NonNull ItemRoom4Binding binding) {
//            super(binding.getRoot());
//            room4Binding = binding;
//            super.setMenuViews(room4Binding.llLeftMenu, room4Binding.llRightMenu);
//            super.setContentItemView(room4Binding.clContentItem);
//            room4Binding.tvTime.setVisibility(View.INVISIBLE);
//            room4Binding.ivTop.setVisibility(View.GONE);
//            room4Binding.ivDelete.setVisibility(View.GONE);
//            room4Binding.ivMute.setVisibility(View.GONE);
//            room4Binding.tvBusinessContent.setVisibility(View.GONE);
//            room4Binding.vSubNodeEnd.setVisibility(View.GONE);
//        }
//
//        @Override
//        public void onBind(ChatRoomEntity entity, int section, int position) {
//            room4Binding.tvName.setText(entity.getName() + "@" + entity.getServiceNumberName());
//
//            // EVAN_FLAG 2020-04-06 (1.10.0) 未讀數量
//            int unReadNumber = entity.getFeatureUnReadNumCount();
//            // EVAN_FLAG 2020-04-06 (1.11.0) 子節點未讀數量
//            // EVAN_FLAG 2020-04-08 (1.10.0) 是否為置頂
//            boolean isTop = entity.getFeatureTop();
//
//            room4Binding.clContentItem.setBackgroundResource(isTop ? R.drawable.selector_item_list_top : R.drawable.selector_item_list);
//            AvatarService.post(context, entity.getAvatarId(), PicSize.SMALL, room4Binding.civIcon, R.drawable.default_avatar);
//
//            room4Binding.tvUnread.setVisibility(unReadNumber <= 0 ? View.INVISIBLE : View.VISIBLE);
//            room4Binding.tvUnread.setText(Unit.getUnReadNumber(unReadNumber, false));
//
//            if (!expansionSet.contains(entity.serviceUniqueId())) {
//                room4Binding.clContentCell.setBackgroundResource(entity.getComponentEntities().size() > 2 ? R.drawable.sectioned_room_3l : R.drawable.sectioned_room_2l);
//                room4Binding.vSubNodeTop.setVisibility(View.GONE);
//                room4Binding.vSubNodeLeft.setVisibility(View.GONE);
//                room4Binding.vSubNodeRight.setVisibility(View.GONE);
//                room4Binding.vSubNodeDivider.setVisibility(View.GONE);
//            } else {
//                room4Binding.clContentCell.setBackgroundResource(0);
//                room4Binding.vSubNodeTop.setVisibility(View.VISIBLE);
//                room4Binding.vSubNodeLeft.setVisibility(View.VISIBLE);
//                room4Binding.vSubNodeRight.setVisibility(View.VISIBLE);
//                room4Binding.vSubNodeDivider.setVisibility(View.VISIBLE);
//                room4Binding.tvUnread.setVisibility(View.GONE);
//            }
//
//            int size = entity.getComponentEntities().size();
//            if (size > 0) {
//                room4Binding.tvName.setText(room4Binding.tvName.getText().toString() + " (" + size + ")");
//            }
//
//            room4Binding.clContentItem.setOnClickListener(v -> {
//                if (itemTouchHelperExtension != null) {
//                    itemTouchHelperExtension.closeOpened();
//                }
//                if (expansionSet.contains(entity.serviceUniqueId())) {
//                    closeNodeTree(entity, position);
//                } else {
//                    computeNodeEnd(entity);
//                    int limit = LIMIT_SUB_LENGTH > 0 ? LIMIT_SUB_LENGTH : entity.getComponentEntities().size();
//                    openNodeTree(entity, position, limit);
//                }
//            });
//
////            // 開發模式
////            if (BuildConfig.DEBUG) {
////                room4Binding.tvName.setTextColor(Color.BLACK);
////                if (entity.isFavourite()) {
////                    room4Binding.tvName.setTextColor(Color.RED);
////                }
////
////                if (entity.isAtMe()) {
////                    room4Binding.tvName.setTextColor(Color.BLUE);
////                }
////
////                if (!Strings.isNullOrEmpty(entity.getBusinessId())) {
////                    room4Binding.tvName.setTextColor(Color.MAGENTA);
////                }
////            }
//        }
//    }
//
//    class SingleItemViewHolder extends ItemSwipeWithActionWidthViewHolder<ChatRoomEntity> {
//        public SingleItemViewHolder(@NonNull ItemRoom4Binding binding) {
//            super(binding.getRoot());
//            room4Binding = binding;
//            super.setMenuViews(room4Binding.llLeftMenu, room4Binding.llRightMenu);
//            super.setContentItemView(room4Binding.clContentItem);
//            room4Binding.ivDelete.setVisibility(View.GONE);
//        }
//
//        @Override
//        public void onBind(ChatRoomEntity entity, int section, int position) {
//            if (entity != null && entity.getServiceNumberType() != null) {
//                switch (entity.getServiceNumberType()) {
//                    case BOSS:
//                        room4Binding.tvName.setText(TextViewHelper.setLeftImage(context, entity.getName() + "@" + entity.getServiceNumberName(), R.drawable.ic_icon_boss_15dp));
//                        break;
//                    case PROFESSIONAL:
//                        room4Binding.tvName.setText(TextViewHelper.setLeftImage(context, entity.getName() + "@" + entity.getServiceNumberName(), R.drawable.icon_service_number_blue_15dp));
//                        break;
//                    case NONE:
//                    case NORMAL:
//                    default:
//                        room4Binding.tvName.setText(TextViewHelper.setLeftImage(context, entity.getName() + "@" + entity.getServiceNumberName(), R.drawable.icon_subscribe_number_pink_15dp));
//                }
//            } else {
//                room4Binding.tvName.setText(TextViewHelper.setLeftImage(context, entity.getName() + "@" + entity.getServiceNumberName(), R.drawable.icon_subscribe_number_pink_15dp));
//            }
//
//
//            // EVAN_FLAG 2020-04-06 (1.10.0) 未讀數量
//            int unReadNumber = entity.getUnReadNum();
//            // EVAN_FLAG 2020-04-06 (1.11.0) 子節點未讀數量
////            int subUnReadNumber = 0;
//            // EVAN_FLAG 2020-06-01 (1.11.0) 是否被設置為靜音
//            boolean isMute = entity.isMute();
//            // EVAN_FLAG 2020-04-08 (1.10.0) 是否為置頂
//            boolean isTop = entity.isTop();
//            // EVAN_FLAG 2020/5/18 (1.11.0) 物件名稱
//            String businessName = entity.getBusinessName();
//            // EVAN_FLAG 2020-02-15 (1.9.1) 最後一筆訊息時間處理 and 渠道
//            MessageEntity lastMessageEntity = entity.getLastMessage();
//
//
//            room4Binding.clContentItem.setBackgroundResource(isTop ? R.drawable.selector_item_list_top : R.drawable.selector_item_list);
//            room4Binding.ivTop.setImageResource(isTop ? R.drawable.ic_no_top : R.drawable.ic_top);
//            room4Binding.ivMute.setImageResource(isMute ? R.drawable.amplification : R.drawable.not_remind);
//            AvatarService.post(context, entity.getAvatarId(), PicSize.SMALL, room4Binding.civIcon, R.drawable.default_avatar);
//
//            room4Binding.tvTime.setVisibility(View.INVISIBLE);
//            room4Binding.civSmallIcon.setVisibility(View.GONE);
//            room4Binding.tvContent.setVisibility(View.INVISIBLE);
//            room4Binding.tvUnread.setVisibility(View.GONE);
//
//
//            if (lastMessageEntity != null) {
//                room4Binding.tvTime.setVisibility(View.VISIBLE);
//                if (lastMessageEntity.getSendTime() <= 0) {
//                    room4Binding.tvTime.setVisibility(View.INVISIBLE);
//                } else {
//                    room4Binding.tvTime.setText(TimeUtil.INSTANCE.getTimeShowString(lastMessageEntity.getSendTime(), true));
//                }
//
//                // EVAN_FLAG 2020-02-15 (1.9.1) 渠道資料處理
//                ChannelType channel = lastMessageEntity.getFrom();
//                if (channel == null) {
//                    room4Binding.civSmallIcon.setVisibility(View.GONE);
//                } else {
//                    room4Binding.civSmallIcon.setVisibility(View.VISIBLE);
//                    switch (channel) {
//                        case FB:
//                            room4Binding.civSmallIcon.setImageResource(R.drawable.ic_fb);
//                            break;
//                        case LINE:
//                            room4Binding.civSmallIcon.setImageResource(R.drawable.ic_line);
//                            break;
//                        case QBI:
//                        case AILE_WEB_CHAT:
//                            room4Binding.civSmallIcon.setImageResource(R.drawable.qbi_icon);
//                            break;
//                        case WEICHAT:
//                            room4Binding.civSmallIcon.setImageResource(R.drawable.wechat_icon);
//                            break;
//                        case IG:
//                            room4Binding.civSmallIcon.setImageResource(R.drawable.ic_ig);
//                            break;
//                        case GOOGLE:
//                            room4Binding.civSmallIcon.setImageResource(R.drawable.ic_google_message);
//                            break;
//                        case CE:
//                            room4Binding.civSmallIcon.setImageResource(R.drawable.ce_icon);
//                            break;
//                        case UNDEF:
//                        default:
//                            room4Binding.civSmallIcon.setVisibility(View.GONE);
//                            break;
//                    }
//                }
//
//                String sendName = "我: ";
//                InputLogBean bean = InputLogBean.from(entity.getUnfinishedEdited());
//                if (entity.getFailedMessage() != null) { // EVAN_FLAG 2020-02-12 (1.9.1) 如果有發送失敗訊息
//                    MessageEntity failedMessage = entity.getFailedMessage();
//                    SpannableStringBuilder builder = new SpannableStringBuilder("");
//                    switch (failedMessage.getType()) {
//                        case AT:
//                            builder = AtMatcherHelper.matcherAtUsers("@", ((AtContent) failedMessage.content()).getMentionContents(), entity.getMembersTable());
//                            break;
//                        default:
//                            builder.append(failedMessage.content().simpleContent());
//                    }
//                    room4Binding.tvContent.setText(TextViewHelper.setLeftImage(context, builder, R.drawable.ic_mes_failure_14dp));
//                } else if (!Strings.isNullOrEmpty(bean.getText())) { // EVAN_FLAG 2020-02-12 (1.9.1) 如果有未編輯完成
//                    room4Binding.tvContent.setText(TextViewHelper.setLeftImage(context, bean.getText(), R.drawable.ic_edit_gray_14dp));
////                room4Binding.tvContent.setText(TextViewHelper.setLeftImageAndHighLight(getContext(),bean.getText(), R.drawable.ic_edit_gray_14dp,bean.getText(), InputLogType.AT.equals(bean.getTodoOverviewType())? 0xFF4A90E2 : 0xFF8F8E94));
//                } else if (entity.getLastMessage() != null) { // EVAN_FLAG 2020-02-12 (1.9.1) 如果有最新一句 信息實體
//                    room4Binding.tvContent.setVisibility(View.VISIBLE);
//                    room4Binding.tvTime.setVisibility(View.VISIBLE);
//
//                    // EVAN_FLAG 2020-02-12 (1.9.1) 判斷 local 有無資料，若有且有備註名
//                    MessageEntity lastMessage = entity.getLastMessage();
//                    if (!Strings.isNullOrEmpty(lastMessage.getSenderId()) && !userId.equals(lastMessage.getSenderId())) {
//                        UserProfileEntity profile = DBManager.getInstance().queryFriend(lastMessage.getSenderId());
//                        if (SourceType.SYSTEM.equals(lastMessage.getSourceType())) {
//                            sendName = "";
//                        } else if (profile != null) {
//                            sendName = (!Strings.isNullOrEmpty(profile.getAlias()) ? profile.getAlias() : profile.getNickName()) + ": ";
//                        }
//                    }
//
//                    // EVAN_FLAG 2020-02-12 (1.9.1) 如果是收回訊息
//                    if (MessageFlag.RETRACT.equals(lastMessage.getFlag())) {
//                        room4Binding.tvContent.setText(sendName + Constant.RETRACT_MSG);
//                    } else {
//                        SpannableStringBuilder builder = new SpannableStringBuilder("");
//                        switch (lastMessage.getType()) {
//                            case AT:
//                                AtContent atContent = (AtContent) lastMessage.content();
//                                builder = AtMatcherHelper.matcherAtUsers("@", atContent.getMentionContents(), entity.getMembersTable());
//                                break;
//                            default:
//                                builder.append(lastMessage.content().simpleContent());
//                        }
//                        builder.insert(0, sendName);
//                        room4Binding.tvContent.setText(builder);
//                    }
//                } else {
//                    room4Binding.tvContent.setText("");
//                }
//            } else {
//                room4Binding.tvContent.setText("");
//            }
//            room4Binding.tvUnread.setText(Unit.getUnReadNumber(unReadNumber, false));
//            room4Binding.tvUnread.setVisibility(unReadNumber <= 0 ? View.INVISIBLE : View.VISIBLE);
//
//            room4Binding.vSubNodeLeft.setVisibility(isSubNodeSet.contains(entity.getId()) && ChatRoomType.services.equals(entity.getType()) ? View.VISIBLE : View.GONE);
//            room4Binding.vSubNodeRight.setVisibility(isSubNodeSet.contains(entity.getId()) && ChatRoomType.services.equals(entity.getType()) ? View.VISIBLE : View.GONE);
//            room4Binding.vSubNodeDivider.setVisibility(isSubNodeSet.contains(entity.getId()) && ChatRoomType.services.equals(entity.getType()) ? View.VISIBLE : View.GONE);
//
//            // EVAN_FLAG 2020-08-04 (1.12.0) 是否完整展開
////            room4Binding.ivSubNodeCenter.setVisibility(entity.isSubCenter() ? View.VISIBLE : View.GONE);
//            room4Binding.ivSubNodeCenter.setVisibility(entity.isSubCenter() && limitOpenDatas.get(entity.getMergeMemberId()) != null ? View.VISIBLE : View.GONE);
//            // EVAN_FLAG 2020-05-22 (1.11.0) 是否為展開最後一筆
//            room4Binding.vSubNodeEnd.setVisibility(entity.isSubEnd() ? View.VISIBLE : View.GONE);
//
//            room4Binding.tvBusinessContent.setVisibility(!Strings.isNullOrEmpty(businessName) ? View.VISIBLE : View.GONE);
//            room4Binding.tvBusinessContent.setText(!Strings.isNullOrEmpty(businessName) ? TextViewHelper.setLeftImage(context, businessName, R.drawable.ic_icon_link_gary_14dp) : "");
//
//            room4Binding.ivTop.setOnClickListener(view -> {
//                if (itemTouchHelperExtension != null) {
//                    itemTouchHelperExtension.closeOpened();
//                }
//                if (onSwipeMenuListener != null) {
//                    onSwipeMenuListener.onSwipeMenuClick(entity, Menu.TOP, position);
//                }
//            });
//
//            room4Binding.ivMute.setOnClickListener(v -> {
//                if (itemTouchHelperExtension != null) {
//                    itemTouchHelperExtension.closeOpened();
//                }
//                if (onSwipeMenuListener != null) {
//                    onSwipeMenuListener.onSwipeMenuClick(entity, Menu.MUTE, position);
//                }
//            });
//
//            room4Binding.clContentItem.setOnClickListener(v -> {
//                if (itemTouchHelperExtension != null) {
//                    itemTouchHelperExtension.closeOpened();
//                }
//                if (onRoomItemClickListener != null) {
//                    onRoomItemClickListener.onItemClick(entity);
//                }
//            });
//
//
//            room4Binding.ivSubNodeCenter.setOnClickListener(v -> {
//                if (itemTouchHelperExtension != null) {
//                    itemTouchHelperExtension.closeOpened();
//                }
//
//                if (limitOpenDatas.get(entity.getMergeMemberId()) != null) {
//                    ChatRoomEntity parent = limitOpenDatas.get(entity.getMergeMemberId());
//                    closeNodeTree(parent, 0);
////                        int limit = LIMIT_SUB_LENGTH > 0 ? LIMIT_SUB_LENGTH : parent.getComponentEntities().size();
//                    openNodeTree(parent, entities.indexOf(parent), parent.getComponentEntities().size());
//                    limitOpenDatas.remove(entity.getMergeMemberId());
//                }
//            });
//
//
////            // 開發模式
////            if (BuildConfig.DEBUG) {
////                room4Binding.tvName.setTextColor(Color.BLACK);
////                if (entity.isFavourite()) {
////                    room4Binding.tvName.setTextColor(Color.RED);
////                }
////
////                if (entity.isAtMe()) {
////                    room4Binding.tvName.setTextColor(Color.BLUE);
////                }
////
////                if (!Strings.isNullOrEmpty(entity.getBusinessId())) {
////                    room4Binding.tvName.setTextColor(Color.MAGENTA);
////                }
////            }
//
//        }
//    }
//
//}
