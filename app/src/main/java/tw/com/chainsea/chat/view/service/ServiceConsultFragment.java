package tw.com.chainsea.chat.view.service;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Sets;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.service.ChatServiceNumberService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.FragmentServiceConsultBinding;
import tw.com.chainsea.chat.view.service.adapter.ServiceNumberConsultListAdapter;

/**
 * Create by evan on 1/22/21
 *
 * @author Evan Wang
 * @date 1/22/21
 */

public class ServiceConsultFragment<L extends ServiceNumberConsultListAdapter.OnSelectListener> extends Fragment {


    ServiceNumberConsultListAdapter<ServiceNumberEntity> adapter = new ServiceNumberConsultListAdapter<ServiceNumberEntity>(ServiceNumberConsultListAdapter.Type.ROW);


    private FragmentServiceConsultBinding binding;

    public ServiceConsultFragment() {
    }

    public static ServiceConsultFragment newInstance(Bundle bundle) {
        ServiceConsultFragment fragment = new ServiceConsultFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ServiceConsultFragment newInstance() {
        return new ServiceConsultFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentServiceConsultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @SuppressLint("UseRequireInsteadOfGet")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvConsultList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        binding.rvConsultList.setAdapter(adapter);

        Set<String> blacklist = Sets.newHashSet();
        if (getArguments() != null) {
            blacklist.addAll(Objects.requireNonNull(getArguments().getStringArrayList(BundleKey.BLACK_LIST.key())));
        }

        ChatServiceNumberService.findConsultList(getContext(), blacklist, RefreshSource.REMOTE, new ServiceCallBack<List<ServiceNumberEntity>, RefreshSource>() {
            @Override
            public void complete(List<ServiceNumberEntity> entities, RefreshSource source) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    adapter.setData(entities).refreshData();
                });
            }

            @Override
            public void error(String message) {

            }
        });

    }

    public void setOnSelectListener(L onSelectListener) {
        adapter.setOnSelectListener(onSelectListener);
    }

    public void setKeyword(String keyword) {
        this.adapter.setKeyword(keyword).refreshData();
    }

    public void setSelectedId(Set<String> ids) {
        if (!ids.isEmpty()) {
            this.adapter.appendSelectedIds(ids).refreshData();
        } else {
            this.adapter.removeSelected().refreshData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
