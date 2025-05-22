package tw.com.chainsea.chat.view.homepage;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.List;

import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference;
import tw.com.chainsea.ce.sdk.service.ChatRoomService;
import tw.com.chainsea.ce.sdk.service.ChatServiceNumberService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.ActivitySubscribeInformationHomepageBinding;
import tw.com.chainsea.chat.lib.ActivityManager;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.ui.activity.ChatActivity;
import tw.com.chainsea.chat.util.AvatarKit;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.util.NameKit;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.chat.view.roomList.mainRoomList.adapter.BaseRoomList3Adapter;
import tw.com.chainsea.chat.view.roomList.mainRoomList.adapter.MainRoomList3Adapter;
import tw.com.chainsea.chat.view.roomList.mainRoomList.listener.OnRoomItem3ClickListener;

public class SubscribeInformationHomepageActivity extends AppCompatActivity implements OnRoomItem3ClickListener<ChatRoomEntity> {
    private ActivitySubscribeInformationHomepageBinding binding;
    private final AvatarKit avatarKit = new AvatarKit();
    private ServiceNumberEntity entity = null;

    private boolean CAN_NEXT = false;
    NameKit nameKit = new NameKit();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.INSTANCE.setTheme(SubscribeInformationHomepageActivity.this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscribe_information_homepage);

        ActivityManager.finishBy(SubscribeInformationHomepageActivity.class);
        ActivityManager.addActivity(SubscribeInformationHomepageActivity.this);

        String subscribeNumberId = getIntent().getStringExtra(BundleKey.SUBSCRIBE_NUMBER_ID.key());

        this.entity = ServiceNumberReference.findSubscribeNumberById(null, subscribeNumberId);
        if (this.entity == null) {
            findServiceNumber(subscribeNumberId);
        } else {
            refreshLayout(this.entity);
        }

        initListener();
    }

    private void initListener() {
        binding.leftAction.setOnClickListener(this::doBackAction);
        binding.ivSubscribe.setOnClickListener(this::doSubscribeAction);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CAN_NEXT = true;
    }

    void doBackAction(View view) {
        finish();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    void doSubscribeAction(View view) {
        String subscribeNumberId = getIntent().getStringExtra(BundleKey.SUBSCRIBE_NUMBER_ID.key());
        ApiManager.doServiceNumberSubscribe(this, subscribeNumberId, true, new ApiListener<String>() {
            @Override
            public void onSuccess(String roomId) {
                findServiceNumber(subscribeNumberId);
            }

            @Override
            public void onFailed(String errorMessage) {

            }
        });
    }

    private void findServiceNumber(String subscribeNumberId) {
        ChatServiceNumberService.findServiceNumber(this, subscribeNumberId, RefreshSource.REMOTE, new ServiceCallBack<ServiceNumberEntity, RefreshSource>() {
            @Override
            public void complete(ServiceNumberEntity serviceNumberEntity, RefreshSource refreshSource) {
                SubscribeInformationHomepageActivity.this.entity = serviceNumberEntity;
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    refreshLayout(serviceNumberEntity);
                });
            }

            @Override
            public void error(String message) {
            }
        });
    }


    private void findRoomItem(String roomId) {
        String selfId = TokenPref.getInstance(this).getUserId();
        ChatRoomService.getInstance().getChatRoomItem(this, selfId, roomId, RefreshSource.REMOTE, new ServiceCallBack<ChatRoomEntity, RefreshSource>() {
            @Override
            public void complete(ChatRoomEntity chatRoomEntity, RefreshSource refreshSource) {
                ChatRoomReference.getInstance().save(chatRoomEntity);
                combinationRelationList(chatRoomEntity);
            }

            @Override
            public void error(String message) {

            }
        });
    }

    private void refreshLayout(ServiceNumberEntity entity) {
        String name = entity.getName();
        binding.tvTitle.setText(name);
        binding.tvSubscribeNumberName.setText(name);
        binding.tvSubscribeNumberContent.setText(entity.getDescription());
//        AvatarService.post(this, entity.getAvatarId(), PicSize.MED, binding.civSubscribeAvatar, R.drawable.default_avatar);
        String avatarId = entity.getAvatarId();
        String shortName = nameKit.getAvatarName(name);
        if (Strings.isNullOrEmpty(avatarId) || AvatarKit.DEFAULT_AVATAR_ID.equals(avatarId)) {
            binding.civSubscribeAvatar.setVisibility(View.INVISIBLE);
            binding.tvAvatar.setVisibility(View.VISIBLE);
            binding.tvAvatar.setText(shortName);
            GradientDrawable gradientDrawable = (GradientDrawable) binding.tvAvatar.getBackground();
            gradientDrawable.setColor(Color.parseColor(nameKit.getBackgroundColor(shortName)));
        } else
            avatarKit.loadCEAvatar(avatarId, binding.civSubscribeAvatar, binding.tvAvatar, name);
        refreshLayout2();
    }

    private void refreshLayout2() {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.ivSubscribeBackground.getLayoutParams();
        params.height = entity.isSubscribe() ? UiHelper.dip2px(this, 270) : ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
        binding.ivSubscribe.setVisibility(entity.isSubscribe() ? View.GONE : View.VISIBLE);
        binding.rvRelatedRoomList.setVisibility(entity.isSubscribe() ? View.VISIBLE : View.GONE);
        binding.xRefreshLayout.setVisibility(entity.isSubscribe() ? View.VISIBLE : View.GONE);
        if (entity.isSubscribe()) {
            ChatRoomEntity roomEntity = ChatRoomReference.getInstance().findById2("", entity.getRoomId(), true, true, false, true, false);
            if (roomEntity == null) {
                findRoomItem(entity.getRoomId());
            } else {
                combinationRelationList(roomEntity);
            }
        }
    }

    private void combinationRelationList(ChatRoomEntity roomEntity) {
        List<ChatRoomEntity> businessEntities = ChatRoomReference.getInstance().findServiceBusinessByServiceNumberIdAndOwnerIdAndNotRoomId(roomEntity.getServiceNumberId(), roomEntity.getOwnerId(), roomEntity.getId(), ChatRoomType.subscribe);
        List<ChatRoomEntity> list = Lists.newArrayList(roomEntity);
        list.addAll(businessEntities);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvRelatedRoomList.setLayoutManager(linearLayoutManager);
        binding.rvRelatedRoomList.setItemAnimator(new DefaultItemAnimator());

        BaseRoomList3Adapter<ChatRoomEntity> adapter = new MainRoomList3Adapter(this)
            .setWhetherToAssemble(false)
            .setFilterConsult(false)
            .setOnRoomItemClickListener(this)
            .setData(list);

        binding.rvRelatedRoomList.setAdapter(adapter);
        adapter.refreshData();

    }

    @Override
    public void onItemClick(ChatRoomEntity chatRoomEntity) {
        if (CAN_NEXT) {
            CAN_NEXT = false;
            String whereCome = getIntent().getStringExtra(BundleKey.WHERE_COME.key());
            String roomId = getIntent().getStringExtra(BundleKey.ROOM_ID.key());
            if (ChatActivity.class.getSimpleName().equals(whereCome) && chatRoomEntity.getId().equals(roomId)) {
                finish();
            } else if (ChatActivity.class.getSimpleName().equals(whereCome)) {
                ActivityTransitionsControl.navigateToChat(this, chatRoomEntity, ChatActivity.class.getSimpleName(), (intent, s) -> {
                    IntentUtil.INSTANCE.start(this, intent);
                    finish();
                    ActivityManager.finishBy(ChatActivity.class);
                });
            } else {
                ActivityTransitionsControl.navigateToChat(this, chatRoomEntity, ChatActivity.class.getSimpleName(), (intent, s) -> IntentUtil.INSTANCE.start(this, intent));
            }
        }
    }

    @Override
    public void onComponentItemClick(ChatRoomEntity chatRoomEntity) {

    }

    @Override
    public void onChildItemClick(ChatRoomEntity chatRoomEntity, String key) {
        if (CAN_NEXT) {
            CAN_NEXT = false;
            ActivityTransitionsControl.navigateToChat(this, chatRoomEntity, ChatActivity.class.getSimpleName(), (intent, s) -> IntentUtil.INSTANCE.start(this, intent));
        }
    }
}
