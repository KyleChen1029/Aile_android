package tw.com.chainsea.chat.view.roomList.mainRoomList.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.View;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.text.StringHelper;
import tw.com.chainsea.ce.sdk.bean.InputLogBean;
import tw.com.chainsea.ce.sdk.bean.InputLogType;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.bean.business.BusinessCode;
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.cache.ChatMemberCacheService;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.ce.sdk.service.UserProfileService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.databinding.ItemBaseRoom6Binding;
import tw.com.chainsea.chat.databinding.ItemIncludeTableRoom6Binding;
import tw.com.chainsea.chat.databinding.ItemSunRoom5Binding;
import tw.com.chainsea.chat.lib.AtMatcherHelper;
import tw.com.chainsea.chat.util.AvatarKit;
import tw.com.chainsea.chat.util.NameKit;
import tw.com.chainsea.chat.util.TextViewHelper;
import tw.com.chainsea.chat.util.TimeUtil;
import tw.com.chainsea.chat.util.Unit;
import tw.com.chainsea.chat.view.roomList.mainRoomList.listener.OnRoomItem3ClickListener;
import tw.com.chainsea.chat.view.roomList.type.Menu;
import tw.com.chainsea.chat.view.roomList.type.SectionedType;
import tw.com.chainsea.custom.view.image.CircleImageView;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemNoSwipeViewHolder;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemSwipeWithActionWidthViewHolder;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperCallback;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension;
import tw.com.chainsea.custom.view.recyclerview.lintener.OnSwipeMenuListener;

/**
 * current by evan on 2020-11-04
 *
 * @author Evan Wang
 * date 2020-11-04
 */
public abstract class BaseRoomList3Adapter<T extends ChatRoomEntity> extends RecyclerView.Adapter<ItemBaseViewHolder<T>> {
    protected Context context;
    protected String userId;
    private RecyclerView.RecycledViewPool sharedPool;
    private OnRoomItem3ClickListener<ChatRoomEntity> onRoomItemClickListener;
    private OnSwipeMenuListener<ChatRoomEntity, Menu> onSwipeMenuListener;

    protected List<T> metadata = Lists.newArrayList();
    protected List<T> entities = Lists.newArrayList();

    protected SetMultimap<String, T> businessSelfRelData = HashMultimap.create();
    protected SetMultimap<String, T> businessExecutorRelData = HashMultimap.create();
    protected SetMultimap<String, T> businessSubscribeRelData = HashMultimap.create();

    public ItemTouchHelperExtension itemTouchHelperExtension;

    private final Set<BaseRoomList3Adapter<T>> subAdapterSet = Sets.newHashSet();
    private final Map<String, Integer> limitOpenDatas = Maps.newHashMap();
    private static final int LIMIT = 3;

    public int limitedQuantity = -1;
    private String bindKey = null;

    protected boolean whetherToAssemble = true;
    protected boolean filterConsult = true;

    public boolean isBindKeyBy(String bindKey) {
        if (Strings.isNullOrEmpty(this.bindKey)) {
            return false;
        }
        return this.bindKey.equals(bindKey);
    }

    public String getBindKey() {
        return this.bindKey;
    }

    public BaseRoomList3Adapter() {

    }

    public BaseRoomList3Adapter(Context context) {
        setContext(context);
    }

    public BaseRoomList3Adapter(Context context, List<T> list) {
        this(context);
        this.metadata = list;
    }

    protected void setContext(Context context) {
        this.context = context;
        if (Strings.isNullOrEmpty(this.userId)) {
            this.userId = TokenPref.getInstance(context).getUserId();
        }
    }

    protected Context getContext() {
        return this.context;
    }

    protected String getUserId() {
        return this.userId;
    }

    public BaseRoomList3Adapter<T> setWhetherToAssemble(boolean whetherToAssemble) {
        this.whetherToAssemble = whetherToAssemble;
        return this;
    }

    public BaseRoomList3Adapter<T> setFilterConsult(boolean filterConsult) {
        this.filterConsult = filterConsult;
        return this;
    }

    public BaseRoomList3Adapter<T> setOnRoomItemClickListener(OnRoomItem3ClickListener<ChatRoomEntity> onRoomItemClickListener) {
        this.onRoomItemClickListener = onRoomItemClickListener;
        return this;
    }

    public BaseRoomList3Adapter<T> setSharedPool(RecyclerView.RecycledViewPool sharedPool) {
        this.sharedPool = sharedPool;
        return this;
    }

    public BaseRoomList3Adapter<T> setOnSwipeMenuListener(OnSwipeMenuListener<ChatRoomEntity, Menu> onSwipeMenuListener) {
        this.onSwipeMenuListener = onSwipeMenuListener;
        return this;
    }

    public BaseRoomList3Adapter<T> setData(List<T> metadata) {
        this.metadata = metadata;
        return this;
    }

    public BaseRoomList3Adapter<T> setData(Set<T> metadata) {
        this.metadata = Lists.newArrayList(metadata);
        return this;
    }

    public BaseRoomList3Adapter<T> bindKey(String bindKey) {
        this.bindKey = bindKey;
        return this;
    }

    public BaseRoomList3Adapter<T> setLimitedQuantity(int limitedQuantity) {
        this.limitedQuantity = limitedQuantity;
        return this;
    }

    public BaseRoomList3Adapter<T> setData(RefreshSource source, List<T> list) {
        switch (source) {
            case LOCAL:
                for (T t : list) {
                    if (this.metadata.contains(t)) {
                    } else {
                        this.metadata.add(t);
                    }
                }
                break;
            case REMOTE:
                for (T t : list) {
                    this.metadata.remove(t);
                    this.metadata.add(t);
                }
                break;
        }
        return this;
    }

    public BaseRoomList3Adapter<T> addData(T t) {
        this.metadata.remove(t);
        this.metadata.add(t);
        return this;
    }

    public BaseRoomList3Adapter<T> setItemTouchHelperExtension(ItemTouchHelperExtension itemTouchHelperExtension) {
        this.itemTouchHelperExtension = itemTouchHelperExtension;
        return this;
    }

    public BaseRoomList3Adapter<T> closeOther(ChatRoomType type, String key) {
        if (Strings.isNullOrEmpty(key)) {
            limitOpenDatas.clear();
            for (BaseRoomList3Adapter<T> a : subAdapterSet) {
                if (ChatRoomType.discuss.equals(type)) {
                } else if (ChatRoomType.subscribe.equals(type)) {
                    a.setData(businessSubscribeRelData.get(a.getBindKey()));
                } else {
                    a.setData(businessExecutorRelData.get(a.getBindKey()));
                }
                a.setLimitedQuantity(0);
            }
        } else {

            for (BaseRoomList3Adapter<T> a : subAdapterSet) {
                if (!a.isBindKeyBy(key)) {
                    if (ChatRoomType.discuss.equals(type)) {
                    } else if (ChatRoomType.subscribe.equals(type)) {
                        a.setData(businessSubscribeRelData.get(a.getBindKey()));
                    } else {
                        a.setData(businessExecutorRelData.get(a.getBindKey()));
                    }
                    a.setLimitedQuantity(0);
                }

            }
            int limit = limitOpenDatas.get(key) != null ? limitOpenDatas.get(key) : 0;
            limitOpenDatas.clear();
            limitOpenDatas.put(key, limit);
        }
        return this;
    }

    public void closeOpenedChild(String key) {
        for (BaseRoomList3Adapter<T> a : subAdapterSet) {
            if (!a.isBindKeyBy(key)) {
                a.emptyRefreshData();
            } else {
                CELog.e("");
            }
        }
    }

    public abstract void sort();

    protected abstract void filter();

    protected abstract void pullAway();

    protected abstract void group();

    @SuppressLint("NotifyDataSetChanged")
    public void refreshData() {
        filter();
        sort();
        pullAway();
        group();
        notifyDataSetChanged();
        if (!subAdapterSet.isEmpty()) {
            for (BaseRoomList3Adapter<T> a : subAdapterSet) {
                a.refreshData();
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void emptyRefreshData() {
        notifyDataSetChanged();
    }

    abstract public void handleEvent(EventMsg eventMsg);

    /**
     * mapping layout @link R.layout.item_base_room_5
     * 聊天列表
     */
    public class GeneralChatRoomViewHolder extends ItemSwipeWithActionWidthViewHolder<T> {
        ItemBaseRoom6Binding baseRoom6Binding;

        public GeneralChatRoomViewHolder(ItemBaseRoom6Binding binding) {
            super(binding.getRoot());
            baseRoom6Binding = binding;
            super.setMenuViews(baseRoom6Binding.llLeftMenu, baseRoom6Binding.llRightMenu);
            super.setContentItemView(baseRoom6Binding.clContentItem);
            baseRoom6Binding.tvBusinessContent.setVisibility(View.GONE);
        }

        @Override
        public void onBind(T t, int section, int position) {
            if (t.getLastMessage() != null && MessageType.AT.equals(t.getLastMessage().getType())) {
                if (t.getMembers() == null || t.getMembers().isEmpty()) {
                    t.setMembers(ChatMemberCacheService.getChatMember(t.getId()));
                }
            }

            if (ChatRoomType.discuss.equals(t.getType()) || (ChatRoomType.group.equals(t.getType()) && Strings.isNullOrEmpty(t.getAvatarId()))) {
                if (t.getMemberAvatarData() == null || t.getMemberAvatarData().isEmpty()) {
                    t.setMemberAvatarData(UserProfileReference.getMemberAvatarData(null, t.getId(), getUserId(), 4));
                }
            }

            String name = t.getTitle(getContext());
            if (!Strings.isNullOrEmpty(t.getBusinessId()) && ChatRoomType.FRIEND_or_SUBSCRIBE.contains(t.getType())) {
                name = String.format("%s", name);
            } else if (!Strings.isNullOrEmpty(t.getBusinessId()) && ChatRoomType.FRIEND_or_SUBSCRIBE.contains(t.getType())) {
                name = t.getBusinessName();
            }

            if (ChatRoomType.person.equals(t.getType())) {
                baseRoom6Binding.ivDelete.setVisibility(View.GONE);
            } else {
                baseRoom6Binding.ivDelete.setVisibility(View.VISIBLE);
            }

            boolean isTop = t.isTop();
            boolean isMute = t.isMute();
            baseRoom6Binding.ivRemind.setVisibility(!t.isMute() ? View.GONE : View.VISIBLE);
            Set<UserProfileEntity> members = Sets.newHashSet(t.getMembers());

            baseRoom6Binding.clContentItem.setBackgroundResource(R.drawable.selector_item_list);
            baseRoom6Binding.civIcon.setImageResource(R.drawable.custom_default_avatar);
            baseRoom6Binding.civIcon.loadAvatarIcon(t.getAvatarId(), t.getName(), t.getId());

            if (isTop) {
                baseRoom6Binding.civSmallIcon.setVisibility(View.VISIBLE);
                baseRoom6Binding.civSmallIcon.setImageResource(R.drawable.ic_s_top);
            } else {
                baseRoom6Binding.civSmallIcon.setVisibility(View.GONE);
            }
            baseRoom6Binding.ivTop.setImageResource(isTop ? R.drawable.ic_no_top : R.drawable.ic_top);
            baseRoom6Binding.ivMute.setImageResource(isMute ? R.drawable.amplification : R.drawable.not_remind);

            CharSequence content = getItemContent(getContext(), t);
            baseRoom6Binding.tvContent.setText(content);

            CharSequence sendTime = getItemSendTime(t);
            baseRoom6Binding.tvTime.setText(sendTime);

            String unReadNumber = Unit.getUnReadNumber(t);
            if (unReadNumber == null) {
                baseRoom6Binding.tvUnread.setVisibility(View.INVISIBLE);
            } else {
                baseRoom6Binding.tvUnread.setText(unReadNumber);
                baseRoom6Binding.tvUnread.setVisibility(View.VISIBLE);
            }

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(StringHelper.getString(name, ""));
//         判斷為社團 or 群組才顯示人數
            if (ChatRoomType.GROUP_or_DISCUSS.contains(t.getType())) {
                builder.append(!t.getMemberIds().isEmpty() ? " (" + t.getMemberIds().size() + ")" : "");
            }

            // 判斷訂閱號或非員工給不同頭像外框色
            baseRoom6Binding.civIcon.setBorder(0, 0);
            boolean hasEmployees = true;
            for (UserProfileEntity a : members) {
                if (!UserType.EMPLOYEE.equals(a.getUserType()) && !a.isHardCode()) {
                    hasEmployees = false;
                }
            }
            if (!hasEmployees && ChatRoomType.friend.equals(t.getType()) && Strings.isNullOrEmpty(t.getBusinessId())) {
                SpannableString spannable = TextViewHelper.setLeftImage(getContext(), builder.toString(), R.drawable.ic_customer_15dp);
                builder.clear();
                builder.append(spannable);
            } else if (ChatRoomType.serviceMember.equals(t.getType())) {
                SpannableString spannable = TextViewHelper.setLeftImage(getContext(), builder + "和祕書群", R.drawable.ic_service_member_b);
                builder.clear();
                builder.append(spannable);
            }


            baseRoom6Binding.tvName.setText(builder);

            baseRoom6Binding.ivFavourite.setVisibility(t.isFavourite() && Strings.isNullOrEmpty(t.getBusinessId()) ? View.VISIBLE : View.INVISIBLE);

            /*---------  Event Binding  ----------*/
            baseRoom6Binding.clContentItem.setOnClickListener(v -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onRoomItemClickListener != null) {
                    onRoomItemClickListener.onItemClick(t);
                }
            });

            baseRoom6Binding.ivTop.setOnClickListener(view -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, Menu.TOP, position);
                }
            });

            baseRoom6Binding.ivMute.setOnClickListener(v -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, Menu.MUTE, position);
                }
            });

            baseRoom6Binding.tvSetupUnreadTag.setText(StringHelper.autoNewLine(getContext(), R.string.alert_notes, t.getUnReadNum() == 0 ? R.string.room_cell_swipe_menu_setup_unread : R.string.room_cell_swipe_menu_setup_read));
            baseRoom6Binding.tvSetupUnreadTag.setBackgroundColor(t.getUnReadNum() == 0 ? context.getColor(R.color.btn_yellow) : 0xFF88B1DE);
            baseRoom6Binding.tvSetupUnreadTag.setOnClickListener(v -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, t.getUnReadNum() == 0 ? Menu.SETUP_UNREAD_TAG : Menu.SETUP_READ, position);
                }
            });

            if (ChatRoomType.serviceMember.equals(t.getType())) {
                baseRoom6Binding.ivDelete.setVisibility(View.GONE);
                baseRoom6Binding.ivDelete.setOnClickListener(null);
            } else {
                baseRoom6Binding.ivDelete.setVisibility(View.VISIBLE);
                baseRoom6Binding.ivDelete.setOnClickListener(view -> {
                    if (itemTouchHelperExtension != null) {
                        itemTouchHelperExtension.closeOpened();
                    }
                    if (onSwipeMenuListener != null) {
                        onSwipeMenuListener.onSwipeMenuClick(t, Menu.DELETE, position);
                    }
                });
            }
            baseRoom6Binding.clContentCell.setBackgroundResource(0);
        }
    }

    /**
     * mapping layout @link R.layout.item_base_room_5
     */
    public class BossChatRoomViewHolder extends ItemSwipeWithActionWidthViewHolder<T> {
        ItemBaseRoom6Binding baseRoom6Binding;

        public BossChatRoomViewHolder(ItemBaseRoom6Binding binding) {
            super(binding.getRoot());
            baseRoom6Binding = binding;
            super.setMenuViews(baseRoom6Binding.llLeftMenu, baseRoom6Binding.llRightMenu);
            super.setContentItemView(baseRoom6Binding.clContentItem);
        }

        @Override
        public void onBind(T t, int section, int position) {
            baseRoom6Binding.tvTime.setVisibility(View.VISIBLE);
            baseRoom6Binding.tvContent.setVisibility(View.VISIBLE);
            String name = t.getTitle(getContext());

            boolean isTop = t.isTop();
            boolean isMute = t.isMute();

            baseRoom6Binding.ivRemind.setVisibility(!t.isMute() ? View.GONE : View.VISIBLE);
            String businessName = t.getBusinessName();

            baseRoom6Binding.clContentItem.setBackgroundResource(R.drawable.selector_item_list);
            if (t.getType() == ChatRoomType.discuss) {
                if (t.getChatRoomMember() != null)
                    baseRoom6Binding.civIcon.getChatRoomMemberIdsAndLoadMultiAvatarIcon(t.getChatRoomMember(), t.getId());
                else if (t.getMemberIds() != null)
                    baseRoom6Binding.civIcon.loadMultiAvatarIcon(t.getMemberIds(), t.getId());
            } else {
                baseRoom6Binding.civIcon.loadAvatarIcon(t.getAvatarId(), t.getName(), t.getId());
            }
            if (isTop) {
                baseRoom6Binding.civSmallIcon.setImageResource(R.drawable.ic_s_top);
                baseRoom6Binding.civSmallIcon.setVisibility(View.VISIBLE);
            } else {
                baseRoom6Binding.civSmallIcon.setVisibility(View.GONE);
            }
            baseRoom6Binding.tvName.setText(name);
            UserProfileService.getProfileIsEmployee(context, t.getOwnerId(), new ServiceCallBack<UserType, RefreshSource>() {
                @Override
                public void complete(UserType type, RefreshSource refreshSource) {
                    switch (type) {
                        case VISITOR:
                            baseRoom6Binding.tvName.setText(TextViewHelper.setLeftImage(getContext(), name, R.drawable.ic_visitor_15dp));
                            break;
                        case CONTACT:
                            baseRoom6Binding.tvName.setText(TextViewHelper.setLeftImage(getContext(), name, R.drawable.ic_customer_15dp));
                            break;
                    }
                }

                @Override
                public void error(String message) {
                    baseRoom6Binding.tvName.setText(TextViewHelper.setLeftImage(getContext(), name, R.drawable.ic_customer_15dp));
                }
            });

            CharSequence content = getItemContent(getContext(), t);
            baseRoom6Binding.tvContent.setText(content);

            CharSequence sendTime = getItemSendTime(t);
            baseRoom6Binding.tvTime.setText(sendTime);

            String unReadNumber = Unit.getUnReadNumber(t);
            if (unReadNumber == null) {
                baseRoom6Binding.tvUnread.setVisibility(View.INVISIBLE);
            } else {
                baseRoom6Binding.tvUnread.setText(unReadNumber);
                baseRoom6Binding.tvUnread.setVisibility(View.VISIBLE);
            }

            baseRoom6Binding.ivTop.setImageResource(isTop ? R.drawable.ic_no_top : R.drawable.ic_top);
            baseRoom6Binding.ivMute.setImageResource(isMute ? R.drawable.amplification : R.drawable.not_remind);
            baseRoom6Binding.ivFavourite.setVisibility(t.isFavourite() && Strings.isNullOrEmpty(t.getBusinessId()) ? View.VISIBLE : View.INVISIBLE);

            /*---------  Event Binding  ----------*/
            baseRoom6Binding.clContentItem.setOnClickListener(v -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onRoomItemClickListener != null) {
                    onRoomItemClickListener.onItemClick(t);
                }
            });

            baseRoom6Binding.ivTop.setOnClickListener(view -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, Menu.TOP, position);
                }
            });

            baseRoom6Binding.ivMute.setOnClickListener(v -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, Menu.MUTE, position);
                }
            });

            baseRoom6Binding.tvSetupUnreadTag.setText(StringHelper.autoNewLine(getContext(), R.string.alert_notes, t.getUnReadNum() == 0 ? R.string.room_cell_swipe_menu_setup_unread : R.string.room_cell_swipe_menu_setup_read));
            baseRoom6Binding.tvSetupUnreadTag.setBackgroundColor(t.getUnReadNum() == 0 ? context.getColor(R.color.btn_yellow) : 0xFF88B1DE);
            baseRoom6Binding.tvSetupUnreadTag.setOnClickListener(v -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, t.getUnReadNum() == 0 ? Menu.SETUP_UNREAD_TAG : Menu.SETUP_READ, position);
                }
            });

            baseRoom6Binding.ivDelete.setOnClickListener(view -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, Menu.DELETE, position);
                }
            });
            baseRoom6Binding.clContentCell.setBackgroundResource(0);

            baseRoom6Binding.tvBusinessContent.setVisibility(!Strings.isNullOrEmpty(businessName) ? View.VISIBLE : View.GONE);
            baseRoom6Binding.tvBusinessContent.setText(!Strings.isNullOrEmpty(businessName) ? TextViewHelper.setLeftImage(getContext(), businessName, R.drawable.ic_icon_link_gary_14dp) : "");

        }
    }

    /**
     * mapping layout @link R.layout.item_base_room_5
     */
    public class SubscribeChatRoomViewHolder extends ItemSwipeWithActionWidthViewHolder<T> {
        ItemBaseRoom6Binding baseRoom6Binding;

        public SubscribeChatRoomViewHolder(ItemBaseRoom6Binding binding) {
            super(binding.getRoot());
            baseRoom6Binding = binding;
            super.setMenuViews(baseRoom6Binding.llLeftMenu, baseRoom6Binding.llRightMenu);
            super.setContentItemView(baseRoom6Binding.clContentItem);
        }

        @Override
        public void onBind(T t, int section, int position) {
            baseRoom6Binding.tvTime.setVisibility(View.VISIBLE);
            baseRoom6Binding.tvContent.setVisibility(View.VISIBLE);
            String title = t.getTitle(getContext());
            boolean isTop = t.isTop();
            boolean isMute = t.isMute();

            // 最後訊息時間
//            tvTime.setVisibility(View.INVISIBLE);
            baseRoom6Binding.ivRemind.setVisibility(!t.isMute() ? View.GONE : View.VISIBLE);
            baseRoom6Binding.clContentItem.setBackgroundResource(R.drawable.selector_item_list);
            baseRoom6Binding.civIcon.setImageResource(R.drawable.custom_default_avatar);
            if (t.getType() == ChatRoomType.discuss) {
                if (t.getChatRoomMember() != null)
                    baseRoom6Binding.civIcon.getChatRoomMemberIdsAndLoadMultiAvatarIcon(t.getChatRoomMember(), t.getId());
                else if (t.getMemberIds() != null)
                    baseRoom6Binding.civIcon.loadMultiAvatarIcon(t.getMemberIds(), t.getId());
            } else {
                baseRoom6Binding.civIcon.loadAvatarIcon(t.getAvatarId(), t.getName(), t.getId());
            }
            if (isTop) {
                baseRoom6Binding.civSmallIcon.setVisibility(View.VISIBLE);
                baseRoom6Binding.civSmallIcon.setImageResource(R.drawable.ic_s_top);
            } else {
                baseRoom6Binding.civSmallIcon.setVisibility(View.GONE);
            }
            baseRoom6Binding.ivTop.setImageResource(isTop ? R.drawable.ic_no_top : R.drawable.ic_top);

            baseRoom6Binding.ivMute.setImageResource(isMute ? R.drawable.amplification : R.drawable.not_remind);

            baseRoom6Binding.tvName.setText(TextViewHelper.setLeftImage(getContext(), title, R.drawable.icon_subscribe_number_pink_15dp));

            baseRoom6Binding.tvBusinessContent.setVisibility(!Strings.isNullOrEmpty(t.getBusinessName()) ? View.VISIBLE : View.GONE);
            baseRoom6Binding.tvBusinessContent.setText(!Strings.isNullOrEmpty(t.getBusinessName()) ? TextViewHelper.setLeftImage(getContext(), t.getBusinessName(), R.drawable.icon_link_green_14dp) : "");
            baseRoom6Binding.tvBusinessContent.setTextColor(0xFF6bc2ba);

            CharSequence content = getItemContent(getContext(), t);
            baseRoom6Binding.tvContent.setText(content);
            CharSequence sendTime = getItemSendTime(t);
            baseRoom6Binding.tvTime.setText(sendTime);

            /*---------  Event Binding  ----------*/
            baseRoom6Binding.clContentItem.setOnClickListener(v -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onRoomItemClickListener != null) {
                    onRoomItemClickListener.onItemClick(t);
                }
            });

            baseRoom6Binding.ivTop.setOnClickListener(view -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, Menu.TOP, position);
                }
            });

            baseRoom6Binding.ivMute.setOnClickListener(v -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, Menu.MUTE, position);
                }
            });

            baseRoom6Binding.tvSetupUnreadTag.setText(StringHelper.autoNewLine(getContext(), R.string.alert_notes, t.getUnReadNum() == 0 ? R.string.room_cell_swipe_menu_setup_unread : R.string.room_cell_swipe_menu_setup_read));
            baseRoom6Binding.tvSetupUnreadTag.setBackgroundColor(t.getUnReadNum() == 0 ? context.getColor(R.color.btn_yellow) : 0xFF88B1DE);
            baseRoom6Binding.tvSetupUnreadTag.setOnClickListener(v -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, t.getUnReadNum() == 0 ? Menu.SETUP_UNREAD_TAG : Menu.SETUP_READ, position);
                }
            });

            baseRoom6Binding.ivDelete.setOnClickListener(view -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, Menu.DELETE, position);
                }
            });

            baseRoom6Binding.clContentCell.setBackgroundResource(0);
            String unReadNumber = Unit.getUnReadNumber(t);
            if (unReadNumber == null) {
                baseRoom6Binding.tvUnread.setVisibility(View.INVISIBLE);
            } else {
                baseRoom6Binding.tvUnread.setText(unReadNumber);
                baseRoom6Binding.tvUnread.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * mapping layout @link R.layout.item_include_table_room_5
     */
    public class ParentChatRoomViewHolder extends ItemNoSwipeViewHolder<T> implements ItemTouchHelperCallback.OnMoveListener {
        ItemIncludeTableRoom6Binding includeTableRoom6Binding;
        BaseRoomList3Adapter sAdapter;
        private final NameKit nameKit = new NameKit();

        public ParentChatRoomViewHolder(ItemIncludeTableRoom6Binding binding) {
            super(binding.getRoot());
            includeTableRoom6Binding = binding;
            super.setMenuViews(includeTableRoom6Binding.llLeftMenu, includeTableRoom6Binding.llRightMenu);
            super.setContentItemView(includeTableRoom6Binding.clContentItem);
            includeTableRoom6Binding.rvSubList.setLayoutManager(new LinearLayoutManager(getContext()));
            if (sharedPool != null) {
                includeTableRoom6Binding.rvSubList.setRecycledViewPool(sharedPool);
            }
            includeTableRoom6Binding.tvUnread.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onBind(T t, int section, int position) {
            super.onBind(t, section, position);
            String key = t.getBindKey();

            if (!subAdapterSet.contains(this.sAdapter)) {
                ItemTouchHelperExtension itemTouchHelper = new ItemTouchHelperExtension(new ItemTouchHelperCallback(ItemTouchHelper.START | ItemTouchHelper.END)
                    .setFlag(1)
                    .setOnMoveListener(this)
                    .setTag(key));
                this.sAdapter = new ChildRoomList3Adapter(ChatRoomType.friend, key)
                    .setItemTouchHelperExtension(itemTouchHelper)
                    .setOnSwipeMenuListener(onSwipeMenuListener)
                    .setOnRoomItemClickListener(onRoomItemClickListener);
                itemTouchHelper.attachToRecyclerView(includeTableRoom6Binding.rvSubList);
                includeTableRoom6Binding.rvSubList.setAdapter(this.sAdapter);
                subAdapterSet.add(this.sAdapter);
            }

            includeTableRoom6Binding.rvSubList.setVisibility(View.GONE);
            includeTableRoom6Binding.vSubNodeDivider.setVisibility(View.GONE);
            includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.GONE);

            String name = t.isNoMaster() ? t.getName() : t.getTitle(getContext());

            boolean isFavourite = t.isFavourite();
            boolean isTop = t.isTop();
            boolean isAt = t.isAtMe();
            int taskCount = 0;
            int opportunityCount = 0;
            int serviceRequestCount = 0;
            this.sAdapter.setLimitedQuantity(0)
                .setData(ChatRoomType.person.equals(t.getType()) ? businessSelfRelData.get(key) : businessExecutorRelData.get(key));

            if (t.isNoMaster() && t.isHardCode()) {
                AvatarService.post(getContext(), t.getAvatarId(), PicSize.SMALL, includeTableRoom6Binding.civIcon, R.drawable.custom_default_avatar);
            } else {
                if (ChatRoomType.person.equals(t.getType())) {
                    AvatarService.post(getContext(), UserPref.getInstance(getContext()).getUserAvatarId(), PicSize.SMALL, includeTableRoom6Binding.civIcon, R.drawable.custom_default_avatar);
                } else {
                    List<String> avatarIds = t.getAvatarIds(getContext(), 4);
                    if (avatarIds.size() == 1) {
                        if (AvatarKit.DEFAULT_AVATAR_ID.equals(avatarIds.get(0))) {
                            includeTableRoom6Binding.civIcon.setVisibility(View.INVISIBLE);
                            includeTableRoom6Binding.tvAvatar.setVisibility(View.VISIBLE);
                            includeTableRoom6Binding.tvAvatar.setText(nameKit.getAvatarName(name));
                            GradientDrawable gradientDrawable = (GradientDrawable) includeTableRoom6Binding.tvAvatar.getBackground();
                            gradientDrawable.setColor(Color.parseColor(nameKit.getBackgroundColor(name)));
                        } else {
                            includeTableRoom6Binding.civIcon.setVisibility(View.VISIBLE);
                            includeTableRoom6Binding.tvAvatar.setVisibility(View.INVISIBLE);
                        }
                    }
                    AvatarService.post(getContext(), avatarIds, PicSize.SMALL, includeTableRoom6Binding.civIcon, R.drawable.custom_default_avatar); //大頭
                }
            }

            Set<T> subList = ChatRoomType.person.equals(t.getType()) ? businessSelfRelData.get(key) : businessExecutorRelData.get(key);
            for (T sub : subList) {
                if (sub.getBusinessCode() == null) {
                    CELog.e(sub.toString());
                    sub.setBusinessCode(BusinessCode.UNDEF);
                }
                if (!Strings.isNullOrEmpty(sub.getBusinessExecutorId())) {
                    if (sub.getBusinessCode() == null) {
                        CELog.e(sub.toString());
                        sub.setBusinessCode(BusinessCode.UNDEF);
                    }
                    switch (sub.getBusinessCode()) {
                        case TASK:
                            taskCount++;
                            break;
                        case OPPORTUNITY:
                            opportunityCount++;
                            break;
                        case SERVICE_REQUEST:
                            serviceRequestCount++;
                            break;
                    }
                }
            }

            StringBuilder builder = new StringBuilder();
            if (serviceRequestCount > 0) {
                builder.append(String.format(BusinessCode.SERVICE_REQUEST.getSimpleName() + " ", serviceRequestCount));
            }
            if (opportunityCount > 0) {
                builder.append(String.format(BusinessCode.OPPORTUNITY.getSimpleName() + " ", opportunityCount));
            }
            if (taskCount > 0) {
                builder.append(String.format(BusinessCode.TASK.getSimpleName() + " ", taskCount));
            }

            includeTableRoom6Binding.clContentCell.setBackgroundResource(isTop ? R.drawable.selector_item_list_top : R.drawable.selector_item_list);

            if (ChatRoomType.person.equals(t.getType())) {
                includeTableRoom6Binding.tvName.setText(UserPref.getInstance(getContext()).getUserName());
            } else {
                includeTableRoom6Binding.tvName.setText(name);
            }

            includeTableRoom6Binding.tvTime.setText(getItemSendTime(t));
            includeTableRoom6Binding.ivFavourite.setVisibility(isFavourite ? View.VISIBLE : View.INVISIBLE);

            if (builder.length() > 0) {
                builder.insert(0, "執行中:");
            }
            includeTableRoom6Binding.tvBusinessContent.setText(TextViewHelper.setLeftImage(getContext(), builder.toString(), R.drawable.icon_link_green_14dp));
            includeTableRoom6Binding.tvBusinessContent.setTextColor(0xFF6bc2ba);


            String unReadNumber = Unit.getUnReadNumber(t);
            if (unReadNumber == null) {
                includeTableRoom6Binding.tvUnread.setVisibility(View.INVISIBLE);
            } else {
                includeTableRoom6Binding.tvUnread.setText(unReadNumber);
                includeTableRoom6Binding.tvUnread.setVisibility(View.VISIBLE);
            }

            includeTableRoom6Binding.tvUnread.setVisibility(unReadNumber == null ? View.INVISIBLE : View.VISIBLE);

            if (limitOpenDatas.get(key) == null) {
                this.sAdapter.setLimitedQuantity(0).refreshData();
                includeTableRoom6Binding.rvSubList.setVisibility(View.GONE);
                includeTableRoom6Binding.vSubNodeDivider.setVisibility(View.GONE);
                includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.GONE);
            } else {
                includeTableRoom6Binding.tvUnread.setVisibility(View.INVISIBLE);
                // EVAN_FLAG 2020-11-06 這裡做被打開狀態判斷，並判斷打開數量及剩餘數量顯示展開更多功能。
                int totalSize = ChatRoomType.person.equals(t.getType()) ? businessSelfRelData.get(key).size() : businessExecutorRelData.get(key).size();
                int limit = limitOpenDatas.get(key) != null ? limitOpenDatas.get(key) : 0;
                if (totalSize < limit) {
                    limit = totalSize;
                    limitOpenDatas.put(key, limit);
                }
                this.sAdapter.setLimitedQuantity(limit).refreshData();
                includeTableRoom6Binding.rvSubList.setVisibility(View.VISIBLE);
                includeTableRoom6Binding.vSubNodeDivider.setVisibility(View.VISIBLE);
                if (totalSize < 3 || totalSize == limit) {
                    includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.GONE);
                } else {
                    includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.VISIBLE);
                }
            }

            includeTableRoom6Binding.clContentCell.setOnClickListener(v -> {
                closeOther(t.getType(), key).emptyRefreshData();
                this.sAdapter.itemTouchHelperExtension.closeOpened();
                this.sAdapter.setData(ChatRoomType.person.equals(t.getType()) ? businessSelfRelData.get(key) : businessExecutorRelData.get(key)).sort();
                if (this.sAdapter.getItemCount() == 0) {
                    if ((ChatRoomType.person.equals(t.getType()) ? businessSelfRelData.get(key).size() : businessExecutorRelData.get(key).size()) > 3) {
                        this.sAdapter.setLimitedQuantity(3).refreshData();
                        includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.VISIBLE);
                        limitOpenDatas.put(key, LIMIT);
                    } else {
                        this.sAdapter.setLimitedQuantity(ChatRoomType.person.equals(t.getType()) ? businessSelfRelData.get(key).size() : businessExecutorRelData.get(key).size()).refreshData();
                        includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.GONE);
                        limitOpenDatas.put(key, ChatRoomType.person.equals(t.getType()) ? businessSelfRelData.get(key).size() : businessExecutorRelData.get(key).size());
                    }
                    includeTableRoom6Binding.rvSubList.setVisibility(View.VISIBLE);
                    includeTableRoom6Binding.vSubNodeDivider.setVisibility(View.VISIBLE);
                    includeTableRoom6Binding.tvUnread.setVisibility(View.INVISIBLE);
                } else if (this.sAdapter.getItemCount() > 0) {
                    this.sAdapter.setLimitedQuantity(0).refreshData();
                    includeTableRoom6Binding.rvSubList.setVisibility(View.GONE);
                    includeTableRoom6Binding.vSubNodeDivider.setVisibility(View.GONE);
                    includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.GONE);
                    limitOpenDatas.remove(key);
                    includeTableRoom6Binding.tvUnread.setVisibility(unReadNumber == null ? View.INVISIBLE : View.VISIBLE);
                }
            });

            includeTableRoom6Binding.ivSubNodeCenter.setOnClickListener(v -> {
                this.sAdapter.itemTouchHelperExtension.closeOpened();
                this.sAdapter.setLimitedQuantity(ChatRoomType.person.equals(t.getType()) ? businessSelfRelData.get(key).size() : businessExecutorRelData.get(key).size()).refreshData();
                limitOpenDatas.put(key, ChatRoomType.person.equals(t.getType()) ? businessSelfRelData.get(key).size() : businessExecutorRelData.get(key).size());
                v.setVisibility(View.GONE);
            });
        }

        @Override
        public void move(int from, int to) {

        }

        @Override
        public void swiped(int flag, String tag, RecyclerView.ViewHolder viewHolder, int direction) {
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_SWIPE_MENU_CLOSE_OPEN, tag));
        }
    }

    /**
     * mapping layout @link R.layout.item_include_table_room_5
     */
    public class SubscribeParentChatRoomViewHolder extends ItemNoSwipeViewHolder<T> implements ItemTouchHelperCallback.OnMoveListener {
        ItemIncludeTableRoom6Binding includeTableRoom6Binding;

        BaseRoomList3Adapter sAdapter;

        public SubscribeParentChatRoomViewHolder(ItemIncludeTableRoom6Binding binding) {
            super(binding.getRoot());
            includeTableRoom6Binding = binding;
            super.setMenuViews(includeTableRoom6Binding.llLeftMenu, includeTableRoom6Binding.llRightMenu);
            super.setContentItemView(includeTableRoom6Binding.clContentItem);
            includeTableRoom6Binding.rvSubList.setLayoutManager(new LinearLayoutManager(getContext()));
            includeTableRoom6Binding.tvUnread.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onBind(T t, int section, int position) {
            super.onBind(t, section, position);
            String key = t.getServiceNumberId();

            if (!subAdapterSet.contains(this.sAdapter)) {
                ItemTouchHelperExtension itemTouchHelper = new ItemTouchHelperExtension(new ItemTouchHelperCallback(ItemTouchHelper.START | ItemTouchHelper.END)
                    .setFlag(1)
                    .setOnMoveListener(this)
                    .setTag(key));
                this.sAdapter = new ChildRoomList3Adapter(ChatRoomType.friend, key)
                    .setItemTouchHelperExtension(itemTouchHelper)
                    .setOnSwipeMenuListener(onSwipeMenuListener)
                    .setOnRoomItemClickListener(onRoomItemClickListener);
                itemTouchHelper.attachToRecyclerView(includeTableRoom6Binding.rvSubList);
                includeTableRoom6Binding.rvSubList.setAdapter(this.sAdapter);
                subAdapterSet.add(this.sAdapter);
            }

            includeTableRoom6Binding.rvSubList.setVisibility(View.GONE);
            includeTableRoom6Binding.vSubNodeDivider.setVisibility(View.GONE);
            includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.GONE);

            String name = t.getTitle(getContext());

            boolean isFavourite = t.isFavourite();
            boolean isTop = t.isTop();
            boolean isAt = t.isAtMe();
            int unReadNumber = t.getUnReadNum();

            int taskCount = 0;
            int opportunityCount = 0;
            int serviceRequestCount = 0;
            this.sAdapter.setLimitedQuantity(0)
                .setData(businessSubscribeRelData.get(key));

            AvatarService.post(getContext(), t.getAvatarIds(getContext(), 4), PicSize.SMALL, includeTableRoom6Binding.civIcon, R.drawable.custom_default_avatar);

            Set<T> subList = businessSubscribeRelData.get(key);
            for (T sub : subList) {
                if (!Strings.isNullOrEmpty(key)) {
                    switch (sub.getBusinessCode()) {
                        case TASK:
                            taskCount++;
                            break;
                        case OPPORTUNITY:
                            opportunityCount++;
                            break;
                        case SERVICE_REQUEST:
                            serviceRequestCount++;
                            break;
                    }
                } else {
                    CELog.e("is FRIEND ");
                }
            }

            StringBuilder builder = new StringBuilder();
            if (serviceRequestCount > 0) {
                builder.append(String.format(BusinessCode.SERVICE_REQUEST.getSimpleName() + " ", serviceRequestCount));
            }
            if (opportunityCount > 0) {
                builder.append(String.format(BusinessCode.OPPORTUNITY.getSimpleName() + " ", opportunityCount));
            }
            if (taskCount > 0) {
                builder.append(String.format(BusinessCode.TASK.getSimpleName() + " ", taskCount));
            }

            includeTableRoom6Binding.clContentCell.setBackgroundResource(isTop ? R.drawable.selector_item_list_top : R.drawable.selector_item_list);
            includeTableRoom6Binding.tvName.setText(TextViewHelper.setLeftImage(getContext(), name, R.drawable.icon_subscribe_number_pink_15dp));
            includeTableRoom6Binding.tvTime.setText(getItemSendTime(t));
            includeTableRoom6Binding.ivFavourite.setVisibility(isFavourite ? View.VISIBLE : View.INVISIBLE);

            if (builder.length() > 0) {
                builder.insert(0, "執行中:");
            }
            includeTableRoom6Binding.tvBusinessContent.setText(TextViewHelper.setLeftImage(getContext(), builder.toString(), R.drawable.icon_link_green_14dp));
            includeTableRoom6Binding.tvBusinessContent.setTextColor(0xFF6bc2ba);

            includeTableRoom6Binding.tvUnread.setText(Unit.getUnReadNumber(unReadNumber, isAt));
            includeTableRoom6Binding.tvUnread.setVisibility(unReadNumber <= 0 ? View.INVISIBLE : View.VISIBLE);

            if (limitOpenDatas.get(key) == null) {
                this.sAdapter.setLimitedQuantity(0).refreshData();
                includeTableRoom6Binding.rvSubList.setVisibility(View.GONE);
                includeTableRoom6Binding.vSubNodeDivider.setVisibility(View.GONE);
                includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.GONE);
            } else {
                int totalSize = businessSubscribeRelData.get(key).size();
                int limit = limitOpenDatas.get(key);
                if (totalSize < limit) {
                    limit = totalSize;
                    limitOpenDatas.put(key, limit);
                }
                this.sAdapter.setLimitedQuantity(limit).refreshData();
                includeTableRoom6Binding.rvSubList.setVisibility(View.VISIBLE);
                includeTableRoom6Binding.vSubNodeDivider.setVisibility(View.VISIBLE);
                if (totalSize < 3 || totalSize == limit) {
                    includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.GONE);
                } else {
                    includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.VISIBLE);
                }
                includeTableRoom6Binding.tvUnread.setVisibility(View.INVISIBLE);
            }

            includeTableRoom6Binding.clContentCell.setOnClickListener(v -> {
                closeOther(t.getType(), key).emptyRefreshData();
                this.sAdapter.itemTouchHelperExtension.closeOpened();
                this.sAdapter.setData(businessSubscribeRelData.get(key)).sort();
                if (this.sAdapter.getItemCount() == 0) {
                    if (businessSubscribeRelData.get(key).size() > 3) {
                        this.sAdapter.setLimitedQuantity(3).refreshData();
                        includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.VISIBLE);
                        limitOpenDatas.put(key, LIMIT);
                    } else {
                        this.sAdapter.setLimitedQuantity(businessSubscribeRelData.get(key).size()).refreshData();
                        includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.GONE);
                        limitOpenDatas.put(key, businessSubscribeRelData.get(key).size());
                    }
                    includeTableRoom6Binding.rvSubList.setVisibility(View.VISIBLE);
                    includeTableRoom6Binding.vSubNodeDivider.setVisibility(View.VISIBLE);
                    includeTableRoom6Binding.tvUnread.setVisibility(View.INVISIBLE);
                } else if (this.sAdapter.getItemCount() > 0) {
                    this.sAdapter.setLimitedQuantity(0).refreshData();
                    includeTableRoom6Binding.rvSubList.setVisibility(View.GONE);
                    includeTableRoom6Binding.vSubNodeDivider.setVisibility(View.GONE);
                    includeTableRoom6Binding.ivSubNodeCenter.setVisibility(View.GONE);
                    limitOpenDatas.remove(key);
                    includeTableRoom6Binding.tvUnread.setVisibility(unReadNumber <= 0 ? View.INVISIBLE : View.VISIBLE);
                }
            });

            includeTableRoom6Binding.ivSubNodeCenter.setOnClickListener(v -> {
                this.sAdapter.itemTouchHelperExtension.closeOpened();
                this.sAdapter.setLimitedQuantity(businessSubscribeRelData.get(key).size()).refreshData();
                limitOpenDatas.put(key, businessSubscribeRelData.get(key).size());
                v.setVisibility(View.GONE);
            });
        }

        @Override
        public void move(int from, int to) {

        }

        @Override
        public void swiped(int flag, String tag, RecyclerView.ViewHolder viewHolder, int direction) {
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_SWIPE_MENU_CLOSE_OPEN, tag));
        }
    }

    /**
     * mapping layout @link R.layout.item_sun_room_5
     */
    public class ChildChatRoomViewHolder extends ItemSwipeWithActionWidthViewHolder<T> {
        ItemSunRoom5Binding sunRoom5Binding;

        public ChildChatRoomViewHolder(ItemSunRoom5Binding binding) {
            super(binding.getRoot());
            sunRoom5Binding = binding;
            super.setMenuViews(sunRoom5Binding.llLeftMenu, sunRoom5Binding.llRightMenu);
            super.setContentItemView(sunRoom5Binding.clContentItem);
        }

        @Override
        public void onBind(T t, int section, int position) {
            super.onBind(t, section, position);
            if (t.getLastMessage() != null && MessageType.AT.equals(t.getLastMessage().getType())) {
                if (t.getMembers() == null || t.getMembers().isEmpty()) {
                    t.setMembers(ChatMemberCacheService.getChatMember(t.getId()));
                }
            }
            boolean isTop = t.isTop();
            boolean isMute = t.isMute();
            sunRoom5Binding.ivRemind.setVisibility(!t.isMute() ? View.GONE : View.VISIBLE);
            if (ChatRoomType.person.equals(t.getType())) {
                sunRoom5Binding.ivDelete.setVisibility(View.GONE);
            } else {
                sunRoom5Binding.ivDelete.setVisibility(View.VISIBLE);
            }

            sunRoom5Binding.clContentCell.setBackgroundResource(R.drawable.selector_item_list);
            sunRoom5Binding.ivTop.setImageResource(isTop ? R.drawable.ic_no_top : R.drawable.ic_top);
            sunRoom5Binding.ivMute.setImageResource(isMute ? R.drawable.amplification : R.drawable.not_remind);

            if (isTop) {
                sunRoom5Binding.civSmallIcon.setImageResource(R.drawable.ic_s_top);
                sunRoom5Binding.civSmallIcon.setVisibility(View.VISIBLE);
            } else {
                sunRoom5Binding.civSmallIcon.setVisibility(View.GONE);
            }

            SpannableStringBuilder builder = new SpannableStringBuilder();
            // is business
            if (!Strings.isNullOrEmpty(t.getBusinessId())) {
                sunRoom5Binding.civIcon.setImageResource(t.getBusinessCode().getIconId());
                builder.append(StringHelper.getString(t.getBusinessName(), ""));
            } else {
                if (ChatRoomType.discuss.equals(t.getType()) || (ChatRoomType.group.equals(t.getType()) && Strings.isNullOrEmpty(t.getAvatarId()))) {
                    if (t.getMemberAvatarData() == null || t.getMemberAvatarData().isEmpty()) {
                        t.setMemberAvatarData(UserProfileReference.getMemberAvatarData(null, t.getId(), getUserId(), 4));
                    }
                }

                if (ChatRoomType.person.equals(t.getType())) {
                    AvatarService.post(getContext(), t.getAvatarIds2(getContext(), 4), PicSize.SMALL, sunRoom5Binding.civIcon, R.drawable.custom_default_avatar);
                    builder.append(t.getTitle(getContext()));
                } else if (ChatRoomType.FRIEND_or_DISCUSS.contains(t.getType())) {
                    AvatarService.post(getContext(), t.getAvatarIds2(getContext(), 4), PicSize.SMALL, sunRoom5Binding.civIcon, R.drawable.custom_default_avatar);
                    builder.append(StringHelper.getString(t.getName(), ""));
                } else if (ChatRoomType.subscribe.equals(t.getType())) {
                    AvatarService.post(getContext(), t.getAvatarIds2(getContext(), 4), PicSize.SMALL, sunRoom5Binding.civIcon, R.drawable.custom_default_avatar);
                    builder.append(StringHelper.getString(t.getServiceNumberName(), ""));
                } else {
                    sunRoom5Binding.civIcon.setImageResource(R.drawable.custom_default_avatar);
                    builder.append(StringHelper.getString(t.getName(), ""));
                }
            }

            sunRoom5Binding.tvName.setText(builder);

            CharSequence content = getItemContent(getContext(), t);
            sunRoom5Binding.tvContent.setText(content);

            CharSequence sendTime = getItemSendTime(t);
            sunRoom5Binding.tvTime.setText(sendTime);

            String unReadNumber = Unit.getUnReadNumber(t);
            if (unReadNumber == null) {
                sunRoom5Binding.tvUnread.setVisibility(View.INVISIBLE);
            } else {
                sunRoom5Binding.tvUnread.setText(unReadNumber);
                sunRoom5Binding.tvUnread.setVisibility(View.VISIBLE);
            }

            sunRoom5Binding.clContentItem.setOnClickListener(v -> {
                //服務號聊天室點擊處理
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onRoomItemClickListener != null) {
                    String key = Strings.isNullOrEmpty(t.getBusinessExecutorId()) ? t.getBindKey() : t.getBusinessExecutorId();
                    onRoomItemClickListener.onChildItemClick(t, key);
                }
            });

            sunRoom5Binding.ivTop.setOnClickListener(view -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, Menu.TOP, position);
                }
            });

            sunRoom5Binding.ivMute.setOnClickListener(v -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, Menu.MUTE, position);
                }
            });

            sunRoom5Binding.tvSetupUnreadTag.setText(StringHelper.autoNewLine(getContext(), R.string.alert_notes, t.getUnReadNum() == 0 ? R.string.room_cell_swipe_menu_setup_unread : R.string.room_cell_swipe_menu_setup_read));
            sunRoom5Binding.tvSetupUnreadTag.setBackgroundColor(t.getUnReadNum() == 0 ? context.getColor(R.color.btn_yellow) : 0xFF88B1DE);
            sunRoom5Binding.tvSetupUnreadTag.setOnClickListener(v -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, t.getUnReadNum() == 0 ? Menu.SETUP_UNREAD_TAG : Menu.SETUP_READ, position);
                }
            });

            sunRoom5Binding.ivDelete.setOnClickListener(view -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, Menu.DELETE, position);
                }
            });
        }
    }

    /**
     * mapping layout @link R.layout.item_sun_room_5
     */
    public class ChildServiceChatRoomViewHolder extends ItemSwipeWithActionWidthViewHolder<T> {
        SectionedType sectionedType;

        private ItemSunRoom5Binding binding;

        public ChildServiceChatRoomViewHolder(SectionedType sectionedType, ItemSunRoom5Binding binding) {
            this(binding.getRoot());
            this.binding = binding;
            this.sectionedType = sectionedType;
        }

        public ChildServiceChatRoomViewHolder(View itemView) {
            super(itemView);
            super.setMenuViews(binding.llLeftMenu, binding.llRightMenu);
            super.setContentItemView(binding.clContentItem);
            binding.ivTop.setVisibility(View.GONE);
        }

        @Override
        public void onBind(T t, int section, int position) {
            super.onBind(t, section, position);
            String key = SectionedType.MY_or_OTHERS_SERVICE.contains(this.sectionedType) ? this.sectionedType.name() + "-" + t.getServiceNumberId() + "-" + t.getOwnerId() + "-" + t.getServiceNumberAgentId() : t.getServiceNumberId() + "-" + t.getOwnerId();
            boolean isMute = t.isMute();

            binding.clContentCell.setBackgroundResource(R.drawable.selector_item_list);
            binding.ivMute.setImageResource(isMute ? R.drawable.amplification : R.drawable.not_remind);

            SpannableStringBuilder builder = new SpannableStringBuilder();

            if (!Strings.isNullOrEmpty(t.getBusinessId())) {
                binding.civIcon.setImageResource(t.getBusinessCode().getIconId());
                builder.append(StringHelper.getString(t.getBusinessName(), ""));
            } else {
                AvatarService.post(getContext(), t.getAvatarId(), PicSize.SMALL, binding.civIcon, R.drawable.custom_default_avatar);
                builder.append(t.getTitle(getContext()));
            }
            binding.tvName.setText(builder);

            CharSequence content = getItemContent(getContext(), t);
            binding.tvContent.setText(content);

            CharSequence sendTime = getItemSendTime(t);
            binding.tvTime.setText(sendTime);

            String unReadNumber = Unit.getUnReadNumber(t);
            if (unReadNumber == null) {
                binding.tvUnread.setVisibility(View.INVISIBLE);
            } else {
                binding.tvUnread.setText(unReadNumber);
                binding.tvUnread.setVisibility(View.VISIBLE);
            }

            binding.clContentItem.setOnClickListener(v -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onRoomItemClickListener != null) {
                    CELog.e(bindKey);
                    CELog.e(key);
                    onRoomItemClickListener.onChildItemClick(t, bindKey);
                }
            });

            binding.ivMute.setOnClickListener(v -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, Menu.MUTE, position);
                }
            });

            binding.tvSetupUnreadTag.setVisibility(!Strings.isNullOrEmpty(unReadNumber) ? View.GONE : View.VISIBLE);
            binding.tvSetupUnreadTag.setText(StringHelper.autoNewLine(getContext(), R.string.alert_notes, R.string.room_cell_swipe_menu_setup_untreated));
            binding.tvSetupUnreadTag.setOnClickListener(!Strings.isNullOrEmpty(unReadNumber) ? null : (View.OnClickListener) v -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, Menu.SETUP_UNREAD_TAG, position);
                }
            });

            binding.ivDelete.setOnClickListener(view -> {
                if (itemTouchHelperExtension != null) {
                    itemTouchHelperExtension.closeOpened();
                }
                if (onSwipeMenuListener != null) {
                    onSwipeMenuListener.onSwipeMenuClick(t, Menu.DELETE, position);
                }
            });
        }
    }

    private CharSequence getItemSendTime(T t) {
        if (t == null) {
            return "";
        }
        MessageEntity lastMessageEntity = t.getLastMessage();
        if (t.getLastMessage() != null) {
            if (lastMessageEntity.getSendTime() > 0) {
                return TimeUtil.INSTANCE.getTimeShowString(lastMessageEntity.getSendTime(), true);
            }
        }
        return "";
    }

    /**
     * display order
     * 1、failedMessage
     * 2、unEdit Text
     * 3、lastMessage Content
     */
    private CharSequence getItemContent(Context context, T t) {
        if (t == null) {
            return new SpannableString("");
        }
        MessageEntity failedMessage = t.getFailedMessage();
        // sort index == 0
        if (failedMessage != null) {
            SpannableStringBuilder builder = new SpannableStringBuilder("");
            if (Objects.requireNonNull(failedMessage.getType()) == MessageType.AT) {
                builder = AtMatcherHelper.matcherAtUsers("@", ((AtContent) failedMessage.content()).getMentionContents(), t.getMembersTable());
            } else {
                builder.append(failedMessage.content().simpleContent());
            }
            return TextViewHelper.setLeftImage(getContext(), builder, R.drawable.ic_mes_failure_14dp);
        }

        InputLogBean bean = InputLogBean.from(t.getUnfinishedEdited());
        if (bean != null && !Strings.isNullOrEmpty(bean.getText())) {
            return AtMatcherHelper.setLeftImageAndHighLightAt(getContext(), bean.getText(), R.drawable.ic_edit_gray_14dp, t.getMembersLinkedList(), InputLogType.AT.equals(bean.getType()) ? (ChatRoomType.subscribe.equals(t.getType()) ? 0xFF8F8E94 : 0xFF4A90E2) : 0xFF8F8E94);
        }

        MessageEntity lastMessageEntity = t.getLastMessage();
        if (lastMessageEntity != null) {
            String sendId = lastMessageEntity.getSenderId();
            String selfId = TokenPref.getInstance(context).getUserId();

            String sendName = ChatRoomType.subscribe.equals(t.getType()) ? selfId.equals(sendId) ? "我" : t.getName() : "我";
            if (!Strings.isNullOrEmpty(lastMessageEntity.getSenderId()) && !userId.equals(lastMessageEntity.getSenderId())) {
                UserProfileEntity profile = DBManager.getInstance().queryFriend(lastMessageEntity.getSenderId());
                if (profile != null) {
                    sendName = ChatRoomType.subscribe.equals(t.getType()) ? t.getName() : !Strings.isNullOrEmpty(profile.getAlias()) ? profile.getAlias() : profile.getNickName();
                } else {
                    sendName = ChatRoomType.subscribe.equals(t.getType()) ? t.getName() : lastMessageEntity.getSenderName();
                }
            }

            if (MessageFlag.RETRACT.equals(lastMessageEntity.getFlag())) {
                return new SpannableString(sendName + ": " + Constant.RETRACT_MSG);
            } else {
                SpannableStringBuilder builder = new SpannableStringBuilder("");
                try { // lastMessage會有Null的狀況(因為最後送的content是null, 然後轉存時不知道為啥沒把Text type加進去), 目前先做try catch避免crash
                    if (Objects.requireNonNull(lastMessageEntity.getType()) == MessageType.AT) {
                        AtContent atContent = (AtContent) lastMessageEntity.content();
                        builder = AtMatcherHelper.matcherAtUsers("@", atContent.getMentionContents(), t.getMembersTable());
                    } else {
                        builder.append(lastMessageEntity.content().simpleContent());
                    }
                    builder.insert(0, sendName + ": ");
                } catch (Exception ignored) {
                }
                return builder;
            }
        }
        return new SpannableString("");
    }

    private ChannelType setChannelIcon(CircleImageView icon, ChatRoomEntity t) {
        icon.setVisibility(View.GONE);
        if (t == null) {
            return null;
        }

        if (t.getLastMessage() == null) {
            return null;
        }

        if (t.getLastMessage().getFrom() == null) {
            return null;
        }

        icon.setVisibility(View.VISIBLE);
        switch (t.getLastMessage().getFrom()) {
            case FB:
                icon.setImageResource(R.drawable.ic_fb);
                break;
            case LINE:
                icon.setImageResource(R.drawable.ic_line);
                break;
            case QBI:
            case AILE_WEB_CHAT:
                icon.setImageResource(R.drawable.qbi_icon);
                break;
            case WEICHAT:
                icon.setImageResource(R.drawable.wechat_icon);
                break;
            case IG:
                icon.setImageResource(R.drawable.ic_ig);
                break;
            case GOOGLE:
                icon.setImageResource(R.drawable.ic_google_message);
                break;
            case CE:
                icon.setImageResource(R.drawable.ce_icon);
                break;
            case UNDEF:
            default:
                icon.setVisibility(View.GONE);
                break;
        }

        return t.getLastMessage().getFrom();
    }
}
