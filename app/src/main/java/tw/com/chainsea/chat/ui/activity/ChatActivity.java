//package tw.com.chainsea.chat.ui.activity;
//
//import static tw.com.chainsea.ce.sdk.bean.room.ChatRoomType.discuss;
//import static tw.com.chainsea.ce.sdk.bean.room.ChatRoomType.group;
//import static tw.com.chainsea.ce.sdk.bean.room.ChatRoomType.system;
//
//import android.content.Intent;
//import android.content.res.Configuration;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.widget.PopupWindow;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.DrawableRes;
//import androidx.annotation.Nullable;
//import androidx.core.view.ViewCompat;
//import androidx.databinding.DataBindingUtil;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentTransaction;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.recyclerview.widget.DefaultItemAnimator;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.common.base.Strings;
//import com.google.common.collect.ImmutableMap;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.common.collect.Sets;
//import com.google.gson.reflect.TypeToken;
//
//import org.greenrobot.eventbus.EventBus;
//import org.greenrobot.eventbus.Subscribe;
//import org.greenrobot.eventbus.ThreadMode;
//
//import java.io.Serializable;
//import java.lang.reflect.Type;
//import java.text.MessageFormat;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Set;
//
//import tw.com.aile.sdk.bean.message.MessageEntity;
//import tw.com.chainsea.android.common.datetime.DateTimeHelper;
//import tw.com.chainsea.android.common.event.KeyboardHelper;
//import tw.com.chainsea.android.common.json.JsonHelper;
//import tw.com.chainsea.android.common.log.CELog;
//import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
//import tw.com.chainsea.chat.util.TextViewHelper;
//import tw.com.chainsea.android.common.ui.UiHelper;
//import tw.com.chainsea.ce.sdk.bean.BadgeDataModel;
//import tw.com.chainsea.ce.sdk.bean.CrowdEntity;
//import tw.com.chainsea.ce.sdk.bean.GroupRefreshBean;
//import tw.com.chainsea.ce.sdk.bean.GroupUpgradeBean;
//import tw.com.chainsea.ce.sdk.bean.PicSize;
//import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
//import tw.com.chainsea.ce.sdk.bean.account.UserType;
//import tw.com.chainsea.ce.sdk.bean.business.BusinessEntity;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
//import tw.com.chainsea.ce.sdk.bean.room.DiscussMemberSocket;
//import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus;
//import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType;
//import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberAddModel;
//import tw.com.chainsea.ce.sdk.cache.ChatMemberCacheService;
//import tw.com.chainsea.ce.sdk.database.DBManager;
//import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
//import tw.com.chainsea.ce.sdk.database.sp.UserPref;
//import tw.com.chainsea.ce.sdk.event.EventBusUtils;
//import tw.com.chainsea.ce.sdk.event.EventMsg;
//import tw.com.chainsea.ce.sdk.event.MsgConstant;
//import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
//import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
//import tw.com.chainsea.ce.sdk.http.ce.request.RoomCreateRequest;
//import tw.com.chainsea.ce.sdk.network.model.response.DeviceRecordItem;
//import tw.com.chainsea.ce.sdk.reference.AccountRoomRelReference;
//import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
//import tw.com.chainsea.ce.sdk.reference.MessageReference;
//import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
//import tw.com.chainsea.ce.sdk.service.AvatarService;
//import tw.com.chainsea.ce.sdk.service.BusinessService;
//import tw.com.chainsea.ce.sdk.service.ChatMessageService;
//import tw.com.chainsea.ce.sdk.service.ChatRoomService;
//import tw.com.chainsea.ce.sdk.service.TodoService;
//import tw.com.chainsea.ce.sdk.service.UserProfileService;
//import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
//import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource;
//import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
//import tw.com.chainsea.chat.App;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.aiff.AiffManager;
//import tw.com.chainsea.chat.aiff.database.AiffDB;
//import tw.com.chainsea.chat.aiff.database.entity.AiffInfo;
//import tw.com.chainsea.chat.base.Constant;
//import tw.com.chainsea.chat.config.AiffDisplayLocation;
//import tw.com.chainsea.chat.config.AiffEmbedLocation;
//import tw.com.chainsea.chat.config.BundleKey;
//import tw.com.chainsea.chat.databinding.ActivityChatLayoutBinding;
//import tw.com.chainsea.chat.databinding.GridMemberViewBinding;
//import tw.com.chainsea.chat.databinding.GridViewBinding;
//import tw.com.chainsea.chat.lib.ActivityManager;
//import tw.com.chainsea.chat.lib.ChatService;
//import tw.com.chainsea.chat.lib.ToastUtils;
//import tw.com.chainsea.chat.mainpage.view.MainPageActivity;
//import tw.com.chainsea.chat.network.contact.ViewModelFactory;
//import tw.com.chainsea.chat.searchfilter.view.activity.CreateDiscussActivity;
//import tw.com.chainsea.chat.searchfilter.view.activity.CreateGroupActivity;
//import tw.com.chainsea.chat.searchfilter.view.fragment.ContactPersonClientSearchFragment;
//import tw.com.chainsea.chat.searchfilter.view.fragment.ServiceNumberSearchFragment;
//import tw.com.chainsea.chat.service.ActivityTransitionsControl;
//import tw.com.chainsea.chat.style.RoomThemeStyle;
//import tw.com.chainsea.chat.ui.adapter.ChatRoomMembersAdapter;
//import tw.com.chainsea.chat.ui.adapter.LoginDevicesInfoAdapter;
//import tw.com.chainsea.chat.ui.adapter.RichMenuAdapter;
//import tw.com.chainsea.chat.ui.adapter.entity.RichMenuInfo;
//import tw.com.chainsea.chat.ui.dialog.BottomSheetDialogBuilder;
//import tw.com.chainsea.chat.ui.fragment.ChatFragment;
//import tw.com.chainsea.chat.util.NoDoubleClickListener;
//import tw.com.chainsea.chat.util.SortUtil;
//import tw.com.chainsea.chat.util.UnreadUtil;
//import tw.com.chainsea.chat.view.BaseActivity;
//import tw.com.chainsea.chat.view.Information.grouppage.GroupPagerActivity;
//import tw.com.chainsea.chat.view.base.viewmodel.HomeViewModel;
//import tw.com.chainsea.chat.view.chat.ChatViewModel;
//import tw.com.chainsea.chat.view.chatroom.SingleChatSettingActivity;
//import tw.com.chainsea.chat.view.chatroom.adapter.listener.OnRoomItemClickListener;
//import tw.com.chainsea.chat.view.contact.ContactPersonFragment;
//import tw.com.chainsea.chat.view.login.LogoutSmsDialogFragment;
//import tw.com.chainsea.chat.view.roomAction.UpgradeDiscussToGroupActivity;
//import tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ServiceNumberTimeAdapter;
//import tw.com.chainsea.chat.widget.GridItemDecoration;
//import tw.com.chainsea.custom.view.alert.AlertView;
//import tw.com.chainsea.custom.view.image.CircleImageView;
//import tw.com.chainsea.custom.view.progress.IosProgressBar;
//import tw.com.chainsea.custom.view.recyclerview.MaxHeightRecyclerView;
//
//
//public class ChatActivity extends BaseActivity implements ChatRoomMembersAdapter.OnItemClickListener {
//    private static final String TAG = ChatActivity.class.getSimpleName();
//
//    private HomeViewModel homeViewModel;
//
//    private ChatViewModel chatViewModel;
//
//    private final long ANIMATE_TIME = 1;
//    private TextView rightCancelTV;
//    private ChatRoomEntity chatRoomEntity;
//    private String roomId;
//    private String userName;
//    private String userId;
//    private final List<UserProfileEntity> members = Lists.newArrayList();
//    private List<String> memberIds = Lists.newArrayList();
//    private PopupWindow popupWindow, memberPopupWindow;
//    private ChatFragment chatFragment;
//    private IosProgressBar progressBar;
//    private ChatRoomMembersAdapter chatRoomMembersAdapter;
//
//    // 是否在編輯聊天室資訊狀態
//    private boolean IS_ROOM_INFO_EDIT = false;
//    // 防止多次點擊轉場
//    private boolean CAN_NEXT = false;
//
//    private RoomThemeStyle themeStyle = RoomThemeStyle.UNDEF;
//    private ActivityChatLayoutBinding binding;
//
//    private AiffManager aiffManager;
//
//    private ActivityResultLauncher<Intent> addMemberARL = null, addProvisionalMemberARL = null, updateGroupARL = null,
//    toGroupSessionARL = null, selectCodeARL=null;
//
//    private OnRefreshProvisionalMemberListener onRefreshProvisionalMemberListener;
//    public void setOnRefreshProvisionalMemberListener(ChatActivity.OnRefreshProvisionalMemberListener onRefreshProvisionalMemberListener) {
//        this.onRefreshProvisionalMemberListener = onRefreshProvisionalMemberListener;
//    }
//    private RecyclerView aiffRV;
//    private LoginDevicesInfoAdapter loginDevicesInfoAdapter = null;
//    private boolean isDeletedMember = false;
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        initViewModel();
//        observeData();
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_layout);
//        binding.searchCancelTV.setOnClickListener(this::doSearchCancelAction);
//        binding.inviteIV.setOnClickListener(this::toInvite);
//        binding.leftAction.setOnClickListener(this::doBackAction);
//        binding.rightAction.setOnClickListener(this::rightAction);
//        binding.ivSearch.setOnClickListener(this::doSearchAction);
//        binding.ivChannel.setOnClickListener(this::doChannelChangeAction);
//        Intent intent = getIntent();
//        String friendUserId = "";
//        if(intent != null) {
//            if (intent.hasExtra(BundleKey.EXTRA_ROOM_ENTITY.key())) {
//                Serializable serializable = intent.getSerializableExtra(BundleKey.EXTRA_ROOM_ENTITY.key());
//                if (serializable instanceof ChatRoomEntity) {
//                    this.chatRoomEntity = (ChatRoomEntity) serializable;
//                    this.roomId = this.chatRoomEntity.getId();
//                } else {
//                    this.roomId = intent.getStringExtra(BundleKey.EXTRA_SESSION_ID.key());
//                    this.chatRoomEntity = ChatRoomReference.getInstance().findById2( "", roomId, true, true, true, true, true);
//                }
//            } else {
//                this.roomId = intent.getStringExtra(BundleKey.EXTRA_SESSION_ID.key());
//                this.chatRoomEntity = ChatRoomReference.getInstance().findById2( "", roomId, true, true, true, true, true);
//            }
//            if (intent.hasExtra(BundleKey.WHERE_COME.key())) {
//                String whereCome = intent.getStringExtra(BundleKey.WHERE_COME.key());
//                if (!Strings.isNullOrEmpty(whereCome) &&
//                        (ContactPersonFragment.class.getSimpleName().equals(whereCome) ||
//                                ContactPersonClientSearchFragment.class.getSimpleName().equals(whereCome) ||
//                                ServiceNumberSearchFragment.class.getSimpleName().equals(whereCome))
//                        && chatRoomEntity != null && !chatRoomEntity.getType().equals(ChatRoomType.person)) {//個人聊天室不更新交互時間
//                    ChatRoomReference.getInstance().updateInteractionTimeById(chatRoomEntity.getId());
//                    chatRoomEntity.setUpdateTime(System.currentTimeMillis());
//                }
//            }
//            //虛擬聊天室 如果不是好友, 則只會有部分資訊
//            userName = intent.getStringExtra(BundleKey.USER_NICKNAME.key());
//            userId = TokenPref.getInstance(this).getUserId();
//            friendUserId = intent.getStringExtra(BundleKey.USER_ID.key());
//            Log.d("TAG", "userName = " + userName);
//            Log.d("TAG", "userId = " + userId);
//        }
//
////        EventBusUtils.register(this);
//        if(roomId == null) CELog.e(" (roomId == null)");
//
//        if(chatRoomEntity == null) {
//            //虛擬聊天室加好友
//            CELog.e(" chatRoomEntity == null");
//            if (!Strings.isNullOrEmpty(friendUserId)) {
//                chatViewModel.getChatRoomEntity(friendUserId, userName);
//                initVirtualChatRoom();
//            }
//        }  else {
//            UserPref.getInstance(this).setCurrentRoomId(roomId);
//            aiffManager = new AiffManager(this, roomId);
//            init();
//        }
//
//        initPopupWindows();
//        getData();
//        addMemberARL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//            if (result.getResultCode() == RESULT_OK) {
//                Bundle bundle = result.getData().getExtras();
//                if (bundle != null) {
//                    Type listType = new TypeToken<ArrayList<UserProfileEntity>>(){}.getType();
//                    List<UserProfileEntity> data = JsonHelper.getInstance().from(bundle.getString("data"), listType);
//                    onInviteSuccess(data);
//                }
//            }
//        });
//
//        updateGroupARL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//            if (result.getResultCode() == RESULT_OK) {
//                if (result.getData() != null) {
//                    String sessionId = result.getData().getStringExtra(BundleKey.EXTRA_SESSION_ID.key());
//                    String groupName = result.getData().getStringExtra(BundleKey.EXTRA_TITLE.key());
//                    chatRoomEntity = (ChatRoomEntity) result.getData().getSerializableExtra(BundleKey.EXTRA_SESSION.key());
//                    if (chatRoomEntity == null) {
//                        chatRoomEntity = ChatRoomReference.getInstance().findById(sessionId);
//                    }
//                    CELog.e("升级后ChatActivity  : " + sessionId);
//                    if (chatRoomEntity != null) {
//                        CELog.e("升级后ChatActivity 能查到聊天室 : " + chatRoomEntity.getId());
//                        chatRoomEntity.setType(group);
//                        chatRoomEntity.setName(groupName);
//                        init();
//                    }
//                }
//            }
//        });
//        toGroupSessionARL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//            if (result.getResultCode() == RESULT_OK) {
//                if (result.getData() != null) {
//                    String[] extra = result.getData().getStringArrayExtra(Constant.ACTIVITY_RESULT);
//                    if(extra != null){
//                        TextViewHelper.setGroupRoomTitle(binding.title, extra[0], Integer.parseInt(extra[1]), false);
//                    }
//                }
//            }
//        });
//        selectCodeARL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//            if (result.getResultCode() == RESULT_OK) {
//                if (result.getData() != null) {
//                    final String userId = TokenPref.getInstance(this).getUserId();
//                    Bundle bundle = result.getData().getExtras();
//                    //ArrayList<String> accountIds = result.getData().getStringArrayListExtra(Constant.ACTIVITY_RESULT);
//                    Type listType = new TypeToken<List<String>>(){}.getType();
//                    List<String> accountIds = JsonHelper.getInstance().from(bundle.getString("data"), listType);
////                    final String[] accountIds = result.getData().getStringArrayExtra(Constant.ACTIVITY_RESULT);
//                    List<String> ids = Lists.newArrayList();
////                    List<UserProfileEntity> members = DBManager.getInstance().findMembersByRoomId(roomId);
//                    List<UserProfileEntity> members = ChatMemberCacheService.getChatMember(roomId);
//                    ids.add(members.get(0).getId());
//                    ids.add(members.get(1).getId());
//                    accountIds.forEach(id -> {
//                        if (!members.contains(id)) {
//                            ids.add(id);
//                        }
//                    });
//                    showLoadingView();
//                    ApiManager.doRoomCreate(this, discuss, ids, "normal", new RoomCreateRequest.Listener() {
//                        @Override
//                        public void onCreateSuccess(String id) {
//                            ApiManager.doRoomItem(ChatActivity.this, id, userId, new ApiListener<ChatRoomEntity>() {
//                                @Override
//                                public void onSuccess(ChatRoomEntity entity) {
//                                    CELog.e("jerry==邀请建立多人聊天==" + entity.toString());
//
//                                    entity.setType(discuss);
//                                    broadcastAddGroup();
////                                    if (entity.getTime() == 0) {
////                                        entity.setTime(System.currentTimeMillis());
////                                    }
//
//                                    // EVAN_FLAG 2019-10-21 創建房間成功， 這裡更新交互時間
//                                    entity.setUpdateTime(System.currentTimeMillis());
//                                    ChatRoomReference.getInstance().save(entity);
//
//                                    ThreadExecutorHelper.getHandlerExecutor().execute(() -> {
//                                        hideLoadingView();
//                                        ActivityTransitionsControl.navigateToChat(ChatActivity.this, ChatRoomReference.getInstance().findById(entity.getId()), ChatActivity.class.getSimpleName(), (intent, s) -> startActivity(intent));
//                                        ChatActivity.this.finish();
//                                    }, 500L);
//                                }
//
//                                @Override
//                                public void onFailed(String errorMessage) {
//                                    hideLoadingView();
//                                    if (!TextUtils.isEmpty(errorMessage)) {
//                                        ToastUtils.showToast(ChatActivity.this, errorMessage);
//                                    }
//                                }
//                            });
//                        }
//
//                        @Override
//                        public void onCreateFailed(String errorMessage) {
//                            if (!TextUtils.isEmpty(errorMessage)) {
//                                hideLoadingView();
//                                ToastUtils.showToast(ChatActivity.this, errorMessage);
//                            }
//                        }
//                    });
//                }
//            }
//        });
//        addProvisionalMemberARL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//            if (result.getResultCode() == RESULT_OK) {
//                Bundle bundle = result.getData().getExtras();
//                if (bundle != null) {
//                    Type listType = new TypeToken<ArrayList<String>>() {
//                    }.getType();
//                    List<String> data = JsonHelper.getInstance().from(bundle.getString("data"), listType);
//                    if(onRefreshProvisionalMemberListener!=null)
//                        onRefreshProvisionalMemberListener.onRefreshMemberList(data);
//                }
//            }
//        });
//
//        // EVAN_FLAG 2019-09-05 設定邀請權限
//        setUpInvitePermissions();
//        setLoginDevicesStatus();
//    }
//
//    private void getData() {
//        if (chatRoomEntity == null) return;
//        // -1 是被左滑標記未讀
//        if(chatRoomEntity.getUnReadNum() > 0 || chatRoomEntity.getUnReadNum() == -1) {
//            homeViewModel.setRead(chatRoomEntity);
//        }
//        // 如果在服務號列表下，只拿服務號列表的未讀數
//        if (chatRoomEntity.getListClassify().equals(ChatRoomSource.SERVICE)) {
//            homeViewModel.getServiceRoomUnReadSum();
//        } else {
//            homeViewModel.getChatRoomListUnReadSum();
//        }
//        if(chatRoomEntity.getUnReadNum() > 0) {
//            homeViewModel.setRead(chatRoomEntity);
//        }
//    }
//
//    private void observeData() {
//        homeViewModel.getChatRoomUnreadNumber().observe(this, this::setUnreadCount);
//        homeViewModel.getServiceRoomUnreadNumber().observe(this, this::setUnreadCount);
//
//        chatViewModel.getChatRoomEntity().observe(this, chatRoomEntity -> {
//            this.chatRoomEntity = chatRoomEntity;
//            this.roomId = chatRoomEntity.getId();
//            UserPref.getInstance(this).setCurrentRoomId(roomId);
//            aiffManager = new AiffManager(this, roomId);
//            init();
//            initPopupWindows();
//            getData();
//            ChatRoomReference.getInstance().updateInteractionTimeById(chatRoomEntity.getId());
//            chatRoomEntity.setUpdateTime(System.currentTimeMillis());
//            // 加完好友通知更新聊天室列表
//            EventBus.getDefault().post(new EventMsg(MsgConstant.REFRESH_ROOM_BY_LOCAL));
//        });
//        homeViewModel.getSendLoginDevicesList().observe(this, deviceRecordItems -> {
//            loginDevicesInfoAdapter.submitList(deviceRecordItems);
//            binding.scopeDevices.setVisibility(ChatRoomType.person.equals(chatRoomEntity.getType()) ? View.VISIBLE : View.GONE);
//            binding.scopeDevicesList.setVisibility(ChatRoomType.person.equals(chatRoomEntity.getType()) ? View.VISIBLE : View.GONE);
//            loginDevicesInfoAdapter.setItemClickListener( deviceRecordItem -> {
//                if(homeViewModel.getTimer().hasObservers())
//                    homeViewModel.getTimer().removeObservers(this);
//                doShowLoginDeviceSettingBottomSheetDialog(deviceRecordItem);
//                return null;
//            });
//            binding.devicesNumber.setText(String.valueOf(deviceRecordItems.size()));
//        });
//        homeViewModel.getTimer().observe(this, count -> {
//            if(count <= 0) {
//                if(binding.scopeDevicesList.getVisibility() == View.VISIBLE)
//                    binding.scopeDevicesList.setVisibility(View.GONE);
//            }
//        });
//        homeViewModel.getSendToast().observe(this, res -> ToastUtils.showToast(this, getString(res)));
//        chatViewModel.getSendUpdateMember().observe(this, entity -> { //聊天室成員被剔除或新增，更新title及成員數
//            members.clear();
//            members.addAll(entity.getSecond().getMembers());
//            isDeletedMember = entity.getFirst();
//            if(entity.getSecond().getType().equals(group)) {
//                TextViewHelper.setGroupRoomTitle(binding.title, entity.getSecond().getName(), entity.getSecond().getMemberIds().size(), false);
//            } else {
//                TextViewHelper.setDiscussTitle(binding.title, entity.getSecond().getName(), entity.getSecond().getChatRoomMember().size(), false);
//            }
//            if(chatRoomMembersAdapter!=null) {
//                chatRoomMembersAdapter
//                        .setData(entity.getSecond().getMembers())
//                        .refreshData();
//            }
//        });
//        chatViewModel.getSendProvisionalMember().observe(this, list -> {
//            if(onRefreshProvisionalMemberListener!=null)
//                onRefreshProvisionalMemberListener.onRefreshMemberList(list);
//        });
//
//        chatViewModel.getSendCloseChatActivity().observe(this, Object -> { //已不是聊天室成員
//            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.REMOVE_GROUP_FILTER));
//            finish();
//        });
//    }
//
//    private void doShowLoginDeviceSettingBottomSheetDialog(DeviceRecordItem deviceRecordItem) {
//        new BottomSheetDialogBuilder(this, getLayoutInflater())
//                .getOnlineDeviceOperation(deviceRecordItem,
//                        isSelf -> {
//                            if(isSelf) { //本機登出
//                                new LogoutSmsDialogFragment().show(getSupportFragmentManager(), "Logout");
//                            } else {
//                                //其他設備強制登出
//                                new AlertView.Builder()
//                                        .setContext(this)
//                                        .setStyle(AlertView.Style.Alert)
//                                        .setMessage(getString(R.string.text_device_force_logout_tip))
//                                        .setOthers(new String[]{getString(R.string.cancel), getString(R.string.text_for_sure)})
//                                        .setOnItemClickListener((o, pos) -> {
//                                            if (pos == 1) {
//                                                homeViewModel.doForceLogoutDevice(Objects.requireNonNull(deviceRecordItem.getDeviceId()));
//                                            }
//                                        })
//                                        .build()
//                                        .setCancelable(true)
//                                        .show();
//                            }
//                            return null;
//                        },
//                        () -> {//自動登入設置
//                            if(deviceRecordItem.getRememberMe()) {
//                                //取消自動登入
//                                new AlertView.Builder()
//                                        .setContext(this)
//                                        .setStyle(AlertView.Style.Alert)
//                                        .setMessage(getString(R.string.text_device_cancel_auto_login_tip))
//                                        .setOthers(new String[]{getString(R.string.cancel), getString(R.string.text_for_sure)})
//                                        .setOnItemClickListener((o, pos) -> {
//                                            if (pos == 1) {
//                                                homeViewModel.doCancelAutoLogin(deviceRecordItem.getId());
//                                            }
//                                        })
//                                        .build()
//                                        .setCancelable(true)
//                                        .show();
//                            } else {
//                                //允許自動登入
//                                new AlertView.Builder()
//                                        .setContext(this)
//                                        .setStyle(AlertView.Style.Alert)
//                                        .setMessage(getString(R.string.text_device_allow_auto_login_tip))
//                                        .setOthers(new String[]{getString(R.string.cancel), getString(R.string.text_for_sure)})
//                                        .setOnItemClickListener((o, pos) -> {
//                                            if (pos == 1) {
//                                                homeViewModel.doAllowAutoLogin(deviceRecordItem.getId());
//                                            }
//                                        })
//                                        .build()
//                                        .setCancelable(true)
//                                        .show();
//                            }
//                            return null;
//                        },
//                        () -> {
//                            //刪除設備
//                            new AlertView.Builder()
//                                    .setContext(this)
//                                    .setStyle(AlertView.Style.Alert)
//                                    .setMessage(getString(R.string.text_device_delete_login_device_tip))
//                                    .setOthers(new String[]{getString(R.string.cancel), getString(R.string.text_for_sure)})
//                                    .setOnItemClickListener((o, pos) -> {
//                                        if (pos == 1) {
//                                            homeViewModel.doDeleteLoginDevice(deviceRecordItem.getUniqueID(), deviceRecordItem.getId(), deviceRecordItem.getDeviceId());
//                                        }
//                                    })
//                                    .build()
//                                    .setCancelable(true)
//                                    .show();
//                            return null;
//                        }
//                ).show();
//    }
//
//    private void initViewModel() {
//        ViewModelFactory viewModelFactory = new ViewModelFactory(this.getApplication());
//        homeViewModel = new ViewModelProvider(this, viewModelFactory).get(HomeViewModel.class);
//        chatViewModel = new ViewModelProvider(this, viewModelFactory).get(ChatViewModel.class);
//    }
//
//    private void initPopupWindows() {
//        View contentView = LayoutInflater.from(this).inflate(R.layout.grid_view, null);
//        contentView.setBackgroundColor(themeStyle.getMainColor());
//        aiffRV = contentView.findViewById(R.id.recycler_view);
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
//        aiffRV.setLayoutManager(gridLayoutManager);
//        aiffRV.addItemDecoration(new GridItemDecoration());
//        aiffRV.setItemAnimator(new DefaultItemAnimator());
//        aiffRV.setHasFixedSize(true);
//        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        popupWindow.setBackgroundDrawable(new ColorDrawable(0x10101010));
//        popupWindow.setOutsideTouchable(true);
//        popupWindow.setFocusable(false);
//        popupWindow.setOnDismissListener(() -> binding.rightAction.setImageResource(R.drawable.icon_aipower_open));
//        aiffRV.measure(0, 0);
//    }
//
//    private void initVirtualChatRoom() {
//        binding.title.setText(getTitleText());
//        addFragment(ChatFragment.newInstance(userName, userId));
//    }
//
//    private void calculateUnreadNumber() {
//        int count =
//                homeViewModel.getServiceRoomUnreadNumber().getValue() + homeViewModel.getChatRoomUnreadNumber().getValue();
//        if (count == 0) {
//            binding.unreadNum.setVisibility(View.GONE);
//        } else {
//            binding.unreadNum.setText(UnreadUtil.INSTANCE.getUnreadText(count));
//            binding.unreadNum.setVisibility(View.VISIBLE);
//        }
//    }
//
//    private void init() {
////        Utils.getDefKeyboardHeight(this);
//        // EVAN_FLAG 2020-03-03 (1.10.0) 設置聊天室主題風格
//        if (chatRoomEntity != null) {
//            if (chatRoomEntity.getType() != null) {
//                if (ChatRoomType.services.equals(chatRoomEntity.getType())) {
//                    String selfId = TokenPref.getInstance(this).getUserId();
//                    if (ServiceNumberType.BOSS.equals(chatRoomEntity.getServiceNumberType()) && selfId.equals(chatRoomEntity.getServiceNumberOwnerId())) {
//                        themeStyle = RoomThemeStyle.UNDEF;
//                    } else {
//                        themeStyle = RoomThemeStyle.SERVICES;
//                    }
//                }
//                if (!Strings.isNullOrEmpty(chatRoomEntity.getBusinessId())) {
//                    themeStyle = RoomThemeStyle.BUSINESS;
//                }
//            }
//
//            MessageEntity msg = (MessageEntity) getIntent().getSerializableExtra(BundleKey.EXTRA_MESSAGE.key());
//            String keyWord = getIntent().getStringExtra(BundleKey.SEARCH_KEY.key());
//            String unReadId = getIntent().getStringExtra(BundleKey.UNREAD_MESSAGE_ID.key());
//            if(msg == null) Log.d("TAG", "msg == null");
//            else  Log.d("TAG", "msg != null");
//            addFragment(ChatFragment.newInstance(msg, chatRoomEntity, unReadId, keyWord));
//
//            if (ChatRoomType.GROUP_or_DISCUSS.contains(chatRoomEntity.getType())) {
//                getSessionMembers();
//            }
//        }
//
//        themeStyle();
//        setTitleBar();
//        if(roomId != null){
//            ApiManager.doMemberList(this, roomId, new ApiListener<List<UserProfileEntity>>() {
//                @Override
//                public void onSuccess(List<UserProfileEntity> memberList) {
//                    for (UserProfileEntity profile : memberList) {
//                        if (!Strings.isNullOrEmpty(profile.getAvatarId())) {
//                            MessageReference.updateMessageAvatar(null, profile.getId(), profile.getAvatarId());
//                        }
//                    }
//                }
//
//                @Override
//                public void onFailed(String errorMessage) {}
//            });
//        }
//    }
//
//    public void setThemeStyle(RoomThemeStyle themeStyle) {
//        this.themeStyle = themeStyle;
//        themeStyle();
//    }
//
//    private void themeStyle() {
//        getWindow().setStatusBarColor(themeStyle.getMainColor());
//        binding.titleBar.setBackgroundColor(themeStyle.getMainColor());
//        binding.searchBar.setBackgroundColor(themeStyle.getMainColor());
//    }
//
//    private void addFragment(ChatFragment fragment) {
//        chatFragment = createFragment(fragment);
//        chatFragment.setOnChatRoomTitleChangeListener(title -> binding.title.setText(title));
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        for (Fragment f : getSupportFragmentManager().getFragments()) {
//            if (f instanceof ChatFragment) {
//                getSupportFragmentManager().beginTransaction().remove(f).commit();
//            }
//        }
//        if (!chatFragment.isAdded()) {
//            transaction.add(R.id.contentFL, chatFragment, TAG);
//        }
//        transaction.show(chatFragment).commit();
//    }
//
//    protected void setTitleBar() {
//        binding.title.setGravity(Gravity.CENTER);
//        if (chatRoomEntity != null && ChatRoomType.subscribe.equals(chatRoomEntity.getType())) {
//            binding.title.setText(TextViewHelper.setLeftImage(this, getTitleText(), R.drawable.icon_subscribe_number_pink_15dp));
//            binding.rightAction.setImageResource(R.drawable.icon_aipower_open);
//        } else if (chatRoomEntity != null && ChatRoomType.services.equals(chatRoomEntity.getType())) {
//            binding.title.setText(chatRoomEntity.getServiceNumberName());
//            // get user data
//            getServiceIcon(chatRoomEntity.getOwnerId());
//        } else if (chatRoomEntity != null && ChatRoomType.serviceMember.equals(chatRoomEntity.getType())) {
//            binding.rightAction.setVisibility(View.VISIBLE);
//            binding.rightAction.setImageResource(R.drawable.icon_aipower_open);
//            binding.title.setText(chatRoomEntity != null && ChatRoomType.serviceMember.equals(chatRoomEntity.getType()) ?
//                    chatRoomEntity.getServiceNumberType().equals(ServiceNumberType.BOSS) && chatRoomEntity.getServiceNumberOwnerId().equals(userId) ? TextViewHelper.setLeftImage(this, getTitleText(), R.drawable.ic_service_member_b) : TextViewHelper.setLeftImage(this, getTitleText(), R.drawable.ic_service_member_group_16dp) : getTitleText());
//        } else if (chatRoomEntity != null && ChatRoomType.group.equals(chatRoomEntity.getType())) {
//            TextViewHelper.setGroupRoomTitle(binding.title, chatRoomEntity.getName(), chatViewModel.getGroupMember(chatRoomEntity.getId()), false);
//        } else if (chatRoomEntity != null && ChatRoomType.person.equals(chatRoomEntity.getType())) {
//            binding.title.setText(TextViewHelper.setLeftImage(this, getTitleText(), R.drawable.icon_self_chat_room_20dp));
//        } else if (chatRoomEntity != null && discuss.equals(chatRoomEntity.getType())) {
//            TextViewHelper.setDiscussTitle(binding.title, chatRoomEntity.getName(), chatRoomEntity.getChatRoomMember() != null ? chatRoomEntity.getChatRoomMember().size() : chatRoomEntity.getMemberIds().size(), false);
//        } else if(chatRoomEntity == null && userId != null){ //虛擬聊天室 訪客未加好友狀態
//            binding.rightAction.setVisibility(View.GONE);
//        } else {
//            binding.rightAction.setImageResource(R.drawable.icon_aipower_open);
//            TokenPref.getInstance(this).isEnableCall();
//            binding.title.setText(getTitleText());
//        }
//    }
//
//    /**
//     * 依照user type 顯示不同Icon
//     */
//    private void getServiceIcon(String userId) {
//        UserProfileService.getProfileIsEmployee(this, userId, new ServiceCallBack<UserType, RefreshSource>() {
//            @Override
//            public void complete(UserType type, RefreshSource refreshSource) {
//                switch (type) {
//                    case VISITOR:
//                        binding.title.setText(TextViewHelper.setLeftImage(ChatActivity.this, chatRoomEntity.getName(), R.drawable.ic_visitor_15dp));
//                        break;
//                    case CONTACT:
//                        binding.title.setText(TextViewHelper.setLeftImage(ChatActivity.this, chatRoomEntity.getName(), R.drawable.ic_customer_15dp));
//                        break;
//                }
//            }
//
//            @Override
//            public void error(String message) {
//                binding.title.setText(TextViewHelper.setLeftImage(ChatActivity.this, chatRoomEntity.getName(), R.drawable.ic_customer_15dp));
//            }
//        });
//    }
//
//    /**
//     * 若如果聊天室類型為{SessionType} 點擊 title 後行為。
//     * 多人或社團: 顯示該聊天室成員列表
//     * 單人或朋友: 直接進入對象主頁
//     */
//    protected ChatFragment createFragment(ChatFragment mFragment) {
//        if (chatRoomEntity == null) {
//
//        } else if (ChatRoomType.person.equals(chatRoomEntity.getType())) {
//            binding.titleBar.setOnClickListener(v -> {
//                ActivityTransitionsControl.navigateToSelfPage(this, (intent, s) -> startActivity(intent));
//            });
//        } else if (ChatRoomType.services.equals(chatRoomEntity.getType())) {
//            if (!Strings.isNullOrEmpty(chatRoomEntity.getBusinessId())) {
//                binding.titleBar.setOnClickListener(v -> {
//                    List<ChatRoomEntity> businessEntities = ChatRoomReference.getInstance().findServiceBusinessByServiceNumberIdAndOwnerIdAndNotRoomId(chatRoomEntity.getServiceNumberId(), chatRoomEntity.getOwnerId(), chatRoomEntity.getId(), ChatRoomType.services);
//                    showSinkingBusinessMemberListPopupWindow(chatRoomEntity.getId(), chatRoomEntity.getBusinessId(), businessEntities);
//                });
//            } else {
//                if (ChatRoomType.services.equals(chatRoomEntity.getType())) {
//                    binding.titleBar.setOnClickListener(v -> {
//                        if (CAN_NEXT) {
//                            CAN_NEXT = false;
//                            UserProfileEntity profile = UserProfileReference.findById(null, chatRoomEntity.getOwnerId());
//                            if (profile == null) {
//                                UserProfileService.getProfile(this, RefreshSource.REMOTE, chatRoomEntity.getOwnerId(), new ServiceCallBack<UserProfileEntity, RefreshSource>() {
//                                    @Override
//                                    public void complete(UserProfileEntity userProfileEntity, RefreshSource source) {
//                                        ActivityManager.addActivity(ChatActivity.this);
//                                        if(UserType.VISITOR.equals(profile.getUserType()) || UserType.CONTACT.equals(profile.getUserType())) {
//                                            if(!checkClientMainPageFromAiff()) {
//                                                ActivityTransitionsControl.navigateToVisitorHomePage(ChatActivity.this, chatRoomEntity.getOwnerId(), roomId, profile.getUserType(), profile.getNickName(), (intent, s) -> {
//                                                    startActivity(intent.putExtra(BundleKey.WHERE_COME.key(), profile.getName()));
//                                                });
//                                            }
//                                        }else{
//                                            ActivityTransitionsControl.navigateToEmployeeHomePage(ChatActivity.this, userProfileEntity.getId(), userProfileEntity.getUserType(), (intent, s) -> startActivity(intent));
//                                        }
//                                    }
//
//                                    @Override
//                                    public void error(String message) {
//                                        CELog.e("");
//                                    }
//
//                                });
//                            } else {
//                                if(UserType.VISITOR.equals(profile.getUserType()) || UserType.CONTACT.equals(profile.getUserType())) {
//                                    if(!checkClientMainPageFromAiff()) {
//                                        ActivityTransitionsControl.navigateToVisitorHomePage(ChatActivity.this, chatRoomEntity.getOwnerId(), roomId, profile.getUserType(), profile.getNickName(), (intent, s) -> {
//                                            startActivity(intent.putExtra(BundleKey.WHERE_COME.key(), profile.getName()));
//                                        });
//                                    }
//                                }else{
//                                    ActivityTransitionsControl.navigateToEmployeeHomePage(ChatActivity.this, profile.getId(), profile.getUserType(), (intent, s) -> startActivity(intent));
//                                }
//                            }
//                        }
//                    });
//                }
//            }
//        } else if (ChatRoomType.GROUP_or_DISCUSS.contains(chatRoomEntity.getType())) {
//            binding.titleBar.setOnClickListener(v -> {
//                if (memberPopupWindow != null && memberPopupWindow.isShowing()) return;
//                showSinkingMemberListPopupWindow(chatRoomEntity.getId(), chatRoomEntity.getBusinessId(), members);
//            });
//        } else if (ChatRoomType.FRIEND_or_STRANGE.contains(chatRoomEntity.getType())) {
//            if (!Strings.isNullOrEmpty(chatRoomEntity.getBusinessId())) {
//                binding.titleBar.setOnClickListener(v -> showSinkingBusinessMemberListPopupWindow(chatRoomEntity.getId(), chatRoomEntity.getBusinessId(), Lists.newArrayList()));
//            } else {
//                binding.titleBar.setOnClickListener(v -> {
//                    List<String> memberIds = AccountRoomRelReference.findMemberIdsByRoomId(null, chatRoomEntity.getId());
//
//                    if (memberIds.size() > 1) {
//                        String userId = TokenPref.getInstance(this).getUserId();
//                        for (String id : memberIds) {
//                            if (!id.equals(userId)) {
//                                if (CAN_NEXT) {
//                                    CAN_NEXT = false;
//                                    UserProfileService.getProfile(this, RefreshSource.REMOTE, id, new ServiceCallBack<UserProfileEntity, RefreshSource>() {
//                                        @Override
//                                        public void complete(UserProfileEntity userProfileEntity, RefreshSource source) {
//                                            CELog.e("");
//                                            ActivityManager.addActivity(ChatActivity.this);
//                                            ActivityTransitionsControl.navigateToEmployeeHomePage(ChatActivity.this, userProfileEntity.getId(), userProfileEntity.getUserType(), new ActivityTransitionsControl.CallBack<Intent, String>() {
//                                                @Override
//                                                public void complete(Intent intent, String s) {
//                                                    startActivity(intent);
//                                                }
//                                            });
//
//                                        }
//
//                                        @Override
//                                        public void error(String message) {
//                                            CELog.e("");
//                                        }
//
//                                    });
//                                }
//                                break;
//                            }
//                        }
//                    }
//                });
//            }
//        } else if (ChatRoomType.serviceMember.equals(chatRoomEntity.getType())) {
//            binding.titleBar.setOnClickListener(v -> {
//                showSinkingServiceMemberListPopupWindow(chatRoomEntity.getId());
//            });
//        } else if (ChatRoomType.subscribe.equals(chatRoomEntity.getType())) {
//            binding.titleBar.setOnClickListener(v -> {
//                navigateToSubscribePage();
//            });
//        }
//        return mFragment;
//    }
//
//    private void getSessionMembers() {
//        if(roomId == null) return;
//        List<UserProfileEntity> members = ChatMemberCacheService.getChatMember(roomId);
//        this.members.clear();
//        for (UserProfileEntity member : members) {
//            if (member.getId().equals(chatRoomEntity.getOwnerId())) {
//                this.members.add(0, member);
//            } else {
//                this.members.add(member);
//            }
//        }
//
//        ApiManager.doRoomHomePage(this, roomId, new ApiListener<CrowdEntity>() {
//            @Override
//            public void onSuccess(CrowdEntity crowdEntity) {
//                if (crowdEntity == null) {
//                    return;
//                }
//                ChatMemberCacheService.refresh(roomId);
//                ChatActivity.this.members.clear();
////                AccountRoomRelReference.deleteRelByRoomId(null, roomId);
//                chatRoomEntity.setOwnerId(crowdEntity.getOwnerId());
//                ChatRoomReference.getInstance().updateOwnerIdById(crowdEntity.getId(), crowdEntity.getOwnerId());
//                Map<String, String> avatarsData = Maps.newHashMap();
//                //將擁有者放到第一個 再來是管理者 最後才是一般成員
//                List<UserProfileEntity> soredList = SortUtil.INSTANCE.sortOwnerManagerByBoolean(crowdEntity.getMemberArray());
//
//                for (UserProfileEntity member : soredList) {
//                    ChatActivity.this.members.add(member);
////                    DBManager.getInstance().insertFriends(member);
//                    CELog.d("聊天室存成员数据库" + roomId);
//                    if (!Strings.isNullOrEmpty(member.getAvatarId())) {
//                        avatarsData.put(member.getId(), member.getAvatarId());
//                    }
//                }
//
//                UserProfileReference.saveUserProfiles(null, Sets.newHashSet(crowdEntity.getMemberArray()));
//                MessageReference.updateMessageAvatars(null, avatarsData);
//                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//                    if (chatRoomMembersAdapter != null) {
//                        chatRoomMembersAdapter.refreshData();
//                    }
//                });
//            }
//
//            @Override
//            public void onFailed(String errorMessage) {
//
//            }
//        });
//    }
//
//    // EVAN_FLAG 2019-09-05 如果聊天是類型不是，就顯示邀請加入按鈕功能
//    //  SessionType.SERVICES = 6;//服务号聊天室
//    //  SessionType.SUBSCRIBE = 7;//订阅服务号聊天室
//    //  SessionType.SYSTEM = 12;//系統聊天室
//    private void setUpInvitePermissions() {
//        if(chatRoomEntity == null) return;
//        if (!ChatRoomType.SERVICES_or_SUBSCRIBE_or_SYSTEM_or_PERSON_or_SERVICE_MEMBER.contains(chatRoomEntity.getType())) {
//            binding.inviteIV.setVisibility(View.VISIBLE);
//        } else {
//            binding.inviteIV.setVisibility(View.GONE);
//        }
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        // EVAN_FLAG 2019-11-12 解決從背景回來無法同步當下信息已讀問題。
//        if(chatRoomEntity != null) {
//            ChatMessageService.doMessageReadAllByRoomId(this, chatRoomEntity, chatRoomEntity.getUnReadNum(), null, new ServiceCallBack<ChatRoomEntity, Enum>() {
//                @Override
//                public void complete(ChatRoomEntity entity, Enum anEnum) {
////                CELog.w(chatRoomEntity + "");
//                    if (entity != null) {
//                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.REFRESH_ROOM_BY_LOCAL, JsonHelper.getInstance().toJson(entity)));
//                    }
//                }
//
//                @Override
//                public void error(String message) {
//                }
//            });
//            chatRoomEntity.setUnReadNum(0);
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        CAN_NEXT = true;
//        if (chatRoomEntity != null) {
//            ChatRoomService.getInstance().getBadge(this, chatRoomEntity.getId());
//        }
//        //设置未读消息数
//        App.isNotification = false;
//        App.isChatPager = true;
//        //删除所有通知A
//        // EVAN_FLAG 2020-02-18 (1.10.0) 暫時拔除 linphone
//    }
//
//    private void setLoginDevicesStatus() {
//        if(chatRoomEntity == null) return;
//        if(ChatRoomType.person.equals(chatRoomEntity.getType())) {
//            if(loginDevicesInfoAdapter == null)
//                loginDevicesInfoAdapter = new LoginDevicesInfoAdapter();
//            binding.RvLoginDevices.setAdapter(loginDevicesInfoAdapter);
//            homeViewModel.startCountdown(5);
//            homeViewModel.getLoginDevicesList();
//            binding.ivDevices.setOnClickListener(new NoDoubleClickListener() {
//                @Override
//                protected void onNoDoubleClick(View v) {
//                    binding.scopeDevicesList.setVisibility(binding.scopeDevicesList.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
//                }
//            });
//        }
//    }
//
//    //TODO 是否可以與ChatRoomEntity的getTitle合併
//    public String getTitleText() {
//        ChatRoomEntity entity = ChatRoomReference.getInstance().findById(roomId);
//        if (entity == null) entity = chatRoomEntity;
//
//        binding.tvBusinessName.setVisibility(View.GONE);
//        String userId = TokenPref.getInstance(this).getUserId();
//        if (entity != null) {
//
//            if(chatRoomEntity != null) chatRoomEntity.setCustomName(entity.isCustomName());
//
//            ChatRoomType type = entity.getType();
//
//            if (!Strings.isNullOrEmpty(entity.getBusinessName())) {
//                binding.tvBusinessName.setVisibility(View.VISIBLE);
//                binding.tvBusinessName.setText(MessageFormat.format("{0}", entity.getBusinessName()));
//            }
//
//            if (ChatRoomType.services.equals(type)) {
//
//                if (ServiceNumberType.BOSS.equals(entity.getServiceNumberType())) {
//                    if (!userId.equals(entity.getServiceNumberOwnerId())) {
//                        return entity.getName();
//                    }
//                    return chatRoomEntity.getServicesNumberTitle(userId);
//                } else if (!entity.getOwnerId().equals(userId)) {
//                    String titleName = "未知";
//                    if (entity.getName() != null && !entity.getName().isEmpty()) {
//                        titleName = entity.getName();
//                    }else {
//                        UserProfileEntity userProfileEntity = DBManager.getInstance().queryFriend(entity.getOwnerId());
//                        if (userProfileEntity != null) {
//                            titleName = !Strings.isNullOrEmpty(userProfileEntity.getName()) ? userProfileEntity.getName() : "未知";
//                        }
//                    }
//                    return titleName;
//                }
//            }
//
//            if (ChatRoomType.serviceMember.equals(type)) {
//                List<String> memberIds = AccountRoomRelReference.findMemberIdsByRoomId(null, roomId);
//                if (userId.equals(entity.getServiceNumberOwnerId())) {
//                    binding.tvBusinessName.setVisibility(View.GONE);
////                    this.tvBusinessName.setText("" + entity.getServiceNumberName() + "和祕書群(" + memberIds.size() + ")");
//                    return entity.getServiceNumberName() + "和祕書群(" + memberIds.size() + ")";
//                } else {
//                    binding.tvBusinessName.setVisibility(View.VISIBLE);
//                    binding.tvBusinessName.setText(MessageFormat.format("{0}", entity.getServiceNumberName()));
//                    return entity.getServiceNumberType().equals(ServiceNumberType.BOSS)
//                            ? getString(R.string.text_chat_room_service_member_business, "(" + memberIds.size() + ")")
//                            : getString(R.string.text_chat_room_service_member_other, "(" + memberIds.size() + ")");
//                }
//            }
//
//            return entity.getTitle(this);
//        } else if (userName != null){ //虛擬聊天室 使用者名稱用user name
//            return userName;
//        } else {
//            return null;
//        }
//    }
//    private void rightViewToggle() {
//        if(chatRoomEntity == null) return;
//
//        //ViewCompat.animate(binding.rightAction).rotation(180).setDuration(ANIMATE_TIME).start();
//        binding.rightAction.setImageResource(R.drawable.icon_aipower_close);
//
//        if (ChatRoomType.person.equals(chatRoomEntity.getType())) {
//            showSelfPopupWindow();
//        } else if (system.equals(chatRoomEntity.getType())) {
//            showSystemPopupWindow();
//        } else if (discuss.equals(chatRoomEntity.getType())) {
//            showDiscussPopupWindow();
//        } else if (ChatRoomType.friend.equals(chatRoomEntity.getType())) {
//            showSinglePopupWindow();
//        } else if (ChatRoomType.group.equals(chatRoomEntity.getType())) {
//            //社團聊天室
//            showGroupPopupWindow();
//        } else if (ChatRoomType.serviceMember.equals(chatRoomEntity.getType()) && ServiceNumberType.BOSS.equals(chatRoomEntity.getServiceNumberType())) {
//            //商務號秘書群聊天室
//            showBossServiceMemberPopupWindow();
//        } else if (ChatRoomType.services.equals(chatRoomEntity.getType()) && ServiceNumberType.BOSS.equals(chatRoomEntity.getServiceNumberType())) {
//            //商務號聊天室
//            showServicePopupWindow();
//        } else if ((ChatRoomType.services.equals(chatRoomEntity.getType()) && chatRoomEntity.getServiceNumberOpenType().contains("I") && chatRoomEntity.getOwnerId().equals(userId)) ||
//                (ChatRoomType.subscribe.equals(chatRoomEntity.getType()) && chatRoomEntity.getServiceNumberOpenType().contains("I"))
//        ) {
//            //服務號員工進線聊天室(詢問者)
//            //訂閱內部服務號
//            showServiceRoomEmployeePopupWindow();
//        } else if (ChatRoomType.services.equals(chatRoomEntity.getType()) && chatRoomEntity.getServiceNumberOpenType().contains("I") && !chatRoomEntity.getOwnerId().equals(userId)) {
//            //服務號員工進線聊天室(服務人員)
//            showServiceRoomAgentPopupWindow();
//        } else if (ChatRoomType.serviceMember.equals(chatRoomEntity.getType()) && !ServiceNumberType.BOSS.equals(chatRoomEntity.getServiceNumberType())) {
//            //服務號服務成員聊天室
//            showServiceMemberRoomPopupWindow();
//        } else if (ChatRoomType.services.equals(chatRoomEntity.getType()) && chatRoomEntity.getServiceNumberOpenType().contains("O") && !chatRoomEntity.getOwnerId().equals(userId)) {
//            //服務號客戶進線聊天室
//            showServiceRoomContactPopupWindow();
//        }
////        else
////            showServiceMemberPopupWindow();
//    }
//
//    /**
//     * 右上下拉選單
//     * @param isEmployee 判斷內部服務號 是否是需要諮詢的員工 或是 服務人員
//     * */
//    private PopupWindow getPopupWindow(List<RichMenuInfo> aiffList, String displayLocation, boolean isEmployee) {
//        GridViewBinding popupWindowBinding = GridViewBinding.inflate(getLayoutInflater());
//        popupWindowBinding.getRoot().setBackgroundColor(themeStyle.getMainColor());
//        PopupWindow popupWindow = new PopupWindow(popupWindowBinding.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        //rich menu
//        List<RichMenuInfo> richMenuInfoList = getRichMenuList(isEmployee);
////        if (ChatRoomType.subscribe.equals(chatRoomEntity.getType())) {
////            richMenuInfoList.remove(0);
////        }
//        richMenuInfoList.addAll(aiffList);
//        // recyclerView adapter
//        RichMenuAdapter richMenuAdapter = new RichMenuAdapter(richMenuInfoList.size() >= 16 ? richMenuInfoList.subList(0, 16) : richMenuInfoList);
//        richMenuAdapter.setOnItemClickListener((adapter, view, position) -> {
//            popupWindow.dismiss();
//            RichMenuInfo info = (RichMenuInfo) adapter.getItem(position);
//            onRichMenuItemClick(info);
//        });
//
//        if (aiffList.isEmpty()) {
//            popupWindowBinding.tvMore.setVisibility(View.GONE);
//        } else {
//            popupWindowBinding.tvMore.setVisibility(View.VISIBLE);
//            popupWindowBinding.tvMore.setOnClickListener(v -> {
//                popupWindow.dismiss();
//                doMoreMenuAction(AiffEmbedLocation.ChatRoomMenu.name(), displayLocation, aiffList,
//                        richMenuAdapter);
//            });
//        }
//
//        // recyclerView
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
//        popupWindowBinding.recyclerView.setLayoutManager(gridLayoutManager);
//        popupWindowBinding.recyclerView.addItemDecoration(new GridItemDecoration());
//        popupWindowBinding.recyclerView.setItemAnimator(new DefaultItemAnimator());
//        popupWindowBinding.recyclerView.setHasFixedSize(true);
//        popupWindowBinding.recyclerView.setAdapter(richMenuAdapter);
//
//        // popupWindows setting
//        popupWindow.setBackgroundDrawable(new ColorDrawable(0x10101010));
//        popupWindow.setOutsideTouchable(true);
//        popupWindow.setFocusable(true);
//        popupWindow.setOnDismissListener(() -> binding.rightAction.setImageResource(R.drawable.icon_aipower_open));
//        popupWindowBinding.recyclerView.measure(0, 0);
//
//        return popupWindow;
//    }
//
//    /**
//     * 右上下拉選單
//     * */
//    private PopupWindow getPopupWindow(List<RichMenuInfo> aiffList, String displayLocation) {
//        return getPopupWindow(aiffList, displayLocation, chatRoomEntity.getProvisionalIds().contains(userId) && chatRoomEntity.getListClassify() == ChatRoomSource.MAIN);
//    }
//
//    private List<RichMenuInfo> getAiffList(String... displayLocation) {
//        List<AiffInfo> aiffInfoList = AiffDB.getInstance(this).getAiffInfoDao().getAiffInfoListByIndex();
//        Set<RichMenuInfo> aiffSet = new LinkedHashSet<>();
//        aiffInfoList.forEach(aiffInfo -> {
//            if (!aiffInfo.getEmbedLocation().equals(AiffEmbedLocation.ChatRoomMenu.name())) return;
//            for (String location : displayLocation) {
//                if (aiffInfo.getDisplayLocation().contains(location)) {
//                    RichMenuInfo info = new RichMenuInfo(RichMenuInfo.MenuType.AIFF.getType(), aiffInfo.getId(), aiffInfo.getPictureId(),
//                            aiffInfo.getTitle(), aiffInfo.getName(), aiffInfo.getPinTimestamp(), aiffInfo.getUseTimestamp());
//                    aiffSet.add(info);
//                }
//            }
//        });
//        List<RichMenuInfo> aiffList = Lists.newArrayList(aiffSet);
//        Collections.sort(aiffList);
//        return  aiffList;
//    }
//
//    private List<RichMenuInfo> getRichMenuList(boolean isEmployee) {
//        boolean isServiceNumber = chatRoomEntity.getType().equals(ChatRoomType.services);
//        List<RichMenuInfo> richMenuInfoList = RichMenuInfo.getServiceNumberChatRoomTopRichMenus(chatRoomEntity.isMute(), isServiceNumber, isEmployee||chatRoomEntity.getType()!=ChatRoomType.services);
//        RichMenuInfo richMenuInfo1 = null;
//        RichMenuInfo richMenuInfo2 = null;
//        RichMenuInfo richMenuInfo3;
//        String userId = TokenPref.getInstance(ChatActivity.this).getUserId();
//        switch (chatRoomEntity.getType()) {
//            case group:
//                if (chatRoomEntity.getOwnerId().equals(userId)) { //群組且群主，解散群組
//                    richMenuInfo1 = new RichMenuInfo(RichMenuInfo.MenuType.FIXED.getType(),
//                            RichMenuInfo.FixedMenuId.DISMISS_CROWD, R.drawable.ic_quit, R.string.base_top_rich_menu_dismiss_crowd, true);
//                } else {     //群組且群聊
//                    richMenuInfo1 = new RichMenuInfo(RichMenuInfo.MenuType.FIXED.getType(),
//                            RichMenuInfo.FixedMenuId.EXIT_CROWD, R.drawable.ic_quit, R.string.base_top_rich_menu_exit_crowd, true);
//                }
//                richMenuInfo2 = new RichMenuInfo(RichMenuInfo.MenuType.FIXED.getType(),
//                        RichMenuInfo.FixedMenuId.MAIN_PAGE, R.drawable.ic_home_page, R.string.text_home_page, true);
//                richMenuInfoList.add(richMenuInfo2);
//                richMenuInfoList.add(richMenuInfo1);
//                break;
//            case discuss:
//                richMenuInfo1 = new RichMenuInfo(RichMenuInfo.MenuType.FIXED.getType(),
//                        RichMenuInfo.FixedMenuId.UPGRADE, R.drawable.new_group, R.string.text_transfer_to_crowd, true);
//                richMenuInfo2 = new RichMenuInfo(RichMenuInfo.MenuType.FIXED.getType(),
//                        RichMenuInfo.FixedMenuId.EXIT_DISCUSS, R.drawable.ic_quit, R.string.base_top_rich_menu_exit_discuss, true);
//                richMenuInfo3 = new RichMenuInfo(RichMenuInfo.MenuType.FIXED.getType(),
//                        RichMenuInfo.FixedMenuId.MAIN_PAGE, R.drawable.ic_home_page, R.string.text_home_page, true);
//                richMenuInfoList.add(richMenuInfo1);
//                richMenuInfoList.add(richMenuInfo3);
//                richMenuInfoList.add(richMenuInfo2);
//                break;
//        }
//
//        return richMenuInfoList;
//    }
//
//    private void showBossServiceMemberPopupWindow() {
//        List<RichMenuInfo> aiffList = getAiffList(AiffDisplayLocation.BossServiceMemberRoom.name());
//        getPopupWindow(aiffList, AiffDisplayLocation.BossServiceMemberRoom.name()).showAsDropDown(binding.titleBar);
//    }
//    private void showSystemPopupWindow() {
//        List<RichMenuInfo> aiffList = getAiffList(AiffDisplayLocation.SystemRoom.name());
//        getPopupWindow(aiffList, AiffDisplayLocation.SystemRoom.name()).showAsDropDown(binding.titleBar);
//    }
//    private void showDiscussPopupWindow() {
//        List<RichMenuInfo> aiffList = getAiffList(AiffDisplayLocation.DiscussRoom.name());
//        getPopupWindow(aiffList, AiffDisplayLocation.DiscussRoom.name()).showAsDropDown(binding.titleBar);
//    }
//    private void showSelfPopupWindow() {
//        List<RichMenuInfo> aiffList = getAiffList(AiffDisplayLocation.SelfRoom.name());
//        getPopupWindow(aiffList,AiffDisplayLocation.SelfRoom.name()).showAsDropDown(binding.titleBar);
//    }
//    /**
//     * 群組頂部進階選單
//     * group_chat_menu_out
//     */
//    private void showGroupPopupWindow() {
//        if(chatRoomEntity == null) return;
//        List<RichMenuInfo> aiffList = Lists.newArrayList();
//        String displayLocation = "";
//        if(chatRoomEntity.getOwnerId().equals(userId)) { // group owner
//            aiffList.addAll(getAiffList(AiffDisplayLocation.GroupRoomOwner.name(), AiffDisplayLocation.GroupRoom.name()));
//            displayLocation = AiffDisplayLocation.GroupRoomOwner.name()+","+AiffDisplayLocation.GroupRoom.name();
//        }else { // not group owner
//            aiffList = getAiffList(AiffDisplayLocation.GroupRoom.name());
//            displayLocation =  AiffDisplayLocation.GroupRoom.name();
//        }
//        getPopupWindow(aiffList, displayLocation).showAsDropDown(binding.titleBar);
//    }
//
//    //朋友進階功能
//    public void showSinglePopupWindow() {
//        List<RichMenuInfo> aiffList = getAiffList(AiffDisplayLocation.PrivateRoom.name());
//        getPopupWindow(aiffList, AiffDisplayLocation.PrivateRoom.name()).showAsDropDown(binding.titleBar);
//    }
//
//    public void showServiceRoomContactPopupWindow() {
//        List<RichMenuInfo> aiffList = getAiffList(AiffDisplayLocation.ServiceRoomContact.name());
//        getPopupWindow(aiffList, AiffDisplayLocation.ServiceRoomContact.name()).showAsDropDown(binding.titleBar);
//    }
//    public void showServiceMemberRoomPopupWindow() {
//        List<RichMenuInfo> aiffList = getAiffList(AiffDisplayLocation.ServiceMemberRoom.name());
//        getPopupWindow(aiffList, AiffDisplayLocation.ServiceMemberRoom.name()).showAsDropDown(binding.titleBar);
//    }
//    public void showServiceRoomEmployeePopupWindow() {
//        List<RichMenuInfo> aiffList = getAiffList(AiffDisplayLocation.ServiceRoomEmployee.name());
//        getPopupWindow(aiffList, AiffDisplayLocation.ServiceRoomEmployee.name(), true).showAsDropDown(binding.titleBar);
//    }
//    public void showServiceRoomAgentPopupWindow() {
//        List<RichMenuInfo> aiffList = getAiffList(AiffDisplayLocation.ServiceRoomAgent.name());
//        getPopupWindow(aiffList, AiffDisplayLocation.ServiceRoomAgent.name()).showAsDropDown(binding.titleBar);
//    }
//
//    //服務號聊天室進階功能
//    public void showServicePopupWindow() {
//        List<RichMenuInfo> aiffList = getAiffList(AiffDisplayLocation.BossServiceRoom.name());
//        getPopupWindow( aiffList, AiffDisplayLocation.BossServiceRoom.name()+","+AiffDisplayLocation.ServiceRoom.name()).showAsDropDown(binding.titleBar);
//    }
//
//    private void doMoreMenuAction(String embedLocation, String displayLocation, List<RichMenuInfo> dataList, RichMenuAdapter richMenuAdapter) {
//        aiffManager.showAiffList(dataList);
//    }
//
//    /**
//     * 物件卡片
//     */
//    private void addBusinessCard(boolean show, View view, RoomThemeStyle themeStyle, BusinessEntity entity) {
//        if (show) {
//            view.findViewById(R.id.business_card).setVisibility(View.VISIBLE);
//            view.findViewById(R.id.business_card).setOnClickListener(v -> Toast.makeText(this, "請至Aile Client看物件詳細", Toast.LENGTH_SHORT).show());
//            CircleImageView civIcon = view.findViewById(R.id.civ_icon);
//            TextView tvBusinessName = view.findViewById(R.id.tv_business_name);
//            TextView tvEndTime = view.findViewById(R.id.tv_end_date);
//            TextView tvBusinessManager = view.findViewById(R.id.tv_business_manager);
//            TextView tvBusinessExecutor = view.findViewById(R.id.tv_business_executor);
//            TextView tvDescription = view.findViewById(R.id.tv_description);
//            TextView tvCustomer = view.findViewById(R.id.tv_business_customer);
//
//            TextView tvCategory = view.findViewById(R.id.tv_category_name);
//            TextView tvPrimaryName = view.findViewById(R.id.tv_primary_name);
//            tvCategory.setSelected(true);
//
//            tvBusinessName.setText(entity.getName());
//            if (entity.getEndTimestamp() > 0) {
//                tvEndTime.setText(DateTimeHelper.format(entity.getEndTimestamp(), "yyyy-MM-dd HH:mm:ss"));
//            } else {
//                tvEndTime.setText(entity.getEndTime());
//            }
//
//            tvBusinessManager.setText(entity.getManagerName());
//            tvBusinessExecutor.setText(entity.getExecutorName());
//            tvDescription.setText(entity.getDescription());
//            tvCustomer.setText(entity.getCustomerName());
//            if (entity.getCode() != null) {
//                tvCategory.setText(entity.getCode().getName());
//            }
//            tvPrimaryName.setText(entity.getPrimaryName());
//            view.findViewById(R.id.iv_footer_left).setScaleX(-1);
//            view.findViewById(R.id.iv_footer_left).setScaleY(1);
//
//            // 主題色
//            if (RoomThemeStyle.SERVICES_or_SUBSCRIBE.contains(themeStyle)) {
//                view.findViewById(R.id.v_theme_left).setBackgroundColor(0xFFffbc42);
//                view.findViewById(R.id.v_theme_right).setBackgroundColor(0xFFffbc42);
//                view.findViewById(R.id.v_theme_end).setBackgroundColor(0xFFffbc42);
//                view.findViewById(R.id.iv_footer_left).setBackgroundColor(0xFFffbc42);
//                view.findViewById(R.id.iv_footer_right).setBackgroundColor(0xFFffbc42);
//            }
//
//            AvatarService.post(this, entity.getExecutorAvatarId(), PicSize.SMALL, civIcon, R.drawable.default_avatar);
//        }
//    }
//
//    /**
//     * 下沈式物件聊天室列表
//     */
//    private void showSinkingBusinessMemberListPopupWindow(String roomId, String businessId, final List<ChatRoomEntity> entities) {
//        View contentView = LayoutInflater.from(this).inflate(R.layout.popup_business_room_list, null);
//        if (!Strings.isNullOrEmpty(businessId)) {
//            BusinessService.getBusinessItem(this, roomId, businessId, new ServiceCallBack<BusinessEntity, RefreshSource>() {
//                @Override
//                public void complete(BusinessEntity entity, RefreshSource source) {
//                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> addBusinessCard(true, contentView, themeStyle, entity));
//                }
//
//                @Override
//                public void error(String message) {
//                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> addBusinessCard(false, contentView, themeStyle, null));
//                }
//            });
//        }
//
//        GridViewBinding popupWindowBinding = GridViewBinding.inflate(LayoutInflater.from(this));
//        popupWindowBinding.getRoot().setBackgroundColor(themeStyle.getMainColor());
//        PopupWindow popupWindow = new PopupWindow(popupWindowBinding.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        popupWindow.setBackgroundDrawable(new ColorDrawable(0x10101010));
//        popupWindow.setOutsideTouchable(true);
//        popupWindow.setFocusable(true);
//        popupWindow.setOnDismissListener(() -> binding.rightAction.setImageResource(R.drawable.icon_aipower_open));
//
//        MaxHeightRecyclerView recyclerView = contentView.findViewById(R.id.rv_chat_room_list);
//        int height = UiHelper.getDisplayHeight(this);
//        recyclerView.setMaxHeight(height / 3);
//        chatFragment.hideKeyboard();
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//
//        ServiceNumberTimeAdapter adapter = new ServiceNumberTimeAdapter()
//                .setData(entities, false)
//                .setOnRoomItemClickListener(new OnRoomItemClickListener<ChatRoomEntity>() {
//                    @Override
//                    public void onItemClick(ChatRoomEntity chatRoomEntity) {
//                        if (CAN_NEXT) {
//                            CAN_NEXT = false;
//                            ActivityTransitionsControl.navigateToChat(ChatActivity.this, chatRoomEntity, ChatActivity.class.getSimpleName(), (intent, s) -> {
//                                startActivity(intent);
//                                popupWindow.dismiss();
//                                finish();
//                            });
//                        }
//                    }
//
//                    @Override
//                    public void onComponentItemClick(ChatRoomEntity chatRoomEntity) {
//
//                    }
//                });
//        recyclerView.setAdapter(adapter);
//        adapter.refreshData();
//        popupWindow.showAsDropDown(binding.titleBar);
//    }
//
//    /**
//     * 下沈式成員列表
//     */
//    private void showSinkingMemberListPopupWindow(String roomId, String businessId, final List<UserProfileEntity> memberList) {
//        GridMemberViewBinding gridMemberViewBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.grid_member_view, null, false);
//        View contentView = gridMemberViewBinding.getRoot();
//        if (!Strings.isNullOrEmpty(businessId)) {
//            BusinessService.getBusinessItem(this, roomId, businessId, new ServiceCallBack<BusinessEntity, RefreshSource>() {
//                @Override
//                public void complete(BusinessEntity entity, RefreshSource source) {
//                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> addBusinessCard(true, contentView, themeStyle, entity));
//                }
//
//                @Override
//                public void error(String message) {
//                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> addBusinessCard(false, contentView, themeStyle, null));
//                }
//            });
//        }
//        gridMemberViewBinding.ivHomePage.setVisibility(View.VISIBLE);
//        gridMemberViewBinding.tvHomePage.setVisibility(View.VISIBLE);
//        gridMemberViewBinding.ivHomePage.setOnClickListener(v -> toMainPage(chatRoomEntity.getType()));
//        gridMemberViewBinding.tvHomePage.setOnClickListener(v -> toMainPage(chatRoomEntity.getType()));
//        contentView.setBackgroundColor(themeStyle.getMainColor());
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//
//        gridMemberViewBinding.recyclerView.setLayoutManager(linearLayoutManager);
//        gridMemberViewBinding.recyclerView.setItemAnimator(new DefaultItemAnimator());
//
//        gridMemberViewBinding.recyclerView.setHasFixedSize(true);
//        chatRoomMembersAdapter = new ChatRoomMembersAdapter(false, false, chatRoomEntity.getType())
//                .setData(memberList)
//                .setOnItemClickListener(this);
//
//        gridMemberViewBinding.recyclerView.setAdapter(chatRoomMembersAdapter);
//        gridMemberViewBinding.recyclerView.measure(0, 0);
//
//        chatRoomMembersAdapter.refreshData();
//        memberPopupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        memberPopupWindow.setBackgroundDrawable(new ColorDrawable(0x10101010));
//        memberPopupWindow.setOutsideTouchable(false);
//        memberPopupWindow.setFocusable(true);
//        memberPopupWindow.showAsDropDown(binding.titleBar);
//        memberPopupWindow.setOnDismissListener(() -> {
//            binding.rightAction.setImageResource(R.drawable.icon_aipower_open);
//            ViewCompat.animate(binding.rightAction).rotation(0).setDuration(ANIMATE_TIME).start();
//        });
//
//        if(!isDeletedMember) getSessionMembers();
//    }
//
//    private void toMainPage(ChatRoomType type) {
//        Intent intent = new Intent(this, MainPageActivity.class);
//        intent.putExtra(BundleKey.ROOM_ID.key(), chatRoomEntity.getId())
//                .putExtra(BundleKey.MEMBERS_LIST.key(), (Serializable) members)
//                .putExtra(BundleKey.ROOM_TYPE.key(), type.name());
//        startActivity(intent);
//    }
//    /**
//     * 服務號成員聊天室，成員列表
//     */
//    private void showSinkingServiceMemberListPopupWindow(String roomId) {
//        View contentView = LayoutInflater.from(this).inflate(R.layout.grid_member_view, null);
//
//        contentView.setBackgroundColor(themeStyle.getMainColor());
//        RecyclerView chatRoomMembersRecyclerView = contentView.findViewById(R.id.recycler_view);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//        chatRoomMembersRecyclerView.setLayoutManager(linearLayoutManager);
//        chatRoomMembersRecyclerView.setItemAnimator(new DefaultItemAnimator());
//
//        List<UserProfileEntity> list = ChatMemberCacheService.getChatMember(roomId);
//        chatRoomMembersRecyclerView.setHasFixedSize(true);
//        chatRoomMembersAdapter = new ChatRoomMembersAdapter(false, false, chatRoomEntity.getType())
//                .setData(Lists.newArrayList(list))
//                .setOnItemClickListener(this);
//
//        chatRoomMembersRecyclerView.setAdapter(chatRoomMembersAdapter);
//        chatRoomMembersRecyclerView.measure(0, 0);
//
//        chatRoomMembersAdapter.refreshData();
//        memberPopupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        memberPopupWindow.setBackgroundDrawable(new ColorDrawable(0x10101010));
//        memberPopupWindow.setOutsideTouchable(true);
//        memberPopupWindow.setFocusable(true);
//        memberPopupWindow.showAsDropDown(binding.titleBar);
//        memberPopupWindow.setOnDismissListener(() -> binding.rightAction.setImageResource(R.drawable.icon_aipower_open));
//    }
//
//    private void addMember(String roomId, List<UserProfileEntity> memberList, ChatRoomType chatRoomType) {
//        ActivityTransitionsControl.navigateToAddMember(this, roomId, memberList, (intent, s) -> addMemberARL.launch(intent));
//    }
//
//    private void addProvisionalMember(String roomId) {
//        ActivityTransitionsControl.navigateToAddProvisionalMember(this, roomId, chatRoomEntity.getServiceNumberId(), Lists.newArrayList(chatRoomEntity.getProvisionalIds())
//                , chatRoomEntity.getServiceNumberAgentId(), ((intent, s) -> addProvisionalMemberARL.launch(intent)));
//    }
//
//    private ChatRoomEntity getSession() {
//        List<Fragment> fragments = getSupportFragmentManager().getFragments();
//        ChatFragment fragment = (ChatFragment) fragments.get(0);
//        return fragment.getChatRoom();
//    }
//
//    private void setRightCancelView() {
//        rightCancelTV = new TextView(this);
//        rightCancelTV.setText("取消");
//        rightCancelTV.setTextSize(16);
//        rightCancelTV.setPadding(10, 0, 10, 0);
//        rightCancelTV.setTextColor(Color.parseColor("#ffffff"));
//    }
//
//    public void setRightView() {
//        if (rightCancelTV == null) {
//            setRightCancelView();
//        }
////        setRightView(rightCancelTV);
//        binding.rightAction.setOnClickListener(view -> {
//            ChatActivity.this.hideChecked();
//        });
//    }
//
//    private void hideChecked() {
//        chatFragment.hideChecked();
//        hideRightCancelView();
//    }
//
//    public void hideRightCancelView() {
////        setRightView(mRightImageView);
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        toBack();
//    }
//
//    // EVAN_FLAG 2019-08-31 依照來源返回不同 Activity
//    private void toBack() {
////        ActivityManager.finishAll();
//        finish();
//        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        App.isNotification = true;
//        App.isChatPager = false;
//        //如果有悬浮窗，则切换
//        // EVAN_FLAG 2020-02-18 (1.10.0) 暫時拔除 linphone
////        CallView callView = chatFragment.getCallView();
////        CallData data = callView.getData();
////        int status = data.getStatus();
////        if (callView.getVisibility() == View.VISIBLE && status != CallData.WAIT) {
////            int type = data.getTodoOverviewType();
////            SipCallManager sipCallManager = callView.getSipCallManager();
////            if (type == CallData.SINGLE) {
////                if (status == CallData.COMMING) {
////                    LinphoneService.getInstance().createSingleComming(data);
////                } else if (status == CallData.CALLING) {
////                    LinphoneService.getInstance().createSingleCalling(data.getCallKey(), data.getRoomId(), data.isSpeaker(), data.isMute());
////                } else {
////                    LinphoneService.getInstance().showConnectedView(data, sipCallManager);
////                }
////            } else if (type == CallData.GROUP) {
////                if (status == CallData.COMMING) {
////                    int joinNum = data.getJoinNum();
////                    LinphoneService.getInstance().createGroupComming(data);
////                } else if (status == CallData.CALLING) {
////                    LinphoneService.getInstance().createGroupCalling(data.getMeetingId(), data.getRoomId(), data.isSpeaker(), data.isMute());
////                } else {
////                    LinphoneService.getInstance().showConnectedView(data, sipCallManager);
////                }
////            } else if (type == CallData.EXTENSION) {
////                if (status == CallData.COMMING) {
////                    LinphoneService.getInstance().createExtensionComming(data);
////                } else if (status == CallData.CALLING) {
////                    LinphoneService.getInstance().createServiceCalling(data.getCallKey(), data.getRoomId(), data.getCallerType(), data.isSpeaker(), data.isMute());
////                } else {
////                    LinphoneService.getInstance().showConnectedView(data, sipCallManager);
////                }
////            } else if (type == CallData.SERVICE) {
////                if (status == CallData.COMMING) {
////                    LinphoneService.getInstance().createServiceComming(data.getCallerId(), data.getMeetingId(), data.getRoomId(), data.getCallKey(), data.getMillis());
////                } else if (status == CallData.CALLING) {
////                    LinphoneService.getInstance().createServiceCalling(data.getCallKey(), data.getRoomId(), data.getCallerType(), data.isSpeaker(), data.isMute());
////                } else {
////                    LinphoneService.getInstance().showConnectedView(data, sipCallManager);
////                }
////            }
////            callView.setVisibility(View.GONE);
////            callView.release();
////        }
//    }
//
//
//
//    @Override
//    protected void onStop() {
//        super.onStop();
////        Log.d("ChatActivity", "onStop()");
//        if (chatRoomEntity != null) {
//            ChatRoomService.getInstance().getBadge(this, chatRoomEntity.getId());
//        }
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//    }
//
////    @Override
////    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
////        super.onActivityResult(requestCode, resultCode, data);
////        if (resultCode == Activity.RESULT_CANCELED && IS_ROOM_INFO_EDIT) {
////            openRoomInfoEdit(null);
////        }
////    }
//
//    private void updateSession() {
//        String userId = TokenPref.getInstance(this).getUserId();
//        ApiManager.doRoomItem(this, chatRoomEntity.getId(), userId, new ApiListener<ChatRoomEntity>() {
//            @Override
//            public void onSuccess(ChatRoomEntity entity) {
//                hideLoadingView();
//                entity.setType(discuss);
//                String title = entity.getName();
//                chatRoomEntity.setName(title);
//                chatRoomEntity.setAvatarId(entity.getAvatarId());
//                chatRoomEntity.setCustomName(true);
//                ChatRoomReference.getInstance().save(chatRoomEntity);
//
//                EventBusUtils.sendEvent(new EventMsg(MsgConstant.GROUP_REFRESH_FILTER, new GroupRefreshBean(chatRoomEntity.getId(), null, null)));
//
//                if(!Strings.isNullOrEmpty(chatRoomEntity.getAvatarId())){
//                    String avatarUrlRefresh = JsonHelper.getInstance().toJson(ImmutableMap.of(
//                            "key", "avatarUrl",
//                            "values", chatRoomEntity.getAvatarId(),
//                            "roomId", roomId));
//                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_REFRESH_FILTER, avatarUrlRefresh));
//                }
//                String nameRefresh = JsonHelper.getInstance().toJson(ImmutableMap.of(
//                        "key", "name",
//                        "values", chatRoomEntity.getName(),
//                        "roomId", roomId));
//                EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_REFRESH_FILTER, nameRefresh));
//            }
//
//            @Override
//            public void onFailed(String errorMessage) {
//                hideLoadingView();
//                if (!TextUtils.isEmpty(errorMessage)) {
//                    ToastUtils.showToast(ChatActivity.this, errorMessage);
//                }
//            }
//        });
//    }
//
//    private void broadcastAddGroup() {
//        EventBusUtils.sendEvent(new EventMsg(MsgConstant.ADD_GROUP_FILTER));
//    }
//
//    public void showLoadingView() {
//        if (progressBar == null) {
//            progressBar = progressBar.show(this, getString(R.string.wording_loading), true, false, dialog -> {
//            });
//        }
//    }
//
//    public void hideLoadingView() {
//        if (progressBar != null && progressBar.isShowing()) {
//            progressBar.dismiss();
//        }
//
//    }
//
//    private void onRichMenuItemClick(RichMenuInfo info) {
////        RichMenuInfo info = (RichMenuInfo) adapter.getItem(position);
//        RichMenuInfo.FixedMenuId fixedMenuId = info.getMenuId();
//        if (info.getType() == RichMenuInfo.MenuType.FIXED.getType()) {
//            switch (fixedMenuId) {
//                case SEARCH:
//                    doSearchAction(null);
//                    break;
//                case AMPLIFICATION:
//                case MUTE:
//                    ChatRoomService.getInstance().changeMute(this, chatRoomEntity, new ServiceCallBack<ChatRoomEntity, Enum>() {
//                        @Override
//                        public void complete(ChatRoomEntity entity, Enum anEnum) {
//                            if (chatRoomEntity != null) {
//                                chatRoomEntity.setMute(entity.isMute());
//                            }
//                            EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHANGE_MUTE_ROOM, JsonHelper.getInstance().toJson(entity)));
//                        }
//
//                        @Override
//                        public void error(String message) {
//
//                        }
//                    });
//                    break;
//                case SETUP:
//                    if (chatRoomEntity.getType().equals(group) || chatRoomEntity.getType().equals(ChatRoomType.business)) {
//                        toGroupSet();
//                    } else if (chatRoomEntity.getType().equals(discuss)) {
//                        toDiscuss();
//                    } else {
//                        if (chatRoomEntity.getType().equals(ChatRoomType.friend)) {
//                            toSingleSet();
//                        }
//                    }
//                    break;
//                case UPGRADE: //轉為社團
//                    Intent intent = new Intent(this, CreateGroupActivity.class);
//                    intent.putExtra(BundleKey.ROOM_ID.key(), chatRoomEntity.getId());
//                    intent.putExtra(BundleKey.MEMBERS_LIST.key(), Lists.newArrayList(chatRoomEntity.getMemberIds()));
//                    if(chatRoomEntity.isCustomName())
//                        intent.putExtra(BundleKey.CHAT_ROOM_NAME.key(), chatRoomEntity.getName());
//                    startActivity(intent);
//                    break;
//                case DISMISS_CROWD:
//                case EXIT_CROWD:
//                case EXIT_DISCUSS:
//                    String userId = TokenPref.getInstance(ChatActivity.this).getUserId();
//                    String message = chatRoomEntity.getOwnerId().equals(userId) && isGroup() ? "您是否要解散社團？" : group.equals(chatRoomEntity.getType()) ? "是否退出社團？" : "退出後將清除聊天室及聊天室紀錄，您確定要退出嗎？";
//                    String title = (!isGroup()) ? "退出聊天室" : "";
//                    new AlertView.Builder()
//                            .setContext(this)
//                            .setStyle(AlertView.Style.Alert)
//                            .setTitle(title)
//                            .setMessage(message)
//                            .setOthers(new String[]{"取消", "確定"})
//                            .setOnItemClickListener((o, pos) -> {
//                                if (pos == 1) {
//                                    showLoadingView();
//                                    if ("您是否要解散社團？".equals(message)) {
//                                        dismissGroup(chatRoomEntity.getOwnerId(), chatRoomEntity.getId());
//                                    } else {
//                                        quitGroup(chatRoomEntity.getId(), null);
//                                    }
//                                }
//                            })
//                            .build()
//                            .setCancelable(true)
//                            .show();
//                    break;
//                case NEW_MEMBER:
//                    addProvisionalMember(roomId);
//                    break;
//                case MAIN_PAGE:
//                    toMainPage(chatRoomEntity.getType());
//                    break;
//            }
//        }
//
//        if (info.getType() == RichMenuInfo.MenuType.AIFF.getType()) {
//            AiffInfo aiffInfo = AiffDB.getInstance(this).getAiffInfoDao().getAiffInfo(info.getId());
//            aiffManager.showAiffViewByInfo(aiffInfo);
//            aiffInfo.setUseTimestamp(System.currentTimeMillis());
//            AiffDB.getInstance(this).getAiffInfoDao().upsert(aiffInfo);
//        }
//    }
//
//    private com.chad.library.adapter.base.listener.OnItemClickListener onRichMenuItemClickListener = (adapter, view, position) -> {
//        if (popupWindow != null && popupWindow.isShowing()) {
//            popupWindow.dismiss();
//        }
//
//        RichMenuInfo info = (RichMenuInfo) adapter.getItem(position);
//        RichMenuInfo.FixedMenuId fixedMenuId = info.getMenuId();
//        if (info.getType() == RichMenuInfo.MenuType.FIXED.getType()) {
//            switch (fixedMenuId) {
//                case SEARCH:
//                    doSearchAction(null);
//                    break;
//                case AMPLIFICATION:
//                case MUTE:
//                    ChatRoomService.getInstance().changeMute(this, chatRoomEntity, new ServiceCallBack<ChatRoomEntity, Enum>() {
//                        @Override
//                        public void complete(ChatRoomEntity entity, Enum anEnum) {
//                            if (chatRoomEntity != null) {
//                                chatRoomEntity.setMute(entity.isMute());
//                            }
//                            EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHANGE_MUTE_ROOM, JsonHelper.getInstance().toJson(entity)));
//                        }
//
//                        @Override
//                        public void error(String message) {
//
//                        }
//                    });
//                    break;
//                case SETUP:
//                    if (chatRoomEntity.getType().equals(group) || chatRoomEntity.getType().equals(ChatRoomType.business)) {
//                        toGroupSet();
//                    } else if (chatRoomEntity.getType().equals(discuss)) {
//                        toDiscuss();
//                    } else {
//                        if (chatRoomEntity.getType().equals(ChatRoomType.friend)) {
//                            toSingleSet();
//                        }
//                    }
//                    break;
//                case UPGRADE:
//                    ApiManager.doRoomHomePage(this, roomId, new ApiListener<CrowdEntity>() {
//                        @Override
//                        public void onSuccess(CrowdEntity crowdEntity) {
//                            members.clear();
//                            if (crowdEntity == null) {
//                                return;
//                            }
//                            Set<String> memberIdSet = Sets.newHashSet();
//                            for (UserProfileEntity member : crowdEntity.getMemberArray()) {
//                                if (chatRoomEntity.getOwnerId().equals(member.getId())) {
//                                    members.add(0, member);
//                                } else {
//                                    members.add(member);
//                                }
//                                memberIdSet.add(member.getId());
//                                DBManager.getInstance().insertFriends(member);
//                            }
//
//                            AccountRoomRelReference.batchSaveByAccountIdsAndRoomId(null, roomId, Lists.newArrayList(memberIdSet));
//
//                            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//                                setTitleBar();
//                                if (chatRoomMembersAdapter != null) {
//                                    chatRoomMembersAdapter.refreshData();
//                                }
//
//                                if (CAN_NEXT) {
//                                    CAN_NEXT = false;
//                                    if (crowdEntity.getMemberArray() != null && crowdEntity.getMemberArray().size() >= 3) {
//                                        Intent intent = new Intent(ChatActivity.this, UpgradeDiscussToGroupActivity.class);
//                                        intent.putExtra("roomId", roomId);
//                                        String avatarUrl = crowdEntity.getAvatarUrl();
//                                        if (!Strings.isNullOrEmpty(avatarUrl)) {
//                                            intent.putExtra("avatarUrl", avatarUrl);
//                                        }
//                                        updateGroupARL.launch(intent);
//                                    } else {
//                                        CAN_NEXT = true;
//                                        Toast.makeText(ChatActivity.this, "該聊天室成員不足3名，無法升級為社團", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            });
//
//                        }
//
//                        @Override
//                        public void onFailed(String errorMessage) {
//
//                        }
//                    });
//                    break;
//                case DISMISS_CROWD:
//                case EXIT_CROWD:
//                case EXIT_DISCUSS:
//                    String userId = TokenPref.getInstance(ChatActivity.this).getUserId();
//                    String message = chatRoomEntity.getOwnerId().equals(userId) && isGroup() ? "您是否要解散社團？" : group.equals(chatRoomEntity.getType()) ? "是否退出社團？" : "是否退出群聊？";
//                    new AlertView.Builder()
//                            .setContext(this)
//                            .setStyle(AlertView.Style.Alert)
//                            .setMessage(message)
//                            .setOthers(new String[]{"取消", "確定"})
//                            .setOnItemClickListener((o, pos) -> {
//                                if (pos == 1) {
//                                    showLoadingView();
//                                    if ("您是否要解散社團？".equals(message)) {
//                                        dismissGroup(chatRoomEntity.getOwnerId(), chatRoomEntity.getId());
//                                    } else {
//                                        quitGroup(chatRoomEntity.getId(), null);
//                                    }
//                                }
//                            })
//                            .build()
//                            .setCancelable(true)
//                            .show();
//                    break;
//                case NEW_MEMBER:
//                    addProvisionalMember(roomId);
//                    break;
//            }
//        }
//
//        if (info.getType() == RichMenuInfo.MenuType.AIFF.getType()) {
//            AiffInfo aiffInfo = AiffDB.getInstance(this).getAiffInfoDao().getAiffInfo(info.getId());
//            aiffManager.showAiffViewByInfo(aiffInfo);
//            aiffInfo.setUseTimestamp(System.currentTimeMillis());
//            AiffDB.getInstance(this).getAiffInfoDao().upsert(aiffInfo);
//        }
//    };
//
//    //針對進階功能視窗以圖案對應點擊事件
//    //預計棄用
////    @Override
////    public void onMenuItemClick(View v, int imageRes) {
////        if (popupWindow != null) {
////            popupWindow.dismiss();
////        }
////        switch (imageRes) {
////            case R.drawable.group_chat_menu_infomulti1:
////                if (chatRoomEntity.getType().equals(ChatRoomType.FRIEND)) {
////                    memberIds = AccountRoomRelReference.findMemberIdsByRoomId(null, roomId);
//////                    memberIds = DBManager.getInstance().queryMemberIds(roomId);
////                    //TODO 确认单聊聊天室成员是否正确
////                    //从server更新单聊聊天室的成员，2个人
////                    ApiManager.doRoomHomePage(this, roomId, new ApiListener<CrowdEntity>() {
////                        @Override
////                        public void onSuccess(CrowdEntity crowdEntity) {
////                            if (crowdEntity == null) {
////                                return;
////                            }
////                            AccountRoomRelReference.deleteRelByRoomId(null, roomId);
////                            AccountRoomRelReference.saveProfiles(null, crowdEntity.getUsers(), roomId);
////                            for (UserProfileEntity member : crowdEntity.getUsers()) {
////                                DBManager.getInstance().insertFriends(member);
////                            }
////                            String userId = TokenPref.getInstance(ChatActivity.this).getUserId();
////                            if (!DBManager.getInstance().queryFriendIsBlock(memberIds.get(0).equals(userId) ? memberIds.get(1) : memberIds.get(0))) {
////                            } else {
////                                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
////                                    Toast.makeText(ChatActivity.this, "請先解除封鎖。", Toast.LENGTH_SHORT).show();
////                                });
////                            }
////                        }
////
////                        @Override
////                        public void onFailed(String errorMessage) {
////
////                        }
////                    });
////                } else {
////                    ApiManager.doMemberList(this, chatRoomEntity.getId(), new ApiListener<List<UserProfileEntity>>() {
////
////                        @Override
////                        public void onSuccess(List<UserProfileEntity> userProfileEntities) {
////                            members.clear();
////                            String ownerId = chatRoomEntity.getOwnerId();
////                            for (UserProfileEntity account : userProfileEntities) {
////                                DBManager.getInstance().insertFriends(account);
////                                String nickname = account.getNickName();
////                                String accountId = account.getId();
////                                if (accountId.equals(ownerId)) {
////                                    members.add(0, account);
////                                } else {
////                                    members.add(account);
////                                }
////                            }
////                            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
////                                chatRoomMembersAdapter.refreshData();
////                            });
////                        }
////
////                        @Override
////                        public void onFailed(String errorMessage) {
////                            ToastUtils.showToast(ChatActivity.this, errorMessage);
////                        }
////                    });
////                }
////
////                break;
////            case R.drawable.group_chat_menu_nonremind:
////            case R.drawable.group_chat_menu_file:
////            case R.drawable.group_chat_menu_scan:
////            case R.drawable.group_chat_menu_pic_all:
////            case R.drawable.group_chat_menu_broadcast:
////                break;
////            case R.drawable.group_chat_menu_phone:
////                // EVAN_FLAG 2020-02-18 (1.10.0) 暫時拔除 linphone
//////                if (chatRoomEntity.getTodoOverviewType().equals(ChatRoomType.FRIEND)) {
//////                    chatFragment.singleCall();
//////                } else if (chatRoomEntity.getTodoOverviewType().equals(ChatRoomType.GROUP) || chatRoomEntity.getTodoOverviewType().equals(ChatRoomType.DISCUSS)) {
//////                    chatFragment.groupCall();
//////                }
////                break;
////            case R.drawable.add_people3:
////                getSessionMembers();
////                addMember(roomId, members);
////                break;
////            default:
////                break;
////        }
////    }
//
////    @Override
////    public void onUnblockWarning(View v, int nameRes, boolean isEnable) {
////        Toast.makeText(this, "請先解除封鎖", Toast.LENGTH_SHORT).show();
////    }
//
//    public boolean isGroup() {
//        return chatRoomEntity.getType().equals(group);
//    }
//
//    public void toInvite(View view) {
//        if (CAN_NEXT) {
//            switch (chatRoomEntity.getType()) {
//                case group:     // = 5;//群组聊天室
//                case discuss:     // = 8;//多人讨论组
//                    getSessionMembers();
//                    addMember(roomId, members, chatRoomEntity.getType());
//                    break;
//                case friend:     // = 3;//好友聊天室
//                    Intent intent = new Intent(this, CreateDiscussActivity.class);
//                    intent.putExtra(BundleKey.ACCOUNT_IDS.key(), new ArrayList<>(chatRoomEntity.getMemberIds()));
//                    startActivity(intent);
//                    break;
//                case self:     // = 1;//个人聊天室（当前用户自己的多载具聊天室）
//                case strange:     // = 2;//非好友单人聊天室
//                case broast:     // = 4;//广播聊天室
//                case services:     // = 6;//服务号聊天室
//                case subscribe:     // = 7;//订阅服务号聊天室
//                case service:     // = 9;//服务号专人聊天室（gw转人工用）
//                case undef:     // = 10;
//                case vistor:     // = 11;
//                case system:     // = 12;//系統聊天室
//                case business:     // = 13;//系統聊天室
//                    break;
//                default:
////            ApiManager.getInstance().doMemberList(this, session.getId());
//                    break;
//            }
//            CAN_NEXT = false;
//        }
//
//    }
//
//    public void invite() {
//        memberIds = AccountRoomRelReference.findMemberIdsByRoomId(null, roomId);
////        memberIds = DBManager.getInstance().queryMemberIds(roomId);
//        //TODO 确认单聊聊天室成员是否正确
//        //从server更新单聊聊天室的成员，2个人
//        ApiManager.doRoomHomePage(this, roomId, new ApiListener<CrowdEntity>() {
//            @Override
//            public void onSuccess(CrowdEntity crowdEntity) {
//                if (crowdEntity == null) {
//                    return;
//                }
//                AccountRoomRelReference.deleteRelByRoomId(null, roomId);
////                DBManager.getInstance().delAccount_Room_All(roomId);
//                for (UserProfileEntity member : crowdEntity.getMemberArray()) {
//                    DBManager.getInstance().insertFriends(member);
//                    CELog.e("聊天室存成员数据库" + roomId);
//                    AccountRoomRelReference.saveByAccountIdAndRoomId(null, member.getId(), roomId);
////                    DBManager.getInstance().insertAccount_Room(roomId, member.getId());
//                }
//                String userId = TokenPref.getInstance(ChatActivity.this).getUserId();
//                if (!DBManager.getInstance().queryFriendIsBlock(memberIds.get(0).equals(userId) ? memberIds.get(1) : memberIds.get(0))) {
//                    ActivityTransitionsControl.toInvites(ChatActivity.this, roomId, (intent, s) -> {
//                        selectCodeARL.launch(intent);
//                        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
//                    });
//                } else {
//                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//                        Toast.makeText(ChatActivity.this, "請先解除封鎖。", Toast.LENGTH_SHORT).show();
//                    });
//                }
//            }
//
//            @Override
//            public void onFailed(String errorMessage) {
//
//            }
//        });
//    }
//
//    private void toSingleSet() {
//        if (CAN_NEXT) {
//            CAN_NEXT = false;
//            Intent intent = new Intent(this, SingleChatSettingActivity.class);
//            intent.putExtra(BundleKey.EXTRA_SESSION.key(), chatRoomEntity);
//            startActivity(intent);
//        }
//    }
//
//    private void toDiscuss() {
//        if (CAN_NEXT) {
//            CAN_NEXT = false;
//            Intent intent = new Intent(this, DiscussionPageActivity.class);
//            intent.putExtra(BundleKey.EXTRA_SESSION_ID.key(), chatRoomEntity.getId());
//            toGroupSessionARL.launch(intent);
//        }
//    }
//
//    private void toGroupSet() {
//        if (CAN_NEXT) {
//            CAN_NEXT = false;
//            if (TextUtils.isEmpty(chatRoomEntity.getOwnerId())) {
//                String userId = TokenPref.getInstance(this).getUserId();
//
//                ApiManager.doRoomItem(this, chatRoomEntity.getId(), userId, new ApiListener<ChatRoomEntity>() {
//                    @Override
//                    public void onSuccess(ChatRoomEntity entity) {
//                        ChatRoomReference.getInstance().save(entity);
//                        Intent intent = new Intent(ChatActivity.this, GroupPagerActivity.class);
//                        intent.putExtra(BundleKey.EXTRA_SESSION_ID.key(), entity.getId());
//                        startActivity(intent);
//                    }
//
//                    @Override
//                    public void onFailed(String errorMessage) {
//                        if (!TextUtils.isEmpty(errorMessage)) {
//                            ToastUtils.showToast(ChatActivity.this, errorMessage);
//                        }
//                        CAN_NEXT = true;
//                    }
//                });
//
//            } else {
//                Intent intent = new Intent(this, GroupPagerActivity.class);
//                intent.putExtra(BundleKey.EXTRA_SESSION_ID.key(), chatRoomEntity.getId());
//                startActivity(intent);
//            }
//        }
//    }
//
//    private void dismissGroup(String ownerId, String roomId) {
//        showLoadingView();
//        String userId = TokenPref.getInstance(ChatActivity.this).getUserId();
//        if (userId.equals(ownerId)) {
//            ApiManager.doRoomDismiss(this, roomId, new ApiListener<String>() {
//                @Override
//                public void onSuccess(String s) {
//                    hideLoadingView();
//                    ChatRoomReference.getInstance().deleteById(roomId);
//
//                    removeGroup();
//                    TodoService.unBindRoom(ChatActivity.this, roomId, null);
//                    Toast.makeText(ChatActivity.this, "已經解散群聊", Toast.LENGTH_SHORT).show();
//                    chatFragment.release();
//                    finish();
//                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
//                }
//
//                @Override
//                public void onFailed(String errorMessage) {
//                    hideLoadingView();
//                    Toast.makeText(ChatActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
//                }
//            });
//        } else {
//            quitGroup(roomId, null);
//        }
//    }
//
//    private void quitGroup(String roomId, String ownerId) {
//        showLoadingView();
//        ApiManager.doChatMemberExit(this, roomId, ownerId, new ApiListener<String>() {
//            @Override
//            public void onSuccess(String s) {
//                hideLoadingView();
//                Toast.makeText(ChatActivity.this, "已經退出群聊", Toast.LENGTH_SHORT).show();
//                ChatRoomReference.getInstance().deleteById(roomId);
//                DBManager.getInstance().deleteRoomListItem(roomId); //v2 api
//                DBManager.getInstance().deleteGroup(roomId);
//                chatFragment.release();
//                removeGroup();
//                TodoService.unBindRoom(ChatActivity.this, roomId, null);
//                finish();
//                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
//            }
//
//            @Override
//            public void onFailed(String errorMessage) {
//                hideLoadingView();
//                Toast.makeText(ChatActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void removeGroup() {
//        EventBusUtils.sendEvent(new EventMsg(MsgConstant.REMOVE_GROUP_FILTER, chatRoomEntity.getId()));
//    }
//
//    @Override
//    public void onItemClick(View view, UserProfileEntity account, int position) {
//        if (CAN_NEXT) {
//            CAN_NEXT = false;
//            toChatRoomByUserProfile(account);
//        }
//    }
//
//    /**
//     * 依照 使用者資訊轉場至聊天室頁面
//     */
//    public void toChatRoomByUserProfile(UserProfileEntity account) {
//        if (account != null) {
//            String userId = TokenPref.getInstance(this).getUserId();
//            if (!account.getId().equals(userId)) {
//                account = DBManager.getInstance().queryFriend(account.getId());
//                if(account.getRoomId() != null) {
//                    if(account.getRoomId().isEmpty()) { //roomId若為空，則添加好友並打開聊天室
//                        startActivity(
//                                new Intent(this, ChatActivity.class)
//                                        .putExtra(BundleKey.USER_NICKNAME.key(), account.getNickName())
//                                        .putExtra(BundleKey.USER_ID.key(), account.getId())
//                        );
//                    }else
//                        navigationToChat(account.getRoomId());
//                }else {
//                    UserProfileEntity userProfile = account;
//                    ChatService.getInstance().addContact(new ApiListener<String>() {
//                        @Override
//                        public void onSuccess(final String roomId) {
//                            navigationToChat(roomId);
//                        }
//
//                        @Override
//                        public void onFailed(String errorMessage) {
//                            //上方選單點選後未加好友導到虛擬聊天室
//                            Intent intent = new Intent(ChatActivity.this, ChatActivity.class);
//                            intent.putExtra(BundleKey.USER_NICKNAME.key(), (userProfile.getNickName() != null) ? userProfile.getNickName() : userProfile.getName());
//                            intent.putExtra(BundleKey.USER_ID.key(), userProfile.getId());
//                            startActivity(intent);
//                        }
//                    }, account.getId(), account.getName());
//                }
//
////                if (AccountType.FRIEND.equals(account.getType())) {
////                    ChatRoomEntity session = ChatRoomReference.getInstance().findById(account.getRoomId());
////                    if (session == null) {
////                        // 本地無 Room entity 重新向Server 取得
////                        ApiManager.doRoomItem(this, account.getRoomId(), account.getId(), new ApiListener<ChatRoomEntity>() {
////                            @Override
////                            public void onSuccess(ChatRoomEntity entity) {
////                                ChatRoomReference.getInstance().save(entity);
////                                ActivityTransitionsControl.navigateToChat(ChatActivity.this, ChatRoomReference.getInstance().findById(entity.getId()), ChatActivity.class.getSimpleName(), (intent, s) -> {
////                                    startActivity(intent);
////                                    finish();
////                                });
////
//////                                Intent intent = new Intent(ChatActivity.this, ChatActivity.class);
//////                                intent.putExtra(BundleKey.EXTRA_SESSION_ID.key(), entity.getId());
//////                                startActivity(intent);
////
////                            }
////
////                            @Override
////                            public void onFailed(String errorMessage) {
////                                ToastUtils.showToast(ChatActivity.this, "有電話進入;系統異常,無法正常顯示！");
////                                CAN_NEXT = true;
////                            }
////                        });
////                        return;
////                    }
////
////                    // EVAN_FLAG 2020-01-06 (1.9.0) 如果已經在聊天室內不在轉場
////                    if (!chatRoomEntity.getId().equals(account.getRoomId())) {
////                        ActivityTransitionsControl.navigateToChat(ChatActivity.this, account.getRoomId(), (intent, s) -> {
////                            startActivity(intent);
////                            finish();
////                        });
////
////
////                        // EVAN_FLAG 2019-09-11 非自己本身，點擊聊天室成員，到聊天室
//////                        Intent intent = new Intent(this, ChatActivity.class);
//////                        intent.putExtra(BundleKey.EXTRA_SESSION_ID.key(), account.getRoomId());
//////                        startActivity(intent);
//////                        finish();
////                    } else {
////                        Toast.makeText(ChatActivity.this, "已經在當前聊天室", Toast.LENGTH_SHORT).show();
////                    }
////
//////                    Intent intent = new Intent(this, FriendPagerActivity.class);
//////                    intent.putExtra(Constant.ACCOUNT_ID, account.getId());
//////                    startActivity(intent);
////                } else {
////                    //上方選單點選後未加好友導到虛擬聊天室
////                    Intent intent = new Intent(this, ChatActivity.class);
////                    intent.putExtra(BundleKey.USER_NICKNAME.key(), (account.getNickName() != null) ? account.getNickName() : account.getName());
////                    intent.putExtra(BundleKey.USER_ID.key(), account.getId());
////                    startActivity(intent);
////                }
//            } else {
//                ActivityTransitionsControl.navigateToSelfPage(this, (intent, s) -> startActivity(intent));
////                Intent intent = new Intent(this, SelfPagerActivity.class);
////                intent.putExtra(BundleKey.ACCOUNT_ID.key(), account.getId());
////                startActivity(intent);
//            }
//            if (popupWindow != null) {
//                popupWindow.dismiss();
//            }
//        }
//    }
//
//    private void navigationToChat(String roomId) {
//        ChatRoomEntity entity = ChatRoomReference.getInstance().findById(roomId);
//        if (entity == null) {
//            String userId = TokenPref.getInstance(this).getUserId();
//            ApiManager.doRoomItem(this, roomId, userId, new ApiListener<ChatRoomEntity>() {
//                @Override
//                public void onSuccess(ChatRoomEntity entity) {
//                    boolean status = ChatRoomReference.getInstance().save(entity);
//                    if (status) {
//                        ActivityTransitionsControl.navigateToChat(ChatActivity.this, entity, ContactPersonFragment.class.getSimpleName(), (intent, s) -> startActivity(intent));
//                    } else {
//                        onFailed("save room entity failed ");
//                    }
//                }
//
//                @Override
//                public void onFailed(String errorMessage) {
//                    CELog.e(errorMessage);
//                    CAN_NEXT = true;
//                }
//            });
//        } else {
//            ActivityTransitionsControl.navigateToChat(this, entity, ContactPersonFragment.class.getSimpleName(), (intent, s) -> startActivity(intent));
//        }
//    }
//
//    public void refreshPager(ChatRoomEntity chatRoomEntity) {
//        if (chatRoomEntity == null)
//            return;
//        this.chatRoomEntity = chatRoomEntity;
//        setTitleBar();
//        ChatRoomReference.getInstance().save(this.chatRoomEntity);
//    }
//
//    public String getRoomId() {
//        return roomId;
//    }
//
//
//    public void setTitleText(String nickname) {
//        binding.title.setText(nickname);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
////        Log.d("ChatActivity", "onDestroy()");
////        EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_CLEAR_KEEP_SCREEN_ON));
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
////        EventBusUtils.unregister(this);
////        App application = (App) getApplication();
////        application.currentRoomId = "";
//        UserPref.getInstance(this).setCurrentRoomId("");
//        // EVAN_FLAG 2020-06-08 (1.11.0) 強制觸發量處理控制
////        SyncReadBatchUpdateController.send();
////        ThreadUtils.shutdownOnSubThread();
//        addMemberARL.unregister();
//        addProvisionalMemberARL.unregister();
//        updateGroupARL.unregister();
//        toGroupSessionARL.unregister();
//        selectCodeARL.unregister();
//    }
//
//    private void setUnreadNumber(int unreadNumber, String roomId) {
//        if (roomId.equals(chatRoomEntity.getId()) || roomId.isEmpty()) return;
//        if (unreadNumber > 0) {
//            setUnreadCount(unreadNumber);
//        }
//    }
//
//    private void setUnreadCount(int count) {
//        if(count > 0){
//            binding.unreadNum.setText(UnreadUtil.INSTANCE.getUnreadText(count));
//            binding.unreadNum.setVisibility(View.VISIBLE);
//        }else {
//            binding.unreadNum.setVisibility(View.GONE);
//        }
//    }
//
//    public void showTopMenu(boolean isShow) {
//        binding.llRight.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
//    }
//    public void showToolBar(boolean isShow) {
//        binding.titleBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
//    }
//
//    public void disableInvite() {
//        binding.inviteIV.setVisibility(View.GONE);
//    }
//
//    public void triggerToolbarClick() {
//        binding.titleBar.callOnClick();
//    }
//
//    public void hangup() {
//        // EVAN_FLAG 2020-02-18 1.10.0 暫時拔除 linphone
////        CallView callView = chatFragment.getCallView();
////        if (callView.getVisibility() == View.VISIBLE) {
////            callView.hangup();
////        }
//    }
//
//    public void navigateToSubscribePage() {
//        if (CAN_NEXT) {
//            CAN_NEXT = false;
//            //公众号详情
//            ActivityTransitionsControl.navigateToSubscribePage(this, chatRoomEntity.getServiceNumberId(), chatRoomEntity.getId(), true, (intent, s) -> {
//                startActivity(intent);
//                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
//            });
////                ChatRoomEntity session = getSession();
////                Intent intent = new Intent(this, VipcnDetailActivity.class);
////                intent.putExtra(BundleKey.SERVICE_NUMBER_ID.key(), chatRoomEntity.getServiceNumberId());
////                intent.putExtra(BundleKey.SERVICE_NUMBER_NAME.key(), chatRoomEntity.getName());
////                intent.putExtra(BundleKey.IS_VIPCN_FROM_CHAT.key(), true);
////                startActivity(intent);
////                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void handleEvent(EventMsg eventMsg) {
//        switch (eventMsg.getCode()) {
//            case MsgConstant.NOTICE_SELF_EXIT_ROOM:
//                String exitRoomId = eventMsg.getString();
//                if (chatRoomEntity != null && chatRoomEntity.getId().equals(exitRoomId)) {
//                    finish();
//                }
//                break;
//            case MsgConstant.NOTICE_CLOSE_OLD_ROOM:
//                    finish();
//                break;
////            case MsgConstant.NAVIGATE_TO_CHAT_ROOM:
////                navigateToChat(eventMsg.getString());
////                break;
//            case MsgConstant.UPDATE_ALL_BADGE_NUMBER_EVENT:
//                break;
//            case MsgConstant.UPDATE_MAIN_BADGE_NUMBER_EVENT:
//            case MsgConstant.UPDATE_SERVICE_BADGE_NUMBER_EVENT:
//                if (eventMsg.getData() instanceof BadgeDataModel) {
//                    BadgeDataModel badgeDataModel = (BadgeDataModel) eventMsg.getData();
//                    setUnreadNumber(badgeDataModel.getUnReadNumber(), badgeDataModel.getRoomId());
//                }
//                break;
//            //多人聊天室 成員加入
//            case MsgConstant.NOTICE_DISCUSS_MEMBER_ADD:
//            //社團/服務號臨時成员加入
//            case MsgConstant.GROUP_REFRESH_FILTER:
//                if (eventMsg.getData() == null) return;
//                Map<String, Object> newMemberData = JsonHelper.getInstance().fromToMap(eventMsg.getData().toString());
//                if (Objects.equals(newMemberData.get("roomId"), chatRoomEntity.getId())) {
//                    List<String> memberIds = (List<String>) newMemberData.get("memberIds");
//                    if(memberIds!=null)
//                        chatViewModel.doHandleMemberFromDB(memberIds, chatRoomEntity.getId(), false);
//                }
//                break;
//            case MsgConstant.GROUP_UPGRADE_FILTER:
//                //修改群聊名称
//                GroupUpgradeBean mGroupUpgradeBean = (GroupUpgradeBean) eventMsg.getData();
//                String gTitle = mGroupUpgradeBean.getTitle();
//                String gSessionId = mGroupUpgradeBean.getSessionId();
//                if (!TextUtils.isEmpty(gSessionId) && gSessionId.equals(this.roomId) && !TextUtils.isEmpty(gTitle)) {
//                    ApiManager.doRoomHomePage(this, this.roomId, new ApiListener<CrowdEntity>() {
//                        @Override
//                        public void onSuccess(CrowdEntity crowdEntity) {
//                            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//                                chatRoomEntity.setName(crowdEntity.getName());
//                                chatRoomEntity.setType(group);
//                                chatRoomEntity.setOwnerId(crowdEntity.getOwnerId());
//                                setTitleBar();
//                                if (chatRoomMembersAdapter != null) {
//                                    chatRoomMembersAdapter.refreshData();
//                                }
//                            });
//
//                            members.clear();
//                            if (crowdEntity == null) {
//                                return;
//                            }
//                            AccountRoomRelReference.deleteRelByRoomId(null, roomId);
//                            Set<String> memberIdSet = Sets.newHashSet();
//                            for (UserProfileEntity member : crowdEntity.getMemberArray()) {
//                                if (chatRoomEntity.getOwnerId().equals(member.getId())) {
//                                    members.add(0, member);
//                                } else {
//                                    members.add(member);
//                                }
//                                memberIdSet.add(member.getId());
//                                DBManager.getInstance().insertFriends(member);
//                                CELog.e("聊天室存成员数据库" + ChatActivity.this.roomId);
//                            }
////                            AccountRoomRelReference.deleteRelByRoomId(null, roomId);
//                            AccountRoomRelReference.batchSaveByAccountIdsAndRoomId(null, roomId, Lists.newArrayList(memberIdSet));
//                            ChatRoomReference.getInstance().save(chatRoomEntity);
//                            DBManager.getInstance().insertGroup(crowdEntity);
//                            ChatRoomReference.getInstance().updateTitleById(ChatActivity.this.roomId, crowdEntity.getName());
//                        }
//
//                        @Override
//                        public void onFailed(String errorMessage) {
//
//                        }
//                    });
//                }
//                break;
//            case MsgConstant.NOTICE_DISCUSS_ROOM_TITLE_UPDATE: // 多人聊天室被改名
//                binding.title.setText(getTitleText());
//                break;
//            case MsgConstant.CHAT_TITLE_FILTER:
//                String title1 = (String) eventMsg.getData();
//                binding.title.setText(title1);
//                break;
//            case MsgConstant.ACCOUNT_REFRESH_FILTER:
//                UserProfileEntity mAccount = (UserProfileEntity) eventMsg.getData();
//                List<String> memberIds = AccountRoomRelReference.findMemberIdsByRoomId(null, roomId);
//                if (memberIds.contains(mAccount.getId())) {
//                    setTitleBar();
//                    chatFragment.updateAccountForMessage(mAccount);
//                }
//                break;
////            case MsgConstant.UI_NOTICE_TO_TODO_ITEM:
//            case MsgConstant.NOTICE_FINISH_ACTIVITY:
//                finish();
//                overridePendingTransition(0, 0);
//                break;
//            case MsgConstant.NOTICE_KEEP_SCREEN_ON:
//                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//                break;
//            case MsgConstant.NOTICE_CLEAR_KEEP_SCREEN_ON:
//                break;
//
//            //臨時成員轉成服務人員
//            case MsgConstant.NOTICE_SERVICE_NUMBER_ADD_FROM_PROVISIONAL:
//                ServiceNumberAddModel serviceNumberAddModel = JsonHelper.getInstance().from(eventMsg.getData().toString(), ServiceNumberAddModel.class);
//                //先判斷是否是同聊天室和同服務號
//                if (serviceNumberAddModel.getRoomId().equals(chatRoomEntity.getId()) && serviceNumberAddModel.getServiceNumberId().equals(chatRoomEntity.getServiceNumberId())){
//                    //再判斷當前的服務人員是否是臨時成員
//                    if (serviceNumberAddModel.getMemberIds().contains(userId)) {
//                        ApiManager.doRoomItem(this, serviceNumberAddModel.getRoomId(), userId, new ApiListener<ChatRoomEntity>() {
//                            @Override
//                            public void onSuccess(ChatRoomEntity roomEntity) {
//                                showProvisionalToServiceNumber(roomEntity);
//                            }
//
//                            @Override
//                            public void onFailed(String errorMessage) {
//                                CELog.e(errorMessage);
//                            }
//                        });
//                    } else {
//                        serviceNumberAddModel.getMemberIds().forEach(memberId -> {
//                            onRefreshProvisionalMemberListener.onRemoveMember(memberId);
//                        });
//                    }
//                }
//                break;
//            case MsgConstant.NOTICE_DISCUSS_MEMBER_EXIT:
//                //多人聊天室成員離開 更新下拉成員選單及 title
//                DiscussMemberSocket memberExitSocket = JsonHelper.getInstance().from(eventMsg.getData(), DiscussMemberSocket.class);
//                if (memberExitSocket.getRoomId().equals(roomId)) {
//                    chatViewModel.setDiscussRoomTitleWhenMemberRemoved(chatRoomEntity, Lists.newArrayList(memberExitSocket.getUserId()));
//                }
//                break;
//            case MsgConstant.NOTICE_DISCUSS_GROUP_MEMBER_REMOVED: //多人聊天室or社團成員被剔除
//                Map<String, Object> data = JsonHelper.getInstance().fromToMap(eventMsg.getData().toString());
//                if (Objects.equals(data.get("roomId"), chatRoomEntity.getId())) {
//                    List<String> deletedMemberIds = (List<String>) data.get("deletedMemberIds");
//                    if(deletedMemberIds!=null)
//                        chatViewModel.doHandleMemberFromDB(deletedMemberIds, chatRoomEntity.getId(), true);
//                }
//                break;
//            case MsgConstant.NOTICE_DISCUSS_GROUP_USER_PROFILE_CHANGED: //多人聊天室成員更名
//                Map<String, Object> userProfile = JsonHelper.getInstance().fromToMap(eventMsg.getData().toString());
//                String userId = Objects.requireNonNull(userProfile.get("userId")).toString();
//                if (chatRoomEntity.getMemberIds().contains(userId)) {
//                    chatViewModel.doUpdateRoomTitle(chatRoomEntity.getId(), userId, Objects.requireNonNull(userProfile.get("nickName")).toString());
//                }
//                break;
//            default:
//                break;
//        }
//    }
//
//    private void showProvisionalToServiceNumber(ChatRoomEntity roomEntry) {
//        new AlertView.Builder()
//                .setContext(this)
//                .setStyle(AlertView.Style.Alert)
//                .setTitle("您已成為服務號成員")
//                .setMessage("此服務號管理者已將您加入到服務號成員的行列之中")
//                .setOthers(new String[]{"確定"})
//                .setOnItemClickListener((o, position) -> {
//                    ActivityTransitionsControl.navigateToChat(this, roomEntry, ChatFragment.class.getSimpleName(), (intent, s) -> startActivity(intent));
//                    finish();
//                })
//                .build()
//                .setCancelable(true)
//                .setOnDismissListener(null)
//                .show();
//    }
//
//    public void doBackAction(View v) {
////        ActivityManager.finishAll();
//        KeyboardHelper.hide(v);
//        UserPref.getInstance(this).setCurrentRoomId("");
//        finish();
//        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
//    }
//
//    public void rightAction(View view) {
//        view.post(() -> {
//            if (!ChatActivity.this.isFinishing()) {
//                rightViewToggle();
//            }
//        });
//    }
//
//    void doNodeAction(View view) {
//        doSearchCancelAction(view);
//        if(chatRoomEntity == null) return;
//        if (CAN_NEXT) {
//            CAN_NEXT = false;
//            ActivityTransitionsControl.navigateToFullTextSearch(this, chatRoomEntity.getId(), binding.title.getText().toString(), (intent, s) -> {
//                startActivity(intent);
//            });
//        }
//    }
//
//    /**
//     * 聊天室內搜索功能
//     */
//    public void doSearchAction(View view) {
//        binding.etSearch.requestFocus();
//        KeyboardHelper.open(binding.etSearch);
//        chatFragment.doSearchAction(binding.searchBar, binding.etSearch, binding.clearInput);
//    }
//    /**
//     * 切換渠道
//     * version 1.10.o
//     */
//    public void doChannelChangeAction(View view) {
//        chatFragment.doChannelChangeAction();
//    }
//
//    public void setChannelIconVisibility(@DrawableRes int resId, ServiceNumberStatus status) {
//        binding.ivChannel.setImageResource(resId);
//        if (ServiceNumberStatus.ON_LINE.equals(status)) {
//            binding.ivChannel.setVisibility(View.GONE);
//        } else {
//            binding.ivChannel.setVisibility(View.VISIBLE);
//        }
//    }
//
//    /**
//     * 搜索模式展開後，取消事件
//     */
//    public void doSearchCancelAction(View view) {
//        KeyboardHelper.hide(binding.searchCancelTV);
//        binding.searchBar.setVisibility(View.GONE);
//        Objects.requireNonNull(binding.etSearch.getText()).clear();
//        if(chatFragment != null) chatFragment.doSearchCancelAction();
//    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            // 搜索模式已經打開，先關閉
//            if (binding.searchBar.getVisibility() == View.VISIBLE) {
//                doSearchCancelAction(null);
//                return true;
//            }
//            // 若主題聊天室已經開啟，先關閉
//            if (chatFragment != null) {
//                if (chatFragment.isFloatViewOpenAndExecuteClose()) {
//                    return true;
//                }
//            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//    private void onInviteSuccess(List<UserProfileEntity> list) {
//        this.members.addAll(list);
//        if (!ChatRoomType.discuss.equals(chatRoomEntity.getType())) {
//            binding.title.setText(getTitleText());
//            updateSession();
//        }
//        if (chatRoomMembersAdapter != null) {
//            chatRoomMembersAdapter.refreshData();
//        }
//    }
//
//
//    private FragmentTouchListener fragmentTouchListener = null;
//    public void setRegisterFragmentTouchListener(FragmentTouchListener listener) {
//        this.fragmentTouchListener = listener;
//    }
//
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if(ev.getAction() == MotionEvent.ACTION_UP) {
//            if (fragmentTouchListener != null)
//                fragmentTouchListener.onTouchEvent(ev);
//        }
//        return super.dispatchTouchEvent(ev);
//    }
//
//    public interface FragmentTouchListener {
//        boolean onTouchEvent(MotionEvent event);
//    }
//    public interface OnRefreshProvisionalMemberListener {
//        void onRefreshMemberList(List<String> list);
//
//        void onRemoveMember(String memberId);
//    }
//
//    public boolean checkClientMainPageFromAiff() {
//        //AIFF 客戶主頁
//        List<AiffInfo> aiffInfoList = AiffDB.getInstance(this).getAiffInfoDao().getAiffInfoListByIndex();
//        if(aiffInfoList.size() > 0) {
//            for (AiffInfo aiff : aiffInfoList) {
//                //CELog.d("Kyle2 name="+aiff.getName()+", embed="+aiff.getEmbedLocation()+", diaplay="+aiff.getDisplayLocation());
//                if(aiff.getEmbedLocation().equals(AiffEmbedLocation.ContactHome.name())) {
//                    AiffInfo aiffInfo = AiffDB.getInstance(this).getAiffInfoDao().getAiffInfo(aiff.getId());
//                    aiffManager.showAiffViewByInfo(aiffInfo);
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//}
