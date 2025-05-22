//package tw.com.chainsea.chat.view.business.fragment;
//
//import android.content.Context;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.helper.ItemTouchHelper;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import java.util.Iterator;
//import java.util.List;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.service.ActivityTransitionsControl;
//import tw.com.chainsea.chat.ui.fragment.BaseFragment;
//import tw.com.chainsea.chat.view.base.NavigationButtonTab;
//import tw.com.chainsea.chat.view.chatroom.adapter.listener.OnRoomItemClickListener;
//import tw.com.chainsea.chat.view.roomList.baseList.adapter.BaseRoomList2Adapter;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperCallback;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension;
//
//public class BusinessMainRoomListFragment extends BaseFragment<NavigationButtonTab, ChatRoomEntity> implements OnRoomItemClickListener<ChatRoomEntity> {
//
//    @BindView(R.id.rv_list)
//    RecyclerView rvList;
//    ItemTouchHelperCallback mCallback = new ItemTouchHelperCallback(ItemTouchHelper.START | ItemTouchHelper.END);
//    ItemTouchHelperExtension itemTouchHelper = new ItemTouchHelperExtension(mCallback);
//    BaseRoomList2Adapter adapter = new BaseRoomList2Adapter();
//    boolean CAN_NEXT = false;
//
//    public static BusinessMainRoomListFragment newInstance() {
//        return new BusinessMainRoomListFragment();
//    }
//
//    public BusinessMainRoomListFragment() {
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_business_main_room_list, container, false);
//        ButterKnife.bind(this, view);
//        return view;
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
////        itemTouchHelper.attachToRecyclerView(rvList);
//        rvList.setLayoutManager(new LinearLayoutManager(getContext()));
//        rvList.setAdapter(adapter);
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
//    @Override
//    public NavigationButtonTab getType() {
//        return NavigationButtonTab.MAIN;
//    }
//
//    @Override
//    public void setData(ChatRoomEntity roomEntity) {
//    }
//
//    @Override
//    public void setData(List<ChatRoomEntity> list) {
//        filter(list);
//        adapter.setItemTouchHelperExtension(itemTouchHelper)
//                .setData(list, true)
//                .setOnRoomItemClickListener(this)
//                .refreshData();
//    }
//
//    private void filter(List<ChatRoomEntity> list) {
//        Iterator<ChatRoomEntity> iterator = list.iterator();
//        while (iterator.hasNext()) {
//            if (ChatRoomType.SERVICES.equals(iterator.next().getType())) {
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
