package tw.com.chainsea.chat.view.enlarge;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.MessageReference;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.ActivityEnLargeMessageBinding;
import tw.com.chainsea.chat.ui.adapter.WrapContentLinearLayoutManager;
import tw.com.chainsea.chat.view.enlarge.adapter.EnLargeMessageAdapter;
import tw.com.chainsea.custom.view.image.CircleImageView;
import tw.com.chainsea.custom.view.recyclerview.CurrentPagerSnapHelper;

// EVAN_FLAG 2020-04-14 (1.10.1) 放大訊息列表功能，未完成
public class EnLargeMessageActivity extends AppCompatActivity implements EnLargeMessageAdapter.OnEnLargeMessageListener {

    EnLargeMessageAdapter adapter;
    ChatRoomEntity roomEntity;

    private ActivityEnLargeMessageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT != Build.VERSION_CODES.O) this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        binding = ActivityEnLargeMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        String roomId = getIntent().getStringExtra(BundleKey.ROOM_ID.key());
        String messageId = getIntent().getStringExtra(BundleKey.MESSAGE_ID.key());

        roomEntity = ChatRoomReference.getInstance().findById(roomId);
        MessageEntity entity = MessageReference.findById(messageId);
        List<MessageEntity> entities = MessageReference.findByRoomId(roomId);
        Collections.sort(entities);

        adapter = new EnLargeMessageAdapter(Lists.newArrayList(), roomEntity)
            .setOnEnLargeMessageListener(this)
            .setData(entities);
        binding.rvMessageList.setLayoutManager(new WrapContentLinearLayoutManager(this));
        binding.rvMessageList.setAdapter(adapter);
        adapter.refreshData();

        new CurrentPagerSnapHelper()
            .setUpRecyclerView(binding.rvMessageList)
            .setOnCurrentPagerSnapListener(position -> scrollToPositionTo(position, true));

        int index = adapter.indexOf(entity);
        if (index > 0) {
            scrollToPositionTo(index, false);
        }
        initListener();
    }

    private void scrollToPositionTo(int position, boolean isSmooth) {
        if (binding.rvMessageList.getScrollState() != 0) {
            //recycleView正在滑动
            return;
        }
        adapter.handleCurrentPosition(position);
        if (isSmooth) {
            binding.rvMessageList.smoothScrollToPosition(position);
        } else {
            binding.rvMessageList.scrollToPosition(position);
        }
    }

    private void initListener() {
        binding.civLast.setOnClickListener(this::doLastAction);
        binding.ivLast.setOnClickListener(this::doLastAction);
        binding.civNext.setOnClickListener(this::doNextAction);
        binding.ivNext.setOnClickListener(this::doNextAction);
    }

    public void doLastAction(View view) {
        int current = ((LinearLayoutManager) Objects.requireNonNull(binding.rvMessageList.getLayoutManager())).findFirstCompletelyVisibleItemPosition();
        int position = current - 1;
        scrollToPositionTo(Math.max(position, 0), true);
    }

    public void doNextAction(View view) {
        int current = ((LinearLayoutManager) Objects.requireNonNull(binding.rvMessageList.getLayoutManager())).findFirstCompletelyVisibleItemPosition();
        int position = current + 1;
        scrollToPositionTo(Math.min(position, adapter.getItemCount() - 1), true);
    }

    @Override
    public void doLastAvatar(MessageEntity entity) {
        binding.civLast.setVisibility(entity == null ? View.INVISIBLE : View.VISIBLE);
        setAvatar(entity, binding.civLast);
    }

    @Override
    public void doNextAvatar(MessageEntity entity) {
        binding.civNext.setVisibility(entity == null ? View.INVISIBLE : View.VISIBLE);
        setAvatar(entity, binding.civNext);
    }

    private void setAvatar(MessageEntity entity, CircleImageView civAvatar) {
        if (entity != null) {
            AvatarService.post(this, entity.getAvatarId(), PicSize.SMALL, civAvatar, R.drawable.custom_default_avatar);
            if (roomEntity != null) {
                civAvatar.setBorder(0, 0);
                for (UserProfileEntity a : roomEntity.getMembers()) {
                    if (a.getId().equals(entity.getSenderId()) && !"ALL".equals(a.getNickName())) {
                        if (!UserType.EMPLOYEE.equals(a.getUserType())) {
                            civAvatar.setBorder(2, R.drawable.circle_session_employee_bg);
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
//        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
//        overridePendingTransition(R.anim.show_anim, R.anim.hide_anim);
    }
}
