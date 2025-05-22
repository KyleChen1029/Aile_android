package tw.com.chainsea.chat.view.todo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import tw.com.chainsea.android.common.event.KeyboardHelper;
import tw.com.chainsea.ce.sdk.bean.business.BusinessEntity;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.chat.databinding.FragmentBusinessCooperateBinding;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.ui.fragment.BaseFragment;
import tw.com.chainsea.chat.view.todo.adapter.BusinessListMeAdapter;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperCallback;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension;

public class BusinessCooperateFragment extends BaseFragment<TodoOverviewType, BusinessEntity> implements IBusinessFragment, BusinessListMeAdapter.OnBusinessListMeItemClockListener<BusinessEntity> {

    BusinessListMeAdapter adapter = new BusinessListMeAdapter();

    ItemTouchHelperCallback mCallback = new ItemTouchHelperCallback(ItemTouchHelper.START | ItemTouchHelper.END);
    ItemTouchHelperExtension itemTouchHelper = new ItemTouchHelperExtension(mCallback);

    TodoOverviewType type = TodoOverviewType.BUSINESS_COOPERATE;

    FragmentBusinessCooperateBinding binding;
    public BusinessCooperateFragment() {
    }

    public static BusinessCooperateFragment newInstance() {
        return new BusinessCooperateFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBusinessCooperateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        itemTouchHelper.attachToRecyclerView(binding.rvList);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        this.adapter.setOnBusinessListMeItemClockListener(this);
        binding.rvList.setAdapter(this.adapter);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public TodoOverviewType getType() {
        return type;
    }

    @Override
    public void setData(BusinessEntity entity) {
        super.setData(entity);
    }

    @Override
    public void setData(List<BusinessEntity> list) {
        super.setData(list);
        if (adapter != null) {
            adapter.setType(this.type).setData(list).refreshData();
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_UPDATE_TODO_OVER_VIEW_ITEM_COUNT));
        }
    }

    @Override
    public void refresh() {
        super.refresh();
        if (adapter != null) {
            adapter.refreshData();
        }
    }

    @Override
    public int getDataCount() {
        if (adapter != null) {
            return adapter.getExpiredCount(System.currentTimeMillis());
        }
        return super.getDataCount();
    }

    @Override
    public void setKeyword(String keyword) {
        if (adapter != null) {
            adapter.setKeyword(keyword).refreshData();
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_UPDATE_TODO_OVER_VIEW_ITEM_COUNT));
        }
    }

    @Override
    public void onItemSelect(TodoOverviewType type, BusinessEntity entity, int position) {
        KeyboardHelper.hide(getView());
        if (this.onActionListener != null) {
            this.onActionListener.action("select", entity);
        }
        ActivityTransitionsControl.navigateToBusinessDetail(getContext(), entity.getId(), (intent, s) -> startActivity(intent));
    }
}
