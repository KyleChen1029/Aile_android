package tw.com.chainsea.chat.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import tw.com.chainsea.chat.R;

/**
 * Created by sunhui on 2018/3/7.
 */

public class EditDialog extends Dialog implements View.OnClickListener {

    private TextView mTitle;
    private EditText mEdit;
    private TextView mCancel;
    private TextView mConfirm;

    private OnConfirmListenner mConfirmListenner;

    public EditDialog(Context context) {
        this(context, R.style.ios_bottom_dialog);
    }

    public EditDialog(Context context, int themeResId) {
        super(context, themeResId);
        setContentView(R.layout.edit_dialog_layout);
        initView();
    }

    private void initView() {
        mTitle = (TextView) findViewById(R.id.tv_title);
        mCancel = (TextView) findViewById(R.id.btn_cancel);
        mConfirm = (TextView) findViewById(R.id.btn_yes);
        mEdit = (EditText) findViewById(R.id.et_layout);
        mConfirm.setOnClickListener(this);
        mCancel.setOnClickListener(this);

        /*mEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return event.getAction() == KeyEvent.KEYCODE_ENTER;
            }
        });*/

        //点击空白区域可以取消dialog
//        this.setCanceledOnTouchOutside(true);
        // 点击back键可以取消dialog
        this.setCancelable(true);
        Window window = this.getWindow();
        //让Dialog顯示在屏幕的底部
        window.setGravity(Gravity.CENTER);
        //设置窗口出现和窗口隐藏的动画
        window.setWindowAnimations(R.style.fade_dialog_anim);
        //设置BottomDialog的宽高属性
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                dismiss();
                break;
            case R.id.btn_yes:
                String text = mEdit.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    mConfirmListenner.onConfirm(text);
//                    if (text.length() <= 8) {
//                        dismiss();
//                    }
                } else {
                    Toast.makeText(getContext(), "您還沒有輸入內容", Toast.LENGTH_SHORT).show();
//                    ToastUtils.showToast(getContext(), "您還沒有輸入內容");
                }
                break;
        }
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public void setEditHint(String hint) {
        mEdit.setHint(hint);
    }

    public void setContent(String content) {
        mEdit.setText(content);
        mEdit.setSelection(mEdit.getText().length());
    }

    public void setEditInputType(int type) {
        mEdit.setInputType(type);
    }

    public void setConfirmListenner(OnConfirmListenner confirmListenner) {
        mConfirmListenner = confirmListenner;
    }

    public interface OnConfirmListenner {
        void onConfirm(String text);
    }

}
