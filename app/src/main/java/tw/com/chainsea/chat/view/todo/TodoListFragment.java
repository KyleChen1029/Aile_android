package tw.com.chainsea.chat.view.todo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.event.KeyboardHelper;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.ProcessStatus;
import tw.com.chainsea.ce.sdk.bean.common.EnableType;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity;
import tw.com.chainsea.ce.sdk.bean.todo.TodoStatus;
import tw.com.chainsea.ce.sdk.bean.todo.Type;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.MessageReference;
import tw.com.chainsea.ce.sdk.reference.TodoReference;
import tw.com.chainsea.ce.sdk.service.ChatRoomService;
import tw.com.chainsea.ce.sdk.service.TodoService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.chatroomfilter.OnDataGetCallback;
import tw.com.chainsea.chat.chatroomfilter.StickyHeaderItemDecorator;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.FragmentTodoListBinding;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.ui.fragment.BaseFragment;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.view.globalSearch.Sectioned;
import tw.com.chainsea.chat.view.todo.adapter.TodoListAdapter;
import tw.com.chainsea.custom.view.alert.AlertView;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperCallback;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension;

public class TodoListFragment extends BaseFragment<TodoOverviewType, TodoEntity> implements TodoListAdapter.OnTodoDetailItemListener<TodoEntity> {
    private static final String TAG = TodoListFragment.class.getSimpleName();

    TodoOverviewType type = TodoOverviewType.SCHEDULE_LIST;
    ItemTouchHelperCallback mCallback = new ItemTouchHelperCallback(ItemTouchHelper.START);
    ItemTouchHelperExtension itemTouchHelper = new ItemTouchHelperExtension(mCallback);
    TodoListAdapter adapter = new TodoListAdapter();

    TodoSettingDialog todoSettingDialog;

    private boolean CAN_NEXT = false;
    public static int REQUEST_CODE = 10101;

    FragmentTodoListBinding binding;
    private OnDataGetCallback callback;

    public TodoListFragment() {

    }

    public static TodoListFragment newInstance(OnDataGetCallback callback, String roomId) {
        TodoListFragment todoListFragment = new TodoListFragment();
        todoListFragment.setOnDataGetCallback(callback);
        Bundle bundle = new Bundle();
        bundle.putString(BundleKey.ROOM_ID.key(), roomId);
        todoListFragment.setArguments(bundle);
        return todoListFragment;
    }

    public void setOnDataGetCallback(OnDataGetCallback listener) {
        this.callback = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTodoListBinding.inflate(inflater, container, false);
        EventBusUtils.register(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StickyHeaderItemDecorator stickyHeaderItemDecorator = new StickyHeaderItemDecorator();
        stickyHeaderItemDecorator.attachRecyclerView(adapter, binding.rvList, adapter);
        this.itemTouchHelper.attachToRecyclerView(binding.rvList);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        this.adapter.setItemTouchHelper(itemTouchHelper)
            .setOnTodoDetailItemListener(this);
        binding.rvList.setAdapter(this.adapter);
        refreshData(RefreshSource.ALL);
        initListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        CAN_NEXT = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBusUtils.unregister(this);
    }

    public void refreshData(RefreshSource source) {
        String roomId = "";
        if (getArguments() != null) {
            roomId = getArguments().getString(BundleKey.ROOM_ID.key(), "");
        }
        TodoService.getTodoEntities(getContext(), roomId, source, new ServiceCallBack<List<TodoEntity>, RefreshSource>() {
            @Override
            public void complete(List<TodoEntity> entities, RefreshSource source) {
                if (callback != null) {
                    callback.onDataGet(entities.isEmpty());
                }
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    adapter.setData(entities).refreshData();
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_UPDATE_TODO_OVER_VIEW_ITEM_COUNT));
                });
            }

            @Override
            public void error(String message) {
                if (callback != null) {
                    callback.onDataGet(true);
                }
            }
        });
    }

    //點擊懸浮視窗時呼叫
    public void selectToTodoItem(TodoEntity entity) {
        if (entity != null) {
            TodoEntity selectItem = TodoReference.findById(null, entity.getId());
            if (selectItem != null && !TodoStatus.DELETED.equals(selectItem.getStatus())) {
                this.adapter.setDataAndSetting(selectItem).refreshData();
                onFocus(new TodoEntity.Builder().type(Type.SETTING).build(), Type.SETTING);
            }
        }
    }

    @Override
    public TodoOverviewType getType() {
        return type;
    }

    public void refreshNowTime() {
        if (this.adapter != null) {
            this.adapter.refreshData();
        }
    }

    @Override
    public void setKeyword(String keyword) {
        onHideFloatingButton(Strings.isNullOrEmpty(keyword), true);
        if (this.adapter != null) {
            this.adapter.setKeyword(keyword).refreshData();
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_UPDATE_TODO_OVER_VIEW_ITEM_COUNT));
        }
    }

    @Override
    public int getDataCount() {
        if (this.adapter != null) {
            return this.adapter.outDateCount();
        }
        return super.getDataCount();
    }

    @Override
    public void onSettingChange(TodoEntity entity) {
        CELog.e(entity.toString());
    }

    @Override
    public void navigateToChat(TodoEntity entity) {
        checkingData(entity);
    }

    @Override
    public void onEdit(TodoEntity entity) {
        if (itemTouchHelper != null) {
            itemTouchHelper.closeOpened();
        }

        if (this.todoSettingDialog != null && this.todoSettingDialog.isShowing()) {
            return;
        }
        todoSettingDialog = new TodoSettingDialog(requireContext(), entity.getRoomId(), entity.getId(), Lists.newArrayList(entity));
        todoSettingDialog.setOnListener(onListener);
        todoSettingDialog.show();
    }

    private void checkingData(TodoEntity entity) {
        String roomId = entity.getRoomId();
        if (Strings.isNullOrEmpty(roomId)) return;
        String messageId = entity.getMessageId();
        MessageEntity message = MessageReference.findById(messageId);
        if (message != null && (message.getFlag() == MessageFlag.RETRACT || message.getFlag() == MessageFlag.DELETED)) {
            Toast.makeText(requireContext(), getString(R.string.text_todo_message_deleted), Toast.LENGTH_SHORT).show();
            return;
        }
        boolean hasData = ChatRoomReference.getInstance().hasLocalData(roomId);

        if (hasData) {
            navigateToChat(roomId, messageId);
        } else {
            String selfId = TokenPref.getInstance(getContext()).getUserId();
            ChatRoomService.getInstance().getChatRoomItem(getContext(), selfId, roomId, RefreshSource.REMOTE, new ServiceCallBack<ChatRoomEntity, RefreshSource>() {
                @Override
                public void complete(ChatRoomEntity entity, RefreshSource source) {
                    ChatRoomReference.getInstance().save(entity);
                    navigateToChat(roomId, messageId);
                }

                @Override
                public void error(String message) {

                }
            });
        }
    }

    private void navigateToChat(String roomId, String messageId) {
        if (!Strings.isNullOrEmpty(roomId) && Strings.isNullOrEmpty(messageId)) {
            ActivityTransitionsControl.navigateToChat(getContext(), roomId, (intent, s) -> IntentUtil.INSTANCE.start(requireContext(), intent));
        } else if (!Strings.isNullOrEmpty(roomId) && !Strings.isNullOrEmpty(messageId)) {
            MessageEntity messageEntity = MessageReference.findByIdAndRoomId(null, messageId, roomId);
            if (messageEntity != null) {
                ActivityTransitionsControl.navigateToChat(getContext(), roomId, messageEntity, (intent, s) -> IntentUtil.INSTANCE.start(requireContext(), intent));
            } else {
                ActivityTransitionsControl.navigateToChat(getContext(), roomId, (intent, s) -> IntentUtil.INSTANCE.start(requireContext(), intent));
            }
        }
    }

    @Override
    public void onFocus(TodoEntity entity, Type type) {
        if (this.todoSettingDialog != null && this.todoSettingDialog.isShowing()) {
            return;
        }

        if (this.onActionListener != null) {
            this.onActionListener.action("select", entity);
        }

        List<TodoEntity> list = Lists.newArrayList();
        List<Sectioned<TodoEntity, TodoListAdapter.SectionedType, String>> sectioneds = this.adapter.getSections();
        for (Sectioned<TodoEntity, TodoListAdapter.SectionedType, String> sectioned : sectioneds) {
            list.addAll(sectioned.getDatas());
        }
        //點擊與按下提醒框時導入時呼叫
        todoSettingDialog = new TodoSettingDialog(requireContext(), "", entity.getId(), list);
        todoSettingDialog.setRemindListener(setRemindListener);
        todoSettingDialog.setOnListener(onListener);
        todoSettingDialog.show();
    }

    @Override
    public void onHideFloatingButton(boolean status, boolean isFilterMode) {
        if (this.adapter != null && !Strings.isNullOrEmpty(this.adapter.getKeyword())) {
            status = true;
        }
        if (binding != null && binding.difbAddSchedule != null) {
            binding.difbAddSchedule.setClickable(!status);
            if (status) {
                binding.difbAddSchedule.animate().scaleX(0.0f).scaleY(0.0f).translationY(binding.difbAddSchedule.getHeight()).setInterpolator(new LinearInterpolator()).start();
            } else {
                binding.difbAddSchedule.animate().scaleX(1.0f).scaleY(1.0f).translationY(0).setInterpolator(new LinearInterpolator()).start();
            }
        }
    }

    private void initListener() {
        binding.difbAddSchedule.setOnClickListener(this::doFloatingButtonAction);
    }

    public void doFloatingButtonAction(View view) {
        if (itemTouchHelper != null) {
            itemTouchHelper.closeOpened();
        }

        if (this.todoSettingDialog != null && this.todoSettingDialog.isShowing()) {
            return;
        }

        long now = System.currentTimeMillis();
        String roomId = "";
        if (getArguments() != null) {
            roomId = getArguments().getString(BundleKey.ROOM_ID.key(), "");
        }
        TodoEntity entity = new TodoEntity.Builder()
            .status(TodoStatus.PROGRESS)
            .processStatus(ProcessStatus.UN_SYNC_CREATE)
            .openClock(EnableType.N.isStatus())
            .remindTime(-1)
            .roomId(roomId)
            .createTime(now)
            .updateTime(now)
            .userId(getUserId()).build();

        //用來創建新的計事時呼叫
        todoSettingDialog = new TodoSettingDialog(requireContext(), roomId, entity.getId(), Lists.newArrayList(entity));
        todoSettingDialog.setRemindListener(setRemindListener);
        todoSettingDialog.setOnListener(onListener);
        todoSettingDialog.show();
    }

    private TodoSettingDialog.OnListener onListener = new TodoSettingDialog.OnListener() {
        @Override
        public void navigateToChat(TodoEntity entity) {

        }

        @Override
        public void onDismiss(@NonNull TodoSettingDialog todoSettingDialog) {
            //EventBusUtils.sendEvent(new EventMsg(MsgConstant.SWITCH_BASE_STATUS_BAR_COLOR, 0xFF6B93C2));
            KeyboardHelper.postHide(binding.difbAddSchedule);
        }

        @Override
        public void onShow(@NonNull TodoSettingDialog todoSettingDialog) {
//            EventBusUtils.sendEvent(new EventMsg(MsgConstant.SWITCH_BASE_STATUS_BAR_COLOR, 0xFF222F3E));
        }
    };


    private OnSetRemindTime setRemindListener = isRemind -> {
        if (isRemind) {
            if (!Settings.canDrawOverlays(requireContext())) {
                DateTime today = new DateTime();
                DateTime setNoticeDate = new DateTime(TokenPref.getInstance(requireContext()).getRemindNotice());

                if (today.isAfter(setNoticeDate)) {
                    String[] others = new String[]{requireContext().getString(R.string.alert_cancel),
                        requireContext().getString(R.string.alert_confirm)};
                    new AlertView.Builder()
                        .setContext(requireContext())
                        .setStyle(AlertView.Style.Alert)
                        .setMessage("為了讓您有更好的操作體驗，請允許使用浮動視窗權限。")
                        .setOthers(others)
                        .setOnItemClickListener((o, pos) -> {
                            if (pos == 1) {
                                Intent intent2 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                if (requireContext() instanceof Activity) {
                                    requireActivity().startActivityForResult(intent2, TodoListFragment.REQUEST_CODE);
                                }
                            } else {
                                DateTime dt = new DateTime();
                                DateTimeFormatter forPattern = DateTimeFormat.forPattern("yyyy-MM-dd");
                                DateTime dtp = forPattern.parseDateTime(dt.plusDays(7).toString("yyyy-MM-dd"));
                                //拒絕給予權限時，紀錄時間，一週後再問一次
                                TokenPref.getInstance(requireContext()).setRemindNotice(dtp.getMillis());
                                Toast.makeText(requireContext(), "許可權授予失敗，無法開啟浮動視窗", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .build()
                        .setCancelable(true)
                        .show();
                } else {
                    Toast.makeText(requireContext(), "請賦予懸浮視窗權限以獲得最即時的通知", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleAsyncEvent(EventMsg eventMsg) {
        switch (eventMsg.getCode()) {
            case MsgConstant.INTERNET_STSTE_FILTER:
                if ("true".equals(eventMsg.getData().toString())) {
                    refreshData(RefreshSource.ALL);
//                    TodoService.doSync(getContext());
                    TodoService.doSync(getContext());
                }
                break;
            case MsgConstant.REFRESH_FILTER:
                refreshData(RefreshSource.REMOTE);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleUiEvent(EventMsg eventMsg) {
        switch (eventMsg.getCode()) {
            case MsgConstant.NOTICE_TODO_UNBIND_ROOM:
                String roomId = eventMsg.getString();
                this.adapter.unBindRoomId(roomId).refreshData();
                CELog.e("" + roomId);
                break;
            case MsgConstant.UI_NOTICE_TODO_REFRESH:
                TodoEntity entity = JsonHelper.getInstance().from(eventMsg.getString(), TodoEntity.class);
                if (TodoStatus.DELETED.equals(entity.getStatus())) {
                    this.adapter.remove(entity);
                    this.adapter.remove(new TodoEntity.Builder().type(Type.SETTING).build());
                    this.adapter.refreshData();
                    if (isVisible() && isAdded()) {
                        Toast.makeText(getContext(), "刪除成功", Toast.LENGTH_SHORT).show();
                    }
                } else if (!ProcessStatus.UN_SYNC_DELETE.equals(entity.getProcessStatus())) {
                    this.adapter.remove(entity).setData(entity).refreshData();
                }
                callback.onDataGet(adapter.getDataCount() == 0);
                break;
        }
    }
}
