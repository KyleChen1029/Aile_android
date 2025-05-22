package tw.com.chainsea.chat.view.homepage;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.model.Member;
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.ce.sdk.service.UserProfileService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.FragmentGroupMemberListBinding;
import tw.com.chainsea.chat.ui.adapter.GroupMemberListAdapter;
import tw.com.chainsea.chat.util.NoDoubleClickListener;
import tw.com.chainsea.chat.util.SortUtil;

public class GroupMemberListFragment extends Fragment {
    private FragmentGroupMemberListBinding binding;
    private Context context;
    private static final String SERVICE_NUMBER_ID = "SERVICE_NUMBER_ID";
    private static final String SERVICE_BROADCAST_ROOM_ID = "SERVICE_BROADCAST_ROOM_ID";
    private String serviceNumberId;
    private ServiceNumberEntity serviceNumberEntity;
    private GroupMemberListAdapter adapter;

    public static GroupMemberListFragment newInstance(String serviceNumberId, String broadcastRoomId) {
        GroupMemberListFragment fragment = new GroupMemberListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SERVICE_NUMBER_ID, serviceNumberId);
        bundle.putString(SERVICE_BROADCAST_ROOM_ID, broadcastRoomId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serviceNumberId = getArguments().getString(SERVICE_NUMBER_ID);
            serviceNumberEntity = ServiceNumberReference.findBroadcastServiceNumberById(null, serviceNumberId);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_group_member_list, container, false);
        ViewGroup.LayoutParams params = binding.viewSpace.getLayoutParams();
        params.height = UiHelper.dip2px(context, 24);
        binding.viewSpace.setLayoutParams(params);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.toolbar.title.setText(getString(R.string.group_member_list));
        binding.toolbar.leftAction.setOnClickListener(clickListener);

        UserProfileService.getEmployeeProfile(requireActivity(), RefreshSource.REMOTE, new ServiceCallBack<List<UserProfileEntity>, RefreshSource>() {
            @Override
            public void complete(List<UserProfileEntity> userProfiles, RefreshSource source) {
                List<UserProfileEntity> displays = new ArrayList<>();
                for (UserProfileEntity entity : userProfiles) {
                    if (!entity.isBlock() && UserType.EMPLOYEE.equals(entity.getUserType())) {
                        for (Member member : serviceNumberEntity.getMemberItems()) {
                            if (member.getId().equals(entity.getId())) {
                                entity.setPrivilege(member.getPrivilege());
                            }
                        }
                        displays.add(entity);
                    }
                }

                displays = SortUtil.INSTANCE.sortServiceNumberOwnerManagerByPrivilege(displays);
                adapter = new GroupMemberListAdapter(displays);
                binding.recycler.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
                binding.recycler.setAdapter(adapter);
                binding.recycler.setHasFixedSize(true);
                binding.recycler.setItemAnimator(new DefaultItemAnimator());
                binding.recycler.setNestedScrollingEnabled(false);

                binding.toolbar.title.setText(MessageFormat.format(getString(R.string.group_member_list) + "({0})", adapter.getData().size()));

                initListener();
            }

            @Override
            public void error(String message) {

            }
        });

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
    }

    private void initListener() {
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            UserProfileEntity userProfileEntity = (UserProfileEntity) adapter.getData().get(position);
            ApiManager.doTenantEmployeeDel(requireActivity(), userProfileEntity.getId(), new ApiListener<String>() {
                @Override
                public void onSuccess(String s) {
                    adapter.removeAt(position);
                    userProfileEntity.setUserType(UserType.CONTACT);
                    boolean isSave = UserProfileReference.saveUserProfile(null, userProfileEntity);
                }

                @Override
                public void onFailed(String errorMessage) {
                    Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
        @Override
        protected void onNoDoubleClick(View v) {
            if (v.equals(binding.toolbar.leftAction)) {
                requireActivity().onBackPressed();
            }
        }
    };


}
