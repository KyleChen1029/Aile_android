package tw.com.chainsea.chat.view.todo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import tw.com.chainsea.android.common.event.KeyboardHelper;
import tw.com.chainsea.android.common.event.OnHKClickListener;
import tw.com.chainsea.android.common.network.NetworkHelper;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.ProcessStatus;
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity;
import tw.com.chainsea.ce.sdk.bean.todo.TodoStatus;
import tw.com.chainsea.ce.sdk.bean.todo.Type;
import tw.com.chainsea.ce.sdk.service.TodoService;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemTodoSettingViewBinding;
import tw.com.chainsea.chat.view.business.DateSelector;
import tw.com.chainsea.chat.view.business.adapter.DateSelectorAdapter;
import tw.com.chainsea.chat.view.todo.OnSetRemindTime;
import tw.com.chainsea.chat.widget.GridItemDecoration;
import tw.com.chainsea.custom.view.recyclerview.AnimationAdapter;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemNoSwipeViewHolder;

/**
 * current by evan on 2020-10-30
 *
 * @author Evan Wang
 * date 2020-10-30
 */
public class TodoSettingAdapter extends AnimationAdapter<ItemBaseViewHolder<TodoEntity>> {
    List<TodoEntity> matadatas = Lists.newArrayList();
    private Context context;
    private OnTodoSettingListener onTodoSettingListener;
    private OnSetRemindTime setRemindTimeListener;
    private View.OnClickListener backgroundOnClickListener;


    public TodoSettingAdapter() {
        super.setAnimationEnable(false);
    }

    public void setBackgroundOnClickListener(View.OnClickListener backgroundOnClickListener) {
        this.backgroundOnClickListener = backgroundOnClickListener;
    }


    @Override
    public void executeAnimatorEnd(int position) {

    }

    @NonNull
    @Override
    public ItemBaseViewHolder<TodoEntity> onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        if (context == null) {
            this.context = parent.getContext();
        }
        ItemTodoSettingViewBinding binding = ItemTodoSettingViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new NoDataItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemBaseViewHolder<TodoEntity> holder, int position) {
        TodoEntity entity = this.matadatas.get(position);
        holder.onBind(entity, 0, position);
    }

    @Override
    public int getItemCount() {
        return this.matadatas.size();
    }

    private void sort() {
//        Collections.sort(this.matadatas, new Ordering<TodoEntity>() {
//            @Override
//            public int compare(@NullableDecl TodoEntity left, @NullableDecl TodoEntity right) {
//                return ComparisonChain.start()
//                        .compare(left.getWeights3(), right.getWeights3())
////                        .compare(left.getWeights(), right.getWeights())
//                        .compare(right.getWeights2(), left.getWeights2())
//                        .result();
//            }
//        });
    }

    private void filter() {

    }

    public int indexOf(String id) {
        if (!Strings.isNullOrEmpty(id)) {
            int index = matadatas.indexOf(new TodoEntity.Builder().type(Type.MAIN).id(id).build());
            return index > 0 ? index : -1;
        }
        return -1;
    }


    public TodoSettingAdapter setData(List<TodoEntity> list) {
        this.matadatas = list;
        return this;
    }

    public TodoSettingAdapter remove(TodoEntity entity) {
        this.matadatas.remove(entity);
        return this;
    }

    public TodoSettingAdapter setOnTodoSettingListener(OnTodoSettingListener onTodoSettingListener) {
        this.onTodoSettingListener = onTodoSettingListener;
        return this;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        sort();
        filter();
        notifyDataSetChanged();
    }

    class NoDataItemViewHolder extends ItemNoSwipeViewHolder<TodoEntity> {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.TAIWAN);
        List<DateSelector> dateSelectors = DateSelector.TODO_ITEM_SETTING_DATE_SELECTOR;

        private ItemTodoSettingViewBinding binding;

        public NoDataItemViewHolder(ItemTodoSettingViewBinding binding) {
            super(binding.getRoot());
            int paddingWidth = UiHelper.dip2px(itemView.getContext(), 40.0f);
            int displayWidth = UiHelper.getDisplayWidth(itemView.getContext());
            itemView.getLayoutParams().width = displayWidth - paddingWidth;
            itemView.setForegroundGravity(Gravity.CENTER);
            this.binding = binding;
        }


        // 判断当前EditText是否可滚动
        private boolean canVerticalScroll(EditText editText) {
            return editText.getLineCount() > editText.getMaxLines();
        }

        @Override
        @SuppressLint("ClickableViewAccessibility")
        public void onBind(TodoEntity entity, int section, int position) {
            super.onBind(entity, section, position);

            if (backgroundOnClickListener != null) {
                binding.getRoot().setOnClickListener(backgroundOnClickListener);
            }

            binding.clContentBox.setOnClickListener(OnHKClickListener.newEmpty());

            long remindTime = entity.getRemindTime();
            if (remindTime > 0) {
                Calendar nowCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"), Locale.TAIWAN);
                nowCal.setTimeInMillis(remindTime);
                entity.setCurrent(nowCal);
                binding.ivClock2.setImageResource(R.drawable.ic_clock_blue_25dp);
            } else {
                binding.ivClock2.setImageResource(R.drawable.ic_clock_gray_25dp);
            }

            binding.dateTimePick.setVisibility(View.GONE);
            binding.etTitle.setText(entity.getTitle());

            long now = System.currentTimeMillis();
            if (remindTime > 0) {
                if (remindTime < now) {
//                    tvClockText.setText(R.string.todo_remind_timeout_alert);
                    binding.tvClockText.setText(format.format(remindTime));
//                    tvClockText.setText("超時:" + format.format(remindTime));
                } else {
                    binding.tvClockText.setText(format.format(remindTime));
                }
            } else {
                binding.tvClockText.setText(R.string.todo_do_not_remind);
            }

            binding.ivClock2.setSelected(entity.getOpenClock());

            binding.tvInputStatus.setText(MessageFormat.format("{0}/2000", entity.getTitle().length()));

            // android:background="@drawable/radius_search_view_bg"
            binding.etTitle.setBackgroundResource(0);
            binding.etTitle.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && binding.dateTimePick.getVisibility() == View.VISIBLE) {
                    binding.dateTimePick.setVisibility(View.GONE);
                }
                binding.etTitle.setBackgroundResource(hasFocus ? R.drawable.radius_search_view_bg : 0);
                entity.setDeleteClickCount(0);
                resetDeleteFun(binding.btnDelete, entity.getDeleteClickCount());
            });

            //監控目前輸入的標題字數，目前顯示最多n/2000個字
            binding.etTitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    binding.tvInputStatus.setText(MessageFormat.format("{0}/2000", s.length()));
                    binding.btnConfirm.setSelected(!s.toString().isEmpty());
                    binding.btnConfirm.setEnabled(!s.toString().isEmpty());
                    binding.btnComplete.setSelected(!binding.etTitle.getText().toString().isEmpty());
                    binding.btnComplete.setEnabled(!binding.etTitle.getText().toString().isEmpty());
                }
            });

            binding.btnConfirm.setSelected(!binding.etTitle.getText().toString().isEmpty());
            binding.btnConfirm.setEnabled(!binding.etTitle.getText().toString().isEmpty());

            binding.btnComplete.setSelected(!binding.etTitle.getText().toString().isEmpty());
            binding.btnComplete.setEnabled(!binding.etTitle.getText().toString().isEmpty());


            binding.etTitle.setOnTouchListener((v, event) -> {
                if ((v.getId() == R.id.et_title && canVerticalScroll(binding.etTitle))) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
                return false;
            });

            binding.tvClockText.setOnClickListener(v -> {
                if (binding.dateTimePick.getVisibility() == View.VISIBLE) {
                    binding.dateTimePick.setVisibility(View.GONE);
                } else {
                    if (entity.getCurrent() != null) {
                        binding.dateTimePick.setCurrent(entity.getCurrent());
                    } else {
                        binding.dateTimePick.reset();
                    }
                    binding.dateTimePick.setVisibility(View.VISIBLE);
                    binding.etTitle.clearFocus();
                    KeyboardHelper.hide(binding.etTitle);
                }
                entity.setDeleteClickCount(0);
                resetDeleteFun(binding.btnDelete, entity.getDeleteClickCount());
            });
//            if (BuildConfig.DEBUG) {
//                dateSelectors = DateSelector.TODO_ITEM_SETTING_DATE_SELECTOR_DEBUG;
//            }
            DateSelectorAdapter adapter = new DateSelectorAdapter()
                .setList(dateSelectors)
                .setOnDateSelectorListener(new DateSelectorAdapter.OnDateSelectorListener() {
                    @Override
                    public void onCustomize() {
                        if (binding.dateTimePick.getVisibility() == View.VISIBLE) {
                            binding.dateTimePick.setVisibility(View.GONE);
                        } else {
                            if (entity.getCurrent() != null) {
                                binding.dateTimePick.setCurrent(entity.getCurrent());
                            } else {
                                binding.dateTimePick.reset();
                            }
                            binding.dateTimePick.setVisibility(View.VISIBLE);
                            binding.etTitle.clearFocus();
                            KeyboardHelper.hide(binding.etTitle);
                        }
                        entity.setDeleteClickCount(0);
                        resetDeleteFun(binding.btnDelete, entity.getDeleteClickCount());
                        if (!binding.etTitle.getText().toString().isEmpty()) {
                            binding.btnConfirm.setSelected(true);
                            binding.btnConfirm.setEnabled(true);
                            binding.btnComplete.setSelected(true);
                            binding.btnComplete.setEnabled(true);
                        }
                    }

                    @Override
                    public void onDateSelect(DateSelector selector, long timeMillis, String date) {
                        binding.dateTimePick.setVisibility(View.GONE);
                        binding.etTitle.clearFocus();
                        if (entity.getCurrent() == null) {
                            binding.ivClock2.setSelected(true);
//                                sbAlarm.setChecked(true);
                            binding.ivClock2.setImageResource(R.drawable.ic_clock_blue_25dp);
                            entity.setCurrent(Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"), Locale.TAIWAN));
                        } else if (System.currentTimeMillis() > entity.getCurrent().getTimeInMillis()) {
                            binding.ivClock2.setSelected(true);
//                                sbAlarm.setChecked(true);
                            binding.ivClock2.setImageResource(R.drawable.ic_clock_blue_25dp);
                            entity.setCurrent(Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"), Locale.TAIWAN));
                        }
                        switch (selector) {
                            case PLUS_8_SEC:
                                entity.getCurrent().set(Calendar.SECOND, entity.getCurrent().get(Calendar.SECOND) + 8);
                                break;
                            case PLUS_10_MINUTE:
                                entity.getCurrent().set(Calendar.MINUTE, entity.getCurrent().get(Calendar.MINUTE) + 10);
                                break;
                            case PLUS_30_MINUTE:
                                entity.getCurrent().set(Calendar.MINUTE, entity.getCurrent().get(Calendar.MINUTE) + 30);
                                break;
                            case PLUS_1_HOUR:
                                entity.getCurrent().set(Calendar.HOUR, entity.getCurrent().get(Calendar.HOUR_OF_DAY) + 1);
                                break;
                            case TODAY:
                                entity.getCurrent().set(Calendar.DATE, entity.getCurrent().get(Calendar.DATE) + 1);
                                break;
                            case ONE_WEEK:
                                entity.getCurrent().set(Calendar.DATE, entity.getCurrent().get(Calendar.DATE) + 7);
                                break;
                            case ONE_MONTH:
                                entity.getCurrent().set(Calendar.MONTH, entity.getCurrent().get(Calendar.MONTH) + 1);
                                break;
                        }
                        binding.tvClockText.setText(format.format(entity.getCurrent().getTime()));
                        binding.tvClear.setVisibility(View.VISIBLE);
                        entity.setDeleteClickCount(0);
                        resetDeleteFun(binding.btnDelete, entity.getDeleteClickCount());
                        if (!binding.etTitle.getText().toString().isEmpty()) {
                            binding.btnConfirm.setSelected(true);
                            binding.btnConfirm.setEnabled(true);
                            binding.btnComplete.setSelected(true);
                            binding.btnComplete.setEnabled(true);
                        }
                    }

                    @Override
                    public void onClear(DateSelector selector, long timeMillis, String date) {
                        if (!binding.etTitle.getText().toString().isEmpty()) {
                            binding.btnConfirm.setSelected(true);
                            binding.btnConfirm.setEnabled(true);
                            binding.btnComplete.setSelected(true);
                            binding.btnComplete.setEnabled(true);
                        }
                    }
                });

            binding.rvDateSelector.setLayoutManager(new GridLayoutManager(context, dateSelectors.size()));
            binding.rvDateSelector.setHasFixedSize(true);
            binding.rvDateSelector.addItemDecoration(new GridItemDecoration(Color.TRANSPARENT));
            binding.rvDateSelector.setItemAnimator(new DefaultItemAnimator());
            binding.rvDateSelector.setAdapter(adapter);
            binding.rvDateSelector.measure(0, 0);
            binding.dateTimePick.setDateTimeFormat(format);
            if (entity.getCurrent() != null) {
                if (entity.getCurrent().getTimeInMillis() < now) {
                    binding.dateTimePick.reset();
                } else {
                    binding.dateTimePick.setCurrent(entity.getCurrent());
                }
            } else {
                binding.dateTimePick.reset();
            }

            binding.ivClock2.setImageResource(entity.getOpenClock() ? R.drawable.ic_clock_blue_25dp : R.drawable.ic_clock_gray_25dp);
            binding.ivClock2.setSelected(entity.getOpenClock());
            binding.ivClock2.setOnClickListener(v -> {
                boolean isChecked = !v.isSelected();
                if (isChecked && entity.getCurrent() == null) {
                    v.setSelected(false);
                    isChecked = false;
                }
                v.setSelected(!v.isSelected());
//                entity.setOpenClock(isChecked);
                binding.ivClock2.setImageResource(isChecked ? R.drawable.ic_clock_blue_25dp : R.drawable.ic_clock_gray_25dp);
                entity.setDeleteClickCount(0);
                resetDeleteFun(binding.btnDelete, entity.getDeleteClickCount());
            });

            binding.dateTimePick.setOnDateTimePickerListener((current, dateTime, millis) -> {
                if (entity.getCurrent() == null) {
                    binding.ivClock2.setSelected(true);
                    binding.ivClock2.setImageResource(R.drawable.ic_clock_blue_25dp);
                }

                entity.setCurrent(current);
                binding.tvClockText.setText(dateTime);
                binding.tvClear.setVisibility(View.VISIBLE);
            });


            binding.tvClear.setVisibility(entity.getCurrent() == null ? View.GONE : View.VISIBLE);
            // clearTime
            binding.tvClear.setOnClickListener(v -> {
                binding.etTitle.clearFocus();
                binding.tvClockText.setText(R.string.todo_do_not_remind);
                entity.setCurrent(null);
//                sbAlarm.setChecked(false);
                binding.ivClock2.setSelected(false);
                binding.ivClock2.setImageResource(R.drawable.ic_clock_gray_25dp);
                binding.dateTimePick.setVisibility(View.GONE);
                v.setVisibility(View.GONE);
                entity.setDeleteClickCount(0);
                resetDeleteFun(binding.btnDelete, entity.getDeleteClickCount());
                binding.btnConfirm.setSelected(true);
                binding.btnConfirm.setEnabled(true);
                binding.btnComplete.setSelected(true);
                binding.btnComplete.setEnabled(true);
            });

            // cancel callBack to upperLayer View call popup dismiss
            binding.ivClose.setOnClickListener(v -> {
                binding.etTitle.clearFocus();
                if (onTodoSettingListener != null) {
                    binding.etTitle.setFocusable(false);
                    KeyboardHelper.hide(binding.etTitle);
                    onTodoSettingListener.onCancel();
                }
            });
            binding.btnCancel.setOnClickListener(v -> {
                binding.ivClose.performClick();
            });

            if (Strings.isNullOrEmpty(entity.getId())) {
                binding.btnComplete.setVisibility(View.GONE);
                binding.btnComplete.setOnClickListener(null);
                binding.btnDelete.setVisibility(View.GONE);
                binding.btnDelete.setOnClickListener(null);
                binding.btnCancel.setVisibility(View.VISIBLE);
            } else {
                // 修改
                if (TodoStatus.DONE.equals(entity.getStatus())) {
                    binding.btnComplete.setVisibility(View.GONE);
                    binding.btnComplete.setOnClickListener(null);
                } else {
                    binding.btnComplete.setVisibility(View.VISIBLE);
                    binding.btnConfirm.setSelected(false);
                    binding.btnConfirm.setEnabled(false);

                    binding.btnComplete.setOnClickListener(v -> {
                        if (!NetworkHelper.hasNetWork(context)) {
                            Toast.makeText(context, R.string.todo_reminder_done_error, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        TodoService.completeAndSync(context, entity, RefreshSource.REMOTE);
                        if (onTodoSettingListener != null) {
                            binding.etTitle.setFocusable(false);
                            KeyboardHelper.hide(binding.etTitle);
                            onTodoSettingListener.onCancel();
                        }
                    });
                }

                binding.btnDelete.setVisibility(View.VISIBLE);
                resetDeleteFun(binding.btnDelete, entity.getDeleteClickCount());
                // delete action
                binding.btnDelete.setOnClickListener(v -> {
                    binding.etTitle.clearFocus();
                    if (entity.getDeleteClickCount() > 0) {
                        if (!NetworkHelper.hasNetWork(context)) {
                            Toast.makeText(context, R.string.todo_reminder_delete_error, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        TodoService.deleteAndSync(context, entity, RefreshSource.REMOTE);
                        if (onTodoSettingListener != null) {
                            binding.etTitle.setFocusable(false);
                            KeyboardHelper.hide(binding.etTitle);
                            onTodoSettingListener.onCancel();
                        }
                    } else {
                        entity.setDeleteClickCount(entity.getDeleteClickCount() + 1);
                        resetDeleteFun(binding.btnDelete, entity.getDeleteClickCount());
                    }
                });
                binding.btnConfirm.setText(TodoStatus.DONE.equals(entity.getStatus()) ? R.string.todo_restart_remind : R.string.alert_save);
            }

            // confirm
            binding.btnConfirm.setOnClickListener(v -> {
                binding.etTitle.clearFocus();
                if (Strings.isNullOrEmpty(binding.etTitle.getText().toString().trim())) {
                    Toast.makeText(context, R.string.todo_edit_input_verify_alert, Toast.LENGTH_SHORT).show();
                } else if (entity.getCurrent() != null && entity.getCurrent().getTimeInMillis() <= System.currentTimeMillis()) {
                    Toast.makeText(context, R.string.todo_please_select_effective_reminder_time, Toast.LENGTH_SHORT).show();
                } else if (!NetworkHelper.hasNetWork(context)) {
                    String message = context.getString(R.string.todo_reminder_created_error);
                    if (TodoStatus.DONE.equals(entity.getStatus())) {
                        message = context.getString(R.string.todo_reminder_done_error);
                    } else if (!Strings.isNullOrEmpty(entity.getId())) {
                        message = context.getString(R.string.todo_reminder_update_error);
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                } else {
                    entity.setRemindTime(entity.getCurrent() != null ? entity.getCurrent().getTime().getTime() : 0L);
                    entity.setTitle(binding.etTitle.getText().toString());
//                if (TodoStatus.DONE.equals(entity.getStatus())) {
//                    entity.setStatus(TodoStatus.PROGRESS);
//                }

                    entity.setOpenClock(binding.ivClock2.isSelected());

                    if (Strings.isNullOrEmpty(entity.getId())) {
                        String uuid = String.valueOf(UUID.randomUUID());
                        entity.setId(uuid);
                    }


                    if (ProcessStatus.UN_SYNC_CREATE.equals(entity.getProcessStatus())) {
                        TodoService.saveAndSync(context, entity, RefreshSource.REMOTE, null);
                    } else {
                        TodoService.updateAndSync(context, entity, RefreshSource.REMOTE);
                    }

                    if (entity.getRemindTime() != 0) {
                        if (setRemindTimeListener != null) {
                            setRemindTimeListener.onSetRemind(true);
                        }
                    }

                    if (onTodoSettingListener != null) {
                        binding.etTitle.setFocusable(false);
                        KeyboardHelper.hide(binding.etTitle);
                        onTodoSettingListener.onCancel();
                    }
                }
            });
        }

        private void resetDeleteFun(Button button, int count) {
            if (count >= 1) {
                button.setTextColor(0xFFFF0033);
                button.setText(R.string.alert_delete_double_check);
            } else {
                button.setTextColor(0xFF0076ff);
                button.setText(R.string.alert_delete);
            }
        }
    }

    public void setRemindListener(OnSetRemindTime setRemindTimeListener) {
        this.setRemindTimeListener = setRemindTimeListener;
    }

    public interface OnTodoSettingListener {
        void onCancel();

        void navigateToChat(TodoEntity entity);

        void remove(TodoEntity entity);
    }
}
