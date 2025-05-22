package tw.com.chainsea.chat.view.homepage;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.luck.picture.lib.utils.ToastUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.text.StringHelper;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.account.AccountType;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.bean.label.Label;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.cache.ChatMemberCacheService;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.network.NetworkManager;
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse;
import tw.com.chainsea.ce.sdk.network.model.request.GetRoomMemberRequest;
import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomMemberResponse;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.LabelReference;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.ce.sdk.service.ChatRoomService;
import tw.com.chainsea.ce.sdk.service.LabelService;
import tw.com.chainsea.ce.sdk.service.PhotoService;
import tw.com.chainsea.ce.sdk.service.UserProfileService;
import tw.com.chainsea.ce.sdk.service.listener.ProgressServiceCallBack;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.ActivityEmployeeInformationHomepageBinding;
import tw.com.chainsea.chat.databinding.ItemBaseRoom6Binding;
import tw.com.chainsea.chat.network.contact.ContactPersonViewModel;
import tw.com.chainsea.chat.network.contact.ViewModelFactory;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity;
import tw.com.chainsea.chat.ui.activity.ComplaintActivity;
import tw.com.chainsea.chat.ui.dialog.InputDialogBuilder;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.util.NoDoubleClickListener;
import tw.com.chainsea.chat.util.TextViewHelper;
import tw.com.chainsea.chat.util.Unit;
import tw.com.chainsea.chat.view.account.BackgroundCanvasTransformer;
import tw.com.chainsea.chat.view.chat.ChatService;
import tw.com.chainsea.chat.widget.AnimatorToast;
import tw.com.chainsea.custom.view.alert.AlertView;
import tw.com.chainsea.custom.view.progress.IosProgressBar;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperCallback;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension;

public class EmployeeInformationHomepageActivity extends BaseHomepageActivity implements LifecycleEventObserver {
    private ActivityEmployeeInformationHomepageBinding binding;
    private static final int ADD_CONTACT_REQUEST_CODE = 0x2384;
    private UserProfileEntity profile;
    private String accountId;
    private String roomId;
    private RecyclerAdapter adapter;
    private String userId;

    private IosProgressBar progressBar;

    private ContactPersonViewModel contactPersonViewModel;
    private Dialog editNameDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_employee_information_homepage);
        accountId = getIntent().getStringExtra(BundleKey.ACCOUNT_ID.key());
        getLifecycle().addObserver(this);

        userId = TokenPref.getInstance(this).getUserId();
        binding.ivCall.setVisibility(View.GONE);
        binding.ivBarCode.setVisibility(View.GONE);
        binding.ivChat.setVisibility(View.GONE);
        binding.leftAction.setOnClickListener(this::doLeftAction);
        binding.rightAction.setOnClickListener(this::doRightAction);
        binding.ivFavourite.setOnClickListener(this::doFavouriteAction);
        binding.ivChat.setOnClickListener(this::doNavigateToChatAction);

        binding.tvName.setOnClickListener(this::doEditNameAction);
        binding.ivEditName.setOnClickListener(this::doEditNameAction);

        adapter = new RecyclerAdapter();
        ItemTouchHelperExtension itemTouchHelper = new ItemTouchHelperExtension(new ItemTouchHelperCallback(ItemTouchHelper.START));
        itemTouchHelper.attachToRecyclerView(binding.recycler);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.recycler.setAdapter(adapter);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setItemAnimator(new DefaultItemAnimator());
        binding.recycler.setNestedScrollingEnabled(false);

        binding.searchView.setOnClickListener(v -> binding.searchView.setIconified(false));
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        initViewModel();
        observeData();
    }

    private void initViewModel() {
        ViewModelFactory contactPersonFactory = new ViewModelFactory(getApplication());
        contactPersonViewModel = new ViewModelProvider(this,
            contactPersonFactory).get(ContactPersonViewModel.class);
    }

    private void observeData() {
        contactPersonViewModel.getLoading().observe(this, isLoading -> {
            if (isLoading) {
                if (progressBar == null) {
                    progressBar = IosProgressBar.show(this, getString(R.string.wording_loading), true, false, dialog -> {
                    });
                }
            } else {
                if (progressBar != null) {
                    progressBar.dismiss();
                }
            }
        });

        contactPersonViewModel.getRoomId().observe(this, roomId -> ActivityTransitionsControl.navigateToChat(this, roomId, (intent, s) -> {
            startActivity(intent);
            finish();
        }));
        contactPersonViewModel.getUpdatedFriendInfo().observe(this, triple -> {
            if (triple.getFirst()) {

                if (Strings.isNullOrEmpty(triple.getSecond())) {
                    binding.tvName.setText(profile.getOriginName().length() > 17 ? profile.getOriginName().substring(0, 17) + "..." : profile.getOriginName());
                } else {
                    binding.tvName.setText(String.format("%s(%s)", profile.getOriginName(), triple.getSecond()).length() > 17 ? String.format("%s(%s)", profile.getOriginName(), triple.getSecond()).substring(0, 17) + "..." : String.format("%s(%s)", profile.getOriginName(), triple.getSecond()));
                }
                profile.setAlias(triple.getSecond());
                binding.civAccountAvatar.loadAvatarIcon(profile.getAvatarId(), profile.getNickName(), profile.getId());
                adapter.notifyItemRangeChanged(0, adapter.getItemCount());
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.Do_UPDATE_CONTACT_BY_LOCAL, triple.getThird()));
            }
        });
    }

    private void updateCustomerName(String name) {
        contactPersonViewModel.doUpdateFriendInfo(profile.getId(), name);
    }

    private void doEditNameAction(View v) {
        if (!editNameDialog.isShowing())
            editNameDialog.show();
    }

    /*
        從本地拉資料, 把聊天列表中, 自己和對象同時是聊天室成員的資料顯示出來
     */
    private void getUserRoomItem() {
        if (roomId == null) roomId = UserProfileReference.findRoomIdByAccountId(null, accountId);
        ChatRoomService.getInstance().getChatRoomEntitiesFromDb(this, ChatRoomSource.MAIN, new ServiceCallBack<>() {
            @Override
            public void complete(List<ChatRoomEntity> entities, RefreshSource refreshSource) {
                binding.ivChat.setVisibility(View.VISIBLE);
                ThreadExecutorHelper.getApiExecutor().execute(() -> {
                    List<ChatRoomEntity> rooms = new ArrayList<>();
                    AtomicInteger completedRequests = new AtomicInteger(0);
                    List<ChatRoomEntity> filterRoom = entities.stream().filter( room -> room.getType() != ChatRoomType.broadcast).collect(Collectors.toList());
                    int totalRequests = filterRoom.size();
                    for (ChatRoomEntity entity : filterRoom) {
                        Retrofit retrofit = NetworkManager.INSTANCE.provideRetrofit(EmployeeInformationHomepageActivity.this);
                        ChatService chatService = retrofit.create(ChatService.class);
                        chatService.getMemberForJava(new GetRoomMemberRequest(entity.getId())).enqueue(new Callback<CommonResponse<List<ChatRoomMemberResponse>>>() {
                            @Override
                            public void onResponse(Call<CommonResponse<List<ChatRoomMemberResponse>>> call, Response<CommonResponse<List<ChatRoomMemberResponse>>> response) {
                                try {
                                    if (response.body() == null) return;
                                    List<ChatRoomMemberResponse> memberList = response.body().getItems();
                                    if (memberList == null || memberList.isEmpty()) return;
                                    List<ChatRoomMemberResponse>  filterMemberList = memberList.stream().filter(member -> !member.getDeleted()).collect(Collectors.toList());
                                    if (!isHasCommonChatRoom(filterMemberList)) return;

                                    entity.setChatRoomMember(filterMemberList);
                                    ChatRoomReference.getInstance().updateChatRoomMember(entity.getId(), filterMemberList);
                                    rooms.add(entity);
                                } finally {
                                    if (completedRequests.incrementAndGet() == totalRequests) {
                                        runOnUiThread(() -> adapter.setData(rooms));
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<CommonResponse<List<ChatRoomMemberResponse>>> call, Throwable throwable) {
                                if (completedRequests.incrementAndGet() == totalRequests) {
                                    runOnUiThread(() -> adapter.setData(rooms));
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void error(String message) {
            }
        });
    }

    public boolean isHasCommonChatRoom(List<ChatRoomMemberResponse> memberList) {
        boolean foundId1 = false;
        boolean foundId2 = false;

        for (ChatRoomMemberResponse member : memberList) {
            if (member.getMemberId().equals(userId)) {
                foundId1 = true;
            }
            if (member.getMemberId().equals(accountId)) {
                foundId2 = true;
            }
        }
        return foundId1 && foundId2;
    }

    protected void getUserProfile(RefreshSource source) {
        UserProfileService.getProfile(this, source, accountId, new ServiceCallBack<>() {
            @Override
            public void complete(UserProfileEntity userProfileEntity, RefreshSource source) {
                profile = userProfileEntity;
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> bindingInformation(userProfileEntity));
                if (RefreshSource.LOCAL.equals(source)) {
                    getUserProfile(RefreshSource.REMOTE);
                }
                editNameDialog = new InputDialogBuilder(EmployeeInformationHomepageActivity.this)
                    .setTitle(getString(R.string.text_input_nickname))
                    .setIsCanEmpty(true)
                    .setMaxLength(20)
                    .setInputData(userProfileEntity.getNickName())
                    .setOnConfirmListener(message -> updateCustomerName(message)).create();
            }

            @Override
            public void error(String message) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_CONTACT_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                assert data != null;
                String roomId = data.getStringExtra(BundleKey.ROOM_ID.key());
                profile.setType(AccountType.FRIEND);
                profile.setRoomId(roomId);
                if (DBManager.getInstance().insertFriends(profile)) {
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.ADD_FRIEND_FILTER));
                    Toast.makeText(this, "成功添加" + profile.getNickName() + "為好友", Toast.LENGTH_SHORT).show();
                    getUserProfile(RefreshSource.REMOTE);
                }
            }
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
        @Override
        protected void onNoDoubleClick(View v) {
            if (v.equals(binding.civAccountAvatar)) {
                String url = AvatarService.getAvatarUrl(EmployeeInformationHomepageActivity.this, profile.getAvatarId(), PicSize.LARGE);
                String turl = AvatarService.getAvatarUrl(EmployeeInformationHomepageActivity.this, profile.getAvatarId(), PicSize.SMALL);
                ActivityTransitionsControl.navigateToPhotoGallery(EmployeeInformationHomepageActivity.this, url, turl, (intent, s1) -> startActivity(intent));
            }
        }
    };

    private void bindingInformation(UserProfileEntity profile) {
        String remoteUrl = "";
        BackgroundCanvasTransformer.Res res = BackgroundCanvasTransformer.getBackgroundCanvas(profile.getId());

        if (profile.getHomePagePics() != null) {
            if (!profile.getHomePagePics().isEmpty()) {
                UserProfileEntity.HomePagePic pic = Iterables.getLast(profile.getHomePagePics());
                String picUrl = pic.getPicUrl();
                if (picUrl.startsWith("http")) {
                    remoteUrl = picUrl;
                } else {
                    remoteUrl = TokenPref.getInstance(this).getCurrentTenantUrl() + ApiPath.ROUTE + pic.getPicUrl();
                }
            }
            PhotoService.post(this, remoteUrl, binding.ivBackgroundPhoto, res.getResId(), new ProgressServiceCallBack<>() {
                @Override
                public void progress(float progress, long total) {

                }

                @Override
                public void error(String message) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> binding.ivBackgroundPhoto.setImageResource(res.getResId()));
                }

                @Override
                public void complete(Drawable drawable, RefreshSource source) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> binding.ivBackgroundPhoto.setImageDrawable(drawable));
                }
            });
        } else {
            binding.ivBackgroundPhoto.setImageResource(res.getResId());
        }

        binding.civAccountAvatar.loadAvatarIcon(profile.getAvatarId(), profile.getNickName(), profile.getId());

        if (Strings.isNullOrEmpty(profile.getAlias())) {
            binding.tvName.setText(profile.getNickName().length() > 17 ? profile.getNickName().substring(0, 17) + "..." : profile.getNickName());
        } else {
            binding.tvName.setText(String.format("%s(%s)", profile.getOriginName(), profile.getAlias()).length() > 17 ? String.format("%s(%s)", profile.getOriginName(), profile.getAlias()).substring(0, 17) + "..." : String.format("%s(%s)", profile.getOriginName(), profile.getAlias()));
        }
        if (Strings.isNullOrEmpty(profile.getDuty())) {
            binding.tvDutyName.setText(profile.getDepartment());
        } else {
            binding.tvDutyName.setText(String.format("%s/%s", profile.getDepartment(), profile.getDuty()));
        }

        boolean isFavourite = LabelReference.findIsFavouriteById(null, profile.getId());
        if (isFavourite) {
            binding.ivFavourite.setImageResource(R.drawable.ic_star_yellow);
        } else {
            binding.ivFavourite.setImageResource(R.drawable.ic_star_white);
        }

        binding.civAccountAvatar.setOnClickListener(clickListener);
    }

    void doLeftAction(View v) {
        finish();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    void doRightAction(View v) {
        new AlertView.Builder()
            .setContext(this)
            .setStyle(AlertView.Style.ActionSheet)
            .setOthers(new String[]{profile.isBlock() ? "解除封鎖" : "封鎖", "檢舉"})
            .setOtherTextColor(Color.BLACK)
            .setCancelText("取消")
            .setCancelTextColor(Color.BLACK)
            .setOnItemClickListener((o, position) -> {
                switch (position) {
                    case 0: //點夥伴聯絡人的封鎖位置
                        doBlock();
                        break;
                    case 1:
                        Intent intent1 = new Intent(this, ComplaintActivity.class);
                        intent1.putExtra(Constant.SESSION_TYPE, Constant.OBJECT_TYPE_USER);
                        intent1.putExtra(Constant.OBJECT_USERID, accountId);
                        startActivity(intent1);
                        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
                        break;
                }
            })
            .build()
            .setCancelable(true)
            .show();
    }

    void doFavouriteAction(View v) {
        if (profile == null) {
            return;
        }
        String accountId = profile.getId();
        boolean isFavourite = LabelReference.findIsFavouriteById(null, accountId);
        if (isFavourite) {
            LabelService.removeFavourite(this, accountId, new ServiceCallBack<>() {
                @Override
                public void complete(String s, Enum anEnum) {
                    AnimatorToast.makeSccessToast(EmployeeInformationHomepageActivity.this, "取消收藏").show();
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.REMOVE_LOVE_ACCOUNT_FILTER, accountId));
                    profile.setCollection(false);
                    broadUpdateAccount(profile);
                }

                @Override
                public void error(String errorMessage) {
                    AnimatorToast.makeErrorToast(EmployeeInformationHomepageActivity.this, "取消失敗").show();
                }
            });
        } else {
            LabelService.addFavourite(this, accountId, new ServiceCallBack<>() {
                @Override
                public void complete(Label label, Enum anEnum) {
                    AnimatorToast.makeSccessToast(EmployeeInformationHomepageActivity.this, "收藏成功").show();
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.ADD_LOVE_ACCOUNT_FILTER));
                    profile.setCollection(true);
                    broadUpdateAccount(profile);
                }

                @Override
                public void error(String errorMessage) {
                    AnimatorToast.makeErrorToast(EmployeeInformationHomepageActivity.this, "收藏失敗").show();
                }
            });
        }
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.REFRESH_ROOM_BY_LOCAL));
    }

    private void broadUpdateAccount(UserProfileEntity profile) {
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.ACCOUNT_REFRESH_FILTER, profile));
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> bindingInformation(profile));
    }

    void doNavigateToChatAction(View v) {
        String accountId = getIntent().getStringExtra(BundleKey.ACCOUNT_ID.key());
        String roomId = UserProfileReference.findRoomIdByAccountId(null, accountId);

        if (!Strings.isNullOrEmpty(roomId)) {
            Bundle bundle = new Bundle();
            bundle.putString(BundleKey.EXTRA_SESSION_ID.key(), roomId);
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_FINISH_ACTIVITY));
            IntentUtil.INSTANCE.startIntent(this, ChatNormalActivity.class, bundle);
            finish();
        } else {
            contactPersonViewModel.addContactFriend(profile.getId(), profile.getNickName());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMainEvent(EventMsg eventMsg) {
        if (eventMsg.getCode() == MsgConstant.REFRESH_ROOM_BY_LOCAL) {
            ChatRoomEntity refreshEntity = JsonHelper.getInstance().from(eventMsg.getString(), ChatRoomEntity.class);
            if (adapter != null) {
                adapter.setData(refreshEntity);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
        }
        return super.onKeyDown(keyCode, event);
    }

    public void doBlock() {
        if (profile != null) {
            String id = profile.getId();
            boolean isBlock = !profile.isBlock();
            ApiManager.doUserBlock(this, id, isBlock, new ApiListener<>() {
                @Override
                public void onSuccess(String s) {
                    DBManager.getInstance().setFriendBlock(id, isBlock);
                    if (profile != null) {
                        profile.setBlock(isBlock);
                    }
                    broadUpdateAccount(profile);
                    ThreadExecutorHelper.getIoThreadExecutor().execute(() ->
                        ToastUtils.showToast(EmployeeInformationHomepageActivity.this, isBlock ? getString(R.string.text_block_success) : getString(R.string.text_unblock_success)));
                }

                @Override
                public void onFailed(String errorMessage) {
                    CELog.e(errorMessage);
                }
            });
        }
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_CREATE) {
            getUserRoomItem();
            getUserProfile(RefreshSource.LOCAL);
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements Filterable {
        private Context context;
        private List<ChatRoomEntity> data;
        private List<ChatRoomEntity> filterData;
        private NameFilter filter;

        public void setData(ChatRoomEntity entity) {
            int location = 0;
            for (int i = 0; i < data.size(); i++) {
                if (entity.getId().equals(data.get(i).getId())) {
                    data.set(i, entity);
                    location = i;
                    break;
                } else if (i == data.size() - 1) {
                    data.add(entity);
                    location = data.size();
                }
            }
            filterData = new ArrayList<>(data);
            notifyItemChanged(location);
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setData(List<ChatRoomEntity> data) {
            this.data = data;
            filterData = new ArrayList<>(data);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            context = parent.getContext();
            return new ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_base_room_6, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatRoomEntity entity = data.get(position);
            String type = DBManager.getInstance().queryTypeFromLastMessage(entity.getId());
            if (!Strings.isNullOrEmpty(type) && MessageType.AT.equals(MessageType.of(type))) {
                if (entity.getMembers() == null || entity.getMembers().isEmpty()) {
                    entity.setMembers(ChatMemberCacheService.getChatMember(entity.getId()));
                }
            }

            if (ChatRoomType.discuss.equals(entity.getType()) || (ChatRoomType.group.equals(entity.getType()) && Strings.isNullOrEmpty(entity.getAvatarId()))) {
                if (entity.getMemberAvatarData() == null || entity.getMemberAvatarData().isEmpty()) {
                    entity.setMemberAvatarData(UserProfileReference.getMemberAvatarData(null, entity.getId(), userId, 4));
                }
            }

            String name = entity.getName();
            if (!Strings.isNullOrEmpty(entity.getBusinessId()) && ChatRoomType.FRIEND_or_SUBSCRIBE.contains(entity.getType())) {
                name = String.format("%s", name);
            }

            if (ChatRoomType.person.equals(entity.getType())) {
                holder.binding.ivDelete.setVisibility(View.GONE);
            } else {
                holder.binding.ivDelete.setVisibility(View.VISIBLE);
            }

            boolean isTop = entity.isTop();
            boolean isMute = entity.isMute();
            holder.binding.ivRemind.setVisibility(!entity.isMute() ? View.GONE : View.VISIBLE);
            Set<UserProfileEntity> members = Sets.newHashSet(entity.getMembers());

            if (entity.getType() == ChatRoomType.discuss) {
                if (entity.getChatRoomMember() != null)
                    holder.binding.civIcon.getChatRoomMemberIdsAndLoadMultiAvatarIcon(entity.getChatRoomMember(), entity.getId());
                else if (entity.getMemberIds() != null)
                    holder.binding.civIcon.loadMultiAvatarIcon(entity.getMemberIds(), entity.getId());
            } else if (entity.getType() == ChatRoomType.friend) {
                String friendId = entity.getMemberIds().stream().filter(id -> !id.equals(userId)).findFirst().orElse(null);
                UserProfileEntity user = DBManager.getInstance().queryFriend(friendId);
                if (user != null)
                    holder.binding.civIcon.loadAvatarIcon(entity.getAvatarId(), user.getNickName(), user.getId());
                else
                    holder.binding.civIcon.loadAvatarIcon(entity.getAvatarId(), entity.getName(), entity.getId());
            } else
                holder.binding.civIcon.loadAvatarIcon(entity.getAvatarId(), name, entity.getId());

            if (isTop) {
                holder.binding.civSmallIcon.setVisibility(View.VISIBLE);
                holder.binding.civSmallIcon.setImageResource(R.drawable.ic_s_top);
            } else {
                holder.binding.civSmallIcon.setVisibility(View.GONE);
            }
            holder.binding.ivTop.setImageResource(isTop ? R.drawable.ic_no_top : R.drawable.ic_top);
            holder.binding.ivMute.setImageResource(isMute ? R.drawable.amplification : R.drawable.not_remind);

            String unReadNumber = Unit.getUnReadNumber(entity);
            if (unReadNumber == null) {
                holder.binding.tvUnread.setVisibility(View.INVISIBLE);
            } else {
                holder.binding.tvUnread.setText(unReadNumber);
                holder.binding.tvUnread.setVisibility(View.VISIBLE);
            }

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(StringHelper.getString(name, ""));
//         判斷為社團 or 群組才顯示人數
            if (ChatRoomType.GROUP_or_DISCUSS.contains(entity.getType())) {
                builder.append(!entity.getChatRoomMember().isEmpty() ? " (" + entity.getChatRoomMember().size() + ")" : "");
            }

            // 判斷訂閱號或非員工給不同頭像外框色
            holder.binding.civIcon.setBorder(0, 0);
            boolean hasEmployees = true;
            for (UserProfileEntity a : members) {
                if (!UserType.EMPLOYEE.equals(a.getUserType()) && !a.isHardCode()) {
                    hasEmployees = false;
                }
            }
            if (!hasEmployees && ChatRoomType.friend.equals(entity.getType()) && Strings.isNullOrEmpty(entity.getBusinessId())) {
                SpannableString spannable = TextViewHelper.setLeftImage(context, builder.toString(), R.drawable.ic_customer_15dp);
                builder.clear();
                builder.append(spannable);
            } else if (ChatRoomType.serviceMember.equals(entity.getType())) {
                SpannableString spannable = TextViewHelper.setLeftImage(context, builder + "和祕書群", R.drawable.ic_service_member_b);
                builder.clear();
                builder.append(spannable);
            } else if (ChatRoomType.group.equals(entity.getType())) {
                SpannableString spannable = TextViewHelper.setLeftImage(context, builder.toString(), R.drawable.icon_group_chat_room);
                builder.clear();
                builder.append(spannable);
            }
            holder.binding.tvName.setText(builder);
            holder.binding.ivFavourite.setVisibility(entity.isFavourite() && Strings.isNullOrEmpty(entity.getBusinessId()) ? View.VISIBLE : View.INVISIBLE);
            holder.binding.clContentCell.setBackgroundResource(0);
        }

        @Override
        public int getItemCount() {
            return (data != null) ? data.size() : 0;
        }

        @Override
        public Filter getFilter() {
            if (filter == null) {
                filter = new NameFilter();
            }
            return filter;
        }

        private class ViewHolder extends ItemBaseViewHolder {
            ItemBaseRoom6Binding binding;

            private ViewHolder(ItemBaseRoom6Binding binding) {
                super(binding.getRoot());
                super.setMenuViews(binding.llLeftMenu, binding.llRightMenu);
                this.binding = binding;
                binding.getRoot().setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKey.EXTRA_SESSION_ID.key(), data.get(getAbsoluteAdapterPosition()).getId());
                    IntentUtil.INSTANCE.startIntent(EmployeeInformationHomepageActivity.this, ChatNormalActivity.class, bundle);
                    finish();
                });
            }
        }

        private class NameFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<ChatRoomEntity> results = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    results.addAll(filterData);
                } else {
                    for (ChatRoomEntity entity : filterData) {
                        if ((entity.getName() != null && entity.getName().contains(constraint))) {
                            results.add(entity);
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = results;
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                data.clear();
                data.addAll((Collection<? extends ChatRoomEntity>) results.values);
                notifyDataSetChanged();
            }
        }
    }
}
