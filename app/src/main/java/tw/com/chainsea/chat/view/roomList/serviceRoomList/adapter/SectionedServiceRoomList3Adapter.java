//package tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.ViewGroup;
//
//import androidx.databinding.DataBindingUtil;
//
//import com.google.common.base.Strings;
//import com.google.common.collect.ArrayListMultimap;
//import com.google.common.collect.ComparisonChain;
//import com.google.common.collect.HashMultimap;
//import com.google.common.collect.Iterables;
//import com.google.common.collect.ListMultimap;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.common.collect.SetMultimap;
//import com.google.common.collect.Sets;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import tw.com.aile.sdk.bean.message.MessageEntity;
//import tw.com.chainsea.android.common.json.JsonHelper;
//import tw.com.chainsea.android.common.log.CELog;
//import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
//import tw.com.chainsea.ce.sdk.bean.ServiceNum;
//import tw.com.chainsea.ce.sdk.bean.UpdateAvatarBean;
//import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
//import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType;
//import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
//import tw.com.chainsea.ce.sdk.database.DBManager;
//import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
//import tw.com.chainsea.ce.sdk.event.EventMsg;
//import tw.com.chainsea.ce.sdk.event.MsgConstant;
//import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
//import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
//import tw.com.chainsea.ce.sdk.http.ce.model.Member;
//import tw.com.chainsea.ce.sdk.http.ce.request.ServiceNumberChatroomAgentServicedRequest;
//import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
//import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference;
//import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.databinding.ItemBaseRoom6Binding;
//import tw.com.chainsea.chat.databinding.ItemIncludeTableRoom6Binding;
//import tw.com.chainsea.chat.databinding.ItemSectionedFooterBinding;
//import tw.com.chainsea.chat.databinding.ItemServiceNumNameBinding;
//import tw.com.chainsea.chat.lib.ChatService;
//import tw.com.chainsea.chat.view.globalSearch.Sectioned;
//import tw.com.chainsea.chat.view.roomList.type.NodeCategory;
//import tw.com.chainsea.chat.view.roomList.type.SectionedType;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
//
///**
// * Create by evan on 1/5/21
// *
// * @author Evan Wang
// * date 1/5/21
// */
//public class SectionedServiceRoomList3Adapter extends BaseServiceRoomList3Adapter<ChatRoomEntity, ServiceNumberEntity> {
//    protected List<Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity>> tmpSections = Lists.newArrayList();
//    private Map<String, ServiceNumberEntity> serviceNumberData;
//    private final Map<String, ChatRoomEntity> serviceMemberRoomData;
//
//    public AdapterCallback adapterCallback = null;
//
//    public void setAdapterCallback(AdapterCallback adapterCallback) {
//        this.adapterCallback = adapterCallback;
//    }
//
//    public SectionedServiceRoomList3Adapter(Context context) {
//        super(context);
//        this.serviceNumberData = ServiceNumberReference.findAllServiceNumberData(null);
//        this.serviceMemberRoomData = Maps.newHashMap();
//    }
//
//    @Override
//    protected ItemBaseViewHolder<Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity>> onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
//        ItemServiceNumNameBinding serviceNumNameBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_service_num_name, parent, false);
//        return new ServiceSectionedViewHolder(serviceNumNameBinding);
//    }
//
//    @Override
//    protected void onBindSectionHeaderViewHolder(ItemBaseViewHolder<Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity>> holder, int section) {
//        Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned = this.sections.get(section);
//        holder.onBind(sectioned, section, 0);
//    }
//
//    protected ItemBaseViewHolder<ChatRoomEntity> onCreateItemViewHolder(ViewGroup parent, int viewType) {
//        switch (NodeCategory.of(viewType)) {
//            case GENERAL_PARENT_NODE:
//                ItemIncludeTableRoom6Binding includeTableRoom6Binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_include_table_room_6, parent, false);
//                return new ServiceParentChatRoomViewHolder(includeTableRoom6Binding);
//            case GENERAL_NODE:
//            case CHILD_NODE:
//            default:
//                ItemBaseRoom6Binding baseRoom6Binding  = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_base_room_6, parent, false);
//                return new ServiceGeneralChatRoomViewHolder(baseRoom6Binding);
//        }
//    }
//
//    @Override
//    protected void onBindItemViewHolder(ItemBaseViewHolder<ChatRoomEntity> holder, int section, int position) {
//        holder.onBind(sections.get(section).getDatas().get(position), section, position);
//    }
//
//    @Override
//    protected boolean hasFooterInSection(int section) {
//        return true;
//    }
//
//    @Override
//    protected ItemBaseViewHolder<Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity>> onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
//        ItemSectionedFooterBinding binding = ItemSectionedFooterBinding.inflate(LayoutInflater.from(getContext()), parent, false);
//        return new ServiceSectionedFooterViewHolder(binding);
//    }
//
//    @Override
//    protected void onBindSectionFooterViewHolder(ItemBaseViewHolder<Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity>> holder, int section) {
//        Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned = this.sections.get(section);
//        holder.onBind(sectioned, section, 0);
//    }
//
//    @Override
//    protected int getType(ChatRoomEntity t) {
//        if (t.isHardCode()) {
//            return NodeCategory.GENERAL_PARENT_NODE.getViewType();
//        }
//        if (t.isSub()) {
//            return NodeCategory.CHILD_NODE.getViewType();
//        }
//        return NodeCategory.GENERAL_NODE.getViewType();
//    }
//
//    @Override
//    protected void sort() {
//        if (SortStyle.TIME.equals(this.sortStyle)) {
//            Collections.sort(metadata, (o1, o2) -> ComparisonChain.start()
//                    .compare(o2.getTimeWeights(), o1.getTimeWeights())
//                    .result());
//        }else {
//            Collections.sort(metadata);
//        }
//    }
//
//    @Override
//    protected void sortSectioned() {
//        this.sections.clear();
//        //put AI Servicing sectioned on the top 1
//        //and Monitor AI on the top 2
//        for(Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned: tmpSections) {
//            if(sectioned.getName().contains(SectionedType.ROBOT_SERVICE.getName())){
//                sections.add(sectioned);
//                tmpSections.remove(sectioned);
//                break;
//            }
//        }
//
//        for(Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned: tmpSections) {
//            if(sectioned.getName().contains(SectionedType.MONITOR_AI_SERVICE.getName())) {
//                sections.add(sectioned);
//                tmpSections.remove(sectioned);
//                break;
//            }
//        }
//
//        Collections.sort(this.tmpSections, (o1, o2) -> ComparisonChain.start()
//                .compare(o1.getType().getIndex(), o2.getType().getIndex())
//                .compare(o1.getName(), o2.getName())
//                .result());
//
//        this.sections.addAll(this.tmpSections);
//    }
//
//    @Override
//    protected void filter() {
//        Iterator<ChatRoomEntity> iterator = this.metadata.iterator();
//        while (iterator.hasNext()) {
//            ChatRoomEntity t = iterator.next();
//            if (t.getType() != null) {
//                if (ChatRoomType.broadcast.equals(t.getType()) ||
//                        !ChatRoomSource.SERVICE.equals(t.getListClassify())) {
//                    iterator.remove();
//                }
//            }
//        }
//    }
//
//    /**
//     * // My Service Logic
//     * // selfId == AgentId            -->  selfId.equals(entity.getServiceNumberAgentId())
//     * // isProfessional               -->  ServiceNumberType.PROFESSIONAL.equals(entity.getServiceNumberType())
//     * // status is online or timeout  -->  ServiceNumberStatus.ON_LINE_or_TIME_OUT.equals(entity.getServiceNumberStatus())
//     *
//     * // Others Service Logic
//     * // AgentId != null or ''        -->  !Strings.isNullOrEmpty(entity.getServiceNumberAgentId())
//     * // status is online or timeout  -->  ServiceNumberStatus.ON_LINE_or_TIME_OUT.contains(entity.getServiceNumberStatus())
//     * // unread number > 0            -->  entity.getUnReadNum() > 0
//     * // or
//     * // AgentId != null or ''        -->  !Strings.isNullOrEmpty(entity.getServiceNumberAgentId())
//     * // status is online             -->  ServiceNumberStatus.ON_LINE.equals(entity.getServiceNumberStatus())
//     * // unread number == 0           -->  entity.getUnReadNum() == 0
//     *
//     * // No Agent Service Logic
//     * // AgentId == null or ''        -->  Strings.isNullOrEmpty(entity.getServiceNumberAgentId())
//     * // status is online or timeout  -->  ServiceNumberStatus.ON_LINE_or_TIME_OUT.contains(entity.getServiceNumberStatus())
//     * // unread number >= 0
//     */
//    @Override
//    protected void pullAway() {
//        isSectionedOpenSet.remove("TIME");
//        this.tmpSections.clear();
//        this.serviceMemberRoomData.clear();
//        this.serviceNumberData.clear();
//        this.serviceNumberData = ServiceNumberReference.findAllServiceNumberData(null);
//        // 所有聊天室尚未分類
//        List<ChatRoomEntity> list = Lists.newArrayList(this.metadata);
//        // AI服務
//        List<ChatRoomEntity> robotServiceEntities = Lists.newArrayList();
//        // 監控AI
//        List<ChatRoomEntity> monitorRobotServiceEntities = Lists.newArrayList();
//        // 我的服務中
//        List<ChatRoomEntity> myServiceEntities = Lists.newArrayList();
//        // 服務中
//        List<ChatRoomEntity> othersServiceEntities = Lists.newArrayList();
//        // 剛進件
//        List<ChatRoomEntity> unreadEntities = Lists.newArrayList();
//        // 已經組裝的服務號Id
//        Set<String> hasAssembledServiceNumberIdSet = Sets.newHashSet();
//
//        String selfId = getUserId();
//        ListMultimap<String, ChatRoomEntity> multimap = ArrayListMultimap.create();
//        Iterator<ChatRoomEntity> iterator = list.iterator();
//        while (iterator.hasNext()) {
//            ChatRoomEntity t = iterator.next();
//            if (ChatRoomType.serviceMember.equals(t.getType())
//                    && !SortStyle.TIME.equals(this.sortStyle) //時間排序要顯示
//            ) {
//                this.serviceMemberRoomData.put(t.getServiceNumberId(), t);
//            }
//
//            // 修正如果從普通成員/管理員轉移成商務號擁有者 列表上還會有該服務號聊天室殘留
//            if (t.getServiceNumberType() == ServiceNumberType.BOSS) {
//                if (t.getServiceNumberId().equals(getBossServiceNumberId())) {
//                    iterator.remove();
//                    continue;
//                }
//            }
//
//
//            //AI servicing
//            if (successivelyHandleRobotServicing(t, robotServiceEntities)) {
//                iterator.remove();
//                continue;
//            }
//
//            //Monitor AI
//            if(successivelyHandleMonitorAIServicing(t, monitorRobotServiceEntities)) {
//                iterator.remove();
//                continue;
//            }
//
//            // 剛進件
//            if (successivelyHandleUnread(t, unreadEntities)) {
//                iterator.remove();
//                continue;
//            }
//
//            // 我服務的 條件一
//            if (successivelyHandleMyService(selfId, t, myServiceEntities)) {
//                iterator.remove();
//                continue;
//            }
//
//            // 服務中
//            if (successivelyHandleOthersService(selfId, t, othersServiceEntities)) {
//                iterator.remove();
//                continue;
//            }
//
//            // 剩餘資料存放
//            multimap.put(t.getServiceNumberId(), t);
//        }
//
//        // 處理AI服務邏輯
//        if(!robotServiceEntities.isEmpty()){
//            SectionedType.ROBOT_SERVICE.setName(getContext().getString(SectionedType.ROBOT_SERVICE.getResId()));
//            Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned = Sectioned.<ChatRoomEntity, SectionedType, ServiceNumberEntity>Build()
//                    .type(SectionedType.ROBOT_SERVICE)
//                    .isOpen(isSectionedOpenSet.contains(SectionedType.ROBOT_SERVICE.name()))
//                    .name(SectionedType.ROBOT_SERVICE.getName() + getContext().getString(R.string.text_actioned_num, robotServiceEntities.size()))
//                    .datas(robotServiceEntities)
//                    .build();
//            this.tmpSections.add(sectioned);
//        }
//
//        // 處理監控AI邏輯
//        if(!monitorRobotServiceEntities.isEmpty()){
//            SectionedType.MONITOR_AI_SERVICE.setName(getContext().getString(SectionedType.MONITOR_AI_SERVICE.getResId()));
//            Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned = Sectioned.<ChatRoomEntity, SectionedType, ServiceNumberEntity>Build()
//                    .type(SectionedType.MONITOR_AI_SERVICE)
//                    .isOpen(isSectionedOpenSet.contains(SectionedType.MONITOR_AI_SERVICE.name()))
//                    .name(SectionedType.MONITOR_AI_SERVICE.getName() + getContext().getString(R.string.text_actioned_num, monitorRobotServiceEntities.size()))
//                    .datas(monitorRobotServiceEntities)
//                    .build();
//            this.tmpSections.add(sectioned);
//        }
//
//        // 處理剛進件組合
//        if (!unreadEntities.isEmpty()) {
//            SectionedType.UNREAD_NO_AGENT.setName(getContext().getString(SectionedType.UNREAD_NO_AGENT.getResId()));
//            Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned = Sectioned.<ChatRoomEntity, SectionedType, ServiceNumberEntity>Build()
//                    .type(SectionedType.UNREAD_NO_AGENT)
//                    .isOpen(isSectionedOpenSet.contains(SectionedType.UNREAD_NO_AGENT.name()))
//                    .name(SectionedType.UNREAD_NO_AGENT.getName() + getContext().getString(R.string.text_actioned_num, unreadEntities.size()))
//                    .datas(unreadEntities)
//                    .build();
//            this.tmpSections.add(sectioned);
//        }
//
//        // business data rel table clear
//        // 處理我服務的組合
//        if (!myServiceEntities.isEmpty()) {
//            SectionedType.MY_SERVICE.setName(getContext().getString(SectionedType.MY_SERVICE.getResId()));
//            Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned = Sectioned.<ChatRoomEntity, SectionedType, ServiceNumberEntity>Build()
//                    .type(SectionedType.MY_SERVICE)
//                    .isOpen(isSectionedOpenSet.contains(SectionedType.MY_SERVICE.name()))
//                    .name(SectionedType.MY_SERVICE.getName() + getContext().getString(R.string.text_actioned_num, myServiceEntities.size()))
//                    .datas(myServiceEntities)
//                    .build();
//            this.tmpSections.add(sectioned);
//        }
//
//        // 處理服務中組合
//        if (!othersServiceEntities.isEmpty()) {
//            SectionedType.OTHERS_SERVICE.setName(getContext().getString(SectionedType.OTHERS_SERVICE.getResId()));
//            Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned = Sectioned.<ChatRoomEntity, SectionedType, ServiceNumberEntity>Build()
//                    .type(SectionedType.OTHERS_SERVICE)
//                    .isOpen(isSectionedOpenSet.contains(SectionedType.OTHERS_SERVICE.name()))
//                    .name(SectionedType.OTHERS_SERVICE.getName() + getContext().getString(R.string.text_actioned_num, othersServiceEntities.size()))
//                    .datas(othersServiceEntities)
//                    .build();
//            this.tmpSections.add(sectioned);
//        }
//
//        if (SortStyle.TIME.equals(this.sortStyle)) {
//            List<ChatRoomEntity> remainders = Lists.newArrayList(multimap.values());
//            isSectionedOpenSet.add(getContext().getString(R.string.repairs_issue_options_text_other));
//            String serviceNumberId = !Strings.isNullOrEmpty(remainders.get(0).getServiceNumberId()) ? remainders.get(0).getServiceNumberId() : "";
//            SectionedType type = SectionedType.OTHER;
//            type.setName(getContext().getString(R.string.repairs_issue_options_text_other));
//            Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned = Sectioned.<ChatRoomEntity, SectionedType, ServiceNumberEntity>Build()
//                    .type(type)
//                    .isOpen(true)
//                    .name(type.getName())
//                    .size(remainders.size())
//                    .datas(remainders)
//                    .serviceNumberId(serviceNumberId)
//                    .build();
//            this.tmpSections.add(sectioned);
//            return;
//        }
//
//        // 處理剩餘組合
//        for (Map.Entry<String, Collection<ChatRoomEntity>> entry : multimap.asMap().entrySet()) {
//            hasAssembledServiceNumberIdSet.add(entry.getKey());
//            List<ChatRoomEntity> sublist = Lists.newArrayList(entry.getValue());
//            Collections.sort(sublist);
//            String name = "";
//            String serviceNumberId = "";
//            ServiceNum entity = DBManager.getInstance().queryServiceNumberById(entry.getKey());
//            if (entity != null) {
//                name = entity.getName();
//                serviceNumberId = entity.getServiceNumberId();
//            }else{
//                name = !Strings.isNullOrEmpty(sublist.get(0).getServiceNumberName()) ? sublist.get(0).getServiceNumberName() : "";
//                serviceNumberId = !Strings.isNullOrEmpty(sublist.get(0).getServiceNumberId()) ? sublist.get(0).getServiceNumberId() : "";
//            }
//            if(!serviceNumberId.equals(getBossServiceNumberId())){ //服務號列表不需顯示商務號擁有者自己的服務號
//                boolean isServiceNumberMember = false;
//                if(entity != null && entity.getMemberItems() != null) {
//                    for(Member member: entity.getMemberItems()){ //檢查自己是不是服務號成員
//                        String memberId = member.getId();
//                        if (memberId != null && getUserId().equals(memberId)) {
//                            isServiceNumberMember = true;
//                            break;
//                        }
//                    }
//                    if(isServiceNumberMember) {
//                        SectionedType sectionedType = SectionedType.OTHER;
//                        sectionedType.setName(name);
//                        Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned = Sectioned.<ChatRoomEntity, SectionedType, ServiceNumberEntity>Build()
//                                .type(sectionedType)
//                                .isOpen(isSectionedOpenSet.contains(name))
//                                .name(name)
//                                .hasFooter(true)
//                                .datas(sublist)
//                                .bind(serviceNumberData.get(entry.getKey()))
//                                .serviceNumberId(serviceNumberId)
//                                .build();
//                        this.tmpSections.add(sectioned);
//                    }
//                }
//            }
//        }
//
//        // 補齊未被組裝的服務號，為了看板，[ 成員聊天室，廣播聊天室 ]
//        Map<String, ServiceNumberEntity> map = ServiceNumberReference.findServiceNumberListData();
//        for (Map.Entry<String, ServiceNumberEntity> entry : map.entrySet()) {
//            if (!hasAssembledServiceNumberIdSet.contains(entry.getKey()) ) {
//                ServiceNumberEntity entity = entry.getValue();
//                SectionedType sectionedType = SectionedType.OTHER;
//                sectionedType.setName(entity.getName());
//                Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned = Sectioned.<ChatRoomEntity, SectionedType, ServiceNumberEntity>Build()
//                        .type(sectionedType)
//                        .isOpen(isSectionedOpenSet.contains(entity.getName()))
//                        .name(entity.getName())
//                        .datas(Lists.newArrayList())
//                        .bind(map.get(entry.getKey()))
//                        .serviceNumberId(entity.getServiceNumberId())
//                        .build();
//                this.tmpSections.add(sectioned);
//            }
//        }
//    }
//
//    @Override
//    protected void removeItem(String id) {
//        for(ChatRoomEntity item: metadata) {
//            if(item.getId().equals(id)) {
//                metadata.remove(item);
//                break;
//            }
//        }
//    }
//    @Override
//    protected void group() {
//        this.businessRelData.clear();
//        SetMultimap<String, ChatRoomEntity> myServiceRelData = HashMultimap.create();
//        SetMultimap<String, ChatRoomEntity> unreadRelData = HashMultimap.create();
//        SetMultimap<String, ChatRoomEntity> othersServiceRelData = HashMultimap.create();
//        SetMultimap<String, ChatRoomEntity> othersRelData = HashMultimap.create();
//        SetMultimap<String, ChatRoomEntity> robotServicingRelData = HashMultimap.create();
//        SetMultimap<String, ChatRoomEntity> monitorAIServicingRelData = HashMultimap.create();
//        for (Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned : this.tmpSections) {
//            SectionedType type = sectioned.getType();
//            switch (type) {
//                case MY_SERVICE:
//                    assembleBusinessRelTableByType(sectioned.getType(), sectioned.getDatas(), myServiceRelData);
//                    assembleSectioned(sectioned, myServiceRelData);
//                    break;
//                case OTHERS_SERVICE:
//                    assembleBusinessRelTableByType(sectioned.getType(), sectioned.getDatas(), othersServiceRelData);
//                    assembleSectioned(sectioned, othersServiceRelData);
//                    break;
//                case UNREAD_NO_AGENT:
//                    assembleBusinessRelTableByType(sectioned.getType(), sectioned.getDatas(), unreadRelData);
//                    assembleSectioned(sectioned, unreadRelData);
//                    break;
//                case OTHER:
//                    assembleBusinessRelTableByType(sectioned.getType(), sectioned.getDatas(), othersRelData);
//                    assembleSectioned(sectioned, othersRelData);
////                    if (sectioned.getBind() != null) {
////                        String id = sectioned.getBind().getServiceNumberId();
////                        ChatRoomEntity entity = serviceMemberRoomData.get(id);
////                        if (entity != null) {
////                            sectioned.getBind().setUnreadNumber(entity.getUnReadNum());
////                        }
////                    }
//                    break;
//                case ROBOT_SERVICE:
//                    assembleBusinessRelTableByType(sectioned.getType(), sectioned.getDatas(), robotServicingRelData);
//                    assembleSectioned(sectioned, robotServicingRelData);
//                    break;
//                case MONITOR_AI_SERVICE:
//                    assembleBusinessRelTableByType(sectioned.getType(), sectioned.getDatas(), monitorAIServicingRelData);
//                    assembleSectioned(sectioned, monitorAIServicingRelData);
//                    break;
//            }
//        }
//    }
//
//    @Override
//    public void handleEvent(EventMsg eventMsg) {
//        switch (eventMsg.getCode()) {
//            case MsgConstant.REFRESH_ROOM_BY_LOCAL:
//                ChatRoomEntity refreshEntity = JsonHelper.getInstance().from(eventMsg.getString(), ChatRoomEntity.class);
//                if (ChatRoomSource.SERVICE.equals(refreshEntity.getListClassify())) {
//                    int refreshIndex = this.metadata.indexOf(refreshEntity);
//                    if (refreshIndex != -1) {
//                        this.metadata.remove(refreshEntity);
//                        this.metadata.add(refreshIndex, refreshEntity);
//                    } else {
//                        this.metadata.add(refreshEntity);
//                    }
//                    refreshData();
//                } else if (!Strings.isNullOrEmpty(refreshEntity.getConsultSrcRoomId())) {
//                    String id = refreshEntity.getConsultSrcRoomId();
//                    refreshEntity = ChatRoomReference.getInstance().findById2( getUserId(), id, true, true, true, true, true);
//                    if (refreshEntity == null) return;
//                    int refreshIndex = this.metadata.indexOf(refreshEntity);
//                    if (refreshIndex != -1) {
//                        this.metadata.remove(refreshEntity);
//                        this.metadata.add(refreshIndex, refreshEntity);
//                    } else {
//                        this.metadata.add(refreshEntity);
//                    }
//                    refreshData();
//                }
//                break;
//            case MsgConstant.CHANGE_TOP_ROOM:
//                ChatRoomEntity topRoom = JsonHelper.getInstance().from(eventMsg.getString(), ChatRoomEntity.class);
//                for (ChatRoomEntity r : this.metadata) {
//                    if (r.equals(topRoom)) {
//                        r.setTop(topRoom.isTop());
//                        r.setTopTime(topRoom.getTopTime());
//                        refreshData();
//                        return;
//                    }
//                }
//                break;
//            case MsgConstant.CHANGE_MUTE_ROOM:
//                ChatRoomEntity muteRoom = JsonHelper.getInstance().from(eventMsg.getString(), ChatRoomEntity.class);
//                for (ChatRoomEntity r : this.metadata) {
//                    if (r.equals(muteRoom)) {
//                        r.setMute(muteRoom.isMute());
//                        refreshData();
//                        return;
//                    }
//                }
//                break;
//
//            case MsgConstant.SESSION_UPDATE_FILTER:
//                ChatRoomEntity chatRoomEntity = JsonHelper.getInstance().from(eventMsg.getData(), ChatRoomEntity.class);
//
//                ChatRoomEntity entity = ChatRoomReference.getInstance().findById2( getUserId(), chatRoomEntity.getId(), true, true, true, true, true);
//
//                if (entity.getUnReadNum() > 0 && entity.isAtMe()) {
//
//                } else {
//                    entity.setAtMe(chatRoomEntity.isAtMe());
//                }
//                if (ChatRoomSource.SERVICE.equals(entity.getListClassify())) {
//                    int index = this.metadata.indexOf(entity);
//                    if (index < 0) {
//                        this.metadata.add(0, entity);
//                    } else {
//                        this.metadata.remove(entity);
//                        this.metadata.add(index, entity);
//                    }
//                    refreshData();
//                } else if (!Strings.isNullOrEmpty(entity.getConsultSrcRoomId())) {
//                    entity = ChatRoomReference.getInstance().findById2( getUserId(), chatRoomEntity.getConsultSrcRoomId(), true, true, true, true, true);
//                    int index = this.metadata.indexOf(entity);
//                    if (index < 0) {
//                        this.metadata.add(0, entity);
//                    } else {
//                        this.metadata.remove(entity);
//                        this.metadata.add(index, entity);
//                    }
//                    refreshData();
//                }
//                break;
//            case MsgConstant.CHANGE_LAST_MESSAGE: // 更新最後一筆訊息
//            case MsgConstant.SESSION_UPDATE_CALLING_FILTER:
//                String roomId = (String) eventMsg.getData();
//                if (!Strings.isNullOrEmpty(roomId)) {
//                    try {
//                        ChatRoomEntity updateEntity = ChatRoomReference.getInstance().findById(roomId);
//                        if (updateEntity != null && ChatRoomSource.SERVICE.equals(updateEntity.getListClassify())) {
//                            int index = this.metadata.indexOf(updateEntity);
//                            if (index < 0) {
//                                this.metadata.add(0, updateEntity);
//                            } else {
//                                this.metadata.remove(updateEntity);
//                                this.metadata.add(index, updateEntity);
//                            }
//                            refreshData();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                break;
//            case MsgConstant.REMOVE_FRIEND_FILTER:
//                UserProfileEntity mAccount = (UserProfileEntity) eventMsg.getData();
//                ChatRoomEntity friendEntity = ChatRoomReference.getInstance().findById(mAccount.getRoomId());
//
//                if (friendEntity != null) {
//
//                } else {
//                    friendEntity = ChatRoomEntity.Build().id(mAccount.getRoomId()).build();
//                }
//
//                ChatService.getInstance().broadCastUpdateBadgeItem(-friendEntity.getUnReadNum());
//                //删除本地聊天室
//                ChatRoomReference.getInstance().deleteById(mAccount.getRoomId());
//                //清空该聊天室本地消息
////                MessageReference.deleteByRoomId(mAccount.getId());
////                DBManager.getInstance().deleteMessageByRoomId(mAccount.getRoomId());
//                this.metadata.remove(friendEntity);
//                refreshData();
//                break;
//            case MsgConstant.SESSION_REMOVE_FILTER:
//                try {
//                    ChatRoomEntity removeEntity = ChatRoomEntity.Build().id((String) eventMsg.getData()).build();
//                    this.metadata.remove(removeEntity);
//                    refreshData();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case MsgConstant.SEND_UPDATE_AVATAR: // EVAN_FLAG 2020-03-18 (1.10.0) 未處理
//                try {
//                    UpdateAvatarBean updateAvatarBean = (UpdateAvatarBean) eventMsg.getData();
//                    if (updateAvatarBean != null && !Strings.isNullOrEmpty(updateAvatarBean.getRoomId())) {
//                        ChatRoomEntity updateEntity = ChatRoomReference.getInstance().findById(updateAvatarBean.getRoomId());
//                        if (updateEntity != null && ChatRoomSource.SERVICE.equals(updateEntity.getListClassify())) {
//                            int index = this.metadata.indexOf(updateEntity);
//                            if (index < 0) {
//                                this.metadata.add(0, updateEntity);
//                            } else {
//                                this.metadata.remove(updateEntity);
//                                this.metadata.add(index, updateEntity);
//                            }
//                            refreshData();
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case MsgConstant.SYNC_READ: // 收到同步
//                ChatRoomEntity syncEntity = (ChatRoomEntity) eventMsg.getData();
//                if (ChatRoomSource.SERVICE.equals(syncEntity.getListClassify())) {
//                    this.metadata.remove(syncEntity);
//                    this.metadata.add(syncEntity);
//                    refreshData();
//                }
//                break;
//            case MsgConstant.REMOVE_SERVICE_NUM_FILTER:
//                String serviceRoomId = (String) eventMsg.getData();
//
//                ChatRoomEntity serviceEntity = ChatRoomReference.getInstance().findById(serviceRoomId);
//
//                if (serviceEntity != null) {
//
//                } else {
//                    serviceEntity = ChatRoomEntity.Build().id(serviceRoomId).build();
//                }
//                //删除本地聊天室
//                ChatRoomReference.getInstance().deleteById(serviceEntity.getId());
//                //清空该聊天室本地消息
//                this.metadata.remove(serviceEntity);
//                refreshData();
//                break;
//
//            case MsgConstant.MSG_RECEIVED_FILTER: // 外部接收到新訊息處理服務號接線人員邏輯
//                MessageEntity receiverMessage = JsonHelper.getInstance().from(eventMsg.getString(), MessageEntity.class);
//                if (receiverMessage != null) {
//                    checkServiceNumberServiced(receiverMessage.getRoomId());
//                }
//                break;
//            case MsgConstant.REFRESH_ROOM_LIST_BY_ENTITY:
//                if (eventMsg.getData() instanceof ChatRoomEntity) {
//                    if (ChatRoomSource.SERVICE.equals(((ChatRoomEntity) eventMsg.getData()).getListClassify())) {
//                        changeUnEditByRoomId((ChatRoomEntity) eventMsg.getData());
//                    }
//                }
//                break;
//            case MsgConstant.MESSAGE_SEND_FAIL:
//                ChatRoomEntity failEntity = ChatRoomReference.getInstance().findById2( TokenPref.getInstance(getContext()).getUserId(), eventMsg.getString(), true, true, true, true, true);
//                if (failEntity == null) return;
//                if (ChatRoomSource.SERVICE.equals(failEntity.getListClassify())) {
//                    if (this.metadata.contains(failEntity)) {
//                        this.metadata.remove(failEntity);
//                        this.metadata.add(failEntity);
//                    }
//                    refreshData();
//                }
//                break;
//            case MsgConstant.UI_NOTICE_UPDATE_AVATARS_ALL, MsgConstant.REFRESH_CUSTOMER_NAME:
//                refreshData();
//                break;
//            case MsgConstant.NOTICE_UPDATE_AVATARS: // 批量更新頭像
//                Map<String, String> updateAvatars = JsonHelper.getInstance().fromToMap(eventMsg.getString());
//                String accountId = updateAvatars.get("accountId");
//                String avatarId = updateAvatars.get("avatarId");
//                for (ChatRoomEntity entity1 : this.metadata) {
//                    if (entity1.getOwnerId().equals(accountId)) {
//                        entity1.setAvatarId(avatarId);
//                    }
//
//                    for (UserProfileEntity profile : entity1.getMembers()) {
//                        if (profile.getId().equals(accountId)) {
//                            profile.setAvatarId(avatarId);
//                        }
//                    }
//                }
//                refreshData();
//                break;
//            case MsgConstant.DELETE_SERVICE_NUMBER_MEMBER:
//                String serviceNumberId = eventMsg.getString();
//                Iterator<ChatRoomEntity> iterator = this.metadata.iterator();
//                while(iterator.hasNext()) {
//                    ChatRoomEntity entity1 = iterator.next();
//                    if(entity1.getServiceNumberId().equals(serviceNumberId))
//                        iterator.remove();
//                }
//                refreshData();
//                break;
//            case MsgConstant.SERVICE_NUMBER_PERSONAL_START:
//            case MsgConstant.SERVICE_NUMBER_PERSONAL_STOP:
//                //商務號擁有者帳號在轉人工處理，通知更新服務號分組
//                Map<String, String> map = (Map<String, String>) eventMsg.getData();
//                String roomId_ = map.get("roomId");
//                if(!Strings.isNullOrEmpty(roomId_)) {
//                    if (ChatRoomReference.getInstance().findBossInfoByRoomId(roomId_)) {
//                        Iterator<ChatRoomEntity> iterator1 = metadata.listIterator();
//                        while (iterator1.hasNext()) {
//                            ChatRoomEntity entity1 = iterator1.next();
//                            if (entity1.getId().equals(roomId_)) {
//                                iterator1.remove();
//                                break;
//                            }
//                        }
//                        refreshData();
//                    }
//                }
//                break;
//            case MsgConstant.NOTICE_SELF_EXIT_ROOM:
//            //case MsgConstant.NOTICE_DISABLE_SERVICE_NUMBER: //2.2.17.hotfix7 後台禁用服務號功能 測試發現有問題 先暫時rollback功能不上線
//                String _roomId = (String) eventMsg.getData();
//                Iterator<ChatRoomEntity> iterator2 = metadata.listIterator();
//                while (iterator2.hasNext()) {
//                    ChatRoomEntity entity1 = iterator2.next();
//                    if(entity1.getId().equals(_roomId)) {
//                        iterator2.remove();
//                        break;
//                    }
//                }
//                refreshData();
//                break;
//        }
//    }
//
//    private void checkServiceNumberServiced(String roomId) {
//        for (ChatRoomEntity entity : this.metadata) {
//            if (entity.getId().equals(roomId)) {
//                ApiManager.doServiceNumberChatroomAgentServiced(getContext(), roomId, new ApiListener<ServiceNumberChatroomAgentServicedRequest.Resp>() {
//                    @Override
//                    public void onSuccess(ServiceNumberChatroomAgentServicedRequest.Resp resp) {
//                        String serviceNumberAgentId = resp.getServiceNumberAgentId();
//                        if (!entity.getServiceNumberAgentId().equals(serviceNumberAgentId)) {
//                            ChatRoomReference.getInstance().updateServiceNumberAgentIdById(roomId, serviceNumberAgentId);
//                            ChatRoomReference.getInstance().updateServiceNumberStatusById(roomId, resp.getServiceNumberStatus());
//                            entity.setServiceNumberAgentId(serviceNumberAgentId);
//                        }
//                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//                            refreshData();
//                        });
//                    }
//
//                    @Override
//                    public void onFailed(String errorMessage) {
//
//                    }
//                });
//                break;
//            }
//        }
//    }
//
//    public void changeUnEditByRoomId(ChatRoomEntity entity) {
//        if (entity == null || this.sections == null || this.sections.isEmpty()) {
//            return;
//        }
//        for (Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> section : this.sections) {
//            for (ChatRoomEntity chat : section.getDatas()) {
//                if (chat.getId().equals(entity.getId())) {
//                    chat.setUnfinishedEditedTime(entity.getUnfinishedEditedTime());
//                    chat.setUnfinishedEdited(entity.getUnfinishedEdited());
//                }
//            }
//        }
//        refreshData();
//    }
//
//    /**
//     * 群組過程先抽離有物件ID的聊天室
//     */
//    protected void assembleBusinessRelTableByType(SectionedType type, List<ChatRoomEntity> list, SetMultimap<String, ChatRoomEntity> multimap) {
//        if (list.isEmpty()) {
//            return;
//        }
//        Iterator<ChatRoomEntity> iterator = list.iterator();
//        while (iterator.hasNext()) {
//            ChatRoomEntity t = iterator.next();
//            String key = null;
//            switch (type) {
//                case MY_SERVICE:
//                case OTHERS_SERVICE:
//                    key = type.name() + "-" + t.getServiceNumberId() + "-" + t.getOwnerId() + "-" + t.getServiceNumberAgentId();
//                    break;
//                case OTHER:
//                case UNREAD_NO_AGENT:
//                case ROBOT_SERVICE:
//                case MONITOR_AI_SERVICE:
//                    key = t.getServiceNumberId() + "-" + t.getOwnerId();
//                    break;
//            }
//
//            if (!Strings.isNullOrEmpty(t.getBusinessId()) && !Strings.isNullOrEmpty(key)) {
//                this.businessRelData.put(key, t);
//                multimap.put(key, t);
//                iterator.remove();
//            }
//        }
//    }
//
//    /**
//     * 第二層群組，依照Sectioned內資料組合 物件聊天室歸戶邏輯，
//     * 若無法歸戶則取出第一筆做歸戶依據。
//     */
//    private void assembleSectioned(Sectioned<ChatRoomEntity, SectionedType, ServiceNumberEntity> sectioned, SetMultimap<String, ChatRoomEntity> multimap) {
//        List<ChatRoomEntity> list = sectioned.getDatas();
//        Iterator<ChatRoomEntity> iterator = list.iterator();
//        List<ChatRoomEntity> generalList = Lists.newArrayList();
//        while (iterator.hasNext()) {
//            ChatRoomEntity t = iterator.next();
//            String key = "";
//            switch (sectioned.getType()) {
//                case MY_SERVICE:
//                case OTHERS_SERVICE:
//                    key = sectioned.getType().name() + "-" + t.getServiceNumberId() + "-" + t.getOwnerId() + "-" + t.getServiceNumberAgentId();
//                    break;
//                case OTHER:
//                case UNREAD_NO_AGENT:
//                case ROBOT_SERVICE:
//                case MONITOR_AI_SERVICE:
//                    key = t.getServiceNumberId() + "-" + t.getOwnerId();
//                    break;
//            }
//            if (multimap.get(key) != null && !multimap.get(key).isEmpty()) {
//                ChatRoomEntity feature = getFeature(t, multimap.get(key));
//                ChatRoomEntity.ChatRoomEntityBuilder general = newServiceGeneral(t.getServiceNumberId(), t);
//                general.isTop(feature.isTop())
//                        .isAtMe(feature.isAtMe())
//                        .isFavourite(feature.isFavourite())
//                        .businessId(feature.getBusinessId())
//                        .updateTime(feature.getUpdateTime())
//                        .lastMessage(feature.getLastMessage())
//                        .failedMessage(feature.getFailedMessage())
//                        .unfinishedEdited(feature.getUnfinishedEdited())
//                        .unReadNum(feature.getUnReadNum());
//                generalList.add(general.build());
//
//                ChatRoomEntity original = t.toBuilder()
//                        .isSub(true)
//                        .build();
//                this.businessRelData.remove(key, original);
//                this.businessRelData.put(key, original);
//                iterator.remove();
//                multimap.removeAll(key);
//            }
//        }
//
//        // 剩餘資料無法歸戶
//        Iterator<String> executorKeyIterator = multimap.keySet().iterator();
//        while (executorKeyIterator.hasNext()) {
//            String key = executorKeyIterator.next();
//            Set<ChatRoomEntity> sets = multimap.get(key);
//            // EVAN_FLAG 2020-11-09 (1.14.0) group datas size > 1
//            if (sets.size() > 0) {
//                ChatRoomEntity original = getOriginalByType(sets);
//                if (original != null) {
//                    ChatRoomEntity feature = getFeature(original, multimap.get(key));
//                    ChatRoomEntity.ChatRoomEntityBuilder general = newServiceGeneral(original.getServiceNumberId(), original);
//                    general.isTop(feature.isTop())
//                            .isAtMe(feature.isAtMe())
//                            .isFavourite(feature.isFavourite())
//                            .businessId(feature.getBusinessId())
//                            .updateTime(feature.getUpdateTime())
//                            .lastMessage(feature.getLastMessage())
//                            .failedMessage(feature.getFailedMessage())
//                            .unfinishedEdited(feature.getUnfinishedEdited())
//                            .unReadNum(feature.getUnReadNum());
//                    this.businessRelData.remove(key, original);
//                    this.businessRelData.put(key, original);
//                    generalList.add(general.build());
//                    executorKeyIterator.remove();
//                }
//            }
//            // 如過只有一筆不做歸戶動作
////            else if (sets.size() == 1) {
////                generalList.addAll(sets);
////                executorKeyIterator.remove();
////            }
//        }
//
//        Iterable<ChatRoomEntity> combinedIterables = Iterables.unmodifiableIterable(Iterables.concat(list, generalList));
//        List<ChatRoomEntity> resultList = Lists.newArrayList(combinedIterables);
//        if (resultList.isEmpty()) {
//            CELog.w("sectioned 內資料不該為 0 ");
//        }
//        sectioned.setDatas(resultList);
//        sectioned.setOpen(isSectionedOpenSet.contains(sectioned.getName()));
//        sectioned.setSize(isSectionedOpenSet.contains(sectioned.getName()) ? resultList.size() : 0);
////        sectioned.setSize(resultList.size());
//    }
//
//    /**
//     * 針對物件對應不上主要聊天室，取得第一筆最為主要聊天室
//     */
//    private ChatRoomEntity getOriginalByType(Set<ChatRoomEntity> entities) {
//        return Iterables.getFirst(entities, null);
//    }
//
//    private ChatRoomEntity.ChatRoomEntityBuilder newServiceGeneral(String serviceNumberId, ChatRoomEntity t) {
//        return ChatRoomEntity.Build()
//                .isHardCode(true)
//                .isSub(false)
//                .id(t.getId())
//                .name(t.getName())
//                .type(t.getType())
//                .ownerId(t.getOwnerId())
//                .serviceNumberId(serviceNumberId)
//                .serviceNumberName(t.getServiceNumberName())
//                .serviceNumberAvatarId(t.getServiceNumberAvatarId())
//                .serviceNumberType(t.getServiceNumberType())
//                .serviceNumberOwnerId(t.getServiceNumberOwnerId())
//                .serviceNumberAgentId(t.getServiceNumberAgentId())
//                .serviceNumberStatus(t.getServiceNumberStatus())
//                .isTop(t.isTop())
//                .avatarId(t.getAvatarId())
//                .memberIds(t.getMemberIds())
//                .members(t.getMembers())
//                .lastMessage(t.getLastMessage())
//                .unfinishedEdited(t.getUnfinishedEdited())
//                .unfinishedEditedTime(t.getUnfinishedEditedTime())
//                .failedMessage(t.getFailedMessage())
//                .updateTime(t.getUpdateTime());
//    }
//
//    ChatRoomEntity getFeature(ChatRoomEntity original, Set<ChatRoomEntity> entities) {
//        boolean isTop = original.isTop();
//        boolean isFavourite = original.isFavourite();
//        boolean isAtMe = original.isAtMe();
//        int unReadNum = Math.abs(original.getUnReadNum());
//
//        long upDataTime = original.getUpdateTime();
//
//        Set<UserProfileEntity> members = Sets.newHashSet(original.getMembers());
//        String unfinishedEdited = original.getUnfinishedEdited();
//        MessageEntity lastMessage = original.getLastMessage();
//        MessageEntity failedMessage = original.getFailedMessage();
//
//        String businessId = original.getBusinessId();
//        long sendTime = lastMessage != null ? lastMessage.getSendTime() : 0L;
//
//        flag:
//        for (ChatRoomEntity sub : entities) {
//            if (original.equals(sub)) {
//                continue flag;
//            }
//            if (!isTop) {
//                isTop = sub.isTop();
//            }
//
//            if (Math.abs(sub.getUnReadNum()) > 0) {
//                unReadNum += Math.abs(sub.getUnReadNum());
//            }
//
//            if (!isFavourite) {
//                isFavourite = sub.isFavourite();
//            }
//
//            if (!isAtMe) {
//                isAtMe = sub.isAtMe();
//            }
//
//            if (sub.getUpdateTime() > upDataTime) {
//                if (sub.getLastMessage() != null && sendTime < sub.getLastMessage().getSendTime()) {
//                    lastMessage = sub.getLastMessage();
//                    sendTime = sub.getLastMessage().getSendTime();
//                }
//                upDataTime = sub.getUpdateTime();
//                unfinishedEdited = sub.getUnfinishedEdited();
//
//
//                if (sub.getFailedMessage() != null) {
//                    failedMessage = sub.getFailedMessage();
//                }
//
//                if (Strings.isNullOrEmpty(businessId)) {
//                    businessId = sub.getBusinessId();
//                }
//            }
//
//            members.addAll(sub.getMembers());
//        }
//
//        return ChatRoomEntity.Build()
//                .isTop(isTop)
//                .isAtMe(isAtMe)
//                .businessId(businessId)
//                .isFavourite(isFavourite)
//                .updateTime(upDataTime)
//                .lastMessage(lastMessage)
//                .failedMessage(failedMessage)
//                .unfinishedEdited(unfinishedEdited)
//                .unReadNum(unReadNum)
//                .build();
//    }
//
//    public interface AdapterCallback {
//        void checkListIsEmpty(int size);
//    }
//
//}
