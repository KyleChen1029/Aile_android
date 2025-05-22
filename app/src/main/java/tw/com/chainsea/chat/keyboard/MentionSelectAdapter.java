package tw.com.chainsea.chat.keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import tw.com.chainsea.android.common.text.KeyWordHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemMentionSelectBinding;

/**
 * current by evan on 2019-12-03
 */
public class MentionSelectAdapter extends RecyclerView.Adapter<MentionSelectAdapter.ViewHolder> {
    private LinkedList<UserProfileEntity> tmpUserProfiles = Lists.newLinkedList();
    private final LinkedList<UserProfileEntity> userProfiles = Lists.newLinkedList();
    private OnSelectItemListener<UserProfileEntity> onSelectItemListener;
    private String keyword = "E";
    private String userId = "";
    private final Set<String> selectSet = Sets.newHashSet();

    public MentionSelectAdapter(Context ctx) {
        this.userId = TokenPref.getInstance(ctx).getUserId();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        ItemMentionSelectBinding binding = ItemMentionSelectBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserProfileEntity userProfile = this.userProfiles.get(position);
        holder.itemView.setSelected(selectSet.contains(userProfile.getId()));
        String name = !Strings.isNullOrEmpty(userProfile.getAlias()) ? userProfile.getAlias() : userProfile.getNickName();
        String allMembersName = "ALL";
        if (allMembersName.equals(userProfile.getAvatarId())) {
            holder.binding.avatarCIV.setImageResource(R.drawable.ce_icon);
        } else {
            holder.binding.avatarCIV.loadAvatarIcon(userProfile.getAvatarId(), !Strings.isNullOrEmpty(userProfile.getAlias()) ? userProfile.getAlias() : userProfile.getNickName(), userProfile.getId());
        }
        SpannableString spannableString = KeyWordHelper.matcherSearchTitle(0xFF4A90E2, name, this.keyword);
        holder.binding.nameTV.setText(spannableString);

        holder.itemView.setOnClickListener(v -> {
            if (onSelectItemListener != null) {
                if (!v.isSelected()) {
                    onSelectItemListener.onSelect(userProfile, position, !selectSet.isEmpty());
                    v.setSelected(true);
                }
            }
            selectSet.add(userProfile.getId());
        });
    }

    @Override
    public int getItemCount() {
        return this.userProfiles.size();
    }

    public MentionSelectAdapter setKeyword(String keyword) {
        this.keyword = keyword.toUpperCase();
        return this;
    }

    public MentionSelectAdapter setUserProfiles(LinkedList<UserProfileEntity> userProfiles) {
        this.tmpUserProfiles = userProfiles;
        sort(this.tmpUserProfiles);
        filter(this.tmpUserProfiles, this.keyword);
        return this;
    }

    public MentionSelectAdapter setOnSelectItemListener(OnSelectItemListener<UserProfileEntity> onSelectItemListener) {
        this.onSelectItemListener = onSelectItemListener;
        return this;
    }

    public void sort(List<UserProfileEntity> userProfiles) {

    }

    private void filter(List<UserProfileEntity> userProfiles, String keyword) {
        this.keyword = keyword.toUpperCase();
        if (Strings.isNullOrEmpty(this.keyword)) {
            this.keyword = "";
        }

        this.userProfiles.clear();
        for (UserProfileEntity profile : userProfiles) {
            String name = !Strings.isNullOrEmpty(profile.getAlias()) ? profile.getAlias() : profile.getNickName();
            try {
                if (!this.userId.equals(profile.getId()) && (name.toUpperCase().contains(this.keyword) || Strings.isNullOrEmpty(this.keyword))) {
                    this.userProfiles.add(profile);
                }
            } catch (Exception ignored) {
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public MentionSelectAdapter reset() {
        this.selectSet.clear();
        sort(this.tmpUserProfiles);
        filter(this.tmpUserProfiles, this.keyword);
        notifyDataSetChanged();
        return this;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshData() {
        sort(this.tmpUserProfiles);
        filter(this.tmpUserProfiles, this.keyword);
        notifyDataSetChanged();
    }

    public void test() {
        for (UserProfileEntity profileEntity : this.userProfiles) {
            onSelectItemListener.onSelect(profileEntity, 0, true);
        }
    }

    public interface OnSelectItemListener<T> {
        void onSelect(T t, int position, boolean needCalculatePosition);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ItemMentionSelectBinding binding;

        public ViewHolder(ItemMentionSelectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
