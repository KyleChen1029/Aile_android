package tw.com.chainsea.chat.view.roomList.serviceRoomList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.FragmentServiceRoomList2Binding;
import tw.com.chainsea.chat.network.contact.ViewModelFactory;
import tw.com.chainsea.chat.ui.activity.ChatActivity;
import tw.com.chainsea.chat.ui.adapter.WrapContentLinearLayoutManager;
import tw.com.chainsea.chat.ui.dialog.IosProgressDialog;
import tw.com.chainsea.chat.ui.fragment.BaseFragment;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.view.base.viewmodel.HomeViewModel;
import tw.com.chainsea.chat.view.roomList.mainRoomList.RoomListClickInterface;
import tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ServiceNumberAdapter;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension;

/**
 * Create by evan on 1/5/21
 *
 * @author Evan Wang
 * date 1/5/21
 */
public class ServiceRoomList3Fragment extends BaseFragment implements RoomListClickInterface, ServiceNumberAdapter.OnGroupClick {
    private FragmentServiceRoomList2Binding binding;
    private final AtomicBoolean isFirst = new AtomicBoolean(true);
    ItemTouchHelperExtension itemTouchHelper;
    private ServiceNumberSortType sortType = ServiceNumberSortType.BY_GROUP;

    public static ServiceRoomList3Fragment newInstance() {
        return new ServiceRoomList3Fragment();
    }

    private HomeViewModel viewModel;

    ServiceNumberAdapter serviceNumberAdapter = new ServiceNumberAdapter();
    ServiceNumberRoomViewModel serviceNumberRoomViewModel;
    private IosProgressDialog progressDialog;
    private final RecyclerView.AdapterDataObserver adapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            LinearLayoutManager layoutManager = (LinearLayoutManager) binding.rvChatRoomList.getLayoutManager();
            if (layoutManager != null) {
                if (layoutManager.findLastVisibleItemPosition() < layoutManager.getChildCount()) {
                    layoutManager.postOnAnimation(() -> layoutManager.scrollToPosition(0));
                }
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EventBusUtils.register(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service_room_list2, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        ViewModelFactory viewModelFactory = new ViewModelFactory(requireActivity().getApplication());
        serviceNumberRoomViewModel = new ViewModelProvider(this, viewModelFactory).get(ServiceNumberRoomViewModel.class);

        initRecyclerView();

        if (isFirst.get()) { //第一次從DB抓全部
            binding.textviewStillBusy.setVisibility(View.VISIBLE);
            refreshList();
        }
        observerData();
    }

    private void initRecyclerView() {
        serviceNumberAdapter.setRoomListRecyclerViewPool(viewModel.getRoomListRecyclerViewPool());
        serviceNumberAdapter.setRoomListClickInterface(this);
        serviceNumberAdapter.setOnGroupClickInterface(this);
        serviceNumberAdapter.registerAdapterDataObserver(adapterDataObserver);
        binding.rvChatRoomList.setAdapter(serviceNumberAdapter);
        binding.rvChatRoomList.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
    }

    private void observerData() {
        serviceNumberRoomViewModel.getServiceNumberListData().observe(getViewLifecycleOwner(), serviceNumberListModels -> {
            if (serviceNumberAdapter != null) serviceNumberAdapter.setData(serviceNumberListModels);
            binding.textviewStillBusy.setVisibility(View.GONE);
        });

        serviceNumberRoomViewModel.getRefreshGroup().observe(getViewLifecycleOwner(), index -> {
            if (serviceNumberAdapter != null) serviceNumberAdapter.notifyItemChanged(index, false);
        });

        viewModel.getSendRefreshBossServiceNumberOwnerChanged().observe(getViewLifecycleOwner(), isChanged -> {
            if (isChanged) refreshList();
        });
        viewModel.getSendRefreshDB().observe(getViewLifecycleOwner(), isRefresh -> {
            if (isRefresh) {
                serviceNumberRoomViewModel.getServiceNumberFromDb(sortType);
            }
        });
        viewModel.getSendRefreshListByAPI().observe(getViewLifecycleOwner(), isRefresh -> {
            if (isRefresh) refreshList();
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBusUtils.unregister(this);
    }

    private void refreshList() {
        serviceNumberRoomViewModel.getServicedChatRoomFromServer(sortType);
    }


    public void scrollToTop() {
        binding.rvChatRoomList.scrollToPosition(0);
    }


    public void doSwitchSortMode(ServiceNumberSortType sortType) {
        this.sortType = sortType;
        serviceNumberRoomViewModel.getServiceNumberFromDb(this.sortType);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMainEvent(EventMsg eventMsg) {
        switch (eventMsg.getCode()) {
            case MsgConstant.REFRESH_ROOM_BY_LOCAL:
                ChatRoomEntity refreshEntity = JsonHelper.getInstance().from(eventMsg.getString(), ChatRoomEntity.class);
                if (ChatRoomSource.SERVICE.equals(refreshEntity.getListClassify())) {
                    serviceNumberRoomViewModel.getServiceNumberFromDb(sortType);
                }
                break;
            case MsgConstant.CHANGE_LAST_MESSAGE: // 更新最後一筆訊息
            case MsgConstant.SESSION_UPDATE_CALLING_FILTER:
                String targetRoomId = (String) eventMsg.getData();
                if (!Strings.isNullOrEmpty(targetRoomId)) {
                    try {
                        ChatRoomEntity updateEntity = ChatRoomReference.getInstance().findById(targetRoomId);
                        if (updateEntity != null && ChatRoomSource.SERVICE.equals(updateEntity.getListClassify())) {
                            serviceNumberRoomViewModel.getServiceNumberFromDb(sortType);
                        }
                    } catch (Exception ignored) {
                    }
                }
                break;
            case MsgConstant.SYNC_READ: // 收到同步
                ChatRoomEntity syncEntity = (ChatRoomEntity) eventMsg.getData();
                if (ChatRoomSource.SERVICE.equals(syncEntity.getListClassify())) {
                    serviceNumberRoomViewModel.getServiceNumberFromDb(sortType);
                }
                break;
            case MsgConstant.MSG_RECEIVED_FILTER: // 外部接收到新訊息處理服務號接線人員邏輯
                MessageEntity receiverMessage = JsonHelper.getInstance().from(eventMsg.getString(), MessageEntity.class);
                if (receiverMessage != null) {
                    serviceNumberRoomViewModel.getServiceNumberFromDb(sortType);
                }
                break;
            case MsgConstant.UI_NOTICE_SWIPE_MENU_CLOSE_OPEN:
                itemTouchHelper.closeOpened();
                String tag = eventMsg.getString();
                break;
            case MsgConstant.NAVIGATE_TO_CHAT_ROOM:
                if (getContext() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKey.EXTRA_SESSION_ID.key(), eventMsg.getString());
                    IntentUtil.INSTANCE.startIntent(getContext(), ChatActivity.class, bundle);
                }
                break;
            case MsgConstant.UPDATE_ROBOT_SERVICE_LIST: //add/renew robot servicing actioned
            case MsgConstant.NOTICE_ROBOT_SERVICE_WARNED:
                serviceNumberRoomViewModel.getRobotServicingCatRoomFromServer(sortType);
                break;
            case MsgConstant.NOTICE_ROBOT_STOP:
                JSONObject jsonObject = (JSONObject) eventMsg.getData();
                serviceNumberRoomViewModel.onRobotStop(jsonObject.optString("roomId"));
                break;
            case MsgConstant.DELETE_SERVICE_NUMBER_MEMBER:
            case MsgConstant.SERVICE_NUMBER_PERSONAL_STOP://商務號擁有者帳號在轉人工處理，通知更新服務號分組
            case MsgConstant.REFRESH_SERVICE_NUMBER:// 更換服務號頭圖（進服務號主頁才會觸發）
            case MsgConstant.NOTICE_SERVICE_NUMBER_REFRESH_BY_DB:
            case MsgConstant.SERVICE_NUMBER_PERSONAL_START:
            case MsgConstant.MSG_STATUS_FILTER://收到被接手的通知，更新頁面, 轉服務中
                serviceNumberRoomViewModel.getServiceNumberFromDb(sortType);
                break;

            case MsgConstant.REFRESH_CUSTOMER_NAME:
                viewModel.refreshServiceNumberListByDb();
                break;
            case MsgConstant.SERVICE_NUMBER_TRANSFER_STATUS:
                // 服務人員換手
                refreshList();
                break;
            case MsgConstant.REMOVE_GROUP_FILTER:
                //只消除並刷新該聊天室未讀圖標
                String roomId = eventMsg.getString();
                serviceNumberRoomViewModel.clearChatRoomUnread(roomId);
                break;

        }
    }

    private View clickItemView = null;

    @Override
    public void onResume() {
        super.onResume();
        dismissIosDialog();
        if (clickItemView != null) clickItemView.setEnabled(true);
        refreshList();
    }

    @Override
    public void onOpenChat(@NonNull View view) {
        clickItemView = view;
        showIosDialog();
    }

    private void showIosDialog() {
        if (progressDialog == null) progressDialog = new IosProgressDialog(getContext());
        progressDialog.show();
    }

    private void dismissIosDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onOpen(@Nullable String serviceNumberId, @NonNull ServiceNumberListType serviceNumberListType) {
        serviceNumberRoomViewModel.addGroupOpenList(serviceNumberListType, serviceNumberId);
    }

    @Override
    public void onClose(@Nullable String serviceNumberId, @NonNull ServiceNumberListType serviceNumberListType) {
        serviceNumberRoomViewModel.removeGroupOpenList(serviceNumberListType, serviceNumberId);
    }
}
