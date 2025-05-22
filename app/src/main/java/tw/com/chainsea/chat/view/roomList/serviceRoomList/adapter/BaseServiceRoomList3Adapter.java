//package tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter;
//
//import static tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ActionBean.BROADCAST;
//import static tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ActionBean.CHAT;
//import static tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ActionBean.HOME;
//import static tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ActionBean.MEMBERS;
//import static tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ActionBean.WAIT_TRANSFER;
//
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.graphics.drawable.GradientDrawable;
//import android.text.SpannableString;
//import android.text.SpannableStringBuilder;
//import android.view.View;
//
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.ItemTouchHelper;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.common.base.Strings;
//import com.google.common.collect.HashMultimap;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.common.collect.SetMultimap;
//import com.google.common.collect.Sets;
//
//import org.greenrobot.eventbus.EventBus;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import tw.com.aile.sdk.bean.message.MessageEntity;
//import tw.com.chainsea.android.common.event.OnHKClickListener;
//import tw.com.chainsea.android.common.log.CELog;
//import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
//import tw.com.chainsea.android.common.text.StringHelper;
//import tw.com.chainsea.chat.util.IntentUtil;
//import tw.com.chainsea.chat.util.TextViewHelper;
//import tw.com.chainsea.ce.sdk.SdkLib;
//import tw.com.chainsea.ce.sdk.bean.InputLogBean;
//import tw.com.chainsea.ce.sdk.bean.InputLogType;
//import tw.com.chainsea.ce.sdk.bean.PicSize;
//import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
//import tw.com.chainsea.ce.sdk.bean.account.UserType;
//import tw.com.chainsea.ce.sdk.bean.business.BusinessCode;
//import tw.com.chainsea.ce.sdk.bean.msg.ChannelType;
//import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
//import tw.com.chainsea.ce.sdk.bean.msg.SourceType;
//import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
//import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus;
//import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType;
//import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
//import tw.com.chainsea.ce.sdk.database.DBManager;
//import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
//import tw.com.chainsea.ce.sdk.event.EventBusUtils;
//import tw.com.chainsea.ce.sdk.event.EventMsg;
//import tw.com.chainsea.ce.sdk.event.MsgConstant;
//import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
//import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
//import tw.com.chainsea.ce.sdk.http.ce.model.User;
//import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference;
//import tw.com.chainsea.ce.sdk.service.AvatarService;
//import tw.com.chainsea.ce.sdk.service.UserProfileService;
//import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
//import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
//import tw.com.chainsea.chat.App;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.base.Constant;
//import tw.com.chainsea.chat.config.BundleKey;
//import tw.com.chainsea.chat.config.SystemConfig;
//import tw.com.chainsea.chat.databinding.ItemBaseRoom6Binding;
//import tw.com.chainsea.chat.databinding.ItemIncludeTableRoom6Binding;
//import tw.com.chainsea.chat.databinding.ItemSectionedFooterBinding;
//import tw.com.chainsea.chat.databinding.ItemServiceNumNameBinding;
//import tw.com.chainsea.chat.lib.AtMatcherHelper;
//import tw.com.chainsea.chat.lib.ToastUtils;
//import tw.com.chainsea.chat.service.ActivityTransitionsControl;
//import tw.com.chainsea.chat.ui.dialog.WaitTransferDialogBuilder;
//import tw.com.chainsea.chat.util.AvatarKit;
//import tw.com.chainsea.chat.util.NameKit;
//import tw.com.chainsea.chat.util.TimeUtil;
//import tw.com.chainsea.chat.util.UnreadUtil;
//import tw.com.chainsea.chat.view.account.homepage.ServicesNumberManagerHomepageActivity;
//import tw.com.chainsea.chat.view.globalSearch.Sectioned;
//import tw.com.chainsea.chat.view.homepage.BossServiceNumberHomepageActivity;
//import tw.com.chainsea.chat.view.roomList.mainRoomList.adapter.BaseRoomList3Adapter;
//import tw.com.chainsea.chat.view.roomList.mainRoomList.listener.OnRoomItem3ClickListener;
//import tw.com.chainsea.chat.view.roomList.type.Menu;
//import tw.com.chainsea.chat.view.roomList.type.SectionedType;
//import tw.com.chainsea.custom.view.adapter.SectionedRecyclerViewAdapter;
//import tw.com.chainsea.custom.view.image.CircleImageView;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemNoSwipeViewHolder;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemSwipeWithActionWidthViewHolder;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperCallback;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension;
//import tw.com.chainsea.custom.view.recyclerview.lintener.OnSwipeMenuListener;
//
///**
// * Create by evan on 1/5/21
// *
// * @author Evan Wang
// * date 1/5/21
// */
//public abstract class BaseServiceRoomList3Adapter<T extends ChatRoomEntity, S extends ServiceNumberEntity> extends SectionedRecyclerViewAdapter<ItemBaseViewHolder<Sectioned<T, SectionedType, S>>, ItemBaseViewHolder<T>, ItemBaseViewHolder<Sectioned<T, SectionedType, S>>> {
//    private Context context;
//    private RecyclerView.RecycledViewPool sharedPool;
//    protected ItemTouchHelperExtension itemTouchHelperExtension;
//    private String userId;
//    protected List<T> metadata = Lists.newArrayList();
//
//    protected List<Sectioned<T, SectionedType, S>> sections = Lists.newArrayList();
//    private final Set<BaseRoomList3Adapter<T>> subAdapterSet = Sets.newHashSet();
//    Set<String> isSectionedOpenSet = Sets.newHashSet(SectionedType.UNREAD_NO_AGENT.name());
//    protected Map<String, Integer> limitOpenDatas = Maps.newHashMap();
//
//    private static final int LIMIT = 3;
//    private static final int CELL_LIMIT = 10;
//
//    protected SetMultimap<String, T> businessRelData = HashMultimap.create();
//
//    protected OnRoomItem3ClickListener<ChatRoomEntity> onRoomItemClickListener;
//    protected OnSwipeMenuListener<ChatRoomEntity, Menu> onSwipeMenuListener;
//
//    private SectionedServiceRoomList3Adapter.AdapterCallback adapterCallback = null;
//    public enum SortStyle {
//        DEFAULT,
//        TIME,
//        HYBRID
//    }
//
//    protected SortStyle sortStyle = SortStyle.TIME;
//
//    public BaseServiceRoomList3Adapter(Context context) {
//        setContext(context);
//    }
//
//    @Override
//    public void executeAnimatorEnd(int position) {
//
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
//    protected int getSectionItemViewType(int section, int position) {
//        try {
//            return getType(this.sections.get(section).getDatas().get(position));
//        } catch (Exception e) {
//            CELog.e(e.getMessage());
//        }
//        return super.getSectionFooterViewType(section);
//    }
//
//    protected void setContext(Context context) {
//        this.context = context;
//        if (Strings.isNullOrEmpty(this.userId)) {
//            this.userId = TokenPref.getInstance(context).getUserId();
//        }
//    }
//
//    public Context getContext() {
//        return this.context;
//    }
//
//    protected String getUserId() {
//        return this.userId;
//    }
//
//    protected String getBossServiceNumberId() {
//        return TokenPref.getInstance(getContext()).getBossServiceNumberId();
//    }
//
//    public BaseServiceRoomList3Adapter<T, S> setSortStyle(SortStyle sortStyle) {
//        this.sortStyle = sortStyle;
//        return this;
//    }
//
//    public BaseServiceRoomList3Adapter<T, S> setSharedPool(RecyclerView.RecycledViewPool sharedPool) {
//        this.sharedPool = sharedPool;
//        return this;
//    }
//
//    public BaseServiceRoomList3Adapter<T, S> setOnSwipeMenuListener(OnSwipeMenuListener<ChatRoomEntity, Menu> onSwipeMenuListener) {
//        this.onSwipeMenuListener = onSwipeMenuListener;
//        return this;
//    }
//
//    public int getSize() {
//        return metadata.size();
//    }
//
//    public List<T> getLists() { return metadata; }
//    public BaseServiceRoomList3Adapter<T, S> setData(RefreshSource source, List<T> list) {
//        switch (source) {
//            case LOCAL:
//                for (int i = 0; i < list.size(); i++) {
//                    if (this.metadata.contains(list.get(i))) {
//                        int index = metadata.indexOf(list.get(i));
//                        this.metadata.set(index, list.get(i));
//                    } else {
//                        this.metadata.add(list.get(i));
//                    }
//                }
//                break;
//            case REMOTE:
//                for (T t : list) {
//                    this.metadata.remove(t);
//                    this.metadata.add(t);
//                    //CELog.d("Kyle3 REMOTE status=" + t.getServiceNumberStatus().name() + ", name=" + t.getName()+", agent id="+t.getServiceNumberAgentId());
//                }
//                break;
//        }
//        try { //避免轉型失敗當機
//            App.getInstance().serviceChatRoom = (List<ChatRoomEntity>) metadata;
//        } catch (Exception e) {
//            CELog.e(e.getMessage());
//        }
//        return this;
//    }
//
//    public BaseServiceRoomList3Adapter<T, S> setItemTouchHelperExtension(ItemTouchHelperExtension itemTouchHelperExtension) {
//        this.itemTouchHelperExtension = itemTouchHelperExtension;
//        return this;
//    }
//    public void setAdapterCallback(SectionedServiceRoomList3Adapter.AdapterCallback adapterCallback) {
//        this.adapterCallback = adapterCallback;
//    }
//    public BaseServiceRoomList3Adapter<T, S> setOnRoomItemClickListener(OnRoomItem3ClickListener<ChatRoomEntity> onRoomItemClickListener) {
//        this.onRoomItemClickListener = onRoomItemClickListener;
//        return this;
//    }
//
//    public BaseServiceRoomList3Adapter<T, S> closeOther(ChatRoomType type, String key) {
//        if (Strings.isNullOrEmpty(key)) {
//            limitOpenDatas.clear();
//            for (BaseRoomList3Adapter<T> a : subAdapterSet) {
//                a.setData(businessRelData.get(a.getBindKey()));
//                a.setLimitedQuantity(0);
//            }
//        } else {
//            for (BaseRoomList3Adapter<T> a : subAdapterSet) {
//                if (!a.isBindKeyBy(key)) {
//                    a.setData(businessRelData.get(a.getBindKey()));
//                    a.setLimitedQuantity(0);
//                }
//            }
//            int limit = limitOpenDatas.get(key) != null ? limitOpenDatas.get(key) : 0;
//            limitOpenDatas.clear();
//            limitOpenDatas.put(key, limit);
//        }
//        return this;
//    }
//
//    public void closeOpenedChild(String key) {
//        for (BaseRoomList3Adapter<T> a : subAdapterSet) {
//            if (!a.isBindKeyBy(key)) {
//                a.emptyRefreshData();
//            } else {
//                CELog.e("");
//            }
//        }
//    }
//
//    protected abstract int getType(T t);
//
//    protected abstract void sort();
//
//    protected abstract void sortSectioned();
//
//    protected abstract void filter();
//
//    protected abstract void pullAway();
//
//    protected abstract void group();
//
//    protected abstract void removeItem(String id);
//    public void removeRoomId(String roomId) {
//        removeItem(roomId);
//        refreshData();
//    }
//    public void refreshData() {
//        filter();
//        sort();
//        pullAway();
//        group();
//        sortSectioned();
//        notifyDataSetChanged();
//        if (!subAdapterSet.isEmpty()) {
//            for (BaseRoomList3Adapter<T> a : subAdapterSet) {
//                a.refreshData();
//            }
//        }
//    }
//
//    public void emptyRefreshData() {
//        notifyDataSetChanged();
//    }
//
//    abstract public void handleEvent(EventMsg eventMsg);
//
//    /**
//     * Part header
//     */
//    public class ServiceSectionedViewHolder extends ItemNoSwipeViewHolder<Sectioned<T, SectionedType, S>> implements SectionActionAdapter.OnActionListener<ActionBean, ServiceNumberEntity> {
//        private ItemServiceNumNameBinding serviceNumNameBinding;
//        SectionActionAdapter actionAdapter;
//
//        public ServiceSectionedViewHolder(ItemServiceNumNameBinding binding) {
//            super(binding.getRoot());
//            serviceNumNameBinding = binding;
//            serviceNumNameBinding.rvActions.setLayoutManager(new GridLayoutManager(getContext(), 5));
//            actionAdapter = new SectionActionAdapter();
//            serviceNumNameBinding.rvActions.setAdapter(actionAdapter);
//        }
//
//        private int getUnReadNumber(Sectioned<T, SectionedType, S> sectioned) {
//            if (sectioned == null) {
//                return 0;
//            }
//
//            int unReadNumber = 0;
//            if (sectioned.getBind() != null) {
//                unReadNumber += Math.abs(sectioned.getBind().getUnreadNumber());
//            }
//            List<T> list = sectioned.getDatas();
//            if (list != null && !list.isEmpty()) {
//                for (T t : list) {
//                    if (t.isHardCode()) {
//                        String key = SectionedType.MY_or_OTHERS_SERVICE.contains(sectioned.getType())
//                                ? sectioned.getType().name() + "-" + t.getServiceNumberId() + "-" + t.getOwnerId() + "-" + t.getServiceNumberAgentId()
//                                : t.getServiceNumberId() + "-" + t.getOwnerId();
//                        Set<T> subs = businessRelData.get(key);
//                        if (subs != null && !subs.isEmpty()) {
//                            for (T s : subs) {
//                                unReadNumber += Math.abs(s.getUnReadNum());
//                                unReadNumber += Math.abs(s.getConsultSrcUnreadNumber());
//                            }
//                        }
//                    } else {
//                        unReadNumber += Math.abs(t.getUnReadNum());
//                        unReadNumber += Math.abs(t.getConsultSrcUnreadNumber());
//                    }
//                }
//            }
//            return unReadNumber;
//        }
//
//        @Override
//        public void onBind(Sectioned<T, SectionedType, S> sectioned, int section, int position) {
//            super.onBind(sectioned, section, position);
//
//            if (sectioned.isHidingSectioned()) {
//                serviceNumNameBinding.clSectioned.setVisibility(View.GONE);
//                serviceNumNameBinding.clSectionedControl.setVisibility(View.GONE);
//                serviceNumNameBinding.tvUnread.setVisibility(View.GONE);
//            } else {
//                serviceNumNameBinding.tvTitle.setText(sectioned.getName());
//                serviceNumNameBinding.tvUnread.setVisibility(View.GONE);
//                serviceNumNameBinding.clSectioned.setVisibility(View.VISIBLE);
//                serviceNumNameBinding.clSectioned.setBackgroundColor(sectioned.isOpen() ? Color.WHITE : 0xFFF7F7F7);
//                serviceNumNameBinding.tvOpen.setImageResource(sectioned.isOpen() ? R.drawable.ic_expand : R.drawable.ic_close);
//                if (sectioned.getBind() != null) {
//                    String broadcastRoomId = sectioned.getBind().getBroadcastRoomId();
//                    actionAdapter.setUnreadNumber(sectioned.getBind().getUnreadNumber()).refresh();
//                    List<ActionBean> actions = new ArrayList<>();
//                    actions.add(CHAT); //聊天
//                    for (ChatRoomEntity entity : metadata) {
//                        if (entity.getServiceNumberId().equals(sectioned.getBind().getServiceNumberId())
//                                && entity.isTransferFlag()) {
//                            actions.add(WAIT_TRANSFER);
//                            break;
//                        }
//                    }
//                    //群發隱藏, 等新版本完成後在開啟
//                    if (SystemConfig.enableBroadcast) {
//                        if ((sectioned.getBind().isOwner() || sectioned.getBind().isManager()
//                                && (broadcastRoomId != null && !broadcastRoomId.isEmpty()))) {
//                            actions.add(BROADCAST);
//                        }
//                    }
//                    actions.add(MEMBERS); //成員
//                    actions.add(HOME); //主頁
//                    actionAdapter.setData(actions);
//                    if (sectioned.isOpen()) {
//                        serviceNumNameBinding.clSectionedControl.setVisibility(View.VISIBLE);
//                        actionAdapter.bind(sectioned.getBind()).setOnActionListener(this).refresh();
//                    } else {
//                        serviceNumNameBinding.clSectionedControl.setVisibility(View.GONE);
//                        actionAdapter.bind(null).setOnActionListener(null).refresh();
//                    }
//                    serviceNumNameBinding.icon.setVisibility(View.VISIBLE);
//
//                    if(!sectioned.getDatas().isEmpty())
//                        serviceNumNameBinding.icon.setImageResource(sectioned.getDatas().get(0).getServiceNumberOpenType().contains("O") ? R.drawable.ic_slice_o : R.drawable.ic_slice_i);
//                    else{
//                        ServiceNumberEntity entity = ServiceNumberReference.findServiceNumberById(sectioned.getServiceNumberId());
//                        if(entity!=null)
//                            serviceNumNameBinding.icon.setImageResource(entity.getServiceOpenType().contains("O") ? R.drawable.ic_slice_o : R.drawable.ic_slice_i);
//                    }
//                } else {
//                    actionAdapter.bind(null).setOnActionListener(null).refresh();
//                    serviceNumNameBinding.clSectionedControl.setVisibility(View.GONE);
//                    serviceNumNameBinding.icon.setVisibility(View.GONE);
//                }
//                int unreadNumber = getUnReadNumber(sectioned);
//                if (unreadNumber != 0  && !sectioned.isOpen()) {
//                    serviceNumNameBinding.tvUnread.setText(UnreadUtil.INSTANCE.getUnreadText(unreadNumber));
//                    serviceNumNameBinding.tvUnread.setVisibility(View.VISIBLE);
//                } else {
//                    serviceNumNameBinding.tvUnread.setVisibility(View.GONE);
//                }
//
//                serviceNumNameBinding.clSectioned.setOnClickListener(new OnHKClickListener() {
//                    @Override
//                    public void onClick(View v, Object o) {
//                        // TODO Cell open limit == 10
//
//                        int size = sectioned.isHasFooter() ? Math.min(sectioned.getDatas().size(), CELL_LIMIT) : sectioned.getDatas().size();
//                        sectioned.setSize(sectioned.isOpen() ? 0 : size);
////                        sectioned.setSize(sectioned.isOpen() ? 0 : sectioned.getDatas().size());
//                        sectioned.setOpen(!sectioned.isOpen());
//                        if (sectioned.isOpen()) {
//                            isSectionedOpenSet.add(sectioned.getName());
//                        } else {
//                            isSectionedOpenSet.remove(sectioned.getName());
//                        }
//                        notifyDataSetChanged();
//                    }
//                });
//            }
//
//        }
//
//        @Override
//        public void onChatClick(ActionBean bean, ServiceNumberEntity entity) {
//            //成員聊天室點擊事件
//            ApiManager.doServiceNumberItem(SdkLib.getAppContext(), entity.getServiceNumberId(), new ApiListener<ServiceNumberEntity>() {
//                @Override
//                public void onSuccess(ServiceNumberEntity entity) {
//                    if(entity.getStatus().equals(User.Status.DISABLE)) {
//                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> ToastUtils.showToast(App.getContext(), App.getContext().getString(R.string.text_service_number_disabled)));
//                        boolean status = DBManager.getInstance().deleteServiceNumRoomByServiceNumberId(entity.getServiceNumberId());
//                        if (status) {
//                            EventBus.getDefault().post(new EventMsg<>(
//                                    MsgConstant.NOTICE_SERVICE_NUMBER_REFRESH_BY_DB));
//                        }
//                    } else {
//                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NAVIGATE_TO_CHAT_ROOM, entity.getServiceMemberRoomId()));
//                    }
////                    else {
////                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> ToastUtils.showToast(App.getAppContext(), App.getContext().getString(R.string.text_service_number_disabled)));
////                        if(DBManager.getInstance().deleteServiceNumber(entity.getServiceNumberId()))
////                            refreshData();
////                    }
//                }
//                @Override
//                public void onFailed(String errorMessage) { }
//            });
////            ActivityTransitionsControl.navigateToChat(getContext(), entity.getServiceMemberRoomId(), ((intent, s) -> getContext().startActivity(intent)));
//        }
//
//        @Override
//        public void onWaitTransFerClick(ActionBean actionBean, ServiceNumberEntity serviceNumberEntity) {
//            List<ChatRoomEntity> list = new ArrayList<>();
//            for (ChatRoomEntity data : metadata) {
//                if (data.isTransferFlag()) {
//                    list.add(data);
//                }
//            }
//            new WaitTransferDialogBuilder(context, list).create().show();
//        }
//
//        @Override
//        public void onBroadcastClick(ActionBean bean, ServiceNumberEntity entity) {
//            ActivityTransitionsControl.navigateToServiceBroadcastEditor(getContext(), entity.getBroadcastRoomId(), entity.getName(), entity.getServiceNumberId(), (intent, s) -> getContext().startActivity(intent));
//        }
//
//        @Override
//        public void onWelcomeMessageClick(ActionBean bean, ServiceNumberEntity entity) {
//            if (entity.isManager() || entity.isOwner()) {
//                ActivityTransitionsControl.navigateToServiceNumberMoreSettings(getContext(), entity.getServiceNumberId(), (intent, s) -> getContext().startActivity(intent));
//            }
//        }
//
//        @Override
//        public void onMembersClick(ActionBean bean, ServiceNumberEntity entity) {
//            ApiManager.doServiceNumberItem(SdkLib.getAppContext(), entity.getServiceNumberId(), new ApiListener<ServiceNumberEntity>() {
//                @Override
//                public void onSuccess(ServiceNumberEntity entity) {
//                    if(entity.getStatus().equals(User.Status.DISABLE)) {
//                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> ToastUtils.showToast(App.getContext(), App.getContext().getString(R.string.text_service_number_disabled)));
//                        boolean status = DBManager.getInstance().deleteServiceNumRoomByServiceNumberId(entity.getServiceNumberId());
//                        if (status) {
//                            EventBus.getDefault().post(new EventMsg<>(
//                                    MsgConstant.NOTICE_SERVICE_NUMBER_REFRESH_BY_DB));
//                        }
//                    } else {
//                        ActivityTransitionsControl.navigateToServiceAgentsManage(getContext(), entity.getBroadcastRoomId(), entity.getServiceNumberId(), (intent, s) -> IntentUtil.INSTANCE.start(serviceNumNameBinding.getRoot().getContext(), intent));
//                    }
////                    if(entity.getStatus().equals("Enable"))
////                        ActivityTransitionsControl.navigateToServiceAgentsManage(getContext(), entity.getBroadcastRoomId(), entity.getServiceNumberId(), (intent, s) -> getContext().startActivity(intent));
////                    else {
////                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> ToastUtils.showToast(App.getAppContext(), App.getContext().getString(R.string.text_service_number_disabled)));
////                        if(DBManager.getInstance().deleteServiceNumber(entity.getServiceNumberId()))
////                            refreshData();
////                    }
//                }
//                @Override
//                public void onFailed(String errorMessage) { }
//            });
//        }
//
//        @Override
//        public void onHomePageClick(ActionBean bean, ServiceNumberEntity entity) {
//            ApiManager.doServiceNumberItem(SdkLib.getAppContext(), entity.getServiceNumberId(), new ApiListener<ServiceNumberEntity>() {
//                @Override
//                public void onSuccess(ServiceNumberEntity entity) {
//                    if(entity.getStatus().equals(User.Status.DISABLE)) {
//                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> ToastUtils.showToast(App.getContext(), App.getContext().getString(R.string.text_service_number_disabled)));
//                        boolean status = DBManager.getInstance().deleteServiceNumRoomByServiceNumberId(entity.getServiceNumberId());
//                        if (status) {
//                            EventBus.getDefault().post(new EventMsg<>(
//                                    MsgConstant.NOTICE_SERVICE_NUMBER_REFRESH_BY_DB));
//                        }
//                    } else {
//                        showHomePage(entity);
//                    }
////                    if(entity.getStatus().equals("Enable"))
////                        showHomePage(entity);
////                    else {
////                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> ToastUtils.showToast(App.getAppContext(), App.getContext().getString(R.string.text_service_number_disabled)));
////                        if(DBManager.getInstance().deleteServiceNumber(entity.getServiceNumberId()))
////                            refreshData();
////                    }
//                }
//                @Override
//                public void onFailed(String errorMessage) { }
//            });
//        }
//
//        private void showHomePage(ServiceNumberEntity entity) {
//            if (ServiceNumberType.BOSS.getType().equals(entity.getServiceNumberType())) {
//                Intent intent = new Intent(getContext(), BossServiceNumberHomepageActivity.class);
//                intent.putExtra(BundleKey.BROADCAST_ROOM_ID.key(), entity.getBroadcastRoomId());
//                intent.putExtra(BundleKey.SERVICE_NUMBER_ID.key(), entity.getServiceNumberId());
//                IntentUtil.INSTANCE.start(serviceNumNameBinding.getRoot().getContext(), intent);
//            } else if ((ServiceNumberType.MANAGER.getType().equals(entity.getServiceNumberType()))) {
//                if (entity.isManager() || entity.isOwner()) {
//                    Intent intent = new Intent(getContext(), ServicesNumberManagerHomepageActivity.class);
//                    intent.putExtra(BundleKey.SERVICE_NUMBER_ID.key(), entity.getServiceNumberId());
//                    IntentUtil.INSTANCE.start(serviceNumNameBinding.getRoot().getContext(), intent);
//                } else {
//                    ActivityTransitionsControl.navigateToServiceNumberManage(getContext(), entity.getRoomId(), entity.getServiceNumberId(), (intent, s) -> IntentUtil.INSTANCE.start(serviceNumNameBinding.getRoot().getContext(), intent));
//                }
//            } else {
//                ActivityTransitionsControl.navigateToServiceNumberManage(getContext(), entity.getRoomId(), entity.getServiceNumberId(), (intent, s) -> IntentUtil.INSTANCE.start(serviceNumNameBinding.getRoot().getContext(), intent));
//            }
//        }
//    }
//
//    /**
//     * mapping layout @link R.layout.item_include_table_room_6
//     */
//    public class ServiceParentChatRoomViewHolder extends ItemNoSwipeViewHolder<T> implements ItemTouchHelperCallback.OnMoveListener {
//
//        BaseRoomList3Adapter sAdapter;
//        private ItemIncludeTableRoom6Binding includeTableRoom6Binding;
//        private final NameKit nameKit = new NameKit();
//
//        public ServiceParentChatRoomViewHolder(ItemIncludeTableRoom6Binding binding) {
//            super(binding.getRoot());
//            includeTableRoom6Binding = binding;
//            super.setMenuViews(includeTableRoom6Binding.llLeftMenu, includeTableRoom6Binding.llRightMenu);
//            super.setContentItemView(includeTableRoom6Binding.clContentItem);
//            includeTableRoom6Binding.rvSubList.setLayoutManager(new LinearLayoutManager(getContext()));
//            if (sharedPool != null) {
//                includeTableRoom6Binding.rvSubList.setRecycledViewPool(sharedPool);
//            }
//            includeTableRoom6Binding.tvUnread.setVisibility(View.INVISIBLE);
//        }
//
//        @Override
//        public void onBind(T t, int section, int position) {
//            super.onBind(t, section, position);
//            SectionedType type = sections.get(section).getType();
//            String key = SectionedType.MY_or_OTHERS_SERVICE.contains(type)
//                    ? type.name() + "-" + t.getServiceNumberId() + "-" + t.getOwnerId() + "-" + t.getServiceNumberAgentId()
//                    : t.getServiceNumberId() + "-" + t.getOwnerId();
//
//            if (!subAdapterSet.contains(this.sAdapter)) {
//                ItemTouchHelperExtension itemTouchHelper = new ItemTouchHelperExtension(new ItemTouchHelperCallback(ItemTouchHelper.START | ItemTouchHelper.END)
//                        .setFlag(2)
//                        .setOnMoveListener(this)
//                        .setTag(key));
//                this.sAdapter = new ChildServiceRoomList3Adapter(type, key)
//                        .setItemTouchHelperExtension(itemTouchHelper)
//                        .setOnSwipeMenuListener(onSwipeMenuListener)
//                        .setOnRoomItemClickListener(onRoomItemClickListener);
//
//                itemTouchHelper.attachToRecyclerView(this.includeTableRoom6Binding.rvSubList);
//                includeTableRoom6Binding.rvSubList.setAdapter(this.sAdapter);
//                subAdapterSet.add(this.sAdapter);
//            }
//            includeTableRoom6Binding.rvSubList.setVisibility(View.GONE);
//            includeTableRoom6Binding.vSubNodeDivider.setVisibility(View.GONE);
//            includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.GONE);
//
//
//            int taskCount = 0;
//            int opportunityCount = 0;
//            int serviceRequestCount = 0;
//            this.sAdapter
//                    .setLimitedQuantity(0)
//                    .setData(businessRelData.get(key));
//
//            List<String> avatarIds = t.getAvatarIds(getContext(), 4);
//            String name = t.getServicesNumberTitle(userId);
//            if (avatarIds.size() == 1) {
//                if (AvatarKit.DEFAULT_AVATAR_ID.equals(avatarIds.get(0))) {
//                    String nameSpilt = name.substring(0, name.indexOf("@"));
//                    includeTableRoom6Binding.civIcon.setVisibility(View.INVISIBLE);
//                    includeTableRoom6Binding.tvAvatar.setVisibility(View.VISIBLE);
//                    includeTableRoom6Binding.tvAvatar.setText(nameKit.getAvatarName(nameSpilt));
//                    GradientDrawable gradientDrawable = (GradientDrawable) includeTableRoom6Binding.tvAvatar.getBackground();
//                    gradientDrawable.setColor(Color.parseColor(nameKit.getBackgroundColor(nameSpilt)));
//                } else {
//                    includeTableRoom6Binding.civIcon.setVisibility(View.VISIBLE);
//                    includeTableRoom6Binding.tvAvatar.setVisibility(View.INVISIBLE);
//                }
//            }
//            AvatarService.post(getContext(), avatarIds, PicSize.SMALL, includeTableRoom6Binding.civIcon, R.drawable.default_avatar); //大頭
//
//            Set<T> subList = businessRelData.get(key);
//            for (T sub : subList) {
//                if (!Strings.isNullOrEmpty(sub.getBusinessExecutorId())) {
//                    switch (sub.getBusinessCode()) {
//                        case TASK:
//                            taskCount++;
//                            break;
//                        case OPPORTUNITY:
//                            opportunityCount++;
//                            break;
//                        case SERVICE_REQUEST:
//                            serviceRequestCount++;
//                            break;
//                    }
//                }
//            }
//
//            StringBuilder builder = new StringBuilder();
//            if (serviceRequestCount > 0) {
//                builder.append(String.format(BusinessCode.SERVICE_REQUEST.getSimpleName() + " ", serviceRequestCount));
//            }
//            if (opportunityCount > 0) {
//                builder.append(String.format(BusinessCode.OPPORTUNITY.getSimpleName() + " ", opportunityCount));
//            }
//            if (taskCount > 0) {
//                builder.append(String.format(BusinessCode.TASK.getSimpleName() + " ", taskCount));
//            }
//
//            if (t.getServiceNumberType() != null) {
//                switch (t.getServiceNumberType()) {
//                    case BOSS:
//                        includeTableRoom6Binding.tvName.setText(name);
//                        break;
//                    case PROFESSIONAL:
//                        includeTableRoom6Binding.tvName.setText(name);
////                        tvName.setText(TextViewHelper.setLeftImage(getContext(), name, R.drawable.icon_service_number_blue_15dp));
//                        break;
//                    case NONE:
//                    case NORMAL:
//                    default:
//                        includeTableRoom6Binding.tvName.setText(name);
////                        tvName.setText(TextViewHelper.setLeftImage(getContext(), name, R.drawable.icon_subscribe_number_pink_15dp));
//                }
//            } else {
//                includeTableRoom6Binding.tvName.setText(name);
//            }
//            if (builder.length() > 0) {
//                builder.insert(0, "執行中:");
//            }
//
//            includeTableRoom6Binding.tvBusinessContent.setText(TextViewHelper.setLeftImage(getContext(), builder.toString(), R.drawable.icon_link_green_14dp));
//            includeTableRoom6Binding.tvBusinessContent.setTextColor(0xFF6bc2ba);
//
//            String unReadNumber = getParentUnReadNumber(businessRelData.get(key));
//            if (unReadNumber == null) {
//                includeTableRoom6Binding.tvUnread.setVisibility(View.INVISIBLE);
//            } else {
//                includeTableRoom6Binding.tvUnread.setText(unReadNumber);
//                includeTableRoom6Binding.tvUnread.setVisibility(View.VISIBLE);
//            }
//            includeTableRoom6Binding.tvUnread.setVisibility(unReadNumber == null ? View.INVISIBLE : View.VISIBLE);
//
//
//            if (limitOpenDatas.get(key) == null) {
//                this.sAdapter.setLimitedQuantity(0).refreshData();
//                includeTableRoom6Binding.rvSubList.setVisibility(View.GONE);
//                includeTableRoom6Binding.vSubNodeDivider.setVisibility(View.GONE);
//                includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.GONE);
//            } else {
//                includeTableRoom6Binding.tvUnread.setVisibility(View.INVISIBLE);
//                // EVAN_FLAG 2020-11-06 這裡做被打開狀態判斷，並判斷打開數量及剩餘數量顯示展開更多功能。
//                int totalSize = businessRelData.get(key).size();
//                int limit = limitOpenDatas.get(key) != null ? limitOpenDatas.get(key) : 0;
//                if (totalSize < limit) {
//                    limit = totalSize;
//                    limitOpenDatas.put(key, limit);
//                }
//                this.sAdapter.setLimitedQuantity(limit).refreshData();
//                includeTableRoom6Binding.rvSubList.setVisibility(View.VISIBLE);
//                includeTableRoom6Binding.vSubNodeDivider.setVisibility(View.VISIBLE);
//                if (totalSize < 3 || totalSize == limit) {
//                    includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.GONE);
//                } else {
//                    includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.VISIBLE);
//                }
//            }
//
//            includeTableRoom6Binding.clContentCell.setOnClickListener(v -> {
//                closeOther(t.getType(), key).emptyRefreshData();
//                this.sAdapter.itemTouchHelperExtension.closeOpened();
//                this.sAdapter.setData(businessRelData.get(key)).sort();
//                if (this.sAdapter.getItemCount() == 0) {
//                    if (businessRelData.get(key).size() > 3) {
//                        this.sAdapter.setLimitedQuantity(3).refreshData();
//                        includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.VISIBLE);
//                        limitOpenDatas.put(key, LIMIT);
//                    } else {
//                        this.sAdapter.setLimitedQuantity(businessRelData.get(key).size()).refreshData();
//                        includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.GONE);
//                        limitOpenDatas.put(key, businessRelData.get(key).size());
//                    }
//                    includeTableRoom6Binding.rvSubList.setVisibility(View.VISIBLE);
//                    includeTableRoom6Binding.vSubNodeDivider.setVisibility(View.VISIBLE);
//                    includeTableRoom6Binding.tvUnread.setVisibility(View.INVISIBLE);
//                } else if (this.sAdapter.getItemCount() > 0) {
//                    this.sAdapter.setLimitedQuantity(0).refreshData();
//                    includeTableRoom6Binding.rvSubList.setVisibility(View.GONE);
//                    includeTableRoom6Binding.vSubNodeDivider.setVisibility(View.GONE);
//                    includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.GONE);
//                    limitOpenDatas.remove(key);
//                    includeTableRoom6Binding.tvUnread.setVisibility(unReadNumber == null ? View.INVISIBLE : View.VISIBLE);
//                }
//            });
//
//            includeTableRoom6Binding.ivSubNodeCenter.setOnClickListener(v -> {
//                this.sAdapter.itemTouchHelperExtension.closeOpened();
//                this.sAdapter.setLimitedQuantity(businessRelData.get(key).size()).refreshData();
//                limitOpenDatas.put(key, businessRelData.get(key).size());
//                v.setVisibility(View.GONE);
//            });
//        }
//
//        @Override
//        public void move(int from, int to) {
//
//        }
//
//        @Override
//        public void swiped(int flag, String tag, RecyclerView.ViewHolder viewHolder, int direction) {
//            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_SWIPE_MENU_CLOSE_OPEN, tag));
//        }
//    }
//
//
//    public class ServiceSectionedFooterViewHolder extends ItemNoSwipeViewHolder<Sectioned<T, SectionedType, S>> {
//
//        private ItemSectionedFooterBinding binding;
//        public ServiceSectionedFooterViewHolder(ItemSectionedFooterBinding binding) {
//            super(binding.getRoot());
//            this.binding = binding;
//        }
//
//        @Override
//        public void onBind(Sectioned<T, SectionedType, S> sectioned, int section, int position) {
//            super.onBind(sectioned, section, position);
//            boolean notFull = sectioned.getSize() < sectioned.getDatas().size();
//            binding.ivMore.setVisibility(sectioned.isOpen() && sectioned.isHasFooter() && notFull ? View.VISIBLE : View.GONE);
////            ivMore.setVisibility(notFull ? View.VISIBLE : View.GONE);
//
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (notFull) {
//                        int size = Math.min(sectioned.getDatas().size() - sectioned.getSize(), CELL_LIMIT);
//                        sectioned.setSize(sectioned.getSize() + size);
//                        notifyDataSetChanged();
//                    }
//                }
//            });
//        }
//    }
//
//    // 一般聊天室樣式，沒被組合的model
//    // 服務號展開列表下方的顯示, 聊天頁下方的列表
//    public class ServiceGeneralChatRoomViewHolder extends ItemSwipeWithActionWidthViewHolder<T> {
//        private ItemBaseRoom6Binding baseRoom6Binding;
//        private NameKit nameKit = new NameKit();
//
//        public ServiceGeneralChatRoomViewHolder(ItemBaseRoom6Binding binding) {
//            super(binding.getRoot());
//            baseRoom6Binding = binding;
//            super.setMenuViews(baseRoom6Binding.llLeftMenu, baseRoom6Binding.llRightMenu);
//            super.setContentItemView(baseRoom6Binding.clContentItem);
//            baseRoom6Binding.tvBusinessContent.setVisibility(View.GONE);
//            baseRoom6Binding.ivMute.setVisibility(View.GONE);
//        }
//
//        @Override
//        public void onBind(T t, int section, int position) {
//            baseRoom6Binding.clContentCell.setBackgroundResource(ServiceNumberStatus.TIME_OUT.equals(t.getServiceNumberStatus()) ? R.drawable.selector_item_list_timeout : R.drawable.selector_item_list);
//            baseRoom6Binding.ivMute.setImageResource(t.isMute() ? R.drawable.amplification : R.drawable.not_remind);
//            String name = t.getServicesNumberTitle(userId);
//            baseRoom6Binding.tvName.setText(name);
//            if (!ChatRoomType.serviceMember.equals(t.getType())) {
//                UserProfileService.getProfileIsEmployee(context, t.getOwnerId(),
//                        new ServiceCallBack<UserType, RefreshSource>() {
//                            @Override
//                            public void complete(UserType type, RefreshSource refreshSource) {
//                                switch (type) {
//                                    case VISITOR:
//                                        baseRoom6Binding.tvName.setText(
//                                                TextViewHelper.setLeftImage(getContext(), name,
//                                                        R.drawable.ic_visitor_15dp));
//                                        break;
//                                    case CONTACT:
//                                        baseRoom6Binding.tvName.setText(
//                                                TextViewHelper.setLeftImage(getContext(), name,
//                                                        R.drawable.ic_customer_15dp));
//                                        break;
//                                }
//                            }
//
//                            @Override
//                            public void error(String message) {
//                                baseRoom6Binding.tvName.setText(
//                                        TextViewHelper.setLeftImage(getContext(), name,
//                                                R.drawable.ic_customer_15dp));
//                            }
//                        });
//            } else {
//                baseRoom6Binding.tvName.setText(TextViewHelper.setLeftImage(baseRoom6Binding.getRoot().getContext(), name, R.drawable.ic_service_member_group_16dp));
//            }
//            baseRoom6Binding.civIcon.loadAvatarIcon(t.getAvatarId(), t.getName(), t.getId());
//
//            baseRoom6Binding.civSmallIcon.setVisibility(View.GONE);
//            //如果有需要接替則出現
//            if (t.isTransferFlag()) {
//                baseRoom6Binding.txtWaitTransfer.setVisibility(View.VISIBLE);
//            } else {
//                baseRoom6Binding.txtWaitTransfer.setVisibility(View.GONE);
//            }
//            baseRoom6Binding.tvContent.setText(getItemContent(getContext(), t));
//            baseRoom6Binding.tvBusinessContent.setText("");
//
//            String unReadNumber = UnreadUtil.INSTANCE.getUnreadText(t.getUnReadNum());
//            if (Strings.isNullOrEmpty(unReadNumber)) {
//                baseRoom6Binding.tvUnread.setVisibility(View.INVISIBLE);
//            } else {
//                baseRoom6Binding.tvUnread.setText(unReadNumber);
//                baseRoom6Binding.tvUnread.setVisibility(View.VISIBLE);
//            }
////            baseRoom6Binding.tvUnread.setVisibility(unReadNumber == null ? View.INVISIBLE : View.VISIBLE);
//
//            CharSequence sendTime = getItemSendTime(t);
//            baseRoom6Binding.tvTime.setText(sendTime);
//
//            baseRoom6Binding.clContentItem.setOnClickListener(v -> {
//                if (itemTouchHelperExtension != null) {
//                    itemTouchHelperExtension.closeOpened();
//                }
//                if (onRoomItemClickListener != null) {
//                    onRoomItemClickListener.onItemClick(t);
//                }
//            });
//
//            if (t.isTop()) {
//                baseRoom6Binding.ivTop.setVisibility(View.VISIBLE);
//                baseRoom6Binding.ivTop.setImageResource(R.drawable.ic_no_top);
//                baseRoom6Binding.ivTop.setOnClickListener(v -> {
//                    if (itemTouchHelperExtension != null) {
//                        itemTouchHelperExtension.closeOpened();
//                    }
//                    if (onSwipeMenuListener != null) {
//                        onSwipeMenuListener.onSwipeMenuClick(t, Menu.TOP, position);
//                    }
//                });
//            } else {
//                baseRoom6Binding.ivTop.setVisibility(View.GONE);
//                baseRoom6Binding.ivTop.setOnClickListener(null);
//            }
//
//            baseRoom6Binding.tvSetupUnreadTag.setText(StringHelper.autoNewLine(getContext(), R.string.alert_notes, R.string.room_cell_swipe_menu_setup_untreated));
////            baseRoom6Binding.tvSetupUnreadTag.setVisibility(t.getUnReadNum() != 0 ? View.GONE : View.VISIBLE);
//            baseRoom6Binding.tvSetupUnreadTag.setVisibility(View.GONE); //隱藏不需要
//            baseRoom6Binding.ivDelete.setVisibility(View.GONE);//隱藏不需要
//            baseRoom6Binding.tvSetupUnreadTag.setOnClickListener(t.getUnReadNum() != 0 ? null : (View.OnClickListener) v -> {
//                if (itemTouchHelperExtension != null) {
//                    itemTouchHelperExtension.closeOpened();
//                }
//                if (onSwipeMenuListener != null) {
//                    onSwipeMenuListener.onSwipeMenuClick(t, Menu.SETUP_UNREAD_TAG, position);
//                }
//            });
//
//            baseRoom6Binding.ivDelete.setOnClickListener(view -> {
//                if (itemTouchHelperExtension != null) {
//                    itemTouchHelperExtension.closeOpened();
//                }
//                if (onSwipeMenuListener != null) {
//                    onSwipeMenuListener.onSwipeMenuClick(t, Menu.DELETE, position);
//                }
//            });
//        }
//    }
//
//    private String getParentUnReadNumber(Set<T> list) {
//        int unReadNumber = 0;
//        for (T t : list) {
//            unReadNumber += Math.abs(t.getUnReadNum());
//            unReadNumber += Math.abs(t.getConsultSrcUnreadNumber());
//        }
//        return UnreadUtil.INSTANCE.getUnreadText(unReadNumber);
//    }
//
//    private ChannelType setChannelIcon(CircleImageView icon, ChatRoomEntity t) {
//        icon.setVisibility(View.GONE);
//        if (t == null) {
//            return null;
//        }
//
//        if (t.getLastMessage() == null) {
//            return null;
//        }
//
//        if (t.getLastMessage().getFrom() == null) {
//            return null;
//        }
//
//        icon.setVisibility(View.VISIBLE);
//        switch (t.getLastMessage().getFrom()) {
//            case FB:
//                icon.setImageResource(R.drawable.ic_fb);
//                break;
//            case LINE:
//                icon.setImageResource(R.drawable.ic_line);
//                break;
//            case QBI:
//            case AILE_WEB_CHAT:
//                icon.setImageResource(R.drawable.qbi_icon);
//                break;
//            case WEICHAT:
//                icon.setImageResource(R.drawable.wechat_icon);
//                break;
//            case IG:
//                icon.setImageResource(R.drawable.ic_ig);
//                break;
//            case GOOGLE:
//                icon.setImageResource(R.drawable.ic_google_message);
//                break;
//            case CE:
//                icon.setImageResource(R.drawable.ce_icon);
//                break;
//            case UNDEF:
//            default:
//                icon.setVisibility(View.GONE);
//                break;
//        }
//
//        return t.getLastMessage().getFrom();
////        CircleImageView civSmallIcon;
//    }
//
//    private CharSequence getItemContent(Context context, T t) {
//        if (t == null) {
//            return new SpannableString("");
//        }
//        MessageEntity failedMessage = t.getFailedMessage();
//        // sort index == 0
//        if (failedMessage != null) {
//            SpannableStringBuilder builder = new SpannableStringBuilder("");
//            switch (failedMessage.getType()) {
//                case AT:
//                    builder = AtMatcherHelper.matcherAtUsers("@", ((AtContent) failedMessage.content()).getMentionContents(), t.getMembersTable());
//                    break;
//                default:
//                    builder.append(failedMessage.content().simpleContent());
//            }
//            return TextViewHelper.setLeftImage(getContext(), builder, R.drawable.ic_mes_failure_14dp);
//        }
//
//        // sort index == 1
//        InputLogBean bean = InputLogBean.from(t.getUnfinishedEdited());
//        if (!Strings.isNullOrEmpty(bean.getText())) {
//            return AtMatcherHelper.setLeftImageAndHighLightAt(getContext(), bean.getText(), R.drawable.ic_edit_gray_14dp, t.getMembersLinkedList(), InputLogType.AT.equals(bean.getType()) ? (ChatRoomType.subscribe.equals(t.getType()) ? 0xFF8F8E94 : 0xFF4A90E2) : 0xFF8F8E94);
//        }
//
//        // sort index == 2
//        MessageEntity lastMessageEntity = t.getLastMessage();
//        if (lastMessageEntity != null) {
//            String sendId = lastMessageEntity.getSenderId();
//            UserProfileEntity userProfile = DBManager.getInstance().queryFriend(sendId);
//            String selfId = TokenPref.getInstance(context).getUserId();
//            SourceType sourceType = lastMessageEntity.getSourceType();
//            String sendName = "";
//            // 系統訊息
//            if (SourceType.SYSTEM.equals(sourceType)) {
//                return lastMessageEntity.getContent();
//            }
//
//            if (selfId.equals(sendId)) {
//                sendName = "我: ";
//            } else {
//                if (userProfile != null && userProfile.getAlias() != null && !userProfile.getAlias().isEmpty()) {
//                    sendName = userProfile.getAlias();
//                } else if(lastMessageEntity.getSenderName() != null && !lastMessageEntity.getSenderName().isEmpty()){
//                    sendName = lastMessageEntity.getSenderName();
//                }else {
//                    UserProfileEntity profile = DBManager.getInstance().queryFriend(
//                            lastMessageEntity.getSenderId());
//                    if (profile != null) {
//                        sendName = ChatRoomType.subscribe.equals(t.getType()) ? t.getName()
//                                : !Strings.isNullOrEmpty(profile.getAlias()) ? profile.getAlias()
//                                : profile.getNickName();
//                    } else {
//                        sendName = t.getName();
//                    }
//                }
//                sendName += ": ";
//            }
//
//            if (MessageFlag.RETRACT.equals(lastMessageEntity.getFlag())) {
//                return new SpannableString(sendName + Constant.RETRACT_MSG);
//                } else {
//                SpannableStringBuilder builder = new SpannableStringBuilder("");
//                switch (lastMessageEntity.getType()) {
//                    case AT:
//                        AtContent atContent = (AtContent) lastMessageEntity.content();
//                        builder = AtMatcherHelper.matcherAtUsers("@",
//                                atContent.getMentionContents(), t.getMembersTable());
//                        break;
//                    default:
//                            builder.append(lastMessageEntity.content().simpleContent());
//                            //CELog.d("Kyle1 content="+lastMessageEntity.content().simpleContent()+", type="+lastMessageEntity.getType().name());
//                }
//                builder.insert(0, sendName);
//                return builder;
//            }
//        }
//        return new SpannableString("");
//    }
//
//    private CharSequence getItemSendTime(T t) {
//        if (t == null) {
//            return "";
//        }
//        MessageEntity lastMessageEntity = t.getLastMessage();
//        if (t.getLastMessage() != null) {
//            if (lastMessageEntity.getSendTime() > 0) {
//                return TimeUtil.INSTANCE.getTimeShowString(lastMessageEntity.getSendTime(), true);
//            }
//        }
//        return "";
//    }
//
//    /**
//     * // selfId == AgentId            -->  selfId.equals(entity.getServiceNumberAgentId())
//     * // isProfessional               -->  ServiceNumberType.PROFESSIONAL.equals(entity.getServiceNumberType())
//     * // status is online or timeout  -->  ServiceNumberStatus.ON_LINE_or_TIME_OUT.equals(entity.getServiceNumberStatus())
//     */
//    protected void handleMyService(String selfId, List<T> entities, List<T> myServiceEntities) {
//        Iterator<T> iterator = entities.iterator();
//        while (iterator.hasNext()) {
//            T t = iterator.next();
//            boolean isApprove = false;
//            if (t.getUnReadNum() == -1 &&
//                    ServiceNumberStatus.OFF_LINE_or_TIME_OUT.contains(t.getServiceNumberStatus())) {
//                isApprove = true;
//            } else if (selfId.equals(t.getServiceNumberAgentId()) &&
//                    ServiceNumberType.PROFESSIONAL.equals(t.getServiceNumberType()) &&
//                    ServiceNumberStatus.ON_LINE.equals(t.getServiceNumberStatus()) &&
//                    t.getUnReadNum() >= 0) {
//                isApprove = true;
//            } else if (selfId.equals(t.getServiceNumberAgentId()) &&
//                    ServiceNumberType.PROFESSIONAL.equals(t.getServiceNumberType()) &&
//                    ServiceNumberStatus.TIME_OUT.equals(t.getServiceNumberStatus()) &&
//                    t.getUnReadNum() > 0) {
//                isApprove = true;
//            }
//            if (isApprove) {
//                myServiceEntities.add(t);
//                iterator.remove();
//            }
//        }
//    }
//
//    /**
//     * // AgentId != null or ''        -->  !Strings.isNullOrEmpty(entity.getServiceNumberAgentId())
//     * // status is online or timeout  -->  ServiceNumberStatus.ON_LINE_or_TIME_OUT.contains(entity.getServiceNumberStatus())
//     * // unread number > 0            -->  entity.getUnReadNum() > 0
//     * // or
//     * // AgentId != null or ''        -->  !Strings.isNullOrEmpty(entity.getServiceNumberAgentId())
//     * // status is online             -->  ServiceNumberStatus.ON_LINE.equals(entity.getServiceNumberStatus())
//     * // unread number == 0           -->  entity.getUnReadNum() == 0
//     */
//    private void handleOthersService(String selfId, List<T> entities, List<T> othersServiceEntities) {
//        Iterator<T> iterator = entities.iterator();
//        while (iterator.hasNext()) {
//            T t = iterator.next();
//            boolean isApprove = false;
//            if (selfId.equals(t.getServiceNumberAgentId()) &&
//                    !ServiceNumberType.PROFESSIONAL.equals(t.getServiceNumberType()) &&
//                    ServiceNumberStatus.ON_LINE.equals(t.getServiceNumberStatus()) &&
//                    t.getUnReadNum() >= 0) {
//                isApprove = true;
//            } else if (selfId.equals(t.getServiceNumberAgentId()) &&
//                    !ServiceNumberType.PROFESSIONAL.equals(t.getServiceNumberType()) &&
//                    ServiceNumberStatus.TIME_OUT.equals(t.getServiceNumberStatus()) &&
//                    t.getUnReadNum() > 0) {
//                isApprove = true;
//            } else if (!Strings.isNullOrEmpty(t.getServiceNumberAgentId()) &&
//                    ServiceNumberStatus.ON_LINE.equals(t.getServiceNumberStatus()) &&
//                    t.getUnReadNum() >= 0) {
//                isApprove = true;
//            } else if (!Strings.isNullOrEmpty(t.getServiceNumberAgentId()) &&
//                    ServiceNumberStatus.TIME_OUT.equals(t.getServiceNumberStatus()) &&
//                    t.getUnReadNum() > 0) {
//                isApprove = true;
//            }
//
//            if (isApprove) {
//                othersServiceEntities.add(t);
//                iterator.remove();
//            }
//        }
//    }
//
//    /**
//     * // AgentId == null or ''        -->  Strings.isNullOrEmpty(entity.getServiceNumberAgentId())
//     * // status is online or timeout  -->  ServiceNumberStatus.ON_LINE_or_TIME_OUT.contains(entity.getServiceNumberStatus())
//     * // unread number >= 0           -->  entity.getUnReadNum() >= 0
//     */
//    private void handleUnread(List<T> entities, List<T> unreadEntities) {
//        Iterator<T> iterator = entities.iterator();
//        while (iterator.hasNext()) {
//            T t = iterator.next();
//            boolean isApprove = false;
//            if (Strings.isNullOrEmpty(t.getServiceNumberAgentId()) &&
//                    ServiceNumberStatus.ON_LINE.equals(t.getServiceNumberStatus()) &&
//                    t.getUnReadNum() >= 0) {
//                isApprove = true;
//            } else if (Strings.isNullOrEmpty(t.getServiceNumberAgentId()) &&
//                    ServiceNumberStatus.TIME_OUT.equals(t.getServiceNumberStatus()) &&
//                    t.getUnReadNum() > 0) {
//                isApprove = true;
//            }
//
//            if (isApprove) {
//                unreadEntities.add(t);
//                iterator.remove();
//            }
////            if (Strings.isNullOrEmpty(t.getServiceNumberAgentId()) &&
////                    ServiceNumberStatus.ON_LINE_or_TIME_OUT.contains(t.getServiceNumberStatus()) &&
////                    Math.abs(t.getUnReadNum()) > 0
////            ) {
////                unreadEntities.add(t);
////                iterator.remove();
////            }
//        }
//    }
//
//    protected synchronized boolean successivelyHandleMyService(String selfId, T t, List<T> myServiceEntities) {
//        boolean isApprove = false;
//        if (t.getUnReadNum() == -1 &&
//                ServiceNumberStatus.OFF_LINE_or_TIME_OUT.contains(t.getServiceNumberStatus())) {
//            isApprove = true;
//        } else if (selfId.equals(t.getServiceNumberAgentId()) &&
//                ServiceNumberType.PROFESSIONAL.equals(t.getServiceNumberType()) &&
//                ServiceNumberStatus.ON_LINE.equals(t.getServiceNumberStatus()) &&
//                t.getUnReadNum() >= 0) {
//            isApprove = true;
//        } else if (selfId.equals(t.getServiceNumberAgentId()) &&
//                ServiceNumberType.PROFESSIONAL.equals(t.getServiceNumberType()) &&
//                ServiceNumberStatus.TIME_OUT.equals(t.getServiceNumberStatus()) &&
//                t.getUnReadNum() > 0) {
//            isApprove = true;
//        } else if (selfId.equals(t.getServiceNumberAgentId()) &&
//                ServiceNumberStatus.OFF_LINE.equals(t.getServiceNumberStatus()) &&
//                (t.getLastMessage()!=null && !Strings.isNullOrEmpty(t.getLastMessage().getContent()) && !t.getLastMessage().getContent().contains(context.getString(R.string.text_service_time_out))) &&
//                t.getUnReadNum() >= 0) {
//            isApprove = true;
//
//        }
//        if (isApprove) {
//            myServiceEntities.add(t);
//        }
//        return isApprove;
//    }
//
//    protected synchronized boolean successivelyHandleOthersService(String selfId, T t, List<T> othersServiceEntities) {
//        boolean isApprove = false;
//        if (selfId.equals(t.getServiceNumberAgentId()) &&
//                !ServiceNumberType.PROFESSIONAL.equals(t.getServiceNumberType()) &&
//                ServiceNumberStatus.ON_LINE.equals(t.getServiceNumberStatus()) &&
//                t.getUnReadNum() >= 0) {
//            isApprove = true;
//        } else if (selfId.equals(t.getServiceNumberAgentId()) &&
//                !ServiceNumberType.PROFESSIONAL.equals(t.getServiceNumberType()) &&
//                ServiceNumberStatus.TIME_OUT.equals(t.getServiceNumberStatus()) &&
//                t.getUnReadNum() > 0) {
//            isApprove = true;
//        } else if (!Strings.isNullOrEmpty(t.getServiceNumberAgentId()) &&
//                ServiceNumberStatus.ON_LINE.equals(t.getServiceNumberStatus()) &&
//                t.getUnReadNum() >= 0) {
//            isApprove = true;
//        } else if (!Strings.isNullOrEmpty(t.getServiceNumberAgentId()) &&
//                ServiceNumberStatus.TIME_OUT.equals(t.getServiceNumberStatus()) &&
//                t.getUnReadNum() > 0) {
//            isApprove = true;
//        } else if(!Strings.isNullOrEmpty(t.getServiceNumberAgentId()) &&
//                !selfId.equals(t.getServiceNumberAgentId()) &&
//                t.getUnReadNum() >= 0 &&
//                ServiceNumberStatus.ON_LINE.equals(t.getServiceNumberStatus()) &&
//                t.getServiceNumberType().equals(ServiceNumberType.BOSS)
//        ) {
//            //條件∶商務號擁有者接手服務，其他成員顯示服務中
//            isApprove = true;
//        }
//
//        if (isApprove) {
//            othersServiceEntities.add(t);
//        }
//        return isApprove;
//    }
//
//    protected synchronized boolean successivelyHandleUnread(T t, List<T> unreadEntities) {
//        boolean isApprove = false;
//        //商務號的擁有者 不會出現在服務號列表
//        if (ServiceNumberType.BOSS.equals(t.getServiceNumberType())) {
//            //是商務號且不是擁有者
//            if(!Strings.isNullOrEmpty(t.getServiceNumberOwnerId())) {
//                if (Strings.isNullOrEmpty(t.getServiceNumberAgentId()) &&
//                        !t.getServiceNumberOwnerId().equals(userId)) {
//                    if (ServiceNumberStatus.ON_LINE.equals(t.getServiceNumberStatus()) && t.getUnReadNum() >= 0) {
//                        isApprove = true;
//                    } else if (ServiceNumberStatus.TIME_OUT.equals(t.getServiceNumberStatus()) && t.getUnReadNum() > 0) {
//                        isApprove = true;
//                    }
//                }
//            }
//        } else {
//            //不是商務號
//            if (Strings.isNullOrEmpty(t.getServiceNumberAgentId())) {
//                if (ServiceNumberStatus.ON_LINE.equals(t.getServiceNumberStatus()) && t.getUnReadNum() >= 0) {
//                    isApprove = true;
//                } else if (ServiceNumberStatus.TIME_OUT.equals(t.getServiceNumberStatus()) && t.getUnReadNum() > 0) {
//                    isApprove = true;
//                }
//            }
//        }
//        if (isApprove) {
//            unreadEntities.add(t);
//        }
//        return isApprove;
//    }
//
//    protected synchronized boolean successivelyHandleRobotServicing(T t, List<T> robotServiceEntities) {
//        boolean isApprove = ServiceNumberStatus.ROBOT_SERVICE.equals(t.getServiceNumberStatus()) &&
//                !t.deleted && !t.isWarned() && Strings.isNullOrEmpty(t.getServiceNumberAgentId());
//
//        if (isApprove) {
//            robotServiceEntities.add(t);
//            Collections.sort(robotServiceEntities);
//        }
//        return isApprove;
//    }
//
//    protected synchronized boolean successivelyHandleMonitorAIServicing(T t, List<T> monitorRobotServiceEntities) {
//        boolean isApprove = ServiceNumberStatus.ROBOT_SERVICE.equals(t.getServiceNumberStatus()) &&
//                !t.deleted && t.isWarned() && Strings.isNullOrEmpty(t.getServiceNumberAgentId());
//
//        if (isApprove) {
//            monitorRobotServiceEntities.add(t);
//            Collections.sort(monitorRobotServiceEntities);
//        }
//        return isApprove;
//    }
//}
