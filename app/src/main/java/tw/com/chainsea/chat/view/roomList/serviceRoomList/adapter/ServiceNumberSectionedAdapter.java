//package tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.text.SpannableStringBuilder;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.common.base.Strings;
//import com.google.common.collect.ArrayListMultimap;
//import com.google.common.collect.ComparisonChain;
//import com.google.common.collect.ListMultimap;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.common.collect.Sets;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import tw.com.chainsea.android.common.event.OnHKClickListener;
//import tw.com.chainsea.chat.util.TextViewHelper;
//import tw.com.chainsea.ce.sdk.BuildConfig;
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
//import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus;
//import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType;
//import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
//import tw.com.chainsea.ce.sdk.database.DBManager;
//import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
//import tw.com.chainsea.ce.sdk.database.sp.UserPref;
//import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference;
//import tw.com.chainsea.ce.sdk.service.AvatarService;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.base.Constant;
//import tw.com.chainsea.chat.lib.AtMatcherHelper;
//import tw.com.chainsea.chat.service.ActivityTransitionsControl;
//import tw.com.chainsea.chat.util.Unit;
//import tw.com.chainsea.chat.util.TimeUtil;
//import tw.com.chainsea.chat.view.chatroom.adapter.listener.OnRoomItemClickListener;
//import tw.com.chainsea.chat.view.globalSearch.Sectioned;
//import tw.com.chainsea.chat.view.roomList.type.Menu;
//import tw.com.chainsea.chat.view.roomList.type.SectionedType;
//import tw.com.chainsea.custom.view.adapter.SectionedRecyclerViewAdapter;
//import tw.com.chainsea.custom.view.image.CircleImageView;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemNoSwipeViewHolder;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemSwipeWithActionWidthViewHolder;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension;
//import tw.com.chainsea.custom.view.recyclerview.lintener.OnSwipeMenuListener;
//
///**
// * current by evan on 2020-02-15
// */
//public class ServiceNumberSectionedAdapter extends SectionedRecyclerViewAdapter<ServiceNumberSectionedAdapter.HeaderHolder, ItemBaseViewHolder, RecyclerView.ViewHolder> {
//
//    List<Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity>> sections = Lists.newArrayList();
//    List<ChatRoomEntity> metadata = Lists.newArrayList();
//    boolean isComponent = false; // 是否組裝
//    OnRoomItemClickListener<ChatRoomEntity> onRoomItemClickListener;
//    OnSwipeMenuListener<ChatRoomEntity, Menu> onSwipeMenuListener;
//
//    String userId = TokenPref.getInstance(getContext()).getUserId();
//    Set<String> isOpenSet = Sets.newHashSet(SectionedType.UNREAD_NO_AGENT.name());
//    ItemTouchHelperExtension itemTouchHelperExtension;
//
//    private final int LIMIT_SUB_LENGTH = 3;
//
//    Set<String> expansionSet = Sets.newHashSet();
//    Set<String> isSubNodeSet = Sets.newHashSet();
//    private final Map<String, ChatRoomEntity> limitOpenDatas = Maps.newHashMap();
//    private ExpansionType expansionType;
//
//    Map<String, ServiceNumberEntity> serviceNumberData;
//
//    public enum ExpansionType {
//        ALL,
//        UNREAD,
//        BY_NAME
//    }
//    public ServiceNumberSectionedAdapter(){}
//    public ServiceNumberSectionedAdapter setExpansionType(ExpansionType type, Set<String> names) {
//        this.expansionType = type;
//        switch (type) {
//            case ALL:
//                this.isOpenSet = Sets.newHashSet(SectionedType.UNREAD_NO_AGENT.name(), SectionedType.MY_SERVICE.name(), SectionedType.OTHERS_SERVICE.name());
//                for (String name : names) {
//                    this.isOpenSet.add(name);
//                }
//                break;
//            case UNREAD:
//                this.isOpenSet = Sets.newHashSet(SectionedType.UNREAD_NO_AGENT.name());
//                break;
//            case BY_NAME:
//                for (String name : names) {
//                    this.isOpenSet.add(name);
//                }
//                break;
//        }
//        return this;
//    }
//
//    @Override
//    protected int getSectionCount() {
//        return this.sections.size();
//    }
//
//    @Override
//    protected int getItemCountForSection(int section) {
//        return this.sections.get(section).getSize();
//    }
//
//    @Override
//    protected boolean hasFooterInSection(int section) {
//        return false;
//    }
//
//    @Override
//    protected HeaderHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
//        return new HeaderHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_service_num_name, parent, false));
//    }
//
//    @Override
//    protected ItemBaseViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
//        return viewType == 1
//                ? new MultiItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_4, parent, false))
//                : new SingleItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_4, parent, false));
//    }
//
//    @Override
//    protected int getSectionItemViewType(int section, int position) {
////        return super.getSectionItemViewType(section, position);
////        CELog.e(String.format(" Sectioned view section->%s,  position->%s", section, position));
////        CELog.d(String.format(" Sectioned sections.size()->%s,  sections.getData().size()->%s", this.sections.size(), this.sections.get(section).getDatas().size()));
//        ChatRoomEntity entity = this.sections.get(section).getDatas().get(position);
//        switch (entity.getComponentType()) {
//            case MULTI:
//                return 1;
//            case SINGLE:
//            default:
//                return 0;
//        }
//    }
//
//    @Override
//    protected RecyclerView.ViewHolder onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
//        return null;
//    }
//
//    @Override
//    protected void onBindSectionHeaderViewHolder(HeaderHolder holder, int section) {
//        Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned = this.sections.get(section);
//
//        holder.onBind(sectioned, section, 0);
//        // 顯示未讀總數
//        // holder.titleView.setText(sectioned.getName() + "::::" + getUnreadBySectioned(sectioned));
//        holder.titleView.setText(sectioned.getName());
//        holder.clSectioned.setBackgroundColor(sectioned.isOpen() ? Color.WHITE : 0xFFF7F7F7);
//        holder.openView.setImageResource(sectioned.isOpen() ? R.drawable.ic_expand : R.drawable.ic_close);
//
//        if (sectioned.getBind() != null) {
//            holder.clSectionedControl.setVisibility(sectioned.isOpen() ? View.VISIBLE : View.GONE);
//        } else {
//            holder.clSectionedControl.setVisibility(View.GONE);
//        }
////        holder.clSectionedControl.setVisibility(sectioned.isOpen() ? View.VISIBLE : View.GONE);
//        holder.clSectioned.setOnClickListener(new OnHKClickListener() {
//            @Override
//            public void onClick(View v, Object o) {
//                sectioned.setSize(sectioned.isOpen() ? 0 : sectioned.getDatas().size());
//                sectioned.setOpen(!sectioned.isOpen());
//                if (sectioned.isOpen()) {
//                    isOpenSet.add(sectioned.getName());
//                } else {
//                    isOpenSet.remove(sectioned.getName());
//                }
////                refreshData();
//                notifyDataSetChanged();
//            }
//        });
//    }
//
//    @Override
//    protected void onBindItemViewHolder(ItemBaseViewHolder holder, int section, int position) {
//        ChatRoomEntity entity = this.sections.get(section).getDatas().get(position);
//        holder.onBind(entity, section, position);
//    }
//
//    @Override
//    protected void onBindSectionFooterViewHolder(RecyclerView.ViewHolder holder, int section) {
//
//    }
//
//    public ServiceNumberSectionedAdapter setData(List<ChatRoomEntity> list, boolean isComponent) {
//        for (ChatRoomEntity roomEntity : list) {
//            this.metadata.remove(roomEntity);
//            this.metadata.add(roomEntity);
//        }
//        this.isComponent = isComponent;
//        return this;
//    }
//
//    public ServiceNumberSectionedAdapter addData(ChatRoomEntity entity, boolean isComponent) {
//        if (this.metadata.contains(entity)) {
//            this.metadata.remove(entity);
//            this.metadata.add(entity);
//        }
//        this.isComponent = isComponent;
//        return this;
//    }
//
//    public ServiceNumberSectionedAdapter filter(List<ChatRoomEntity> metadata) {
//        return this;
//    }
//
//    public void sortSections(List<Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity>> sections) {
//        Collections.sort(sections, (Comparator<Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity>>) (o1, o2) -> {
//            return ComparisonChain.start()
//                    .compare(o1.getType().getIndex(), o2.getType().getIndex())
//                    .compare(o1.getType().name(), o2.getType().name())
//                    .result();
//        });
//    }
//
//    public void sectionedMessageByDate() {
//        Collections.sort(metadata);
//        ListMultimap<String, ChatRoomEntity> multimap = ArrayListMultimap.create();
//        for (ChatRoomEntity entity : metadata) {
//            multimap.put(entity.getServiceNumberId(), entity);
//        }
//
//        this.sections.clear();
//
//        // 沒人服務且未讀
//        List<ChatRoomEntity> noAgentUnreadEntities = Lists.newArrayList();
//
//        // 我服務中（含未讀）
//        List<ChatRoomEntity> myServiceEntities = Lists.newArrayList();
//
//        // 其它人服務中（含未讀）
//        List<ChatRoomEntity> othersServiceEntities = Lists.newArrayList();
//
//        // 提取出未讀聊天室
//        List<ChatRoomEntity> unreadEntities = Lists.newArrayList();
//
//        Set<String> serviceNumberSectioned = Sets.newHashSet();
//
//        for (Map.Entry<String, Collection<ChatRoomEntity>> entry : multimap.asMap().entrySet()) {
//            List<ChatRoomEntity> list = Lists.newArrayList(entry.getValue());
//            Collections.sort(list);
//
//            // 歸戶
//            List<ChatRoomEntity> entities = component(list);
//            // 提取我服務中（含未讀）
//            handleMyService(userId, entities, myServiceEntities);
//            // 提取其它人服務中（含未讀）
//            handleOthersService(userId, entities, othersServiceEntities);
//            // 提取沒人服務且未讀
//            handleNoAgentUnread(entities, noAgentUnreadEntities);
//            // 提取出未讀聊天室
//            handleUnreadNumber(entities, unreadEntities);
//            if (!entities.isEmpty()) {
//                String name = !Strings.isNullOrEmpty(entities.get(0).getServiceNumberName()) ? entities.get(0).getServiceNumberName() : "";
//                SectionedType sectionedType = SectionedType.OTHER;
//                sectionedType.setName(name);
//                Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned = Sectioned.<ChatRoomEntity, SectionedType, ServiceNumberEntity>Build()
//                        .type(sectionedType)
//                        .isOpen(isOpenSet.contains(name))
//                        .name(name)
//                        .size(isOpenSet.contains(name) ? entities.size() : 0)
//                        .content(name + " (" + entry.getValue().size() + ") ")
//                        .datas(Lists.newArrayList(Sets.newHashSet(entities)))
//                        .bind(this.serviceNumberData.get(entry.getKey()))
//                        .build();
//
//                this.sections.add(sectioned);
//                serviceNumberSectioned.add(name);
//            }
//        }
//
//        UserPref.getInstance(getContext()).setServiceNumberSectioned(serviceNumberSectioned);
//
//        if (!noAgentUnreadEntities.isEmpty()) {
//            Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned = Sectioned.<ChatRoomEntity, SectionedType, ServiceNumberEntity>Build()
//                    .type(SectionedType.UNREAD_NO_AGENT)
//                    .isOpen(isOpenSet.contains(SectionedType.UNREAD_NO_AGENT.name()))
//                    .name(SectionedType.UNREAD_NO_AGENT.name())
//                    .size(isOpenSet.contains(SectionedType.UNREAD_NO_AGENT.name()) ? noAgentUnreadEntities.size() : 0)
//                    .content(SectionedType.UNREAD_NO_AGENT.name() + " (" + noAgentUnreadEntities.size() + ") ")
//                    .datas(Lists.newArrayList(Sets.newHashSet(noAgentUnreadEntities)))
//                    .build();
//            this.sections.add(sectioned);
//        }
//
//        if (!myServiceEntities.isEmpty()) {
//            Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned = Sectioned.<ChatRoomEntity, SectionedType, ServiceNumberEntity>Build()
//                    .type(SectionedType.MY_SERVICE)
//                    .isOpen(isOpenSet.contains(SectionedType.MY_SERVICE.name()))
//                    .name(SectionedType.MY_SERVICE.name())
//                    .size(isOpenSet.contains(SectionedType.MY_SERVICE.name()) ? myServiceEntities.size() : 0)
//                    .content(SectionedType.MY_SERVICE.name() + " (" + myServiceEntities.size() + ") ")
//                    .datas(Lists.newArrayList(Sets.newHashSet(myServiceEntities)))
//                    .build();
//            this.sections.add(sectioned);
//        }
//
//        if (!othersServiceEntities.isEmpty()) {
//            Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned = Sectioned.<ChatRoomEntity, SectionedType, ServiceNumberEntity>Build()
//                    .type(SectionedType.OTHERS_SERVICE)
//                    .isOpen(isOpenSet.contains(SectionedType.OTHERS_SERVICE.name()))
//                    .name(SectionedType.OTHERS_SERVICE.name())
//                    .size(isOpenSet.contains(SectionedType.OTHERS_SERVICE.name()) ? othersServiceEntities.size() : 0)
//                    .content(SectionedType.OTHERS_SERVICE.name() + " (" + othersServiceEntities.size() + ") ")
//                    .datas(Lists.newArrayList(Sets.newHashSet(othersServiceEntities)))
//                    .build();
//            this.sections.add(sectioned);
//        }
//
//        if (!unreadEntities.isEmpty()) {
////            isOpenSet.add(SectionedType.UNREAD.name);
//            Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned = Sectioned.<ChatRoomEntity, SectionedType, ServiceNumberEntity>Build()
//                    .type(SectionedType.UNREAD_NO_AGENT)
//                    .isOpen(isOpenSet.contains(SectionedType.UNREAD_NO_AGENT.name()))
//                    .name(SectionedType.UNREAD_NO_AGENT.name())
//                    .size(isOpenSet.contains(SectionedType.UNREAD_NO_AGENT.name()) ? unreadEntities.size() : 0)
//                    .content(SectionedType.UNREAD_NO_AGENT.name() + " (" + unreadEntities.size() + ") ")
//                    .datas(Lists.newArrayList(Sets.newHashSet(unreadEntities)))
//                    .build();
//            this.sections.add(sectioned);
//        }
//        sortSections(this.sections);
//    }
//
//    /**
//     * 專業服務號，無人接線，有未讀
//     */
//    private void handleNoAgentUnread(List<ChatRoomEntity> entities, List<ChatRoomEntity> noAgentUnreadEntities) {
//        Iterator<ChatRoomEntity> iterator = entities.iterator();
//        while (iterator.hasNext()) {
//            ChatRoomEntity entity = iterator.next();
//            if (hasUnreadNumber(entity) && Strings.isNullOrEmpty(entity.getServiceNumberAgentId())) {
//                noAgentUnreadEntities.add(entity);
//                iterator.remove();
//            }
//        }
//    }
//
//    /**
//     * 專業服務號，接線是自己，不管有無未讀
//     */
//    private void handleMyService(String selfId, List<ChatRoomEntity> entities, List<ChatRoomEntity> myServiceEntities) {
//        Iterator<ChatRoomEntity> iterator = entities.iterator();
//        flag:
//        while (iterator.hasNext()) {
//            ChatRoomEntity entity = iterator.next();
//            if (entity.getComponentEntities() != null && !entity.getComponentEntities().isEmpty()) {
//                for (ChatRoomEntity sub : entity.getComponentEntities()) {
//                    if (!Strings.isNullOrEmpty(sub.getServiceNumberAgentId()) && selfId.equals(sub.getServiceNumberAgentId())) {
//                        // 專業號物件聊天室，type 變成一般服務號
//                        //if (!Strings.isNullOrEmpty(sub.getServiceNumberAgentId()) && ServiceNumberType.PROFESSIONAL.equals(sub.getServiceNumberType()) && selfId.equals(sub.getServiceNumberAgentId())) {
//                        myServiceEntities.add(entity);
//                        iterator.remove();
//                        continue flag;
//                    }
//                }
//            }
//            // selfId == AgentId            -->  selfId.equals(entity.getServiceNumberAgentId())
//            // isProfessional               -->  ServiceNumberType.PROFESSIONAL.equals(entity.getServiceNumberType())
//            // status is online or timeout  -->  ServiceNumberStatus.ON_LINE_or_TIME_OUT.equals(entity.getServiceNumberStatus())
//
//            if (selfId.equals(entity.getServiceNumberAgentId()) &&
//                    ServiceNumberType.PROFESSIONAL.equals(entity.getServiceNumberType()) &&
//                    ServiceNumberStatus.ON_LINE_or_TIME_OUT.equals(entity.getServiceNumberStatus())) {
//                myServiceEntities.add(entity);
//                iterator.remove();
//            }
//        }
//    }
//
//    /**
//     * 專業服務號，接線不是自己，不管有無未讀
//     */
//    private void handleOthersService(String selfId, List<ChatRoomEntity> entities, List<ChatRoomEntity> othersServiceEntities) {
//        Iterator<ChatRoomEntity> iterator = entities.iterator();
//        while (iterator.hasNext()) {
//            ChatRoomEntity entity = iterator.next();
//
//            // AgentId != null or ''        -->  !Strings.isNullOrEmpty(entity.getServiceNumberAgentId())
//            // status is online or timeout  -->  ServiceNumberStatus.ON_LINE_or_TIME_OUT.contains(entity.getServiceNumberStatus())
//            // unread number > 0            -->  entity.getUnReadNum() > 0
//
//            // or
//
//            // AgentId != null or ''        -->  !Strings.isNullOrEmpty(entity.getServiceNumberAgentId())
//            // status is online             -->  ServiceNumberStatus.ON_LINE.equals(entity.getServiceNumberStatus())
//            // unread number == 0           -->  entity.getUnReadNum() == 0
//
//            if (!Strings.isNullOrEmpty(entity.getServiceNumberAgentId()) &&
//                    (ServiceNumberStatus.ON_LINE_or_TIME_OUT.contains(entity.getServiceNumberStatus()) && entity.getUnReadNum() > 0) ||
//                    (ServiceNumberStatus.ON_LINE.equals(entity.getServiceNumberStatus()) && entity.getUnReadNum() == 0)
//            ) {
//                othersServiceEntities.add(entity);
//                iterator.remove();
//            }
//        }
//    }
//
//    /**
//     * 處理未讀聊天室包含被群組
//     */
//    private void handleUnreadNumber(List<ChatRoomEntity> entities, List<ChatRoomEntity> unreadEntities) {
//        Iterator<ChatRoomEntity> iterator = entities.iterator();
//        while (iterator.hasNext()) {
//            ChatRoomEntity entity = iterator.next();
//            if (hasUnreadNumber(entity)) {
//                unreadEntities.add(entity);
//                iterator.remove();
//            }
//        }
//    }
//
//    private boolean hasUnreadNumber(ChatRoomEntity entity) {
//        int unread = entity.getUnReadNum();
//        if (ComponentRoomType.MULTI.equals(entity.getComponentType())) {
//            for (ChatRoomEntity sub : entity.getComponentEntities()) {
//                unread += sub.getUnReadNum();
//            }
//        }
//        return unread > 0;
//    }
//
//    /**
//     * 組
//     */
//    public List<ChatRoomEntity> component(List<ChatRoomEntity> list) {
//        Iterator<ChatRoomEntity> iterator = list.iterator();
//        List<ChatRoomEntity> entities = Lists.newArrayList();
//        flat:
//        while (iterator.hasNext()) {
//            ChatRoomEntity room = iterator.next();
//            room.getComponentEntities().clear();
//            if (this.isComponent) {
//                for (ChatRoomEntity entity : entities) {
//                    if (entity.isServiceListComponent(room)) {
//                        entity.componentAdd(room);
//                        continue flat;
//                    }
//                }
//            }
//            entities.add(room);
//        }
//
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
//        boolean hasGeneral = false;
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
//
//            general.setComponentEntities(components);
//        } else {
//            if (!entity.getComponentEntities().isEmpty()) {
//                general = ChatRoomEntity.Build()
//                        .id(entity.getServiceNumberId() + entity.getOwnerId())
//                        .isHardCode(true)
//                        .avatarId(entity.getAvatarId())
//                        .serviceNumberAvatarId(entity.getServiceNumberAvatarId())
//                        .name(entity.getName())
//                        .memberIds(entity.getMemberIds())
//                        .members(entity.getMembers())
//                        .type(entity.getType())
//                        .serviceNumberId(entity.getServiceNumberId())
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
//
//            }
//        }
//
//        if (general == null) {
//            general = entity;
//        }
//
//        computeNodeEnd(general);
//        return general;
//    }
//
//    public synchronized void refreshData() {
//        filter(this.metadata);
//        this.expansionSet.clear();
//        this.limitOpenDatas.clear();
//        this.isSubNodeSet.clear();
//        this.sections.clear();
//        notifyDataSetChanged();
//        sectionedMessageByDate();
//        notifyDataSetChanged();
//    }
//
//    /**
//     * 計算節點結束位置
//     */
//    private void computeNodeEnd(ChatRoomEntity entity) {
//        entity.setSubEnd(false);
//        entity.setSubCenter(false);
//        entity.setSubTop(false);
//        entity.setSub(false);
//        Collections.sort(entity.getComponentEntities());
//        if (ComponentRoomType.MULTI.equals(entity.getComponentType()) && !entity.getComponentEntities().isEmpty()) {
//            Collections.sort(entity.getComponentEntities());
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
//    public ServiceNumberSectionedAdapter setOnRoomItemClickListener(OnRoomItemClickListener<ChatRoomEntity> onRoomItemClickListener) {
//        this.onRoomItemClickListener = onRoomItemClickListener;
//        return this;
//    }
//
//    public ServiceNumberSectionedAdapter setItemTouchHelperExtension(ItemTouchHelperExtension itemTouchHelperExtension) {
//        this.itemTouchHelperExtension = itemTouchHelperExtension;
//        return this;
//    }
//
//    public ServiceNumberSectionedAdapter setOnSwipeMenuListener(OnSwipeMenuListener<ChatRoomEntity, Menu> onSwipeMenuListener) {
//        this.onSwipeMenuListener = onSwipeMenuListener;
//        return this;
//    }
//
//    @Override
//    public void executeAnimatorEnd(int position) {
//
//    }
//
//    public interface OnItemClickListener<CR extends ChatRoomEntity> {
//        void onItemClick(CR cr);
//
//        void onComponentItemClick(CR cr);
//    }
//
//    private void openNodeTree(ChatRoomEntity entity, int section, int position, int itemSize) {
//        expansionSet.add(entity.serviceUniqueId());
//        isSubNodeSet.addAll(entity.getComponentIds());
////        sections.get(section).getDatas().addAll(position + 1, entity.getComponentEntities());
////        sections.get(section).setSize(sections.get(section).getSize() + entity.getComponentEntities().size());
//
//        for (int i = 0; i < entity.getComponentEntities().size(); i++) {
//            if (i < itemSize) {
//                ChatRoomEntity sub = entity.getComponentEntities().get(i);
//                sub.setAnimationEnable(true);
////                entities.add(position + 1 + i, sub);
//                sections.get(section).getDatas().add(position + 1 + i, sub);
//                sections.get(section).setSize(sections.get(section).getSize() + 1);
//
//            } else {
//                limitOpenDatas.put(entity.serviceUniqueId(), entity);
//            }
//        }
//    }
//
//    private void closeNodeTree(ChatRoomEntity entity, int section, int position) {
//        boolean limitStatus = limitOpenDatas.get(entity.serviceUniqueId()) != null;
//        expansionSet.remove(entity.serviceUniqueId());
//        limitOpenDatas.remove(entity.serviceUniqueId());
//        isSubNodeSet.removeAll(entity.getComponentIds());
//        sections.get(section).getDatas().removeAll(entity.getComponentEntities());
//        sections.get(section).setSize(sections.get(section).getSize() - (limitStatus ? 3 : entity.getComponentEntities().size()));
//        notifyDataSetChanged();
////        refreshData();
//    }
//
////    public class HeaderHolder extends ItemNoSwipeViewHolder<Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity>> implements SectionActionAdapter.OnActionListener<ActionBean, ServiceNumberEntity> {
////        @BindView(R.id.cl_sectioned)
////        ConstraintLayout clSectioned;
////        @BindView(R.id.tv_title)
////        TextView titleView;
////        @BindView(R.id.tv_open)
////        ImageView openView;
////
////        @BindView(R.id.cl_sectioned_control)
////        ConstraintLayout clSectionedControl;
////        SectionActionAdapter actionAdapter;
////        @BindView(R.id.rv_actions)
////        RecyclerView rvActions;
////
////        public HeaderHolder(View itemView) {
////            super(itemView);
////            ButterKnife.bind(this, itemView);
////            // EVAN_FLAG 12/3/20 1.15.0 Service Number Control Options
////            rvActions.setLayoutManager(new GridLayoutManager(getContext(), 5));
////            actionAdapter = new SectionActionAdapter();
////
////            rvActions.setAdapter(actionAdapter);
////        }
////
////        @Override
////        public void onBind(Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned, int section, int position) {
////            super.onBind(sectioned, section, position);
////            titleView.setText(sectioned.getName());
////            clSectioned.setBackgroundColor(sectioned.isOpen() ? Color.WHITE : 0xFFF7F7F7);
////            openView.setImageResource(sectioned.isOpen() ? R.drawable.ic_expand : R.drawable.ic_close);
////            if (sectioned.getBind() != null) {
////                if (sectioned.isOpen()) {
////                    clSectionedControl.setVisibility(View.VISIBLE);
////                    actionAdapter.bind(sectioned.getBind()).setOnActionListener(this).refresh();
////                } else {
////                    clSectionedControl.setVisibility(View.GONE);
////                    actionAdapter.bind(null).setOnActionListener(null).refresh();
////                }
////            } else {
////                actionAdapter.bind(null).setOnActionListener(null).refresh();
////                clSectionedControl.setVisibility(View.GONE);
////            }
//////        holder.clSectionedControl.setVisibility(sectioned.isOpen() ? View.VISIBLE : View.GONE);
////            clSectioned.setOnClickListener(new OnHKClickListener() {
////                @Override
////                public void onClick(View v, Object o) {
////                    sectioned.setSize(sectioned.isOpen() ? 0 : sectioned.getDatas().size());
////                    sectioned.setOpen(!sectioned.isOpen());
////                    if (sectioned.isOpen()) {
////                        isOpenSet.add(sectioned.getName());
////                    } else {
////                        isOpenSet.remove(sectioned.getName());
////                    }
//////                refreshData();
////                    notifyDataSetChanged();
////                }
////            });
////        }
////
////        @Override
////        public void onChatClick(ActionBean bean, ServiceNumberEntity entity) {
////
////        }
////
////        @Override
////        public void onWaitTransFerClick(ActionBean actionBean, ServiceNumberEntity serviceNumberEntity) {
////
////        }
////
////        @Override
////        public void onBroadcastClick(ActionBean bean, ServiceNumberEntity entity) {
////            ActivityTransitionsControl.navigateToServiceBroadcastEditor(getContext(), entity.getBroadcastRoomId(), entity.getName(), entity.getServiceNumberId(), (intent, s) -> getContext().startActivity(intent));
////        }
////
////        @Override
////        public void onWelcomeMessageClick(ActionBean bean, ServiceNumberEntity entity) {
////            if (entity.isManager() || entity.isOwner()) {
////                ActivityTransitionsControl.navigateToServiceNumberMoreSettings(getContext(), entity.getServiceNumberId(), (intent, s) -> getContext().startActivity(intent));
////            }
////        }
////
////        @Override
////        public void onMembersClick(ActionBean bean, ServiceNumberEntity entity) {
////            ActivityTransitionsControl.navigateToServiceAgentsManage(getContext(), entity.getBroadcastRoomId(), entity.getServiceNumberId(), (intent, s) -> getContext().startActivity(intent));
////        }
////
////        @Override
////        public void onHomePageClick(ActionBean bean, ServiceNumberEntity entity) {
////            ActivityTransitionsControl.navigateToServiceNumberManage(getContext(), entity.getRoomId(), entity.getServiceNumberId(), (intent, s) -> getContext().startActivity(intent));
////        }
////    }
//
//    class MultiItemViewHolder extends ItemNoSwipeViewHolder<ChatRoomEntity> {
//        @BindView(R.id.ll_left_menu)
//        View llLeftMenu;
//        @BindView(R.id.ll_right_menu)
//        View llRightMenu;
//        @BindView(R.id.cl_content_item)
//        ConstraintLayout clContentItem;
//
//        @BindView(R.id.cl_content_cell)
//        ConstraintLayout clContentCell;
//
//        @BindView(R.id.iv_top)
//        ImageView ivTop;
//        @BindView(R.id.iv_mute)
//        ImageView ivMute;
//        @BindView(R.id.iv_delete)
//        ImageView ivDelete;
//
//
//        @BindView(R.id.civ_icon)
//        CircleImageView civIcon;
////        @BindView(R.id.civ_small_icon)
////        CircleImageView civSmallIcon;
//
//        @BindView(R.id.tv_name)
//        TextView tvName;
//        @BindView(R.id.tv_time)
//        TextView tvTime;
//
//        //        @BindView(R.id.iv_status)
////        ImageView ivStatus;
//        @BindView(R.id.tv_business_content)
//        TextView tvBusinessContent;
//        @BindView(R.id.tv_content)
//        TextView tvContent;
//        @BindView(R.id.tv_unread)
//        TextView tvUnread;
//
//        @BindView(R.id.tv_sub_unread)
//        TextView tvSubNumber;
////        @BindView(R.id.iv_next)
////        ImageView ivNext;
//
//        @BindView(R.id.v_sub_node_top)
//        View vSubNodeTop;
//        @BindView(R.id.v_sub_node_left)
//        View vSubNodeLeft;
//        @BindView(R.id.v_sub_node_right)
//        View vSubNodeRight;
//        @BindView(R.id.v_sub_node_end)
//        View vSubNodeEnd;
//
//        @BindView(R.id.v_sub_node_divider)
//        View vSubNodeDivider;
//
////        @BindView(R.id.v_shadow)
////        View vShadow;
//
//        public MultiItemViewHolder(@NonNull View itemView) {
//            super(itemView);
//            ButterKnife.bind(this, itemView);
//            super.setMenuViews(llLeftMenu, llRightMenu);
//            super.setContentItemView(clContentItem);
//            tvContent.setVisibility(View.GONE);
//            tvTime.setVisibility(View.INVISIBLE);
//            ivTop.setVisibility(View.GONE);
//            ivDelete.setVisibility(View.GONE);
//            ivMute.setVisibility(View.GONE);
//            tvBusinessContent.setVisibility(View.GONE);
//            vSubNodeEnd.setVisibility(View.GONE);
////            vShadow.setVisibility(View.GONE);
//        }
//
//        @Override
//        public void onBind(ChatRoomEntity entity, int section, int position) {
//            tvName.setText(entity.getName() + "@" + entity.getServiceNumberName());
//
//            // EVAN_FLAG 2020-04-06 (1.10.0) 未讀數量
//            int unReadNumber = entity.getFeatureUnReadNumCount();
//            // EVAN_FLAG 2020-04-06 (1.11.0) 子節點未讀數量
//            // EVAN_FLAG 2020-04-08 (1.10.0) 是否為置頂
//            boolean isTop = entity.getFeatureTop();
//
//            clContentItem.setBackgroundResource(isTop ? R.drawable.selector_item_list_top : R.drawable.selector_item_list);
//            AvatarService.post(getContext(), entity.getAvatarId(), PicSize.SMALL, civIcon, R.drawable.default_avatar);
//
//            tvUnread.setVisibility(unReadNumber <= 0 ? View.INVISIBLE : View.VISIBLE);
//            tvUnread.setText(Unit.getUnReadNumber(unReadNumber));
//
//            if (!expansionSet.contains(entity.serviceUniqueId())) {
//                clContentCell.setBackgroundResource(entity.getComponentEntities().size() > 2 ? R.drawable.sectioned_room_3l : R.drawable.sectioned_room_2l);
//                vSubNodeTop.setVisibility(View.GONE);
//                vSubNodeLeft.setVisibility(View.GONE);
//                vSubNodeRight.setVisibility(View.GONE);
//                vSubNodeDivider.setVisibility(View.GONE);
//            } else {
//                clContentCell.setBackgroundResource(0);
//                vSubNodeTop.setVisibility(View.VISIBLE);
//                vSubNodeLeft.setVisibility(View.VISIBLE);
//                vSubNodeRight.setVisibility(View.VISIBLE);
//                vSubNodeDivider.setVisibility(View.VISIBLE);
//                tvUnread.setVisibility(View.GONE);
//            }
//
//            int size = entity.getComponentEntities().size();
//            if (size > 0) {
//                tvName.setText(tvName.getText().toString() + " (" + size + ")");
//            }
//
//            clContentItem.setOnClickListener(v -> {
//                if (itemTouchHelperExtension != null) {
//                    itemTouchHelperExtension.closeOpened();
//                }
//                if (expansionSet.contains(entity.serviceUniqueId())) {
////                    expansionSet.remove(entity.serviceUniqueId());
////                    isSubNodeSet.removeAll(entity.getComponentIds());
////                    sections.get(section).getDatas().removeAll(entity.getComponentEntities());
////                    sections.get(section).setSize(sections.get(section).getSize() - entity.getComponentEntities().size());
//                    closeNodeTree(entity, section, position);
//                } else {
//                    computeNodeEnd(entity);
////                    expansionSet.add(entity.serviceUniqueId());
////                    isSubNodeSet.addAll(entity.getComponentIds());
////                    sections.get(section).getDatas().addAll(position + 1, entity.getComponentEntities());
////                    sections.get(section).setSize(sections.get(section).getSize() + entity.getComponentEntities().size());
//                    int limit = LIMIT_SUB_LENGTH > 0 ? LIMIT_SUB_LENGTH : entity.getComponentEntities().size();
//                    openNodeTree(entity, section, position, limit);
//                }
//                notifyDataSetChanged();
////                refreshData();
//            });
//
//            // 開發模式
//            if (BuildConfig.DEBUG) {
//                tvName.setTextColor(Color.BLACK);
//                if (entity.isFavourite()) {
//                    tvName.setTextColor(Color.RED);
//                }
//
//                if (entity.isAtMe()) {
//                    tvName.setTextColor(Color.BLUE);
//                }
//
//                if (!Strings.isNullOrEmpty(entity.getBusinessId())) {
//                    tvName.setTextColor(Color.MAGENTA);
//                }
//            }
//        }
//    }
//
//    class SingleItemViewHolder extends ItemSwipeWithActionWidthViewHolder<ChatRoomEntity> {
//        @BindView(R.id.ll_left_menu)
//        View llLeftMenu;
//        @BindView(R.id.ll_right_menu)
//        View llRightMenu;
//        @BindView(R.id.cl_content_item)
//        ConstraintLayout clContentItem;
//
//        @BindView(R.id.cl_content_cell)
//        ConstraintLayout clContentCell;
//
//        @BindView(R.id.iv_top)
//        ImageView ivTop;
//        @BindView(R.id.iv_mute)
//        ImageView ivMute;
//        @BindView(R.id.iv_delete)
//        ImageView ivDelete;
//
//
//        @BindView(R.id.civ_icon)
//        CircleImageView civIcon;
//        @BindView(R.id.civ_small_icon)
//        CircleImageView civSmallIcon;
//
//        @BindView(R.id.tv_name)
//        TextView tvName;
//        @BindView(R.id.tv_time)
//        TextView tvTime;
//
//        //        @BindView(R.id.iv_status)
////        ImageView ivStatus;
//        @BindView(R.id.tv_business_content)
//        TextView tvBusinessContent;
//        @BindView(R.id.tv_content)
//        TextView tvContent;
//        @BindView(R.id.tv_unread)
//        TextView tvUnread;
//
//        @BindView(R.id.tv_sub_unread)
//        TextView tvSubNumber;
////        @BindView(R.id.iv_next)
////        ImageView ivNext;
//
//        @BindView(R.id.v_sub_node_top)
//        View vSubNodeTop;
//        @BindView(R.id.v_sub_node_left)
//        View vSubNodeLeft;
//        @BindView(R.id.v_sub_node_right)
//        View vSubNodeRight;
//        @BindView(R.id.v_sub_node_end)
//        View vSubNodeEnd;
//
//        @BindView(R.id.iv_sub_node_center)
//        ImageView ivSubNodeCenter;
//        @BindView(R.id.v_sub_node_divider)
//        View vSubNodeDivider;
//
////        @BindView(R.id.v_shadow)
////        View vShadow;
//
//
//        public SingleItemViewHolder(@NonNull View itemView) {
//            super(itemView);
//            ButterKnife.bind(this, itemView);
//            super.setMenuViews(llLeftMenu, llRightMenu);
//            super.setContentItemView(clContentItem);
//            ivDelete.setVisibility(View.GONE);
////            vShadow.setVisibility(View.GONE);
//        }
//
//        @Override
//        public void onBind(ChatRoomEntity entity, int section, int position) {
//            if (entity != null && entity.getServiceNumberType() != null) {
//                switch (entity.getServiceNumberType()) {
//                    case BOSS:
//                        tvName.setText(TextViewHelper.setLeftImage(getContext(), entity.getName() + "@" + entity.getServiceNumberName(), R.drawable.ic_icon_boss_15dp));
//                        break;
//                    case PROFESSIONAL:
//                        tvName.setText(TextViewHelper.setLeftImage(getContext(), entity.getName() + "@" + entity.getServiceNumberName(), R.drawable.icon_service_number_blue_15dp));
//                        break;
//                    case NONE:
//                    case NORMAL:
//                    default:
//                        tvName.setText(TextViewHelper.setLeftImage(getContext(), entity.getName() + "@" + entity.getServiceNumberName(), R.drawable.icon_subscribe_number_pink_15dp));
//                }
//            } else {
//                tvName.setText(TextViewHelper.setLeftImage(getContext(), entity.getName() + "@" + entity.getServiceNumberName(), R.drawable.icon_subscribe_number_pink_15dp));
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
//            clContentItem.setBackgroundResource(isTop ? R.drawable.selector_item_list_top : R.drawable.selector_item_list);
//            ivTop.setImageResource(isTop ? R.drawable.ic_no_top : R.drawable.ic_top);
//            ivMute.setImageResource(isMute ? R.drawable.amplification : R.drawable.not_remind);
//            AvatarService.post(getContext(), entity.getAvatarId(), PicSize.SMALL, civIcon, R.drawable.default_avatar);
//
//            tvTime.setVisibility(View.INVISIBLE);
//            civSmallIcon.setVisibility(View.GONE);
//            tvContent.setVisibility(View.INVISIBLE);
//            tvUnread.setVisibility(View.GONE);
//
//
//            if (lastMessageEntity != null) {
//                tvTime.setVisibility(View.VISIBLE);
//                if (lastMessageEntity.getSendTime() <= 0) {
//                    tvTime.setVisibility(View.INVISIBLE);
//                } else {
//                    tvTime.setText(TimeUtil.getTimeShowString(lastMessageEntity.getSendTime(), true));
//                }
//
//                // Channel data processing
//                ChannelType channel = lastMessageEntity.getFrom();
//                if (channel == null) {
//                    civSmallIcon.setVisibility(View.GONE);
//                } else {
//                    civSmallIcon.setVisibility(View.VISIBLE);
//                    switch (channel) {
//                        case FB:
//                            civSmallIcon.setImageResource(R.drawable.facebook_icon);
//                            break;
//                        case LINE:
//                            civSmallIcon.setImageResource(R.drawable.line_icon);
//                            break;
//                        case QBI:
//                        case AILE_WEB_CHAT:
//                            civSmallIcon.setImageResource(R.drawable.qbi_icon);
//                            break;
//                        case WEICHAT:
//                            civSmallIcon.setImageResource(R.drawable.wechat_icon);
//                            break;
//                        case CE:
//                        case UNDEF:
//                        default:
//                            civSmallIcon.setVisibility(View.GONE);
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
//                    tvContent.setText(TextViewHelper.setLeftImage(getContext(), builder, R.drawable.ic_mes_failure_14dp));
//                } else if (!Strings.isNullOrEmpty(bean.getText())) { // EVAN_FLAG 2020-02-12 (1.9.1) 如果有未編輯完成
//                    tvContent.setText(TextViewHelper.setLeftImage(getContext(), bean.getText(), R.drawable.ic_edit_gray_14dp));
////                tvContent.setText(TextViewHelper.setLeftImageAndHighLight(getContext(),bean.getText(), R.drawable.ic_edit_gray_14dp,bean.getText(), InputLogType.AT.equals(bean.getTodoOverviewType())? 0xFF4A90E2 : 0xFF8F8E94));
//                } else if (entity.getLastMessage() != null) { // EVAN_FLAG 2020-02-12 (1.9.1) 如果有最新一句 信息實體
//                    tvContent.setVisibility(View.VISIBLE);
//                    tvTime.setVisibility(View.VISIBLE);
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
//                        tvContent.setText(sendName + Constant.RETRACT_MSG);
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
//                        tvContent.setText(builder);
//                    }
//                } else {
//                    tvContent.setText("");
//                }
//            } else {
//                tvContent.setText("");
//            }
//            tvUnread.setText(Unit.getUnReadNumber(unReadNumber));
//            tvUnread.setVisibility(unReadNumber <= 0 ? View.INVISIBLE : View.VISIBLE);
//
//            // EVAN_FLAG 2020-05-22 (1.11.0) 是否為展開最後一筆
//            vSubNodeEnd.setVisibility(entity.isSubEnd() ? View.VISIBLE : View.GONE);
//            vSubNodeLeft.setVisibility(isSubNodeSet.contains(entity.getId()) ? View.VISIBLE : View.GONE);
//            vSubNodeRight.setVisibility(isSubNodeSet.contains(entity.getId()) ? View.VISIBLE : View.GONE);
//            vSubNodeDivider.setVisibility(isSubNodeSet.contains(entity.getId()) && ChatRoomType.SERVICES.equals(entity.getType()) ? View.VISIBLE : View.GONE);
//            // EVAN_FLAG 2020-08-04 (1.12.0) 是否完整展開
//            ivSubNodeCenter.setVisibility(entity.isSubCenter() && limitOpenDatas.get(entity.serviceUniqueId()) != null ? View.VISIBLE : View.GONE);
//
//            tvBusinessContent.setVisibility(!Strings.isNullOrEmpty(businessName) ? View.VISIBLE : View.GONE);
//            tvBusinessContent.setText(!Strings.isNullOrEmpty(businessName) ? TextViewHelper.setLeftImage(getContext(), businessName, R.drawable.ic_icon_link_gary_14dp) : "");
//
//            ivTop.setOnClickListener(view -> {
//                if (itemTouchHelperExtension != null) {
//                    itemTouchHelperExtension.closeOpened();
//                }
//                if (onSwipeMenuListener != null) {
//                    onSwipeMenuListener.onSwipeMenuClick(entity, Menu.TOP, position);
//                }
//            });
//
//            ivMute.setOnClickListener(v -> {
//                if (itemTouchHelperExtension != null) {
//                    itemTouchHelperExtension.closeOpened();
//                }
//                if (onSwipeMenuListener != null) {
//                    onSwipeMenuListener.onSwipeMenuClick(entity, Menu.MUTE, position);
//                }
//            });
//
//            clContentItem.setOnClickListener(v -> {
//                if (itemTouchHelperExtension != null) {
//                    itemTouchHelperExtension.closeOpened();
//                }
//                if (onRoomItemClickListener != null) {
//                    onRoomItemClickListener.onItemClick(entity);
//                }
//            });
//
//            ivSubNodeCenter.setOnClickListener(v -> {
//                if (itemTouchHelperExtension != null) {
//                    itemTouchHelperExtension.closeOpened();
//                }
//
//                if (limitOpenDatas.get(entity.serviceUniqueId()) != null) {
//                    ChatRoomEntity parent = limitOpenDatas.get(entity.serviceUniqueId());
//                    closeNodeTree(parent, section, 0);
////                    int index = sections.size() + sections.get(section).getDatas().indexOf(parent);
//                    openNodeTree(parent, section, sections.get(section).getDatas().indexOf(parent), parent.getComponentEntities().size());
//                    limitOpenDatas.remove(entity.serviceUniqueId());
//                }
//                notifyDataSetChanged();
////                refreshData();
//            });
//
//            // 開發模式
//            if (BuildConfig.DEBUG) {
//                tvName.setTextColor(Color.BLACK);
//                if (entity.isFavourite()) {
//                    tvName.setTextColor(Color.RED);
//                }
//
//                if (entity.isAtMe()) {
//                    tvName.setTextColor(Color.BLUE);
//                }
//
//                if (!Strings.isNullOrEmpty(entity.getBusinessId())) {
//                    tvName.setTextColor(Color.MAGENTA);
//                }
//            }
//
//        }
//    }
//}
