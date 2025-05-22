package tw.com.chainsea.chat.view.roomList.mainRoomList.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.text.StringHelper;
import tw.com.chainsea.ce.sdk.bean.GroupRefreshBean;
import tw.com.chainsea.ce.sdk.bean.UpdateAvatarBean;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.business.BusinessCode;
import tw.com.chainsea.ce.sdk.bean.business.BusinessEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.reference.BusinessReference;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemBaseRoom6Binding;
import tw.com.chainsea.chat.databinding.ItemIncludeTableRoom6Binding;
import tw.com.chainsea.chat.databinding.ItemSunRoom5Binding;
import tw.com.chainsea.chat.lib.ChatService;
import tw.com.chainsea.chat.view.roomList.type.NodeCategory;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;

/**
 * current by evan on 2020-11-04
 *
 * @author Evan Wang
 * date 2020-11-04
 */
public class MainRoomList3Adapter extends BaseRoomList3Adapter<ChatRoomEntity> {

    protected Set<ChatRoomEntity> otherDatas = Sets.newHashSet();

    Map<String, BusinessEntity> executorData = Maps.newHashMap();

    public MainRoomList3Adapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public ItemBaseViewHolder<ChatRoomEntity> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        super.setContext(parent.getContext());
        NodeCategory category = NodeCategory.of(viewType);
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemBaseRoom6Binding baseRoom6Binding;
        ItemIncludeTableRoom6Binding includeTableRoom6Binding;
        switch (category) {
            case BOSS_NODE:
                baseRoom6Binding = DataBindingUtil.inflate(inflater, R.layout.item_base_room_6, parent, false);
                return new BossChatRoomViewHolder(baseRoom6Binding);
            case CHILD_NODE:
                ItemSunRoom5Binding sunRoom5Binding = DataBindingUtil.inflate(inflater, R.layout.item_sun_room_5, parent, false);
                return new ChildChatRoomViewHolder(sunRoom5Binding);
            case GENERAL_PARENT_NODE:
                includeTableRoom6Binding = DataBindingUtil.inflate(inflater, R.layout.item_include_table_room_6, parent, false);
                return new ParentChatRoomViewHolder(includeTableRoom6Binding);
            case SUBSCRIBE_PARENT_NOD:
                includeTableRoom6Binding = DataBindingUtil.inflate(inflater, R.layout.item_include_table_room_6, parent, false);
                return new SubscribeParentChatRoomViewHolder(includeTableRoom6Binding);
            case SUBSCRIBE_NODE:
                baseRoom6Binding = DataBindingUtil.inflate(inflater, R.layout.item_base_room_6, parent, false);
                return new SubscribeChatRoomViewHolder(baseRoom6Binding);
            case GENERAL_NODE:
            default:
                baseRoom6Binding = DataBindingUtil.inflate(inflater, R.layout.item_base_room_6, parent, false);
                return new GeneralChatRoomViewHolder(baseRoom6Binding);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemBaseViewHolder<ChatRoomEntity> holder, int position) {
        ChatRoomEntity roomEntity = this.entities.get(position);
        holder.onBind(roomEntity, 0, position);
    }

    @Override
    public int getItemCount() {
        return this.entities.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatRoomEntity entity = this.entities.get(position);
        if (entity.isHardCode()) {
            if (ChatRoomType.person.equals(entity.getType())) {
                return NodeCategory.GENERAL_PARENT_NODE.getViewType();
            } else if (ChatRoomType.FRIEND_or_DISCUSS.contains(entity.getType())) {
                return NodeCategory.GENERAL_PARENT_NODE.getViewType();
            } else if (ChatRoomType.subscribe.equals(entity.getType())) {
                return NodeCategory.SUBSCRIBE_PARENT_NOD.getViewType();
            } else {
                CELog.e("");
            }
        }

        if (ChatRoomType.services.equals(entity.getType()) && ServiceNumberType.BOSS.equals(entity.getServiceNumberType())) {
            return NodeCategory.BOSS_NODE.getViewType();
        }

        if (ChatRoomType.subscribe.equals(entity.getType())) {
            return NodeCategory.SUBSCRIBE_NODE.getViewType();
        }

        return NodeCategory.GENERAL_NODE.getViewType();
    }

    @Override
    public void sort() {
        Collections.sort(this.metadata);
    }

    @Override
    protected void filter() {
        Iterator<ChatRoomEntity> iterator = this.metadata.iterator();
        while (iterator.hasNext()) {
            ChatRoomEntity entity = iterator.next();
            if (ChatRoomType.broadcast.equals(entity.getType())) {
                iterator.remove();
            }
            // Being Consulting
            if (!Strings.isNullOrEmpty(entity.getConsultSrcRoomId()) && filterConsult) {
                iterator.remove();
            }
        }
    }

    @Override
    protected void pullAway() {
        otherDatas.clear();
        businessSelfRelData.clear();
        businessExecutorRelData.clear();
        businessSubscribeRelData.clear();
        for (ChatRoomEntity t : this.metadata) {
            if (getUserId().equals(t.getBusinessExecutorId()) && !ChatRoomType.subscribe.equals(t.getType())) {
                businessSelfRelData.put(getUserId(), t);
            } else if (ChatRoomType.FRIEND_or_DISCUSS.contains(t.getType()) && !Strings.isNullOrEmpty(t.getBusinessExecutorId()) && !Strings.isNullOrEmpty(t.getBusinessId())) {
                businessExecutorRelData.put(t.getBusinessExecutorId(), t);
            } else if (ChatRoomType.subscribe.equals(t.getType()) && !Strings.isNullOrEmpty(t.getBusinessId())) {
                businessSubscribeRelData.put(t.getServiceNumberId(), t);
            } else {
                if (!StringHelper.isValidUUID(t.getBusinessExecutorId())) {
                    if (!Strings.isNullOrEmpty(t.getBusinessExecutorId())) {
                        CELog.e("");
                    }
                }
                otherDatas.add(t);
            }
        }
    }

    @Override
    protected void group() {
        if (!whetherToAssemble) {
            this.entities.clear();
            this.entities.addAll(this.metadata);
            Collections.sort(this.entities);
            return;
        }

        SetMultimap<String, ChatRoomEntity> selfRelData = HashMultimap.create(businessSelfRelData);
        SetMultimap<String, ChatRoomEntity> executorRelData = HashMultimap.create(businessExecutorRelData);
        SetMultimap<String, ChatRoomEntity> subscribeRelData = HashMultimap.create(businessSubscribeRelData);

        Iterator<ChatRoomEntity> iterator = otherDatas.iterator();
        List<ChatRoomEntity> generalEntities = Lists.newArrayList();
        while (iterator.hasNext()) {
            ChatRoomEntity t = iterator.next();
            if (ChatRoomType.person.equals(t.getType())) {
                if (selfRelData.get(getUserId()) != null && !selfRelData.get(getUserId()).isEmpty()) {
                    businessSelfRelData.remove(getUserId(), t);
                    t.setSub(true);
                    t.setBindKey(getUserId());
                    businessSelfRelData.put(getUserId(), t);
                    ChatRoomEntity feature = getFeature(t, businessSelfRelData.get(getUserId()));
                    ChatRoomEntity.ChatRoomEntityBuilder general = newGeneral(getUserId(), t);
                    general.isTop(feature.isTop())
                        .isAtMe(feature.isAtMe())
                        .isFavourite(feature.isFavourite())
                        .businessId(feature.getBusinessId())
                        .updateTime(feature.getUpdateTime())
                        .lastMessage(feature.getLastMessage())
                        .failedMessage(feature.getFailedMessage())
                        .unfinishedEdited(feature.getUnfinishedEdited())
                        .unReadNum(feature.getUnReadNum());
                    generalEntities.add(general.build());
                    iterator.remove();
//                    selfRelData.clear();
                    selfRelData.removeAll(getUserId());
                }
                // EVAN_FLAG 2020-11-09 (1.14.0) is friend & friend == executor exp: has friend date
            } else if (ChatRoomType.friend.equals(t.getType())) {
                List<String> memberIds = Lists.newArrayList(t.getMemberIds());
                memberIds.remove(getUserId());
                if (memberIds.isEmpty()) {
                    return;
                }
                String friendId = memberIds.get(0);
                if (executorRelData.get(friendId) != null && !executorRelData.get(friendId).isEmpty()) {
                    businessExecutorRelData.remove(friendId, t);
                    t.setSub(true);
                    t.setBindKey(friendId);
                    businessExecutorRelData.put(friendId, t);
                    ChatRoomEntity feature = getFeature(t, businessExecutorRelData.get(friendId));
                    ChatRoomEntity.ChatRoomEntityBuilder general = newGeneral(friendId, t);
                    general.isTop(feature.isTop())
                        .isAtMe(feature.isAtMe())
                        .isFavourite(feature.isFavourite())
                        .businessId(feature.getBusinessId())
                        .updateTime(feature.getUpdateTime())
                        .lastMessage(feature.getLastMessage())
                        .failedMessage(feature.getFailedMessage())
                        .unfinishedEdited(feature.getUnfinishedEdited())
                        .unReadNum(feature.getUnReadNum());
                    generalEntities.add(general.build());
                    iterator.remove();
                    executorRelData.removeAll(friendId);
                }
            } else if (ChatRoomType.subscribe.equals(t.getType())) {
                String serviceNumberId = t.getServiceNumberId();
                if (subscribeRelData.get(serviceNumberId) != null && !subscribeRelData.get(serviceNumberId).isEmpty()) {
                    businessSubscribeRelData.remove(serviceNumberId, t);
                    t.setSub(true);
                    t.setBindKey(serviceNumberId);
                    businessSubscribeRelData.put(serviceNumberId, t);
                    ChatRoomEntity feature = getFeature(t, businessSubscribeRelData.get(serviceNumberId));
                    ChatRoomEntity.ChatRoomEntityBuilder general = newSubscribeGeneral(serviceNumberId, t);
                    general.isTop(feature.isTop())
                        .isAtMe(feature.isAtMe())
                        .isFavourite(feature.isFavourite())
                        .businessId(feature.getBusinessId())
                        .updateTime(feature.getUpdateTime())
                        .lastMessage(feature.getLastMessage())
                        .failedMessage(feature.getFailedMessage())
                        .unfinishedEdited(feature.getUnfinishedEdited())
                        .unReadNum(feature.getUnReadNum());
                    generalEntities.add(general.build());
                    iterator.remove();
                    subscribeRelData.removeAll(serviceNumberId);
                }
            }
        }

        // 自己的執行物件聊天室
        if (!selfRelData.isEmpty()) {
            Set<ChatRoomEntity> entities = Sets.newHashSet(selfRelData.values());
            ChatRoomEntity original = Iterables.getFirst(entities, null);
            ChatRoomEntity feature = getFeature(original, Sets.newHashSet(selfRelData.values()));
            ChatRoomEntity.ChatRoomEntityBuilder general = newGeneral(getUserId(), original);
            general.isTop(feature.isTop())
                .type(ChatRoomType.person)
                .isAtMe(feature.isAtMe())
                .isFavourite(feature.isFavourite())
                .businessId(feature.getBusinessId())
                .updateTime(feature.getUpdateTime())
                .lastMessage(feature.getLastMessage())
                .failedMessage(feature.getFailedMessage())
                .unfinishedEdited(feature.getUnfinishedEdited())
                .unReadNum(feature.getUnReadNum());
            generalEntities.add(general.build());
            selfRelData.clear();
        }

        Iterator<String> executorKeyIterator = executorRelData.keySet().iterator();
        while (executorKeyIterator.hasNext()) {
            String key = executorKeyIterator.next();
            Set<ChatRoomEntity> sets = executorRelData.get(key);
            // EVAN_FLAG 2020-11-09 (1.14.0) group datas size > 1
            if (!sets.isEmpty()) {
                ChatRoomEntity original = getOriginalByType(ChatRoomType.friend, sets);
                if (original != null) {
                    ChatRoomEntity feature = getFeature(original, executorRelData.get(key));
                    ChatRoomEntity.ChatRoomEntityBuilder general = newGeneral(key, original);
                    general.isTop(feature.isTop())
                        .isAtMe(feature.isAtMe())
                        .isFavourite(feature.isFavourite())
                        .businessId(feature.getBusinessId())
                        .updateTime(feature.getUpdateTime())
                        .lastMessage(feature.getLastMessage())
                        .failedMessage(feature.getFailedMessage())
                        .unfinishedEdited(feature.getUnfinishedEdited())
                        .unReadNum(feature.getUnReadNum());
                    general.isSubEnd(true);
                    general.noMaster(true);

                    if (executorData.get(key) == null) {
                        BusinessEntity b = BusinessReference.findExecutorAvatarAndNameById(null, key);
                        executorData.put(key, b);
                    }

                    if (executorData.get(key) != null) {
                        String avatarId = executorData.get(key).getExecutorAvatarId();
                        String name = executorData.get(key).getExecutorName();
                        if (!Strings.isNullOrEmpty(avatarId)) {
                            general.avatarId(avatarId);
                        }
                        if (!Strings.isNullOrEmpty(name)) {
                            general.name(name);
                        }
                    }
                    generalEntities.add(general.build());
                    executorKeyIterator.remove();
                }
            }
        }

        Iterator<String> subscribeKeyIterator = subscribeRelData.keySet().iterator();
        while (subscribeKeyIterator.hasNext()) {
            String key = subscribeKeyIterator.next();
            ChatRoomEntity original = Iterables.getFirst(subscribeRelData.get(key), null);
            if (original != null) {
                ChatRoomEntity feature = getFeature(original, subscribeRelData.get(key));
                ChatRoomEntity.ChatRoomEntityBuilder general = newSubscribeGeneral(key, original);
                general.isTop(feature.isTop())
                    .isAtMe(feature.isAtMe())
                    .isFavourite(feature.isFavourite())
                    .businessId(feature.getBusinessId())
                    .updateTime(feature.getUpdateTime())
                    .lastMessage(feature.getLastMessage())
                    .failedMessage(feature.getFailedMessage())
                    .unfinishedEdited(feature.getUnfinishedEdited())
                    .unReadNum(feature.getUnReadNum());
                generalEntities.add(general.build());
                subscribeKeyIterator.remove();
            }
        }

        if (!executorRelData.isEmpty()) {
            this.otherDatas.addAll(executorRelData.values());
        }

        this.entities.clear();
        this.entities.addAll(this.otherDatas);
        this.entities.addAll(0, generalEntities);
        Collections.sort(this.entities);
    }

    private ChatRoomEntity getOriginalByType(ChatRoomType type, Set<ChatRoomEntity> entities) {
        for (ChatRoomEntity entity : entities) {
            if (type.equals(entity.getType())) {
                return entity;
            }
        }
        return Iterables.getFirst(entities, null);
    }

    ChatRoomEntity getFeature(ChatRoomEntity original, Set<ChatRoomEntity> entities) {
        boolean isTop = original.isTop();
        boolean isFavourite = original.isFavourite();
        boolean isAtMe = original.isAtMe();
        int unReadNum = Math.abs(original.getUnReadNum());

        long upDataTime = original.getUpdateTime();

        Set<UserProfileEntity> members = Sets.newHashSet(original.getMembers());
        String unfinishedEdited = original.getUnfinishedEdited();
        MessageEntity lastMessage = original.getLastMessage();
        MessageEntity failedMessage = original.getFailedMessage();

        String businessId = original.getBusinessId();
        long sendTime = lastMessage != null ? lastMessage.getSendTime() : 0L;

        flag:
        for (ChatRoomEntity sub : entities) {
            if (original.equals(sub)) {
                continue flag;
            }
            if (!isTop) {
                isTop = sub.isTop();
            }

            if (Math.abs(sub.getUnReadNum()) > 0) {
                unReadNum += Math.abs(sub.getUnReadNum());
            }

            if (!isFavourite) {
                isFavourite = sub.isFavourite();
            }

            if (!isAtMe) {
                isAtMe = sub.isAtMe();
            }

            if (sub.getUpdateTime() > upDataTime) {
                if (sub.getLastMessage() != null && sendTime < sub.getLastMessage().getSendTime()) {
                    lastMessage = sub.getLastMessage();
                    sendTime = sub.getLastMessage().getSendTime();
                }
                upDataTime = sub.getUpdateTime();
                unfinishedEdited = sub.getUnfinishedEdited();


                if (sub.getFailedMessage() != null) {
                    failedMessage = sub.getFailedMessage();
                }

                if (Strings.isNullOrEmpty(businessId)) {
                    businessId = sub.getBusinessId();
                }
            }

            members.addAll(sub.getMembers());
        }

        return ChatRoomEntity.Build()
            .isTop(isTop)
            .isAtMe(isAtMe)
            .businessId(businessId)
            .isFavourite(isFavourite)
            .updateTime(upDataTime)
            .lastMessage(lastMessage)
            .failedMessage(failedMessage)
            .unfinishedEdited(unfinishedEdited)
            .unReadNum(unReadNum)
            .build();
    }

    private ChatRoomEntity.ChatRoomEntityBuilder newSubscribeGeneral(String serviceNumberId, ChatRoomEntity t) {
        return ChatRoomEntity.Build()
//                .id(t.getServiceNumberId())
            .bindKey(serviceNumberId)
            .id(t.getId())
            .isHardCode(true)
            .isAtMe(t.isAtMe())
            .isFavourite(t.isFavourite())
            .isTop(t.isTop())
            .avatarId(t.getAvatarId())
            .memberIds(t.getMemberIds())
            .members(t.getMembers())
            .type(t.getType())
            .serviceNumberAvatarId(t.getServiceNumberAvatarId())
            .name(t.getName())
            .serviceNumberId(serviceNumberId)
            .serviceNumberName(t.getServiceNumberName())
            .lastMessage(t.getLastMessage())
            .unfinishedEdited(t.getUnfinishedEdited())
            .unfinishedEditedTime(t.getUnfinishedEditedTime())
            .failedMessage(t.getFailedMessage())
            .ownerId(t.getOwnerId())
            .updateTime(t.getUpdateTime());
    }

    private ChatRoomEntity.ChatRoomEntityBuilder newGeneral(String friendId, ChatRoomEntity t) {
        return ChatRoomEntity.Build()
            .bindKey(friendId)
            .id(t.getId())
            .isHardCode(true)
            .isAtMe(t.isAtMe())
            .isFavourite(t.isFavourite())
            .isTop(t.isTop())
            .avatarId(t.getAvatarId())
            .memberIds(t.getMemberIds())
            .members(t.getMembers())
            .type(t.getType())
            .businessExecutorId(friendId)
            .serviceNumberAvatarId(t.getServiceNumberAvatarId())
            .name(t.getName())
            .serviceNumberName(t.getServiceNumberName())
            .lastMessage(t.getLastMessage())
            .unfinishedEdited(t.getUnfinishedEdited())
            .unfinishedEditedTime(t.getUnfinishedEditedTime())
            .failedMessage(t.getFailedMessage())
            .ownerId(t.getOwnerId())
            .updateTime(t.getUpdateTime());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changeUnEditByRoomId(ChatRoomEntity entity) {
        if (entity == null || this.metadata == null || this.metadata.isEmpty()) {
            return;
        }
        for (ChatRoomEntity chat : this.metadata) {
            if (chat.getId().equals(entity.getId())) {
                chat.setUnfinishedEditedTime(entity.getUnfinishedEditedTime());
                chat.setUnfinishedEdited(entity.getUnfinishedEdited());
            }
        }

        for (ChatRoomEntity chat : this.entities) {
            if (chat.getId().equals(entity.getId())) {
                chat.setUnfinishedEditedTime(entity.getUnfinishedEditedTime());
                chat.setUnfinishedEdited(entity.getUnfinishedEdited());
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void handleEvent(EventMsg eventMsg) {
        switch (eventMsg.getCode()) {
            case MsgConstant.NOTICE_SELF_EXIT_ROOM:
                metadata.remove(ChatRoomEntity.Build().id(eventMsg.getString()).build());
                refreshData();
                break;
            case MsgConstant.UI_NOTICE_CLOSE_TREE_LIST:
//                closeAllNodeTree();
                break;
            case MsgConstant.REFRESH_ROOM_BY_LOCAL:
                ChatRoomEntity refreshEntity = JsonHelper.getInstance().from(eventMsg.getString(), ChatRoomEntity.class);
                if (ChatRoomSource.MAIN.equals(refreshEntity.getListClassify())) {
                    int refreshIndex = metadata.indexOf(refreshEntity);
                    if (refreshIndex != -1) {
                        metadata.remove(refreshEntity);
                        metadata.add(refreshIndex, refreshEntity);
                    } else {
                        metadata.add(refreshEntity);
                    }
                    refreshData();
                }
                break;
            case MsgConstant.OUTCROP_MENTION_UNREAD_ROOM:
                String json = eventMsg.getData().toString();
                ChatRoomEntity mentionEntity = JsonHelper.getInstance().from(json, ChatRoomEntity.class);
                int mentionIndex = metadata.indexOf(mentionEntity);
                if (mentionIndex >= 0) {
                    metadata.get(mentionIndex).setAtMe(mentionEntity.isAtMe());
                    refreshData();
                }
                break;
            case MsgConstant.ACCOUNT_REFRESH_FILTER: // 6
                UserProfileEntity account = (UserProfileEntity) eventMsg.getData();
//                String userId = TokenSave.getInstance(ctx).getUserId();
                if (!account.getId().equals(userId)) {
                    String roomId = account.getRoomId();
                    ChatRoomEntity entity = ChatRoomReference.getInstance().findById2(userId, roomId, true, true, true, true, true);
                    if (entity != null && ChatRoomSource.MAIN.equals(entity.getListClassify())) {
                        if (account.isBlock()) {
                            metadata.remove(entity);
                        } else {
                            entity.setAvatarId(account.getAvatarId());
                            entity.setName(TextUtils.isEmpty(account.getAlias()) ? account.getNickName() : account.getAlias());
                            ChatRoomReference.getInstance().save(entity);
                            int index = metadata.indexOf(entity);
                            if (index < 0) {
                                if (!Strings.isNullOrEmpty(entity.getBusinessId())) {
                                    metadata.add(0, entity);
                                }
                            } else {
                                metadata.remove(entity);
                                metadata.add(index, entity);
                            }
                            refreshData();
                        }
                    }
                }
                break;
            case MsgConstant.SESSION_UPDATE_FILTER: // 26
                ChatRoomEntity chatRoomEntity = JsonHelper.getInstance().from(eventMsg.getString(), ChatRoomEntity.class);
                ChatRoomEntity entity = ChatRoomReference.getInstance().findById2(this.userId, chatRoomEntity.getId(), true, true, true, true, true);

                if (entity == null) {
                    return;
                }

                if (entity.getLastMessage() == null) {
                    entity.setLastMessage(chatRoomEntity.getLastMessage());
                }

                if (entity.getUnReadNum() > 0 && entity.isAtMe()) {

                } else {
                    entity.setAtMe(chatRoomEntity.isAtMe());
                }

                if (ChatRoomSource.MAIN.equals(entity.getListClassify())) {
                    int index = metadata.indexOf(entity);
                    if (index < 0) {
                        if (!Strings.isNullOrEmpty(entity.getBusinessId())) {
                            metadata.add(0, entity);
                        }
                    } else {
                        metadata.remove(entity);
                        metadata.add(index, entity);
                    }
                    refreshData();
                }
                break;
            case MsgConstant.CHANGE_LAST_MESSAGE: // 50
//            case MsgConstant.IN_COMMING_FILTER: // 27
            case MsgConstant.SESSION_UPDATE_CALLING_FILTER: // 12
                String roomId = (String) eventMsg.getData();
                if (!Strings.isNullOrEmpty(roomId)) {
                    try {
//                        ChatRoomEntity updateEntity = ChatRoomReference.getInstance().findById(roomId);
                        ChatRoomEntity updateEntity = ChatRoomReference.getInstance().findById2(userId, roomId, true, true, true, true, true);
                        if (updateEntity != null && ChatRoomSource.MAIN.equals(updateEntity.getListClassify())) {
                            int index = metadata.indexOf(updateEntity);
                            if (index < 0) {
                                if (!Strings.isNullOrEmpty(updateEntity.getBusinessId())) {
                                    metadata.add(0, updateEntity);
                                }
                            } else {
                                metadata.remove(updateEntity);
                                metadata.add(index, updateEntity);
                            }
                            refreshData();
                        }
                    } catch (Exception ignored) {
                    }
                }
                break;
            case MsgConstant.REMOVE_FRIEND_FILTER: // 7
                UserProfileEntity mAccount = (UserProfileEntity) eventMsg.getData();
//                ChatRoomEntity friendEntity = ChatRoomReference.getInstance().findById(mAccount.getRoomId());
                ChatRoomEntity friendEntity = ChatRoomReference.getInstance().findById2(userId, mAccount.getRoomId(), false, false, false, false, false);

                if (friendEntity != null) {

                } else {
                    friendEntity = ChatRoomEntity.Build().id(mAccount.getRoomId()).build();
                }

                ChatService.getInstance().broadCastUpdateBadgeItem(-friendEntity.getUnReadNum());
                //删除本地聊天室
                ChatRoomReference.getInstance().deleteById(mAccount.getRoomId());
                //清空该聊天室本地消息
//                MessageReference.deleteByRoomId(mAccount.getId());
//                DBManager.getInstance().deleteMessageByRoomId(mAccount.getRoomId());
                metadata.remove(friendEntity);
                refreshData();

                break;
            case MsgConstant.SESSION_REMOVE_FILTER: // 23
            case MsgConstant.REMOVE_GROUP_FILTER: // 5
                try {
                    ChatRoomEntity removeEntity = ChatRoomEntity.Build().id((String) eventMsg.getData()).build();
                    metadata.remove(removeEntity);
                    refreshData();
                } catch (Exception ignored) {

                }
                break;
            case MsgConstant.UI_NOTICE_UPDATE_AVATARS_ALL:
                refreshData();
                break;
            case MsgConstant.NOTICE_UPDATE_AVATARS: // 批量更新頭像
                Map<String, String> updateAvatars = JsonHelper.getInstance().fromToMap(eventMsg.getString());
                String accountId = updateAvatars.get("accountId");
                String avatarId = updateAvatars.get("avatarId");
                for (ChatRoomEntity entity1 : metadata) {
                    for (UserProfileEntity profile : entity1.getMembers()) {
                        if (profile.getId().equals(accountId)) {
                            profile.setAvatarId(avatarId);
                            if (ChatRoomType.friend.equals(entity1.getType()) || entity1.isHardCode()) {
                                entity1.setAvatarId(avatarId);
                            }
                        }
                    }
                }
                refreshData();
                ThreadExecutorHelper.getHandlerExecutor().execute(this::notifyDataSetChanged, 500L);
                break;
            case MsgConstant.SEND_UPDATE_AVATAR: // 40
                try {
                    UpdateAvatarBean updateAvatarBean = (UpdateAvatarBean) eventMsg.getData();
                    if (updateAvatarBean != null && !Strings.isNullOrEmpty(updateAvatarBean.getRoomId())) {
                        ChatRoomEntity updateEntity = ChatRoomReference.getInstance().findById2(userId, updateAvatarBean.getRoomId(), true, true, true, true, true);
//                        ChatRoomEntity updateEntity = ChatRoomReference.getInstance().findById(updateAvatarBean.getRoomId());
                        if (updateEntity != null && ChatRoomSource.MAIN.equals(updateEntity.getListClassify())) {
                            int index = metadata.indexOf(updateEntity);
                            if (index < 0) {
                                if (!Strings.isNullOrEmpty(updateEntity.getBusinessId())) {
                                    metadata.add(0, updateEntity);
                                }
                            } else {
                                metadata.remove(updateEntity);
                                metadata.add(index, updateEntity);
                            }
                            refreshData();
                        }
                    }
                } catch (Exception ignored) {

                }
                break;
            case MsgConstant.GROUP_UPGRADE_FILTER: // 11
            case MsgConstant.GROUP_REFRESH_FILTER: // 10
                try {
                    GroupRefreshBean refreshBean = (GroupRefreshBean) eventMsg.getData();
                    if (refreshBean != null && !Strings.isNullOrEmpty(refreshBean.getSessionId())) {
                        ChatRoomEntity updateEntity = ChatRoomReference.getInstance().findById2(userId, refreshBean.getSessionId(), true, true, true, true, true);
//                        ChatRoomEntity updateEntity = ChatRoomReference.getInstance().findById(refreshBean.getSessionId());
                        if (updateEntity != null && ChatRoomSource.MAIN.equals(updateEntity.getListClassify())) {
                            int index = metadata.indexOf(updateEntity);
                            if (index < 0) {
                                if (!Strings.isNullOrEmpty(updateEntity.getBusinessId())) {
                                    metadata.add(0, updateEntity);
                                }
                            } else {
                                metadata.remove(updateEntity);
                                metadata.add(index, updateEntity);
                            }
                            refreshData();
                        }
                    }
                } catch (Exception ignored) {

                }
                break;
            case MsgConstant.SYNC_READ: // 41
                ChatRoomEntity syncEntity = (ChatRoomEntity) eventMsg.getData();
                if (ChatRoomSource.MAIN.equals(syncEntity.getListClassify())) {
                    int index = metadata.indexOf(syncEntity);
                    if (index < 0) {

                    } else {
                        metadata.get(index).setUnReadNum(0);
                    }
                    refreshData();
                }
                break;
//            case MsgConstant.TRIGGER_READ_ALL: // 49 處理全部已讀
//                if (!"isService".equals(eventMsg.getData())) {
//                    for (ChatRoomEntity readEntity : this.entities) {
//                        if (readEntity.getUnReadNum() > 0) {
//                            ChatMessageService.doMessageReadAllByRoomId(ctx, readEntity, new ServiceCallBack<ChatRoomEntity, Enum>() {
//                                @Override
//                                public void complete(ChatRoomEntity entity, Enum anEnum) {
//                                    // 更新角標數量
//                                    ChatRoomService.getInstance().getBadge(ctx);
//                                    ThreadUtils.runOnMainThread(() -> adapter.refreshData());
//                                }
//
//                                @Override
//                                public void error(String message) {
//
//                                }
//                            });
//                        }
//                    }
//                }
//                break;
            case MsgConstant.REMOVE_SERVICE_NUM_FILTER: // 34
                String serviceRoomId = (String) eventMsg.getData();
                ChatRoomEntity serviceEntity = ChatRoomReference.getInstance().findById2(userId, serviceRoomId, true, true, true, true, true);
                if (serviceEntity != null) {
                } else {
                    serviceEntity = ChatRoomEntity.Build().id(serviceRoomId).build();
                }
//                ChatService.getInstance().broadCastUpdateBadgeItem(-friendEntity.getUnReadNum());
                //删除本地聊天室
                ChatRoomReference.getInstance().deleteById(serviceEntity.getId());
                //清空该聊天室本地消息
//                MessageReference.deleteByRoomId(serviceEntity.getId());
//                DBManager.getInstance().deleteMessageByRoomId(serviceEntity.getId());

                if (metadata.contains(serviceEntity)) {
                    metadata.remove(serviceEntity);
                    refreshData();
                }
                break;
            case MsgConstant.REFRESH_ROOM_LIST_BY_ENTITY: // 45
                if (eventMsg.getData() instanceof ChatRoomEntity) {
                    if (ChatRoomSource.MAIN.equals(((ChatRoomEntity) eventMsg.getData()).getListClassify())) {
//                        ChatRoomEntity entity1 = (ChatRoomEntity) eventMsg.getData();
                        changeUnEditByRoomId((ChatRoomEntity) eventMsg.getData());
                    }
                }
                break;
            case MsgConstant.BUSINESS_BINDING_ROOM_EVENT:
                if (eventMsg.getData() instanceof Map) {
                    Map<String, String> data = (Map<String, String>) eventMsg.getData();
                    String bindRoomId = data.get("roomId");
                    for (ChatRoomEntity r : this.metadata) {
                        if (bindRoomId.equals(r.getId()) && ChatRoomType.discuss.equals(r.getType())) {
                            r.setBusinessId(data.get("businessId"));
                            r.setBusinessName(data.get("businessName"));
                            r.setBusinessCode(BusinessCode.of(data.get("businessCode")));
                            this.refreshData();
                            return;
                        }
                    }
                }
                break;
            case MsgConstant.CHANGE_MUTE_ROOM:
                ChatRoomEntity muteRoom = JsonHelper.getInstance().from(eventMsg.getString(), ChatRoomEntity.class);
                for (ChatRoomEntity r : this.metadata) {
                    if (r.equals(muteRoom)) {
                        r.setMute(muteRoom.isMute());
                        this.refreshData();
                        return;
                    }
                }
                break;
            case MsgConstant.CHANGE_TOP_ROOM:
                ChatRoomEntity topRoom = JsonHelper.getInstance().from(eventMsg.getString(), ChatRoomEntity.class);
                for (ChatRoomEntity r : this.metadata) {
                    if (r.equals(topRoom)) {
                        r.setTop(topRoom.isTop());
                        r.setTopTime(topRoom.getTopTime());
                        this.refreshData();
                        return;
                    }
                }
                break;
            case MsgConstant.DELETE_ROOM:
                ChatRoomEntity deleteRoom = JsonHelper.getInstance().from(eventMsg.getString(), ChatRoomEntity.class);
                this.metadata.remove(deleteRoom);
                this.refreshData();
                break;
            case MsgConstant.MESSAGE_SEND_FAIL:
                ChatRoomEntity failEntity = ChatRoomReference.getInstance().findById2(TokenPref.getInstance(getContext()).getUserId(), eventMsg.getData().toString(), true, true, true, true, true);
                if (ChatRoomSource.MAIN.equals(failEntity.getListClassify())) {
                    this.metadata.remove(failEntity);
                    this.metadata.add(failEntity);
                    this.refreshData();
                }
                break;
            case MsgConstant.NOTICE_APPEND_RECENT_MAIN_ROOMS:
                List<ChatRoomEntity> appends = JsonHelper.getInstance().fromToList(eventMsg.getString(), ChatRoomEntity[].class);
                this.metadata.removeAll(appends);
                this.metadata.addAll(appends);
                this.refreshData();
                break;
        }
    }
}
