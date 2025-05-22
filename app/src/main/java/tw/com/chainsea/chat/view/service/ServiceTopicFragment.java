package tw.com.chainsea.chat.view.service;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Set;

import tw.com.chainsea.ce.sdk.bean.broadcast.TopicEntity;
import tw.com.chainsea.ce.sdk.reference.TopicReference;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.FragmentServiceTopicBinding;
import tw.com.chainsea.chat.view.service.adapter.ServiceTopicAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class ServiceTopicFragment extends Fragment {
    FragmentServiceTopicBinding binding;
    ServiceTopicAdapter adapter = new ServiceTopicAdapter(ServiceTopicAdapter.Type.ROW);

    public ServiceTopicFragment() {
    }

    public static ServiceTopicFragment newInstance(Bundle bundle) {
        ServiceTopicFragment fragment = new ServiceTopicFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ServiceTopicFragment newInstance() {
        return new ServiceTopicFragment();
    }

    public void setOnTopicSelectListener(ServiceTopicAdapter.OnTopicSelectListener onTopicSelectListener) {
        adapter.setOnTopicSelectListener(onTopicSelectListener);
    }

    public void setSelectIds(Set<String> selectIds) {
        adapter.setSelectIds(selectIds).refresh();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service_topic, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvTopicList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        binding.rvTopicList.setAdapter(adapter);
        List<TopicEntity> entities = TopicReference.findAll(null);
        if (getArguments() != null) {
            String[] ids = getArguments().getStringArray(BundleKey.TOPIC_IDS.key());
            if (ids == null) {
                ids = new String[0];
            }
            adapter.setData(entities).select(entities, ids).refresh();
        } else {
            adapter.setData(entities).refresh();
        }
    }


    public void setKeyword(String keyword) {
        this.adapter.setKeyword(keyword).refresh();
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
