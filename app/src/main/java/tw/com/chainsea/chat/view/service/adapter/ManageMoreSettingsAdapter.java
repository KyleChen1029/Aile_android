package tw.com.chainsea.chat.view.service.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;

import java.util.List;

import tw.com.chainsea.android.common.event.OnHKClickListener;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemManageMoreSettingsActionBinding;
import tw.com.chainsea.chat.databinding.ItemManageMoreSettingsEditBinding;
import tw.com.chainsea.chat.databinding.ItemManageMoreSettingsEnableBinding;
import tw.com.chainsea.chat.databinding.ItemManageMoreSettingsInfoBinding;
import tw.com.chainsea.chat.databinding.ItemManageMoreSettingsMembersBinding;
import tw.com.chainsea.chat.databinding.ItemServiceNumberTimeOutSettingBinding;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.view.roomAction.adapter.MemberAdapter;
import tw.com.chainsea.chat.view.service.bean.MoreSettingsBean;
import tw.com.chainsea.chat.view.service.bean.MoreSettingsType;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemNoSwipeViewHolder;

/**
 * current by evan on 2020-07-28
 *
 * @author Evan Wang
 * date 2020-07-28
 */
public class ManageMoreSettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<MoreSettingsBean> settingsBeans;

    String userId;
    ServiceNumberEntity entity;

    OnMoreSettingsItemClickListener<MoreSettingsBean> onMoreSettingsItemClickListener;

    public ManageMoreSettingsAdapter(OnMoreSettingsItemClickListener<MoreSettingsBean> onMoreSettingsItemClickListener) {
        this.onMoreSettingsItemClickListener = onMoreSettingsItemClickListener;
    }

    public void setData(List<MoreSettingsBean> settingsBeans) {
        this.settingsBeans = settingsBeans;
    }


    /**
     * R.layout.item_manage_more_settings_info
     * R.layout.item_manage_more_settings_edit
     * R.layout.item_manage_more_settings_members
     * R.layout.item_manage_more_settings_action
     * R.layout.item_manage_more_settings_enable
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        userId = TokenPref.getInstance(context).getUserId();
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemManageMoreSettingsEnableBinding itemManageMoreSettingsEnableBinding = ItemManageMoreSettingsEnableBinding.inflate(layoutInflater, parent, false);

        ItemManageMoreSettingsActionBinding itemManageMoreSettingsActionBinding = ItemManageMoreSettingsActionBinding.inflate(layoutInflater, parent, false);
        switch (viewType) {
            case 0: //
                ItemManageMoreSettingsInfoBinding itemManageMoreSettingsInfoBinding = ItemManageMoreSettingsInfoBinding.inflate(layoutInflater, parent, false);
                return new ServiceInfoViewHolder(itemManageMoreSettingsInfoBinding);
            case 1:
                ItemManageMoreSettingsEditBinding itemManageMoreSettingsEditBinding = ItemManageMoreSettingsEditBinding.inflate(layoutInflater, parent, false);
                return new DepictionInputViewHolder(itemManageMoreSettingsEditBinding);
            case 2:

                return new EnableViewHolder(itemManageMoreSettingsEnableBinding);
            case 3:
                ItemManageMoreSettingsMembersBinding itemManageMoreSettingsMembersBinding = ItemManageMoreSettingsMembersBinding.inflate(layoutInflater, parent, false);
                return new MembersViewHolder(itemManageMoreSettingsMembersBinding);
            case 4:

                return new BroadcastMessageViewHolder(itemManageMoreSettingsActionBinding);
            case 5:
                return new UpgradeProfessionalViewHolder(itemManageMoreSettingsEnableBinding);
            case 6:
                return new InsideServiceNumberViewHolder(itemManageMoreSettingsEnableBinding);
            case 7:
                return new OutsideServiceNumberViewHolder(itemManageMoreSettingsEnableBinding);
            case 8:
                return new WelcomeMessageViewHolder(itemManageMoreSettingsActionBinding);
            case 9:
                return new PostBackCallbackViewHolder(itemManageMoreSettingsActionBinding);
            case 10:
                ItemServiceNumberTimeOutSettingBinding timeOutSettingBinding = ItemServiceNumberTimeOutSettingBinding.inflate(layoutInflater, parent, false);
                return new TimeOutSettingViewHolder(timeOutSettingBinding);
            default:
                return new UndefViewHolder(new View(context));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MoreSettingsBean bean = settingsBeans.get(position);
        if (holder instanceof DepictionInputViewHolder) {
            ((DepictionInputViewHolder) holder).onBind(entity.getDescription());
        } else {
            ((ItemBaseViewHolder) holder).onBind(bean, 0, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        MoreSettingsBean bean = settingsBeans.get(position);
        return bean.getType().getIndex();
    }

    @Override
    public int getItemCount() {
        return settingsBeans.size();
    }

    public ManageMoreSettingsAdapter setEntity(ServiceNumberEntity entity) {
        this.entity = entity;
//        this.settingsBeans.clear();
//        this.settingsBeans.add(MoreSettingsType.DEPICTION_INPUT.getIndex(), new MoreSettingsBean(entity.getDescription(), MoreSettingsType.DEPICTION_INPUT));
        return this;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        notifyDataSetChanged();
    }

    class ServiceInfoViewHolder extends ItemNoSwipeViewHolder<MoreSettingsBean> {

        private ItemManageMoreSettingsInfoBinding binding;

        public ServiceInfoViewHolder(ItemManageMoreSettingsInfoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(MoreSettingsBean moreSettingsBean, int section, int position) {
            if (entity != null) {
                binding.civIcon.loadAvatarIcon(entity.getAvatarId(), entity.getName(), entity.getServiceNumberId());
                binding.tvTitle.setText(entity.getName());
            }
        }
    }

    static class DepictionInputViewHolder extends RecyclerView.ViewHolder {

        private ItemManageMoreSettingsEditBinding binding;

        public DepictionInputViewHolder(ItemManageMoreSettingsEditBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void onBind(String depiction) {
            binding.etInput.setHint("請輸入服務號描述");
            binding.etInput.setText(depiction);
        }
    }

    static class EnableViewHolder extends ItemNoSwipeViewHolder<MoreSettingsBean> {

        private ItemManageMoreSettingsEnableBinding binding;

        public EnableViewHolder(ItemManageMoreSettingsEnableBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.sbEnable.setChecked(true);
            binding.sbEnable.setEnabled(false);
        }

        @Override
        public void onBind(MoreSettingsBean moreSettingsBean, int section, int position) {
            binding.tvTitle.setText(moreSettingsBean.getTitle());
        }
    }

    @SuppressLint("SetTextI18n")
    class MembersViewHolder extends ItemNoSwipeViewHolder<MoreSettingsBean> {

        private ItemManageMoreSettingsMembersBinding binding;

        public MembersViewHolder(ItemManageMoreSettingsMembersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.rvMembers.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
            binding.rvMembers.setHasFixedSize(true);
            binding.rvMembers.setItemAnimator(new DefaultItemAnimator());
            binding.tvRight.setOnClickListener(view -> {
                ActivityTransitionsControl.navigateToServiceAgentsManage(view.getContext(), entity.getBroadcastRoomId(), entity.getServiceNumberId(), (intent, s) -> view.getContext().startActivity(intent));
            });
            binding.tvLeft.setText("服務號成員 " + entity.getMemberItems().size());
        }

        @Override
        public void onBind(MoreSettingsBean moreSettingsBean, int section, int position) {
            if (entity != null) {
                binding.rvMembers.setAdapter(new MemberAdapter().setData(Lists.newArrayList(entity.getMemberItems())));
            }
        }
    }

    class BroadcastMessageViewHolder extends ItemNoSwipeViewHolder<MoreSettingsBean> {

        private ItemManageMoreSettingsActionBinding binding;

        public BroadcastMessageViewHolder(ItemManageMoreSettingsActionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(MoreSettingsBean moreSettingsBean, int section, int position) {
            binding.tvTitle.setText(moreSettingsBean.getTitle());
            itemView.setOnClickListener(new OnHKClickListener() {
                @Override
                public void onClick(View v, Object o) {
                    if (onMoreSettingsItemClickListener != null) {
                        onMoreSettingsItemClickListener.onItemClick(MoreSettingsType.BROADCAST_MESSAGE, moreSettingsBean);
                    }
                }
            });
        }
    }

    static class UpgradeProfessionalViewHolder extends ItemNoSwipeViewHolder<MoreSettingsBean> {

        private ItemManageMoreSettingsEnableBinding binding;

        public UpgradeProfessionalViewHolder(ItemManageMoreSettingsEnableBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(MoreSettingsBean moreSettingsBean, int section, int position) {
            binding.tvTitle.setText(moreSettingsBean.getTitle());
        }
    }

    static class InsideServiceNumberViewHolder extends ItemNoSwipeViewHolder<MoreSettingsBean> {

        private ItemManageMoreSettingsEnableBinding binding;

        public InsideServiceNumberViewHolder(ItemManageMoreSettingsEnableBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(MoreSettingsBean moreSettingsBean, int section, int position) {
            binding.tvTitle.setText(moreSettingsBean.getTitle());
        }
    }

    static class OutsideServiceNumberViewHolder extends ItemNoSwipeViewHolder<MoreSettingsBean> {

        private ItemManageMoreSettingsEnableBinding binding;

        public OutsideServiceNumberViewHolder(ItemManageMoreSettingsEnableBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(MoreSettingsBean moreSettingsBean, int section, int position) {
            binding.tvTitle.setText(moreSettingsBean.getTitle());
        }
    }

    class WelcomeMessageViewHolder extends ItemNoSwipeViewHolder<MoreSettingsBean> {

        private ItemManageMoreSettingsActionBinding binding;

        public WelcomeMessageViewHolder(ItemManageMoreSettingsActionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(MoreSettingsBean moreSettingsBean, int section, int position) {
            binding.tvTitle.setText(moreSettingsBean.getTitle());
            itemView.setOnClickListener(new OnHKClickListener() {
                @Override
                public void onClick(View v, Object o) {
                    if (onMoreSettingsItemClickListener != null) {
                        onMoreSettingsItemClickListener.onItemClick(MoreSettingsType.WELCOME_MESSAGE, moreSettingsBean);
                    }
                }
            });
        }
    }

    class PostBackCallbackViewHolder extends ItemNoSwipeViewHolder<MoreSettingsBean> {

        private ItemManageMoreSettingsActionBinding binding;

        public PostBackCallbackViewHolder(ItemManageMoreSettingsActionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.tvContent.setVisibility(View.VISIBLE);
        }

        @Override
        public void onBind(MoreSettingsBean moreSettingsBean, int section, int position) {
            binding.tvTitle.setText(moreSettingsBean.getTitle());
            binding.tvContent.setText(itemView.getContext().getString(R.string.service_number_post_back_detail_setting));
            itemView.setOnClickListener(view -> {
                if (onMoreSettingsItemClickListener != null) {
                    onMoreSettingsItemClickListener.onItemClick(MoreSettingsType.POST_BACK_CALLBACK, moreSettingsBean);
                }
            });
        }
    }

    class TimeOutSettingViewHolder extends ItemNoSwipeViewHolder<MoreSettingsBean> {

        private ItemServiceNumberTimeOutSettingBinding binding;

        public TimeOutSettingViewHolder(ItemServiceNumberTimeOutSettingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.tvContent.setVisibility(View.VISIBLE);
        }

        @Override
        public void onBind(MoreSettingsBean moreSettingsBean, int section, int position) {
            binding.tvTitle.setText(moreSettingsBean.getTitle());
            binding.tvContent.setText(itemView.getContext().getString(R.string.service_number_connection_timeout_detail));
            binding.tvTimeOutValue.setText(getServiceNumberDisplayText());
            itemView.setOnClickListener(view -> {
                if (onMoreSettingsItemClickListener != null) {
                    onMoreSettingsItemClickListener.onItemClick(MoreSettingsType.TIMEOUT_SETTING, moreSettingsBean);
                }
            });
        }

        private String getServiceNumberDisplayText() {
            Context context = binding.getRoot().getContext();
            int timeout = entity.getServiceTimeoutTime();
            if (timeout == 0) {
                return context.getString(R.string.text_dialog_time_out_no_limit);
            } else if (timeout < 60) {
                return context.getString(R.string.text_dialog_time_out_thirty_min);
            } else {
                int hour = timeout / 60;
                return switch (hour) {
                    case 1 -> context.getString(R.string.text_dialog_time_out_one_hour);
                    case 6 -> context.getString(R.string.text_dialog_time_out_six_hour);
                    case 12 -> context.getString(R.string.text_dialog_time_out_twelve_hour);
                    case 24 -> context.getString(R.string.text_dialog_time_out_twenty_four_hour);
                    case 48 -> context.getString(R.string.text_dialog_time_out_forty_eight_hour);
                    default -> context.getString(R.string.text_dialog_time_out_no_limit);
                };
            }
        }
    }

    /**
     * 未知
     */
    static class UndefViewHolder extends ItemNoSwipeViewHolder<MoreSettingsBean> {

        public UndefViewHolder(View itemView) {
            super(itemView);
        }
    }


    //MoreSettingsType
    public interface OnMoreSettingsItemClickListener<T> {
        void onItemClick(MoreSettingsType type, T t);
    }

}
