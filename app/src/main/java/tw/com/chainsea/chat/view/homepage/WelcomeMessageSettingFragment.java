package tw.com.chainsea.chat.view.homepage;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.network.model.response.DictionaryItems;
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference;
import tw.com.chainsea.ce.sdk.service.ChatServiceNumberService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.DialogServiceNumberTimeOutBinding;
import tw.com.chainsea.chat.databinding.FragmentWelcomeMessageSettingBinding;
import tw.com.chainsea.chat.network.contact.ViewModelFactory;
import tw.com.chainsea.chat.ui.dialog.IosProgressDialog;
import tw.com.chainsea.chat.util.NoDoubleClickListener;
import tw.com.chainsea.chat.view.service.ServiceNumberHomePageViewModel;

public class WelcomeMessageSettingFragment extends Fragment {
    private static final String SERVICE_NUMBER_ID = "SERVICE_NUMBER_ID";
    private static final String NEED_TITLE_HEIGHT = "NEED_TITLE_HEIGHT";
    private String serviceNumberId;
    private FragmentWelcomeMessageSettingBinding binding;
    private Context context;
    private ServiceNumberEntity entity;

    private ServiceNumberHomePageViewModel serviceNumberHomePageViewModel;

    private Dialog serviceNumberTimeOutDialog;
    private IosProgressDialog iosProgressDialog;

    public static WelcomeMessageSettingFragment newInstance(String serviceNumberId, boolean needTitleHeight) {
        WelcomeMessageSettingFragment fragment = new WelcomeMessageSettingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SERVICE_NUMBER_ID, serviceNumberId);
        bundle.putBoolean(NEED_TITLE_HEIGHT, needTitleHeight);
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
        if(getArguments() != null){
            serviceNumberId = getArguments().getString(SERVICE_NUMBER_ID);
            entity = ServiceNumberReference.findBroadcastServiceNumberById(null, serviceNumberId);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_welcome_message_setting, container, false);
        boolean needTitleHeight = true;
        if(getArguments()!=null) {
            needTitleHeight = getArguments().getBoolean(NEED_TITLE_HEIGHT, true);
        }
        ViewGroup.LayoutParams params = binding.viewSpace.getLayoutParams();
        params.height = UiHelper.dip2px(context, needTitleHeight ? 24 : 0);
        binding.viewSpace.setLayoutParams(params);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.title.setText(getString(R.string.service_number_welcome_message_setting));
        binding.leftAction.setOnClickListener(clickListener);
        binding.layoutConfirm.setOnClickListener(clickListener);
        binding.clTimeOut.setOnClickListener(clickListener);
//        if(getArguments() != null){
//            updateWelcomeMessageUI(getArguments());
//        }
        initViewModel();
        observeData();
        getData();
        setIdleTime();
    }


    private void initViewModel() {
        ViewModelFactory viewModelFactory = new ViewModelFactory(requireActivity().getApplication());
        serviceNumberHomePageViewModel = new ViewModelProvider(this, viewModelFactory).get(ServiceNumberHomePageViewModel.class);
    }

    private void getData() {
        serviceNumberHomePageViewModel.getServiceIdleList();

        ChatServiceNumberService.findServiceNumber(requireContext(), serviceNumberId, RefreshSource.REMOTE, new ServiceCallBack<>() {
            @Override
            public void complete(ServiceNumberEntity entity, RefreshSource source) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    WelcomeMessageSettingFragment.this.entity = entity;
                    updateWelcomeMessageUI();
                    setIdleTime();
                });
            }

            @Override
            public void error(String message) {
            }
        });
    }

    private void observeData() {
        serviceNumberHomePageViewModel.getTimeList().observe(getViewLifecycleOwner(), this::createDialog);
    }

    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
        @Override
        protected void onNoDoubleClick(View v) {
            if (v.equals(binding.leftAction)) {
                requireActivity().onBackPressed();
            }
            else if(v.equals(binding.layoutConfirm)){
                Editable serviceWelcomeMessage = binding.edtFirstWelcomeMessage.getText();
                Editable serviceIdleMessage = binding.edtNoReplyWelcomeMessage.getText();
                Editable everyContactMessage = binding.edtEachWelcomeMessage.getText();
                updateWelcomeMessage(
                        (serviceWelcomeMessage != null) ? serviceWelcomeMessage.toString() : null,
                        (serviceIdleMessage != null) ? serviceIdleMessage.toString() : null,
                        (everyContactMessage != null) ? everyContactMessage.toString() : null,
                        entity.getServiceIdleTime()
                );
            } else if (v.equals(binding.clTimeOut)) {
                showTimeOutDialog();
            }
        }
    };

    private void createDialog(List<DictionaryItems> list) {
        if (getContext() != null) {
            serviceNumberTimeOutDialog = new Dialog(getContext());
            DialogServiceNumberTimeOutBinding dialogServiceNumberTimeOutBinding = DialogServiceNumberTimeOutBinding.inflate(LayoutInflater.from(getContext()));
            serviceNumberTimeOutDialog.setContentView(dialogServiceNumberTimeOutBinding.getRoot());
            ServiceNumberTimeOutAdapter serviceNumberTimeOutAdapter = new ServiceNumberTimeOutAdapter(list, idleTime -> {
                Editable serviceWelcomeMessage = binding.edtFirstWelcomeMessage.getText();
                Editable serviceIdleMessage = binding.edtNoReplyWelcomeMessage.getText();
                Editable everyContactMessage = binding.edtEachWelcomeMessage.getText();
                updateWelcomeMessage(
                        (serviceWelcomeMessage != null) ? serviceWelcomeMessage.toString() : null,
                        (serviceIdleMessage != null) ? serviceIdleMessage.toString() : null,
                        (everyContactMessage != null) ? everyContactMessage.toString() : null,
                        idleTime);
                serviceNumberTimeOutDialog.dismiss();
                showIosDialog();
                return null;
            });
            CustomItemDecoration divider = new CustomItemDecoration(getContext());
            dialogServiceNumberTimeOutBinding.rvTimeList.addItemDecoration(divider);
            dialogServiceNumberTimeOutBinding.rvTimeList.setLayoutManager(new LinearLayoutManager(getContext()));
            dialogServiceNumberTimeOutBinding.rvTimeList.setAdapter(serviceNumberTimeOutAdapter);
            dialogServiceNumberTimeOutBinding.tvTitle.setText(getString(R.string.text_dialog_no_answer_title));
            dialogServiceNumberTimeOutBinding.tvCancel.setOnClickListener(v -> {
                if (serviceNumberTimeOutDialog.isShowing()) serviceNumberTimeOutDialog.dismiss();
            });
        }
    }

    private void showTimeOutDialog() {
        if (serviceNumberTimeOutDialog != null && getActivity() != null) {
            if (!getActivity().isFinishing() && !getActivity().isDestroyed()) {
                serviceNumberTimeOutDialog.show();
                Window window = serviceNumberTimeOutDialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
            }
        }
    }

    private void setIdleTime() {
        String idleTime = "";
        if (getContext() != null) {
            int timeout = entity.getServiceIdleTime();
            if (timeout == 0) {
                idleTime = getContext().getString(R.string.text_dialog_time_out_no_limit);
            } else {
                int min = timeout / 60;
                switch (min) {
                    case 5 -> idleTime = getContext().getString(R.string.text_dialog_time_out_five_min);
                    case 10 -> idleTime = getContext().getString(R.string.text_dialog_time_out_ten_min);
                    case 15 -> idleTime = getContext().getString(R.string.text_dialog_time_out_fifteen_min);
                    case 30 -> idleTime = getContext().getString(R.string.text_dialog_time_out_thirty_min);
                    case 60 -> idleTime = getContext().getString(R.string.text_dialog_time_out_one_hour);
                    default -> idleTime = getContext().getString(R.string.text_dialog_time_out_no_limit);
                }
            }
        }
        binding.tvTimeOutValue.setText(idleTime);
    }

    private void updateWelcomeMessage(String serviceWelcomeMessage, String serviceIdleMessage, String everyContactMessage, int idleTime) {
        ApiManager.updateServiceNumberWelcomeMessage(context, serviceNumberId, serviceWelcomeMessage, serviceIdleMessage, everyContactMessage, idleTime, new ApiListener<>() {
            @Override
            public void onSuccess(String result) {
                entity.setFirstWelcomeMessage(serviceWelcomeMessage);
                entity.setEachWelcomeMessage(everyContactMessage);
                entity.setIntervalWelcomeMessage(serviceIdleMessage);
                entity.setServiceIdleTime(idleTime);
                ServiceNumberReference.updateWelcomeData(entity.getServiceNumberId(), serviceWelcomeMessage, serviceIdleMessage, everyContactMessage, idleTime);
                ThreadExecutorHelper.getMainThreadExecutor().execute(()-> {
                    updateWelcomeMessageUI();
                    setIdleTime();
                    dismissIosDialog();
                    Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailed(String errorMessage) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(()-> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateWelcomeMessageUI(){
        binding.edtFirstWelcomeMessage.setText(entity.getFirstWelcomeMessage());
        binding.edtEachWelcomeMessage.setText(entity.getEachWelcomeMessage());
        binding.edtNoReplyWelcomeMessage.setText(entity.getIntervalWelcomeMessage());
    }

    private void showIosDialog() {
        if (getActivity() != null) {
            if (!getActivity().isFinishing() && !getActivity().isDestroyed())
                if (iosProgressDialog == null) iosProgressDialog = new IosProgressDialog(getActivity());
            iosProgressDialog.show();
        }
    }

    private void dismissIosDialog() {
        if (iosProgressDialog != null) {
            iosProgressDialog.dismiss();
        }
    }
}
