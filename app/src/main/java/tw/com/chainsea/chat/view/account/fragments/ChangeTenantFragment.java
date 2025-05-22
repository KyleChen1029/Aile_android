package tw.com.chainsea.chat.view.account.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ActivityChangeTenantBinding;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.ui.fragment.BaseFragment;
import tw.com.chainsea.chat.view.account.UserInformationHomepageActivity;
import tw.com.chainsea.chat.view.group.GroupCreateActivity;
import tw.com.chainsea.custom.view.alert.AlertView;

@Deprecated
public class ChangeTenantFragment extends BaseFragment<UserInformationHomepageActivity.TabType, String> {
    private ActivityChangeTenantBinding binding;
    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_change_tenant, container, false);
//        ViewGroup.LayoutParams params = binding.viewSpace.getLayoutParams();
//        params.height = getStatusHeight();
//        binding.viewSpace.setLayoutParams(params);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateTenantRelationList();
        initListener();
    }

    private void initListener() {
        binding.ivAddTeam.setOnClickListener(this::tenantAction);
    }

    private void updateTenantRelationList() {
//        binding.layoutOthers.removeAllViews();

//        LayoutInflater inflater = LayoutInflater.from(context);
//        List<RelationTenant> relationTenants = TokenPref.getInstance(context).getCpRelationTenantList();
//        for (RelationTenant tenant : relationTenants) {
//            View item = inflater.inflate(R.layout.item_tenant, binding.layoutOthers, false);
//            ((TextView) item.findViewById(R.id.txt_name)).setText(tenant.getTenantName());
//            ((TextView) item.findViewById(R.id.txt_name)).setTextColor(Color.BLACK);
//            avatarKit.loadCpTenantAvatar(tenant.getAvatarId(), item.findViewById(R.id.img));
//            if (currentTenant.getTenantCode().equals(tenant.getTenantCode())) {
//                item.findViewById(R.id.cl_root).setBackgroundColor(Color.parseColor("#f0faff"));
//                item.findViewById(R.id.iv_check).setVisibility(View.VISIBLE);
//            }
//            item.setOnClickListener(v -> {
//                if (!currentTenant.getTenantCode().equals(tenant.getTenantCode())) {
//                    new AlertView.Builder()
//                            .setContext(requireContext())
//                            .setStyle(AlertView.Style.Alert)
//                            .setTitle("切換團隊")
//                            .setMessage("你確認要切換到" + tenant.getTenantName() + "嗎？")
//                            .setOthers(new String[]{"取消", "確定"})
//                            .setOnItemClickListener((o, position) -> {
//                                if (position == 1) {
//                                    SystemKit.changeTenant(requireActivity(), tenant, false);
//                                }
//                            })
//                            .build()
//                            .setCancelable(true)
//                            .show();
//                }
//            });

//            binding.layoutOthers.addView(item);
//        }
    }

    @Override
    public UserInformationHomepageActivity.TabType getType() {
        return UserInformationHomepageActivity.TabType.CHANGE_TENANT;
    }

    @Override
    public void refresh() {
        super.refresh();
    }

    private void tenantAction(View v) {
        new AlertView.Builder()
                .setContext(requireContext())
                .setStyle(AlertView.Style.ActionSheet)
                .setOthers(new String[]{getString(R.string.join_group), getString(R.string.create_group)})
                .setCancelText(getString(R.string.alert_cancel))
                .setOnItemClickListener((o, position) -> {
                    if (position == 0) {
                        ActivityTransitionsControl.navigateScannerJoinGroup(context);
                    } else if (position == 1) {
                        startActivity(new Intent(context, GroupCreateActivity.class));
                    }
                })
                .build()
                .setOnDismissListener(o -> {
                })
                .setCancelable(true)
                .show();
    }
}
