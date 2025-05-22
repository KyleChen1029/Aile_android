package tw.com.chainsea.chat.view.account.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.google.common.collect.Lists;

import java.util.List;

import tw.com.chainsea.android.common.event.KeyboardHelper;
import tw.com.chainsea.android.common.event.OnHKClickListener;
import tw.com.chainsea.android.common.text.StringHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemAccountInfoSettingRowBinding;
import tw.com.chainsea.custom.view.recyclerview.AnimationAdapter;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemNoSwipeViewHolder;

/**
 * current by evan on 2020-11-11
 *
 * @author Evan Wang
 * date 2020-11-11
 */
public class AccountInfoSettingAdapter extends AnimationAdapter<ItemBaseViewHolder<AccountInfoSettingAdapter.Type>> {
    UserProfileEntity profile;
    List<Type> list = Lists.newArrayList();
    OnAccountSettingListener onAccountSettingListener;

    public enum Type {
        REMARK_NAME(0, "備註名", true, null) {
            @Override
            CharSequence getValues() {
                return StringHelper.getString(getValue(), "");
            }
        },
        REMARK_PHONE(1, "備註電話", true, null) {
            @Override
            CharSequence getValues() {
                return StringHelper.getString(getValue(), "");
            }
        },
        PHONE_NUMBER(2, "手機", false, null) {
            @Override
            CharSequence getValues() {
                return StringHelper.getString(getValue(), "");
            }
        },
        E_MAIL(3, "E-mail", false, null) {
            @Override
            CharSequence getValues() {
                return StringHelper.getString(getValue(), "");
            }
        };

        Type(int index, String name, boolean canEdit, String value) {
            this.index = index;
            this.name = name;
            this.canEdit = canEdit;
            this.value = value;
        }

        private int index;
        private String name;
        private boolean canEdit;
        private String value;

        public Type setValue(String value) {
            this.value = value;
            return this;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public boolean isCanEdit() {
            return canEdit;
        }

        public String getValue() {
            return value;
        }

        abstract CharSequence getValues();

    }

    public AccountInfoSettingAdapter() {
        super.setAnimationEnable(false);
    }

    @Override
    public void executeAnimatorEnd(int position) {

    }

    @NonNull
    @Override
    public ItemBaseViewHolder<Type> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAccountInfoSettingRowBinding binding =
            DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_account_info_setting_row, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemBaseViewHolder<Type> holder, int position) {
        Type type = this.list.get(position);
        holder.onBind(type, 0, position);
    }


    @Override
    public int getItemCount() {
        return this.list.size();
    }


    public AccountInfoSettingAdapter bind(UserProfileEntity profile) {
        this.profile = profile;
        return this;
    }


    public AccountInfoSettingAdapter setOnAccountSettingListener(OnAccountSettingListener onAccountSettingListener) {
        this.onAccountSettingListener = onAccountSettingListener;
        return this;
    }


    @SuppressLint("NotifyDataSetChanged")
    public void refreshData() {
        setListData();
        notifyDataSetChanged();
    }

    private void setListData() {
        this.list.clear();
        if (this.profile != null) {

            if (!this.profile.isBlock()) {
                this.list.addAll(Lists.newArrayList(
                    Type.REMARK_NAME.setValue(this.profile.getAlias()),
                    Type.REMARK_PHONE.setValue(this.profile.getOtherPhone()),
                    Type.PHONE_NUMBER.setValue(this.profile.getMobile() == 0 ? "" : String.valueOf(this.profile.getMobile())),
                    Type.E_MAIL.setValue(this.profile.getEmail())
                ));
            } else {
                this.list.addAll(Lists.newArrayList(
                    Type.REMARK_NAME.setValue(this.profile.getAlias()),
                    Type.REMARK_PHONE.setValue(this.profile.getOtherPhone())
                ));
            }
        }
    }


    public class ViewHolder extends ItemNoSwipeViewHolder<Type> {
        ItemAccountInfoSettingRowBinding accountInfoSettingRowBinding;

        public ViewHolder(ItemAccountInfoSettingRowBinding binding) {
            super(binding.getRoot());
            accountInfoSettingRowBinding = binding;
        }

        @Override
        public void onBind(Type type, int section, int position) {
            super.onBind(type, section, position);
            accountInfoSettingRowBinding.tvName.setText(type.getName());
            accountInfoSettingRowBinding.etValue.setText(type.getValues());
            accountInfoSettingRowBinding.etValue.setSelection(accountInfoSettingRowBinding.etValue.getText().length());
            accountInfoSettingRowBinding.etValue.setEnabled(type.isCanEdit());
            accountInfoSettingRowBinding.ivPen.setVisibility(type.isCanEdit() ? View.VISIBLE : View.GONE);
            if (type.isCanEdit()) {
                accountInfoSettingRowBinding.etValue.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        KeyboardHelper.postOpen(v);
                    } else {
                        String result = accountInfoSettingRowBinding.etValue.getText().toString();
                        if (!type.getValues().equals(result)) {
                            if (onAccountSettingListener != null) {
                                onAccountSettingListener.onModify(profile.getId(), type, result);
                            }
                        }
                        KeyboardHelper.hide(v);
                    }
                });

                itemView.setOnClickListener(new OnHKClickListener<String>() {
                    @Override
                    public void onClick(View v, String s) {
                        accountInfoSettingRowBinding.etValue.requestFocus();
                        accountInfoSettingRowBinding.etValue.setSelection(accountInfoSettingRowBinding.etValue.getText().length());
                    }
                });
            } else {
                accountInfoSettingRowBinding.etValue.setOnFocusChangeListener(null);
                itemView.setOnClickListener(null);
            }
        }
    }

    public interface OnAccountSettingListener {
        void onModify(String id, Type type, String result);

    }

}
