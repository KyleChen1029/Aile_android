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

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.response.TenantServiceNumberListResponse;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.FragmentGroupMemberListBinding;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.ui.adapter.ServiceNumberListAdapter;
import tw.com.chainsea.chat.util.NoDoubleClickListener;

public class ServiceNumberListFragment extends Fragment {
    private FragmentGroupMemberListBinding binding;
    private Context context;
    private ServiceNumberListAdapter adapter;
//    private Gson gson;

    public static ServiceNumberListFragment newInstance() {
        return new ServiceNumberListFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
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
        binding.toolbar.title.setText(getString(R.string.group_service_list));
        binding.toolbar.leftAction.setOnClickListener(clickListener);
        ApiManager.doTenantServiceNumberList(requireContext(), new ApiListener<String>() {

            @Override
            public void onSuccess(String s) {
                TenantServiceNumberListResponse response = JsonHelper.getInstance().from(s, TenantServiceNumberListResponse.class);
                adapter = new ServiceNumberListAdapter(response.getItems());
                binding.recycler.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
                binding.recycler.setAdapter(adapter);
                binding.recycler.setHasFixedSize(true);
                binding.recycler.setItemAnimator(new DefaultItemAnimator());
                binding.recycler.setNestedScrollingEnabled(false);

//                initListener();
            }

            @Override
            public void onFailed(String errorMessage) {
                Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        binding.searchView.setOnClickListener(v-> binding.searchView.setIconified(false));
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

    private void initListener(){
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            TenantServiceNumberListResponse.ItemsDTO itemsDTO = (TenantServiceNumberListResponse.ItemsDTO) adapter.getData().get(position);
            ActivityTransitionsControl.navigateToSubscribePage(requireContext(), itemsDTO.getServiceNumberId(), "",true, (intent, s) -> {
                startActivity(intent);
                requireActivity().overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
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
