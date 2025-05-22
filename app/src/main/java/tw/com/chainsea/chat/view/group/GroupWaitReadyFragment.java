package tw.com.chainsea.chat.view.group;

import static tw.com.chainsea.ce.sdk.database.sp.TokenPref.PreferencesKey.CP_TRANS_TENANT_ID;
import static tw.com.chainsea.ce.sdk.database.sp.TokenPref.PreferencesKey.CP_TRANS_TENANT_INFO;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.SocketEvent;
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.FragmentGroupWaitReadyBinding;
import tw.com.chainsea.chat.util.SystemKit;
import tw.com.chainsea.chat.view.BackFragment;

public class GroupWaitReadyFragment extends BackFragment {
    private Context context;
    private FragmentGroupWaitReadyBinding binding;
    private NavController navController;
//    private GroupViewModel viewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_group_wait_ready, container, false);
        navController = NavHostFragment.findNavController(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        viewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);
        try {
            Glide.with(context)
                    .asGif()
                    .load(R.raw.loading)
                    .into(binding.imgLoading);
        }catch (Exception ignored) {}


//        CpSocket.getInstance().setOnNoticeListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SocketEvent socketEvent){
        switch (socketEvent.getType()) {
            case TransTenantActive:
                RelationTenant response = (RelationTenant) socketEvent.getData();
                TokenPref.getInstance(context)
                        .clearByKey(CP_TRANS_TENANT_INFO)
                        .clearByKey(CP_TRANS_TENANT_ID);
                loginCE(response);
                break;
            case TransTenantDismiss:
                dismissTenantTrans();
                break;
        }
    }

    private void loginCE(RelationTenant relationTenant){
        SystemKit.changeTenant(requireActivity(), relationTenant, false);
    }

    private void dismissTenantTrans() {
        TokenPref.getInstance(context).clearByKey(CP_TRANS_TENANT_INFO);
        TokenPref.getInstance(context).clearByKey(CP_TRANS_TENANT_ID);
        getActivity().finish();
    }
}