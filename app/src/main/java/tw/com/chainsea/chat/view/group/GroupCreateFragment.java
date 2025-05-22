package tw.com.chainsea.chat.view.group;

import static tw.com.chainsea.ce.sdk.database.sp.TokenPref.PreferencesKey.CP_TRANS_TENANT_ID;
import static tw.com.chainsea.ce.sdk.database.sp.TokenPref.PreferencesKey.CP_TRANS_TENANT_INFO;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.SocketEvent;
import tw.com.chainsea.ce.sdk.http.cp.CpApiManager;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.ce.sdk.http.cp.model.TransMember;
import tw.com.chainsea.ce.sdk.http.cp.model.TransTenantInfo;
import tw.com.chainsea.ce.sdk.http.cp.respone.TransTenantCreateResponse;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.socket.cp.model.TransTenantJoinContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.FragmentGroupCreateBinding;
import tw.com.chainsea.chat.qrcode.QrCodeKit;
import tw.com.chainsea.chat.ui.dialog.MessageDialogBuilder;
import tw.com.chainsea.chat.util.NameKit;
import tw.com.chainsea.chat.util.NoDoubleClickListener;
import tw.com.chainsea.chat.view.group.viewmodel.GroupViewModel;
import tw.com.chainsea.chat.zxing.encoding.EncodingHandler;

public class GroupCreateFragment extends Fragment {
    public static final String TAG = GroupCreateFragment.class.getSimpleName();
    private Context context;
    private FragmentGroupCreateBinding binding;
    private NavController navController;
    private GroupViewModel viewModel;
    private TransTenantInfo transTenantInfo;
    private int emptyReadyPosition;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_group_create, container, false);
        navController = NavHostFragment.findNavController(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);
        binding.btnClose.setOnClickListener(clickListener);
        binding.btnCancelCreate.setOnClickListener(clickListener);

//        CpSocket.getInstance().setOnNoticeListener(this);
        emptyReadyPosition = 1;
        //CP創建團隊回復
        transTenantInfo = TokenPref.getInstance(context).getCpTransTenantInfo();
        if (transTenantInfo != null && transTenantInfo.getTenantId() != null) {
            viewModel.tenantId = transTenantInfo.getTenantId();
            List<TransMember> members = transTenantInfo.getTransMembers();
            if (members != null) {
                for (TransMember member : transTenantInfo.getTransMembers()) {
                    addMember(emptyReadyPosition, member.getName());
                }
            } else {
                addMember(emptyReadyPosition, TokenPref.getInstance(context).getCpName());
            }
            setQrCode(TokenPref.getInstance(context).getCpTransTenantId(), TokenPref.getInstance(context).getCpName());
        }
        //CP創建團隊回復-end
        else {
            addMember(emptyReadyPosition, TokenPref.getInstance(context).getCpName());
            createTenantTrans();
        }
    }

    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
        @Override
        protected void onNoDoubleClick(View v) {
            if (v.equals(binding.btnClose)) {
                cancelTenantTrans();
            } else if (v.equals(binding.btnCancelCreate)) {
                cancelTenantTrans();
            }
        }
    };

    private void addMember(int position, String name) {
        NameKit nameKit = new NameKit();
        switch (position) {
            case 1: {
                binding.txtName1.setText(nameKit.getAvatarName(name));
                GradientDrawable gradientDrawable = (GradientDrawable) binding.txtName1.getBackground();
                gradientDrawable.setColor(Color.parseColor(nameKit.getBackgroundColor(name)));
                binding.txtWait1.setText(name);
                binding.imgWait1.setVisibility(View.INVISIBLE);
                binding.txtName1.setVisibility(View.VISIBLE);
                emptyReadyPosition = 2;
                break;
            }
            case 2: {
                binding.txtName2.setText(nameKit.getAvatarName(name));
                VectorDrawable gradientDrawable = (VectorDrawable) binding.txtName2.getBackground();
                gradientDrawable.setTint(Color.parseColor(nameKit.getBackgroundColor(name)));
                binding.txtWait2.setText(name);
                binding.imgWait2.setVisibility(View.INVISIBLE);
                binding.txtName2.setVisibility(View.VISIBLE);
                emptyReadyPosition = 3;
                break;
            }
            case 3: {
                binding.txtName3.setText(nameKit.getAvatarName(name));
                VectorDrawable gradientDrawable = (VectorDrawable) binding.txtName3.getBackground();
                gradientDrawable.setTint(Color.parseColor(nameKit.getBackgroundColor(name)));
                binding.txtWait3.setText(name);
                binding.imgWait3.setVisibility(View.INVISIBLE);
                binding.txtName3.setVisibility(View.VISIBLE);
                emptyReadyPosition = 4;

                navController.navigate(R.id.action_groupCreateFragment_to_groupCreateInfoFragment);
                break;
            }
        }
    }

    private void removeMember(int position) {
        switch (position) {
            case 2: {
                binding.txtName1.setText("");
                binding.imgWait1.setVisibility(View.VISIBLE);
                binding.txtName1.setVisibility(View.INVISIBLE);
                emptyReadyPosition--;
                break;
            }
            case 3: {
                binding.txtName2.setText("");
                binding.txtWait2.setText("等待中");
                binding.imgWait2.setVisibility(View.VISIBLE);
                binding.txtName2.setVisibility(View.INVISIBLE);
                emptyReadyPosition--;
                break;
            }
            case 4: {
                binding.txtName3.setText("");
                binding.txtWait3.setText("等待中");
                binding.imgWait3.setVisibility(View.VISIBLE);
                binding.txtName3.setVisibility(View.INVISIBLE);
                emptyReadyPosition--;
                break;
            }
        }
    }

    private void cancelTenantTrans() {
        new MessageDialogBuilder(context).setTitle("取消創建").setMessage("提醒您，團隊尚未創建完成，若您要取消創建，請按確定。")
            .setOnConfirmListener(message -> {
                dismissTenantTrans(TokenPref.getInstance(context).getCpTransTenantId());
            })
            .create()
            .show();
    }

    private void createTenantTrans() {
        CpApiManager.getInstance().createTenantTrans(context, new CpApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                Log.d("TAG", s);
                TransTenantCreateResponse response = JsonHelper.getInstance().from(s, TransTenantCreateResponse.class);
                if ("0000".equals(response.getStatus())) {
                    if (ErrCode.TENANT_ALREADY_READY.getValue().equals(response.getErrorCode())) { //已經創建該團隊
                        setQrCode(TokenPref.getInstance(context).getCpTransTenantId(), TokenPref.getInstance(context).getCpName());
                    } else {
                        viewModel.tenantId = response.getTenantId();
                        TokenPref.getInstance(context).setCpTransTenantId(response.getTenantId());
                        setQrCode(response.getTenantId(), TokenPref.getInstance(context).getCpName());
                    }
                }
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d("TAG", errorCode + errorMessage);
                if (ErrCode.TENANT_ALREADY_READY.getValue().equals(errorCode)) {
                    setQrCode(TokenPref.getInstance(context).getCpTransTenantId(), TokenPref.getInstance(context).getCpName());
                }
            }
        });
    }

    private void dismissTenantTrans(String tenantId) {
        CpApiManager.getInstance().dismissTenantTrans(context, tenantId, new CpApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                Log.d("TAG", s);
                TokenPref.getInstance(context).clearByKey(CP_TRANS_TENANT_INFO);
                TokenPref.getInstance(context).clearByKey(CP_TRANS_TENANT_ID);
                requireActivity().finish();
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d("TAG", errorCode + errorMessage);
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setQrCode(String transTenantId, String ownerName) {
        String qrcode = new QrCodeKit().getTransTenantQrCode(transTenantId, ownerName);
        try {
            Bitmap codeBitmap = EncodingHandler.createQRCode(qrcode, 600, 600, null);
            binding.layoutScan.setImageBitmap(codeBitmap);
        } catch (Exception ignored) {
            CELog.d("save qrCode id error");
        }
    }

    private void joinTenant(TransTenantJoinContent tenantJoinContent) {
        new MessageDialogBuilder(context).setTitle("加入團隊").setMessage(tenantJoinContent.getMemberName() + "想要加入您的團隊，若您確認請按同意。")
            .setConfirmText("同意")
            .setOnConfirmListener(message -> {
                agreeTenantTransMemberJoin(tenantJoinContent.getTenantId(), tenantJoinContent.getMemberId(), tenantJoinContent.getMemberName());
            })
            .setCancelText("不同意")
            .setOnCancelListener(message -> {
                rejectTenantTransMemberJoin(tenantJoinContent.getTenantId(), tenantJoinContent.getMemberId());
            })
            .create()
            .show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SocketEvent socketEvent) {
        switch (socketEvent.getType()) {
            case TransTenantMemberAdd:
            case TransTenantJoin:
                TransTenantJoinContent tenantJoinContent = JsonHelper.getInstance().from(socketEvent.getData(), TransTenantJoinContent.class);
                joinTenant(tenantJoinContent);
                break;
            case TransTenantExit:
                removeMember(emptyReadyPosition);
                break;
            case TransTenantDismiss:
                TransTenantJoinContent tenantInfo = (TransTenantJoinContent) socketEvent.getData();
                dismissTenantTrans(tenantInfo.getTenantId());
                break;
        }
    }

    private void agreeTenantTransMemberJoin(String tenantId, String memberId, String memberName) {
        CpApiManager.getInstance().agreeTenantTransMemberJoin(context, tenantId, memberId, new CpApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                Log.d("TAG", s);
                addMember(emptyReadyPosition, memberName);
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d("TAG", errorCode + errorMessage);
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void rejectTenantTransMemberJoin(String tenantId, String memberId) {
        CpApiManager.getInstance().rejectTenantTransMemberJoin(context, tenantId, memberId, new CpApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                Log.d("TAG", s);
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d("TAG", errorCode + errorMessage);
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
