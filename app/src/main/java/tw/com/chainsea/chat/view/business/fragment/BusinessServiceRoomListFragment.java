//package tw.com.chainsea.chat.view.business.fragment;
//
//import android.content.Context;
//import android.os.Bundle;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.databinding.DataBindingUtil;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.recyclerview.widget.ItemTouchHelper;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.google.common.collect.Sets;
//
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.databinding.FragmentBusinessServiceRoomListBinding;
//import tw.com.chainsea.chat.service.ActivityTransitionsControl;
//import tw.com.chainsea.chat.ui.fragment.BaseFragment;
//import tw.com.chainsea.chat.view.base.NavigationButtonTab;
//import tw.com.chainsea.chat.view.chatroom.adapter.listener.OnRoomItemClickListener;
////import tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ServiceNumberSectionedAdapter;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperCallback;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension;
//
//public class BusinessServiceRoomListFragment extends BaseFragment<NavigationButtonTab, ChatRoomEntity> implements OnRoomItemClickListener<ChatRoomEntity> {
//    private FragmentBusinessServiceRoomListBinding binding;
//
//    ItemTouchHelperCallback mCallback = new ItemTouchHelperCallback(ItemTouchHelper.START | ItemTouchHelper.END);
//    ItemTouchHelperExtension itemTouchHelper = new ItemTouchHelperExtension(mCallback);
////    ServiceNumberSectionedAdapter adapter = new ServiceNumberSectionedAdapter();
//    boolean CAN_NEXT = false;
//
//    public BusinessServiceRoomListFragment() {
//    }
//
//    public static BusinessServiceRoomListFragment newInstance() {
//        return new BusinessServiceRoomListFragment();
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_business_service_room_list, container, false);
//        return binding.getRoot();
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
//        binding.rvList.setAdapter(adapter);
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        CAN_NEXT = true;
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//    }
//
//
//    @Override
//    public NavigationButtonTab getType() {
//        return NavigationButtonTab.SERVICE;
//    }
//
//    @Override
//    public void setData(ChatRoomEntity roomEntity) {
//    }
//
//    @Override
//    public void setData(List<ChatRoomEntity> list) {
//        filter(list);
//        Set<String> names = Sets.newHashSet();
//        for (ChatRoomEntity entity : list) {
//            names.add(entity.getServiceNumberName());
//        }
//        adapter.setExpansionType(ServiceNumberSectionedAdapter.ExpansionType.ALL, names)
//                .setItemTouchHelperExtension(itemTouchHelper)
//                .setData(list, true)
//                .setOnRoomItemClickListener(this)
//                .refreshData();
//    }
//
//    private void filter(List<ChatRoomEntity> list) {
//        Iterator<ChatRoomEntity> iterator = list.iterator();
//        while (iterator.hasNext()) {
//            if (!ChatRoomType.SERVICES.equals(iterator.next().getType())) {
//                iterator.remove();
//            }
//        }
//    }
//
//    @Override
//    public void onItemClick(ChatRoomEntity roomEntity) {
//        if (CAN_NEXT) {
//            CAN_NEXT = false;
//            ActivityTransitionsControl.navigateToChat(getContext(), roomEntity.getId(), (intent, s) -> startActivity(intent));
//        }
//    }
//
//    @Override
//    public void onComponentItemClick(ChatRoomEntity roomEntity) {
//
//    }
//}
