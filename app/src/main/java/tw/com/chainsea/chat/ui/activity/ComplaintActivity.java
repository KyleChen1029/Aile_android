package tw.com.chainsea.chat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;

import androidx.annotation.NonNull;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.databinding.ActivityComplaintBinding;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.presenter.ComplaintPresenter;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.ui.ife.IComplaintView;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.custom.view.alert.AlertView;

public class ComplaintActivity extends ParentActivity implements IComplaintView {
    private ActivityComplaintBinding binding;
    private String mGroupType = "";
    private String mOutput = "";
    private String mObjectUserId = "";
    private int selectedId = -1;
    private ComplaintPresenter complaintPresenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.INSTANCE.setTheme(ComplaintActivity.this);
        super.onCreate(savedInstanceState);
        binding = (ActivityComplaintBinding) viewDataBinding;
        binding.btnAgreeSend.setClickable(false);
        Intent intent = getIntent();
        mGroupType = intent.getStringExtra(Constant.SESSION_TYPE);
        mObjectUserId = intent.getStringExtra(Constant.OBJECT_USERID);
        complaintPresenter = new ComplaintPresenter(this, mObjectUserId);
        binding.rgComplaint.setOnCheckedChangeListener((group, checkedId) -> {
            // 获取选中的RadioButton的id
            selectedId = group.getCheckedRadioButtonId();
            // 通过id实例化选中的这个RadioButton
            RadioButton choice = findViewById(selectedId);
            // 获取这个RadioButton的text内容
            mOutput = choice.getText().toString();
        });
        initListener();
    }

    @Override
    protected int createView() {
        return R.layout.activity_complaint;
    }

    @Override
    protected void findView() {

    }

    private void initListener() {
        binding.back.setOnClickListener(v -> finish());
        binding.btnAgreeSend.setOnClickListener(v -> {
            if(selectedId > -1 && !mOutput.isEmpty()) {
                if (selectedId == R.id.rb1 ||
                        selectedId == R.id.rb2 ||
                        selectedId == R.id.rb3 ||
                        selectedId == R.id.rb4 ||
                        selectedId == R.id.rb5)
                    complaintToPlatform();
                else if (selectedId == R.id.rb6 ||
                        selectedId == R.id.rb7)
                    complaintToTeam(selectedId);
            } else
                ToastUtils.showToast(this, getString(R.string.text_choose_report_type));
        });
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    private void complaintToTeam(int id) {
        String reportMsg = "";
        showLoadingView(R.string.wording_loading);
        if(id == R.id.rb6)
            reportMsg = getString(R.string.text_report_not_team_member);
        else if(id == R.id.rb7)
            reportMsg = getString(R.string.text_report_quit_member);
        complaintPresenter.complaintToTeam(reportMsg);
    }

    private void complaintToPlatform() {
        showLoadingView(R.string.wording_loading);
        complaintPresenter.complaintToPlatform(this, mObjectUserId, mGroupType, mOutput);
    }

    @Override
    public void dismissLoadingView() {
        hideLoadingView();
    }

    @Override
    public void alertBlockDialog() {
        new AlertView.Builder()
                .setContext(this)
                .setStyle(AlertView.Style.Alert)
                .setMessage(getString(R.string.text_report_message_successful))
                .setOthers(new String[]{getString(R.string.picture_cancel), getString(R.string.text_block)})
                .setOnItemClickListener((o, position) -> {
                    if (position == 1) {
                        complaintPresenter.doBlock(ComplaintActivity.this);
                    }else
                        finish();
                })
                .build()
                .setCancelable(true)
                .show();
    }

    @Override
    public void showToast(@NonNull String msg) {
        ToastUtils.showToast(this, msg);
    }

    @Override
    public void showTipToast(boolean isBlock) {
        ToastUtils.showToast(ComplaintActivity.this, isBlock ? getString(R.string.text_block_success) : getString(R.string.text_unblock_success));
        finish();
    }

    @Override
    public void alertNavigateToChatRoom(@NonNull String roomId) {
        new AlertView.Builder()
                .setContext(this)
                .setStyle(AlertView.Style.Alert)
                .setMessage(getString(R.string.text_report_success_redirect_chat_room))
                .setOthers(new String[]{getString(R.string.text_report_go_and_check_it)})
                .setOnItemClickListener((o, position) -> {
                    if (position == 0) {
                        ActivityTransitionsControl.navigateToChat(this, roomId, (intent, s) -> IntentUtil.INSTANCE.start(this, intent));
                        finish();
                    }
                })
                .build()
                .setCancelable(true)
                .show();
    }
}
