package tw.com.chainsea.custom.view.alert;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.google.common.collect.Lists;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tw.com.chainsea.custom.view.R;
import tw.com.chainsea.custom.view.recyclerview.ConstraintHeightListView;

/**
 * Created by Sai on 15/8/9.
 * 精仿iOSAlertViewController控件
 * 点击取消按钮返回 －1，其他按钮从0开始算
 */
public class AlertView {
    public enum Style {
        ActionSheet,
        Alert,
        MultiAlert,
        EditAlert,
        ImageTextAlert,
//        BusinessAlert,
    }

    private final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM
    );

    public static final int HORIZONTAL_BUTTONS_MAXCOUNT = 2;
    public static final int CANCELPOSITION = -1; // Click the cancel button to return to -1, other buttons start counting from 0

    private String title;
    private String msg;
    private String[] inputDatas;
    private List<EditText> editTexts = Lists.<EditText>newArrayList();
    private View leftView; // Add image to the left
    private OnLeftViewClickListener leftViewClickListener;
    private @ColorInt
    int otherTextColor = 0xFF007AFF;
    private @ColorInt
    int destructiveTextColor = 0xFFFF3b30;
    private int maxHeight = -1;
    private List<String> mDestructive;
    private List<String> mInputDatas;
    private boolean isHints;
    private String cancel;
    private @ColorInt
    int cancelTextColor = 0xFF007AFF;
    private ArrayList<String> mDatas = Lists.newArrayList();

    private WeakReference<Context> contextWeak;
    private ViewGroup contentContainer;
    private ViewGroup decorView; // Root View of activity
    private ViewGroup rootView; //  Root View of AlertView
    private ViewGroup loAlertHeader; // Window header View

    private Style style = Style.Alert;

    private OnDismissListener onDismissListener;
    private OnItemClickListener onItemClickListener;
    private boolean isShowing;

    private List<ImageText> imageTexts;
    private boolean isShowImage;

    private Animation outAnim;
    private Animation inAnim;
    private int gravity = Gravity.CENTER;


    // Customize Data structure
    private Class zlass;
    private String customizeJson;
    private float textSize = 0f;

    public AlertView(Builder builder) {
        this.contextWeak = new WeakReference<>(builder.context);
        this.style = builder.style;
        this.title = builder.title;
        this.msg = builder.msg;
        this.cancel = builder.cancel;
        this.cancelTextColor = builder.cancelTextColor;
        String[] destructive = builder.destructive;
        String[] others = builder.others;
        this.otherTextColor = builder.otherTextColor;
        this.destructiveTextColor = builder.destructiveTextColor;
        this.maxHeight = builder.maxHeight;
        this.onItemClickListener = builder.onItemClickListener;
        this.inputDatas = builder.inputDatas;
        this.isHints = builder.isHints;
        this.leftView = builder.leftView;
        this.leftViewClickListener = builder.leftViewClickListener;
        this.imageTexts = builder.imageTexts;
        this.isShowImage = builder.isShowImage;

        this.zlass = builder.zlass;
        this.customizeJson = builder.customizeJson;
        this.textSize = builder.textSize;
        initData(title, msg, cancel, destructive, others, inputDatas, imageTexts);
        initViews();
        init();
        initEvents();
    }

    public AlertView(String title, String msg, String cancel, String[] destructive, String[] others, String[] inputHints, List<ImageText> imageTexts, Context context, Style style, OnItemClickListener onItemClickListener) {
        this.contextWeak = new WeakReference<>(context);
        if (style != null) {
            this.style = style;
        }
        this.onItemClickListener = onItemClickListener;

        initData(title, msg, cancel, destructive, others, inputHints, imageTexts);
        initViews();
        init();
        initEvents();
    }

    /**
     * retrieve data
     */
    protected void initData(String title, String msg, String cancel, String[] destructive, String[] others, String[] inputDatas, List<ImageText> imageTexts) {
        this.title = title;
        this.msg = msg;
        if (others != null) {
            List<String> mOthers = Arrays.asList(others);
            this.mDatas.addAll(mOthers);
        }
        if (destructive != null) {
            this.mDestructive = Arrays.asList(destructive);
            this.mDatas.addAll(mDestructive);
        }
        if (style == Style.EditAlert) {
            cancel = contextWeak.get().getString(R.string.alert_cancel);
            this.mDestructive = List.of(contextWeak.get().getString(R.string.alert_confirm));
            this.mDatas.addAll(this.mDestructive);
            if (inputDatas == null) {
                this.inputDatas = new String[]{};
            } else {
                this.inputDatas = inputDatas;
            }
            this.mInputDatas = Arrays.asList(this.inputDatas);
        }
        if (cancel != null) {
            this.cancel = cancel;
            if ((style == Style.Alert || style == Style.EditAlert) && mDatas.size() < HORIZONTAL_BUTTONS_MAXCOUNT) {
                this.mDatas.add(0, cancel);
            }
        }
    }

    protected void initViews() {
        Context context = contextWeak.get();
        if (context == null) {
            return;
        }


        LayoutInflater layoutInflater = LayoutInflater.from(context);
        decorView = (ViewGroup) ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        rootView = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview, decorView, false);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));
        contentContainer = (ViewGroup) rootView.findViewById(R.id.content_container);
        int margin_alert_left_right = 0;
        switch (style) {
            case ActionSheet:
                params.gravity = Gravity.BOTTOM;
                margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_actionsheet_left_right);
                params.setMargins(margin_alert_left_right, 0, margin_alert_left_right, margin_alert_left_right);
                contentContainer.setLayoutParams(params);
                gravity = Gravity.BOTTOM;
                initActionSheetViews(layoutInflater);
                break;
            case Alert:
                params.gravity = Gravity.CENTER;
                margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_alert_left_right);
                params.setMargins(margin_alert_left_right, 0, margin_alert_left_right, 0);
                contentContainer.setLayoutParams(params);
                gravity = Gravity.CENTER;
                initAlertViews(layoutInflater);
                break;
            case MultiAlert:
                params.gravity = Gravity.CENTER;
                margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_alert_left_right);
                params.setMargins(margin_alert_left_right, 0, margin_alert_left_right, 0);
                contentContainer.setLayoutParams(params);
                gravity = Gravity.CENTER;
                initActionSheetViews(layoutInflater);
//                initMultiAlertViews(layoutInflater);
                break;
            case EditAlert:
                params.gravity = Gravity.CENTER;
                margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_alert_left_right);
                params.setMargins(margin_alert_left_right, 0, margin_alert_left_right, 0);
                contentContainer.setLayoutParams(params);
                gravity = Gravity.CENTER;
                initEditAlertViews(layoutInflater);
                break;
            case ImageTextAlert:
                params.gravity = Gravity.CENTER;
                margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_alert_left_right);
                params.setMargins(margin_alert_left_right, 0, margin_alert_left_right, 0);
                contentContainer.setLayoutParams(params);
                gravity = Gravity.CENTER;
                initImageTextsAlertViews(layoutInflater);
                break;
//            case BusinessAlert:
//                params.gravity = Gravity.CENTER;
//                margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_alert_left_right);
//                params.setMargins(margin_alert_left_right, 0, margin_alert_left_right, 0);
//                contentContainer.setLayoutParams(params);
//                gravity = Gravity.CENTER;
//                initBusinessAlertViews(layoutInflater);
//                break;
        }
    }

    protected void initHeaderView(ViewGroup viewGroup) {
        loAlertHeader = (ViewGroup) viewGroup.findViewById(R.id.loAlertHeader);
        // Title and message
        TextView tvAlertTitle = (TextView) viewGroup.findViewById(R.id.tvAlertTitle);
        TextView tvAlertMsg = (TextView) viewGroup.findViewById(R.id.tvAlertMsg);
        if (title != null && !title.isEmpty()) {
            tvAlertTitle.setText(title);
        } else {
            tvAlertTitle.setVisibility(View.GONE);
        }

        if (tvAlertTitle.getVisibility() == View.VISIBLE) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tvAlertMsg.getLayoutParams();
            params.setMargins(30, 0, 30, 25);
            tvAlertMsg.setLayoutParams(params);
        }

        if (msg != null && !msg.isEmpty()) {
            tvAlertMsg.setText(msg);
        } else {
            tvAlertMsg.setVisibility(View.GONE);
        }

        if (textSize > 0f) {
            tvAlertTitle.setTextSize(textSize);
        }
    }

    @SuppressLint("InflateParams")
    protected void initListView() {
        Context context = contextWeak.get();
        if (context == null) return;

        ConstraintHeightListView alertButtonListView = contentContainer.findViewById(R.id.alertButtonListView);
        alertButtonListView.setMaxHeight(this.maxHeight);

        // Use cancel as footer View
        if (cancel != null && style == Style.Alert) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_alertbutton, null);
            TextView tvAlert = (TextView) itemView.findViewById(R.id.tvAlert);
            tvAlert.setText(cancel);
            tvAlert.setClickable(true);
            tvAlert.setTypeface(Typeface.DEFAULT_BOLD);
            tvAlert.setTextColor(ContextCompat.getColor(context.getApplicationContext(), R.color.textColor_alert_button_cancel));
            tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_bottom);
            tvAlert.setOnClickListener(new OnTextClickListener(CANCELPOSITION));
            alertButtonListView.addFooterView(itemView);
        }
        AlertViewAdapter adapter = new AlertViewAdapter(mDatas, mDestructive)
            .setDestructiveTextColor(destructiveTextColor)
            .setTextColor(otherTextColor);
        alertButtonListView.setAdapter(adapter);
        alertButtonListView.setOnItemClickListener((adapterView, view, position, l) -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(AlertView.this, position);
            }
            dismiss();
        });
    }

    /**
     * Bottom ActionSheet fragment style
     *
     * @param layoutInflater
     */
    protected void initActionSheetViews(LayoutInflater layoutInflater) {
        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview_actionsheet, contentContainer);
        initHeaderView(viewGroup);
        initListView();
        TextView tvAlertCancel = (TextView) contentContainer.findViewById(R.id.tvAlertCancel);
        if (cancel != null) {
            tvAlertCancel.setVisibility(View.VISIBLE);
            tvAlertCancel.setText(cancel);
            tvAlertCancel.setTextColor(cancelTextColor);
        }
        tvAlertCancel.setOnClickListener(new OnTextClickListener(CANCELPOSITION));
    }

    /**
     * Center can enter Alert style
     */
    @SuppressLint("InflateParams")
    protected void initEditAlertViews(LayoutInflater layoutInflater) {
        Context context = contextWeak.get();
        if (context == null) {
            return;
        }

        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview_edit_alert, contentContainer);
        initHeaderView(viewGroup);
        int position = 0;
        ViewStub viewStub = (ViewStub) contentContainer.findViewById(R.id.viewStubHorizontal);
        viewStub.inflate();
        LinearLayout loAlertButtons = (LinearLayout) contentContainer.findViewById(R.id.loAlertButtons);
        LinearLayout leftContainerLayout = (LinearLayout) contentContainer.findViewById(R.id.leftContainerLayout);

        if (this.leftView != null) {
            leftContainerLayout.removeAllViews();
            leftContainerLayout.addView(this.leftView);
            leftContainerLayout.setOnClickListener(v -> {
                if (leftViewClickListener != null) {
                    leftViewClickListener.onLeftClick(AlertView.this, leftView);
                }
            });
        } else {
            leftContainerLayout.setVisibility(View.GONE);
        }

        editTexts = Lists.<EditText>newArrayList();

        for (int i = 0; i < mDatas.size(); i++) {
            // If not the first button
            if (i != 0) {
                // Add a dividing line between the buttons
                View divier = new View(context);
                divier.setBackgroundColor(ContextCompat.getColor(context.getApplicationContext(),
                    R.color.bgColor_divier));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) context.getResources().getDimension(R.dimen.size_divier), LinearLayout.LayoutParams.MATCH_PARENT);
                loAlertButtons.addView(divier, params);
            }
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_alertbutton, null);
            TextView tvAlert = itemView.findViewById(R.id.tvAlert);
            tvAlert.setClickable(true);

            // Set click effect
            if (mDatas.size() == 1) {
                tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_bottom);
            } else if (i == 0) { // Set the effect of the leftmost button
                tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_left);
            } else if (i == mDatas.size() - 1) { // Set the effect of the rightmost button
                tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_right);
            }
            String data = mDatas.get(i);
            tvAlert.setText(data);

            // Cancel button style
            if (data == cancel) {
                tvAlert.setTypeface(Typeface.DEFAULT_BOLD);
                tvAlert.setTextColor(ContextCompat.getColor(context.getApplicationContext(),
                    R.color.textColor_alert_button_cancel));
                tvAlert.setOnClickListener(new OnEditClickListener(editTexts, CANCELPOSITION));
                position = position - 1;
            }
            // Highlight button style
            else if (mDestructive != null && mDestructive.contains(data)) {
                tvAlert.setTextColor(ContextCompat.getColor(context.getApplicationContext(),
                    R.color.textColor_alert_button_destructive));
            }

            tvAlert.setOnClickListener(new OnEditClickListener(editTexts, position));
            position++;
            loAlertButtons.addView(itemView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        }

        LinearLayout editBodyLayout = contentContainer.findViewById(R.id.editBodyLayout);
        editBodyLayout.removeAllViews();


        for (String input : this.mInputDatas) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_alertinput, null);
            EditText etAlert = (EditText) itemView.findViewById(R.id.etAlert);
            etAlert.setTag(input);
            if (this.isHints) {
                etAlert.setHint(input);
            } else {
                etAlert.setText(input);
            }
            editTexts.add(etAlert);
            editBodyLayout.addView(itemView);
        }
    }

    public List<EditText> getInputs() {
        return this.editTexts;
    }

    public String getInputByPosition(int position) {
        try {
            return this.editTexts.get(position).getText().toString();
        } catch (Exception e) {
            return "";
        }
    }

//    protected void initMultiAlertViews(LayoutInflater layoutInflater) {
//        Context context = contextWeak.get();
//        if (context == null) {
//            return;
//        }
//
//        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview_alert, contentContainer);
//        initHeaderView(viewGroup);
//
//        int position = 0;
//        // If the total data is less than or equal to HORIZONTAL_BUTTONS_MAXCOUNT， else Button
//        if (mDatas.size() <= HORIZONTAL_BUTTONS_MAXCOUNT) {
//            ViewStub viewStub = (ViewStub) contentContainer.findViewById(R.id.viewStubHorizontal);
//            viewStub.inflate();
//            LinearLayout loAlertButtons = (LinearLayout) contentContainer.findViewById(R.id.loAlertButtons);
//            for (int i = 0; i < mDatas.size(); i++) {
//                // If not the first button
//                if (i != 0) {
//                    // Add a dividing line between the buttons
//                    View divier = new View(context);
//                    divier.setBackgroundColor(ContextCompat.getColor(context.getApplicationContext(), 
//    R.color.bgColor_divier));
//                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) context.getResources().getDimension(R.dimen.size_divier), LinearLayout.LayoutParams.MATCH_PARENT);
//                    loAlertButtons.addView(divier, params);
//                }
//                View itemView = LayoutInflater.from(context).inflate(R.layout.item_alertbutton, null);
//                TextView tvAlert = (TextView) itemView.findViewById(R.id.tvAlert);
//                tvAlert.setClickable(true);
//
//                // Set click effect
//                if (mDatas.size() == 1) {
//                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_bottom);
//                } else if (i == 0) { // Set the effect of the leftmost button
//                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_left);
//                } else if (i == mDatas.size() - 1) { // Set the effect of the rightmost button
//                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_right);
//                }
//                String data = mDatas.get(i);
//                tvAlert.setText(data);
//
//                // Cancel button style
//                if (data == cancel) {
//                    tvAlert.setTypeface(Typeface.DEFAULT_BOLD);
//                    tvAlert.setTextColor(ContextCompat.getColor(context.getApplicationContext(), 
//    R.color.textColor_alert_button_cancel));
//                    tvAlert.setOnClickListener(new OnTextClickListener(CANCELPOSITION));
//                    position = position - 1;
//                }
//                // Highlight button style
//                else if (mDestructive != null && mDestructive.contains(data)) {
//                    tvAlert.setTextColor(ContextCompat.getColor(context.getApplicationContext(), 
//    R.color.textColor_alert_button_destructive));
//                }
//
//                tvAlert.setOnClickListener(new OnTextClickListener(position));
//                position++;
//                loAlertButtons.addView(itemView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
//            }
//        } else {
//            ViewStub viewStub = (ViewStub) contentContainer.findViewById(R.id.viewStubVertical);
//            viewStub.inflate();
//            initListView();
//        }
//    }

    /**
     * Center Alert style
     */
    @SuppressLint("InflateParams")
    protected void initAlertViews(LayoutInflater layoutInflater) {
        Context context = contextWeak.get();
        if (context == null) {
            return;
        }

        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview_alert, contentContainer);
        initHeaderView(viewGroup);

        int position = 0;
        // If the total data is less than or equal to HORIZONTAL_BUTTONS_MAXCOUNT， else Button
        if (mDatas.size() <= HORIZONTAL_BUTTONS_MAXCOUNT) {
            ViewStub viewStub = (ViewStub) contentContainer.findViewById(R.id.viewStubHorizontal);
            viewStub.inflate();
            LinearLayout loAlertButtons = (LinearLayout) contentContainer.findViewById(R.id.loAlertButtons);
            for (int i = 0; i < mDatas.size(); i++) {
                // If not the first button
                if (i != 0) {
                    // Add a dividing line between the buttons
                    View divier = new View(context);
                    divier.setBackgroundColor(ContextCompat.getColor(context.getApplicationContext(),
                        R.color.bgColor_divier));
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) context.getResources().getDimension(R.dimen.size_divier), LinearLayout.LayoutParams.MATCH_PARENT);
                    loAlertButtons.addView(divier, params);
                }
                View itemView = LayoutInflater.from(context).inflate(R.layout.item_alertbutton, null);
                TextView tvAlert = (TextView) itemView.findViewById(R.id.tvAlert);
                tvAlert.setClickable(true);

                // Set click effect
                if (mDatas.size() == 1) {
                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_bottom);
                } else if (i == 0) { // Set the effect of the leftmost button
                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_left);
                } else if (i == mDatas.size() - 1) { // Set the effect of the rightmost button
                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_right);
                }
                String data = mDatas.get(i);
                tvAlert.setText(data);

                // Cancel button style
                if (data == cancel) {
                    tvAlert.setTypeface(Typeface.DEFAULT_BOLD);
                    tvAlert.setTextColor(ContextCompat.getColor(context.getApplicationContext(),
                        R.color.textColor_alert_button_cancel));
                    tvAlert.setOnClickListener(new OnTextClickListener(CANCELPOSITION));
                    position = position - 1;
                }
                // Highlight button style
                else if (mDestructive != null && mDestructive.contains(data)) {
                    tvAlert.setTextColor(ContextCompat.getColor(context.getApplicationContext(),
                        R.color.textColor_alert_button_destructive));
                }

                tvAlert.setOnClickListener(new OnTextClickListener(position));
                position++;
                loAlertButtons.addView(itemView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            }
        } else {
            ViewStub viewStub = (ViewStub) contentContainer.findViewById(R.id.viewStubVertical);
            viewStub.inflate();
            initListView();
        }
    }

    /**
     * Center ImageTexts Alert style
     */
    @SuppressLint("InflateParams")
    protected void initImageTextsAlertViews(LayoutInflater layoutInflater) {
        Context context = contextWeak.get();
        if (context == null) return;
        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview_alert, contentContainer);
        initHeaderView(viewGroup);
        ViewStub viewStub = contentContainer.findViewById(R.id.viewStubVertical);
        viewStub.inflate();
        ConstraintHeightListView alertButtonListView = contentContainer.findViewById(R.id.alertButtonListView);
        alertButtonListView.setMaxHeight(this.maxHeight);
        // Use cancel as footerView
        if (cancel != null && style == Style.ImageTextAlert) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_alertbutton, null);
            TextView tvAlert = (TextView) itemView.findViewById(R.id.tvAlert);
            tvAlert.setText(cancel);
            tvAlert.setClickable(true);
            tvAlert.setTypeface(Typeface.DEFAULT_BOLD);
            tvAlert.setTextColor(ContextCompat.getColor(context.getApplicationContext(),
                R.color.textColor_alert_button_cancel));
            tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_bottom);
            tvAlert.setOnClickListener(new OnTextClickListener(CANCELPOSITION));
            alertButtonListView.addFooterView(itemView);
        }
        ImageTextAlertViewAdapter adapter = new ImageTextAlertViewAdapter(this.imageTexts, isShowImage);
        alertButtonListView.setAdapter(adapter);
        alertButtonListView.setOnItemClickListener((adapterView, view, position, l) -> {
            if (onItemClickListener != null) onItemClickListener.onItemClick(AlertView.this, position);
            dismiss();
        });
    }

    protected void init() {
        inAnim = getInAnimation();
        outAnim = getOutAnimation();
    }

    protected void initEvents() {
        //nothing to do
    }

    public AlertView addExtView(View extView) {
        loAlertHeader.addView(extView);
        return this;
    }

    /**
     * show When called
     *
     * @param view 这个View
     */
    private void onAttached(View view) {
        isShowing = true;
        decorView.addView(view);
        contentContainer.startAnimation(inAnim);
    }

    /**
     * Add this View to the root view of the activity
     */
    public void show() {
        if (isShowing()) {
            return;
        }
//        new Handler().postDelayed(() ->, 500l);
        onAttached(rootView);

    }

    /**
     * Check whether the View has been added to the root view
     *
     * @return If the view already exists, the View returns true
     */
    public boolean isShowing() {
        return rootView.getParent() != null && isShowing;
    }

    public void dismiss() {
        // Disappear animation
        outAnim.setAnimationListener(outAnimListener);
        contentContainer.startAnimation(outAnim);
    }

    public void dismissImmediately() {
        decorView.removeView(rootView);
        isShowing = false;
        if (onDismissListener != null) {
            onDismissListener.onDismiss(this);
        }
    }

    public Animation getInAnimation() {
        Context context = contextWeak.get();
        if (context == null) {
            return null;
        }

        int res = AlertAnimateUtil.getAnimationResource(this.gravity, true);
        return AnimationUtils.loadAnimation(context, res);
    }

    public Animation getOutAnimation() {
        Context context = contextWeak.get();
        if (context == null) {
            return null;
        }

        int res = AlertAnimateUtil.getAnimationResource(this.gravity, false);
        return AnimationUtils.loadAnimation(context, res);
    }

    public AlertView setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
        return this;
    }

    class OnEditClickListener implements View.OnClickListener {
        private List<EditText> editTexts;
        private int position;

        public OnEditClickListener(List<EditText> editTexts, int position) {
            this.editTexts = editTexts;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            try {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            } catch (Exception e) {

            }
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(this.editTexts, position);
            }
            dismiss();
        }
    }

    class OnTextClickListener implements View.OnClickListener {

        private int position;

        public OnTextClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(AlertView.this, position);
            }
            dismiss();
        }
    }

    private Animation.AnimationListener outAnimListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            new Handler().post(() -> {
                dismissImmediately();
            });
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    /**
     * It is mainly used to expand the View when there is an input box. When the keyboard pops up, set Margin Bottom up to avoid the input method blocking the interface
     */
    public void setMarginBottom(int marginBottom) {
        Context context = contextWeak.get();
        if (context == null) {
            return;
        }

        int margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_alert_left_right);
        params.setMargins(margin_alert_left_right, 0, margin_alert_left_right, marginBottom);
        contentContainer.setLayoutParams(params);
    }

    public AlertView setCancelable(boolean isCancelable) {
        View view = rootView.findViewById(R.id.outmost_container);

        if (isCancelable) {
            view.setOnTouchListener(onCancelableTouchListener);
        } else {
            view.setOnTouchListener(null);
        }
        return this;
    }

    /**
     * Called when the user touch on black overlay in order to dismiss the dialog
     */
    private final View.OnTouchListener onCancelableTouchListener = (v, event) -> {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            dismiss();
        }
        v.performClick();
        return false;
    };

    /**
     * Builder for arguments
     */
    public static class Builder {
        private Context context;
        private Style style;
        private String title;
        private String msg;
        private String cancel;
        private @ColorInt
        int cancelTextColor = 0xFF007AFF;
        private String[] inputDatas;
        private View leftView;
        private OnLeftViewClickListener leftViewClickListener;
        private boolean isHints;
        private String[] destructive;
        private String[] others;
        private @ColorInt
        int otherTextColor = 0xFF007AFF;
        private @ColorInt
        int destructiveTextColor = 0xFFFF3b30;
        private int maxHeight = -1;
        private OnItemClickListener onItemClickListener;

        private List<ImageText> imageTexts;
        private boolean isShowImage;

        // Customize Data structure
        private Class zlass;
        private String customizeJson;
        private float textSize = 0f;

        public Builder setTitleSize(float size) {
            this.textSize = size;
            return this;
        }

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setStyle(Style style) {
            if (style != null) {
                this.style = style;
            }
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder setCancelText(String cancel) {
            this.cancel = cancel;
            return this;
        }

        public Builder setCancelTextColor(@ColorInt int color) {
            this.cancelTextColor = color;
            return this;
        }

        public Builder setDestructive(String... destructive) {
            this.destructive = destructive;
            return this;
        }

        public Builder setOthers(String[] others) {
            this.others = others;
            return this;
        }

        public Builder setOtherTextColor(@ColorInt int color) {
            this.otherTextColor = color;
            return this;
        }


        public Builder setDestructiveTextColor(@ColorInt int color) {
            this.destructiveTextColor = color;
            return this;
        }

        public Builder setMaxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }

        public Builder setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
            return this;
        }

        // edit view
        public Builder setInputDatas(String[] inputDatas, boolean isHints) {
            this.inputDatas = inputDatas;
            this.isHints = isHints;
            return this;
        }

        // image text
        public Builder setImageTexts(List<ImageText> imageTexts, boolean isShowImage) {
            this.imageTexts = imageTexts;
            this.isShowImage = isShowImage;
            return this;
        }

        // Increase the left UI
        public Builder setLeftView(View view) {
            this.leftView = view;
            return this;
        }

        // Added left UI click event
        public Builder setOnLeftViewClickListener(OnLeftViewClickListener leftViewClickListener) {
            this.leftViewClickListener = leftViewClickListener;
            return this;
        }

        public Builder setCustomizeData(String customizeJson, Class zlass) {
            this.customizeJson = customizeJson;
            this.zlass = zlass;
            return this;
        }

        public AlertView build() {
            return new AlertView(this);
        }
    }

    public static class Business {
        String businessId;
        String businessName;
        String businessCode;
        String businessEndTime;
        String businessManagerId;
        String businessManagerName;
        String businessExecutorId;
        String businessExecutorAvatarId;
        String businessExecutorName;
        String description;

        String avatarUrl;
    }

    public static class ImageText<T> {
        String text;
        String code;
        int res;
        T bind;


        public T getBind() {
            return bind;
        }

        public String getCode() {
            return code;
        }

        ImageText(String text, String code, int res) {
            this.text = text;
            this.code = code;
            this.res = res;
        }


        ImageText(String text, String code, int res, T bind) {
            this.text = text;
            this.code = code;
            this.res = res;
            this.bind = bind;
        }

        public static class Builder<T> {
            int res;
            String text;
            String code;
            T bind;

            public Builder<T> res(int res) {
                this.res = res;
                return this;
            }

            public Builder<T> code(String code) {
                this.code = code;
                return this;
            }


            public Builder<T> text(String text) {
                this.text = text;
                return this;
            }

            public Builder<T> bind(T bind) {
                this.bind = bind;
                return this;
            }

            public ImageText<T> build() {
                if (this.bind != null) {
                    return new ImageText<T>(text, code, res, bind);
                } else {
                    return new ImageText<T>(text, code, res);
                }
            }
        }
    }

}
