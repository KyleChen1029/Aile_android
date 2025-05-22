package tw.com.chainsea.chat.view.todo.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import tw.com.chainsea.android.common.event.OnHKClickListener;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.text.KeyWordHelper;
import tw.com.chainsea.ce.sdk.bean.ProcessStatus;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.common.EnableType;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity;
import tw.com.chainsea.ce.sdk.bean.todo.TodoStatus;
import tw.com.chainsea.ce.sdk.bean.todo.Type;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.ce.sdk.service.TodoService;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.chatroomfilter.StickyHeaderItemDecorator;
import tw.com.chainsea.chat.databinding.ItemServiceNumNameBinding;
import tw.com.chainsea.chat.databinding.ItemTodoBinding;
import tw.com.chainsea.chat.util.TextViewHelper;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.chat.view.globalSearch.Sectioned;
import tw.com.chainsea.chat.view.todo.TodoOverviewType;
import tw.com.chainsea.custom.view.adapter.SectionedRecyclerViewAdapter;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemNoSwipeViewHolder;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemSwipeWithActionWidthViewHolder;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension;

/**
 * current by evan on 2020-07-13
 *
 * @author Evan Wang
 * @date 2020-07-13
 */
public class TodoListAdapter extends SectionedRecyclerViewAdapter<TodoListAdapter.HeaderHolder, ItemBaseViewHolder, RecyclerView.ViewHolder>
        implements StickyHeaderItemDecorator.StickyHeaderInterface {

    private TodoOverviewType type;
    private List<Sectioned<TodoEntity, SectionedType, String>> sections = Lists.newArrayList();
    private Set<String> isOpenSet = Sets.newHashSet(SectionedType.UNFINISHED.name());
    private Set<String> settingSet = Sets.newHashSet();
    private List<TodoEntity> matadatas = Lists.newArrayList();

    ItemTouchHelperExtension itemTouchHelper;
    private OnTodoDetailItemListener<TodoEntity> onTodoDetailItemListener;

    private static final int ITEM = 1010;
    private static final int ITEM_SETTING = 1011;
    private String selfId;
    private long now = System.currentTimeMillis();
    private String keyword = "";

    @Override
    public boolean isHeader(int itemPosition) {
        try {
            return isSectionHeaderPosition(itemPosition);
        } catch (Exception e) {
            return false;
        }
    }


    public enum SectionedType {
        EXPIRED(R.string.todo_list_sectioned_expired),
        COMPLETE(R.string.todo_list_sectioned_complete),
        UNFINISHED(R.string.todo_list_sectioned_processing),
        EMPTY(R.string.warning_empry);


        @StringRes
        private int resId;

        SectionedType(int resId) {
            this.resId = resId;
        }

        public int getResId() {
            return this.resId;
        }
    }

    public TodoListAdapter() {
        this.type = TodoOverviewType.SCHEDULE_LIST;
    }

    @Override
    protected int getSectionCount() {
        return this.sections.size();
    }

    @Override
    protected int getItemCountForSection(int section) {

        return this.sections.get(section).getSize();
    }

    @Override
    protected boolean hasFooterInSection(int section) {
        return false;
    }

    @Override
    protected HeaderHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
        this.selfId = TokenPref.getInstance(parent.getContext()).getUserId();
        ItemServiceNumNameBinding binding = ItemServiceNumNameBinding.inflate(LayoutInflater.from(getContext()), parent, false);
        return new HeaderHolder(binding);
    }

    @Override
    protected ItemBaseViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        ItemTodoBinding binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.getContext()), parent ,false);
        return new MainItemViewHolder(binding);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    protected void onBindSectionHeaderViewHolder(HeaderHolder holder, int section) {
        Sectioned sectioned = this.sections.get(section);
        holder.onBind(sectioned, section, -1);
    }

    @Override
    protected void onBindItemViewHolder(ItemBaseViewHolder holder, int section, int position) {
        TodoEntity entity = this.sections.get(section).getDatas().get(position);
        holder.onBind(entity, section, position);
    }

    @Override
    protected void onBindSectionFooterViewHolder(RecyclerView.ViewHolder holder, int section) {

    }

    @Override
    public void executeAnimatorEnd(int position) {

    }

    @Override
    protected int getSectionHeaderViewType(int section) {
        return super.getSectionHeaderViewType(section);
    }

    @Override
    protected int getSectionItemViewType(int section, int position) {
        TodoEntity entity = this.sections.get(section).getDatas().get(position);
//        if (TodoEntity.Type.MAIN.equals(entity.getType())) {
//            return ITEM;
//        } else {
//            return ITEM_SETTING;
//        }

        return ITEM;
    }

    public TodoListAdapter setItemTouchHelper(ItemTouchHelperExtension itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
        return this;
    }

    public TodoListAdapter setOnTodoDetailItemListener(OnTodoDetailItemListener<TodoEntity> onTodoDetailItemListener) {
        this.onTodoDetailItemListener = onTodoDetailItemListener;
        return this;
    }

    public TodoListAdapter setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public TodoListAdapter setOpenSet(SectionedType sectionedType) {
        this.isOpenSet.add(sectionedType.name());
        return this;
    }

    public TodoListAdapter setData(List<TodoEntity> matadatas) {
        this.matadatas = matadatas;
        return this;
    }

    public int getDataCount() {
        return this.matadatas.size();
    }

    public int outDateCount() {
        if (this.matadatas == null || this.matadatas.isEmpty()) {
            return 0;
        }

        Iterator<TodoEntity> iterator = this.matadatas.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            TodoEntity entity = iterator.next();
            if (TodoStatus.PROGRESS.equals(entity.getStatus())) {
                if (entity.getRemindTime() > 0 && entity.getRemindTime() < now) {
                    count++;
                }
            }
        }
        return count;
    }

    public TodoListAdapter remove(TodoEntity entity) {
        TodoEntity removeItem =    new TodoEntity.Builder()
                .id(entity.getId())
                .type(Type.MAIN)
                .processStatus(entity.getProcessStatus())
                .build();
        this.matadatas.remove(removeItem);
        removeItem.setType(Type.SETTING);
        this.matadatas.remove(removeItem);
        this.settingSet.remove(entity.getId());
        return this;
    }

    public TodoListAdapter setData(TodoEntity entity) {
        this.matadatas.remove(entity);
        this.matadatas.remove(new TodoEntity.Builder().type(Type.SETTING).build());
        this.matadatas.add(entity);
        return this;
    }

    public TodoListAdapter setDataAndSetting(TodoEntity entity) {
        clearAllSetting();
        this.matadatas.remove(entity);
        this.matadatas.add(entity);
        this.settingSet.add(entity.getId());
        return this;
    }

    public TodoListAdapter unBindRoomId(String roomId) {
        Iterator<TodoEntity> iterator = this.matadatas.iterator();
        while (iterator.hasNext()) {
            TodoEntity entity = iterator.next();
            if (roomId.equals(entity.getRoomId())) {
                entity.setRoomId("");
                entity.setRoomEntity(null);
                entity.setMessageEntity(null);
                entity.setMessageId("");
            }

        }
        return this;
    }

    public int findIndex(TodoEntity entity) {
        for (int i = 0; i < this.sections.size(); i++) {
            Sectioned<TodoEntity, SectionedType, String> sectioned = this.sections.get(i);
            int index = sectioned.getDatas().indexOf(entity);
            if (index >= 0) {
                return index + (i + 1);
            }
        }
        return 0;
    }

    public TodoListAdapter clearAllSetting() {
        Iterator<TodoEntity> iterator = matadatas.iterator();
        while (iterator.hasNext()) {
            if (Type.SETTING.equals(iterator.next().getType())) {
                iterator.remove();
            }
        }
        this.settingSet.clear();
        return this;
    }

    private boolean hasSettingView() {
        Iterator<TodoEntity> iterator = matadatas.iterator();
        while (iterator.hasNext()) {
            if (Type.SETTING.equals(iterator.next().getType())) {
                return true;
            }
        }
        return false;
    }

    private void sort() {
        Collections.sort(this.matadatas);
    }

    private void sort(List<TodoEntity> entities) {
        Collections.sort(entities);
    }

    private List<TodoEntity> filter() {
        List<TodoEntity> list = Lists.newArrayList();
        if (Strings.isNullOrEmpty(this.keyword)) {
            list.addAll(this.matadatas);
            return list;
        }

        Iterator<TodoEntity> iterator = this.matadatas.iterator();
        while (iterator.hasNext()) {
            TodoEntity entity = iterator.next();
            if (entity.getTitle().toUpperCase().contains(this.keyword.toUpperCase())) {
                list.add(entity);
            }
        }

        return list;
    }
    @SuppressLint("NotifyDataSetChanged")
    public void refreshData() {
        this.sections.clear();
        this.now = System.currentTimeMillis();
        List<TodoEntity> list = filter();


        ListMultimap<TodoStatus, TodoEntity> multimap = ArrayListMultimap.create();
        Iterator<TodoEntity> iterator2 = list.iterator();
        while (iterator2.hasNext()) {
            TodoEntity entity = iterator2.next();
            multimap.put(entity.getStatus(), entity);
        }

        ListMultimap<TodoStatus, TodoEntity> sortMultimap = new ImmutableListMultimap.Builder<TodoStatus, TodoEntity>()
                .orderKeysBy((o1, o2) -> ComparisonChain.start().compare(o1.getIndex(), o2.getIndex()).result())
                .orderValuesBy((o1, o2) -> ComparisonChain.start()
                        .compare((o1.getOpenClock())? 0 : 1, (o2.getOpenClock())? 0 : 1)
                        .compare((o1.getRemindTime() > 0 && o1.getRemindTime() < System.currentTimeMillis())? 0: 2, (o2.getRemindTime() > 0 && o2.getRemindTime() < System.currentTimeMillis())? 0 : 2)
                        .compare(o2.getWeights6(), o1.getWeights6())
                        .result())
                .putAll(multimap) // or put each entry
                .build();

        for (Map.Entry<TodoStatus, Collection<TodoEntity>> entry : sortMultimap.asMap().entrySet()) {
            boolean isProgress = entry.getKey().equals(TodoStatus.PROGRESS);
            List<TodoEntity> entities = Lists.newArrayList(entry.getValue());

            if (TodoStatus.DONE.equals(entry.getKey())) {
                Collections.sort(entities, (o1, o2) -> ComparisonChain.start()
                        .compare(o2.getWeights6(), o1.getWeights6())
                        .result());
            } else {
                Collections.sort(entities, (o1, o2) -> ComparisonChain.start()
                        .compare((o1.getOpenClock())? 0 : 1, (o2.getOpenClock())? 0 : 1)
                        .compare((o1.getRemindTime() > 0 && o1.getRemindTime() < System.currentTimeMillis())? 0: 2, (o2.getRemindTime() > 0 && o2.getRemindTime() < System.currentTimeMillis())? 0 : 2)
                        .compare(o2.getWeights6(), o1.getWeights6())
                        .result());
            }

            Sectioned<TodoEntity, SectionedType, String> sectioned = Sectioned.<TodoEntity, SectionedType, String>Build()
                    .type(isProgress ? SectionedType.UNFINISHED : SectionedType.COMPLETE)
                    .nameResId(isProgress ? SectionedType.UNFINISHED.getResId() : SectionedType.COMPLETE.getResId())
                    .size(isOpenSet.contains(isProgress ? SectionedType.UNFINISHED.name() : SectionedType.COMPLETE.name()) ? entities.size() : 0)
                    .content("")
                    .datas(entities)
                    .isOpen(isOpenSet.contains(isProgress ? SectionedType.UNFINISHED.name() : SectionedType.COMPLETE.name()))
                    .build();
            this.sections.add(sectioned);
        }

        Sectioned<TodoEntity, SectionedType, String> empty = Sectioned.<TodoEntity, SectionedType, String>Build()
                .type(SectionedType.EMPTY)
                .nameResId(SectionedType.EMPTY.getResId())
                .size(0)
                .content("")
                .datas(Lists.newArrayList())
                .isOpen(false)
                .build();
        this.sections.add(empty);


        notifyDataSetChanged();

        boolean status = hasSettingView();
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
            if (this.onTodoDetailItemListener != null) {
                this.onTodoDetailItemListener.onHideFloatingButton(status, false);
            }
        });
    }

    public List<Sectioned<TodoEntity, SectionedType, String>> getSections() {
        return Lists.newArrayList(this.sections);
    }

    public class HeaderHolder extends ItemNoSwipeViewHolder<Sectioned<TodoEntity, SectionedType, String>> {
       private ItemServiceNumNameBinding binding;

        public HeaderHolder(ItemServiceNumNameBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        @SuppressLint("NotifyDataSetChanged")
        public void onBind(Sectioned<TodoEntity, SectionedType, String> sectioned, int section, int position) {
            binding.clSectionedControl.setVisibility(View.GONE);
            switch (sectioned.getType()) {
                case EXPIRED:
                    itemView.setBackgroundColor(0xFFFFF1F1);
//                    itemView.setBackgroundResource(R.drawable.selector_item_list_timeout);
                    break;
                case COMPLETE:
                    itemView.setBackgroundColor(0xFFF1F4F5);
                    break;
                case EMPTY:
                    itemView.setBackgroundColor(0xFFFFFFFF);
                    binding.clSectionedControl.setVisibility(View.VISIBLE);
                    break;
                case UNFINISHED:
                default:
                    itemView.setBackgroundColor(0xFFFFFFFF);
                    break;
            }

            binding.vLine.setVisibility(SectionedType.EMPTY.equals(sectioned.getType()) ? View.GONE : View.VISIBLE);
            binding.tvTitle.setText(sectioned.getNameResId());
            binding.tvOpen.setVisibility(SectionedType.EMPTY.equals(sectioned.getType()) ? View.GONE : View.VISIBLE);
            binding.tvOpen.setImageResource(sectioned.isOpen() ? R.drawable.ic_expand : R.drawable.ic_close);
            itemView.setOnClickListener(SectionedType.EMPTY.equals(sectioned.getType()) ? null : new OnHKClickListener() {
                @Override
                public void onClick(View v, Object o) {
                    if (SectionedType.UNFINISHED.equals(sectioned.getType()) && sectioned.isOpen()) {
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                            if (onTodoDetailItemListener != null) {
                                onTodoDetailItemListener.onHideFloatingButton(false, false);
                            }
                        });
                    }

                    if (!sectioned.isOpen()) {
                        for (TodoEntity tEntity : sectioned.getDatas()) {
                            if (Type.SETTING.equals(tEntity.getType())) {
                                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                                    if (onTodoDetailItemListener != null) {
                                        onTodoDetailItemListener.onHideFloatingButton(true, false);
                                    }
                                });
                                break;
                            }
                        }
                    }
                    sectioned.setSize(sectioned.isOpen() ? 0 : sectioned.getDatas().size());
                    sectioned.setOpen(!sectioned.isOpen());
                    if (sectioned.isOpen()) {
                        isOpenSet.add(sectioned.getType().name());
                    } else {
                        isOpenSet.remove(sectioned.getType().name());
                    }
                    notifyDataSetChanged();
                }
            });
        }
    }

    class MainItemViewHolder extends ItemSwipeWithActionWidthViewHolder<TodoEntity> {
        private ItemTodoBinding binding;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);

        public MainItemViewHolder(ItemTodoBinding binding) {
            super(binding.getRoot());
            super.setMenuViews(binding.llLeftMenu, binding.llRightMenu);
            super.setContentItemView(binding.clContentItem);
            this.binding = binding;
        }

        @Override
        public void onBind(TodoEntity entity, int section, int position) {
            super.onBind(entity, section, position);
            EnableType type = EnableType.of(entity.getOpenClock());

//            switch (sections.get(section).getType()) {
//                case EXPIRED:
//                    clContentItem.setBackgroundColor(0xFFfff1f1);
//                    ivTodoMask.setImageResource(R.drawable.icon_todo_mask_red_45dp);
//                    break;
//                case COMPLETE:
//                    ivTodoMask.setImageResource(R.drawable.icon_todo_mask_gray_45dp);
//                    clContentItem.setBackgroundColor(0xFFF1F4F5);
//                    break;
//                case UNFINISHED:
//                default:
//                    ivTodoMask.setImageResource(R.drawable.icon_todo_mask_white_45dp);
//                    clContentItem.setBackgroundColor(0xFFFFFFFF);
//                    break;
//            }

            switch (entity.getStatus()) {
                case DONE:
                   binding.ivTodoMask.setImageResource(R.drawable.icon_todo_mask_gray_45dp);
                   binding.clContentItem.setBackgroundColor(0xFFF1F4F5);
                    break;
                case PROGRESS:
                default:
                    if (entity.getRemindTime() > 0 && entity.getRemindTime() < now) {
                        binding.clContentItem.setBackgroundColor(0xFFfff1f1);
                        binding.ivTodoMask.setImageResource(R.drawable.icon_todo_mask_red_45dp);
                    } else {
                        binding.ivTodoMask.setImageResource(R.drawable.icon_todo_mask_white_45dp);
                        binding.clContentItem.setBackgroundColor(0xFFFFFFFF);
                    }
                    break;
            }
            boolean isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
            if (entity.getRoomId() != null && !entity.getRoomId().isEmpty()) {
                ChatRoomEntity chatRoomEntity = ChatRoomReference.getInstance().findById(entity.getRoomId());
                if (chatRoomEntity != null) {
                    if (chatRoomEntity.getType().equals(ChatRoomType.discuss)) {
                        binding.civIcon.getChatRoomMemberIdsAndLoadMultiAvatarIcon(chatRoomEntity.getChatRoomMember(), chatRoomEntity.getId());
                    } else {
                        if (chatRoomEntity.getType().equals(ChatRoomType.person)) {
                            String selfUserId = TokenPref.getInstance(binding.civIcon.getContext()).getUserId();
                            UserProfileEntity userProfile = UserProfileReference.findById(null, selfUserId);
                            if (userProfile != null) {
                                binding.civIcon.loadAvatarIcon(userProfile.getAvatarId(), userProfile.getNickName(), chatRoomEntity.getId());
                            }
                        } else {
                            binding.civIcon.loadAvatarIcon(chatRoomEntity.getAvatarId(), chatRoomEntity.getName(), chatRoomEntity.getId());
                        }
                    }
                } else {
                    binding.civIcon.setImageResource(isGreenTheme ? R.drawable.res_check_list_circle_def_green : R.drawable.res_check_list_circle_def);
                }
            } else {
                binding.civIcon.setImageResource(isGreenTheme ? R.drawable.res_check_list_circle_def_green : R.drawable.res_check_list_circle_def);
            }
            long remindTime = entity.getRemindTime();

            SpannableString spannableString = KeyWordHelper.matcherSearchTitle(isGreenTheme ? Color.parseColor("#06B4A5") : 0xFF4A90E2, entity.getTitle(), keyword);
            binding.tvTitle.setText(spannableString);

            // No RemindTime display'Do not remind'
            if (remindTime > 0) {
                binding.tvContent.setText(format.format(remindTime));
            } else {
                binding.tvContent.setText(R.string.todo_do_not_remind);
            }

            if (!ProcessStatus.UNDEF.equals(entity.getProcessStatus())) {
                binding.tvContent.setText(TextViewHelper.setLeftImageAndHighLight(getContext(), binding.tvContent.getText().toString(), R.drawable.ic_mes_failure_14dp, keyword, isGreenTheme ? Color.parseColor("#06B4A5") : 0xFF4A90E2));
            }

            // No room Id bound
            binding.ivChat.setVisibility(Strings.isNullOrEmpty(entity.getRoomId()) ? View.GONE : View.VISIBLE);

            // Has been completed
            binding.tvComplete.setText(TodoStatus.DONE.equals(entity.getStatus()) ? R.string.todo_unfinished : R.string.alert_complete);
            binding.tvComplete.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            binding.tvComplete.setBackgroundColor(TodoStatus.DONE.equals(entity.getStatus()) ? binding.getRoot().getContext().getColor(R.color.btn_yellow) : ThemeHelper.INSTANCE.isGreenTheme() ? Color.parseColor("#06B4A5") : 0xFF88b1de);

            binding.ivClock.setImageResource(entity.getOpenClock() ? R.drawable.ic_clock_blue_25dp : R.drawable.ic_clock_gray_25dp);

            binding.clContentItem.setOnClickListener(new OnHKClickListener() {
                @Override
                public void onClick(View v, Object o) {
                    if (itemTouchHelper != null) {
                        itemTouchHelper.closeOpened();
                    }

                    if (onTodoDetailItemListener != null) { //點擊列表時呼叫
                        onTodoDetailItemListener.onFocus(entity, Type.SETTING);
                    }
                }
            });

            binding.civIcon.setOnClickListener(Strings.isNullOrEmpty(entity.getRoomId()) ? null : new OnHKClickListener() {
                @Override
                public void onClick(View v, Object o) {
                    if (itemTouchHelper != null) {
                        itemTouchHelper.closeOpened();
                    }

                    if (onTodoDetailItemListener != null) {
                        onTodoDetailItemListener.navigateToChat(entity);
                    }
                }
            });

            binding.tvComplete.setOnClickListener(new OnHKClickListener() {
                @Override
                public void onClick(View v, Object o) {
                    if (itemTouchHelper != null) {
                        itemTouchHelper.closeOpened();
                    }
                    if (onTodoDetailItemListener != null) {
                        if (TodoStatus.DONE.equals(entity.getStatus())) {
                            onTodoDetailItemListener.onEdit(entity);
                        } else {
                            TodoService.completeAndSync(getContext(), entity, RefreshSource.REMOTE);
                        }
                    }
                }
            });

            binding.ivDelete.setOnClickListener(new OnHKClickListener() {
                @Override
                public void onClick(View v, Object o) {
                    if (itemTouchHelper != null) {
                        itemTouchHelper.closeOpened();
                    }
                    if (onTodoDetailItemListener != null) {
                        TodoService.deleteAndSync(getContext(), entity, RefreshSource.REMOTE);
                    }
                }
            });
        }
    }

    public interface OnTodoDetailItemListener<T> {

        void navigateToChat(T t);

        void onEdit(T t);

        void onSettingChange(T t);

        void onFocus(T t, Type type);

        void onHideFloatingButton(boolean status, boolean isFilterMode);
    }

}
