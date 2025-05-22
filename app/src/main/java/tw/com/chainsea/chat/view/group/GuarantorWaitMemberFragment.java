package tw.com.chainsea.chat.view.group;

import static tw.com.chainsea.ce.sdk.database.sp.TokenPref.PreferencesKey.CP_TRANS_TENANT_INFO;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.config.CpConfig;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.SocketEvent;
import tw.com.chainsea.ce.sdk.http.cp.CpApiManager;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.ce.sdk.http.cp.model.TransMember;
import tw.com.chainsea.ce.sdk.http.cp.model.TransTenantInfo;
import tw.com.chainsea.ce.sdk.socket.cp.model.TransTenantJoinContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.FragmentGuarantorWaitMemberBinding;
import tw.com.chainsea.chat.qrcode.QrCodeKit;
import tw.com.chainsea.chat.ui.dialog.MessageDialogBuilder;
import tw.com.chainsea.chat.util.NameKit;
import tw.com.chainsea.chat.util.NoDoubleClickListener;
import tw.com.chainsea.chat.view.BackFragment;
import tw.com.chainsea.chat.zxing.encoding.EncodingHandler;

public class GuarantorWaitMemberFragment extends BackFragment {
    private Context context;
    private NavController navController;
    private FragmentGuarantorWaitMemberBinding binding;
    private int emptyReadyPosition = 1;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guarantor_wait_member, container, false);
        navController = NavHostFragment.findNavController(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnClose.setOnClickListener(clickListener);


        List<TransMember> transMemberArray = null;
        TransTenantInfo transTenantInfo = TokenPref.getInstance(context).getCpTransTenantInfo();
        if (transTenantInfo != null) {
            transMemberArray = transTenantInfo.getTransMembers();
            String Owner = "";
            if (transMemberArray != null) {
                Collections.sort(transMemberArray);
                for (TransMember member : transMemberArray) {
                    addMember(emptyReadyPosition, member.getName());
                    if (CpConfig.TRANS_MEMBER_STATUS.OWNER.equals(member.getStatus())) {
                        Owner = member.getName();
                    }
                }
            }
            String qrcode = new QrCodeKit().getTransTenantQrCode(TokenPref.getInstance(context).getCpTransTenantId(), Owner);
            setQrCode(qrcode);
        }
    }

    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
        @Override
        protected void onNoDoubleClick(View v) {
            if (v.equals(binding.btnClose)) {
                new MessageDialogBuilder(context).setTitle("退出團隊").setMessage("您確認要退出團隊？")
                    .setOnConfirmListener(message -> {
                        exitTenantTrans(TokenPref.getInstance(context).getCpTransTenantId());
                    })
                    .create()
                    .show();
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
                GradientDrawable gradientDrawable = (GradientDrawable) binding.txtName2.getBackground();
                gradientDrawable.setColor(Color.parseColor(nameKit.getBackgroundColor(name)));
                binding.txtWait2.setText(name);
                binding.imgWait2.setVisibility(View.INVISIBLE);
                binding.txtName2.setVisibility(View.VISIBLE);
                emptyReadyPosition = 3;
                break;
            }
        }
    }

    private void exitTenantTrans(String tenantId) {
        CpApiManager.getInstance().exitTenantTransMember(context, tenantId, new CpApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                Log.d("TAG", s);
                TokenPref.getInstance(context).clearByKey(CP_TRANS_TENANT_INFO);
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> Toast.makeText(context, "已成功退出團隊", Toast.LENGTH_SHORT).show());
                requireActivity().finish();
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d("TAG", errorCode + errorMessage);
                TokenPref.getInstance(context).clearByKey(CP_TRANS_TENANT_INFO);
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setQrCode(String url) {
        try {
            Bitmap codeBitmap = EncodingHandler.createQRCode(url, 600, 600, null);
            binding.layoutScan.setImageBitmap(codeBitmap);
        } catch (Exception ignored) {
            CELog.d("save qrCode id error");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SocketEvent socketEvent) {
        switch (socketEvent.getType()) {
            case TransTenantJoin:
            case TransTenantMemberAdd:
                TransTenantJoinContent content = (TransTenantJoinContent) socketEvent.getData();
                addMember(emptyReadyPosition, content.getMemberName());
                break;
        }
    }
}
