package tw.com.chainsea.chat.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.chat.R;

/**
 * Created by sunhui on 2017/5/14.
 */

public class CallMemberAdapter extends RecyclerView.Adapter<CallMemberAdapter.MemberViewHolder> {

    private List<UserProfileEntity> mDatas;
    private OnItemClickListener mLisetener;
    private Context mContext;

    public CallMemberAdapter(Context context, List<UserProfileEntity> datas) {
        mDatas = datas;
        mContext = context;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_member_view, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    @SuppressLint("RecyclerView")
    public void onBindViewHolder(@NonNull MemberViewHolder viewHolder, int position) {
        UserProfileEntity account = mDatas.get(position);
        String name = TextUtils.isEmpty(account.getAlias()) ? account.getNickName() : account.getAlias();
        viewHolder.mName.setText(name);
        try {
            Glide.with(mContext)
                .load(account.getAvatarId())
                .apply(new RequestOptions()
                    .placeholder(R.drawable.custom_default_avatar)
                    .error(R.drawable.custom_default_avatar)
                    .fitCenter())
                .into(((MemberViewHolder) viewHolder).memberIcon);
        } catch (Exception ignored) {
        }

        viewHolder.itemView.setOnClickListener(v -> mLisetener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {

        ImageView delBtn;
        ImageView ownerPic;
        ImageView memberIcon;
        TextView mName;

        public MemberViewHolder(View itemView) {
            super(itemView);
            memberIcon = itemView.findViewById(R.id.iv_avatar);
            ownerPic = itemView.findViewById(R.id.iv_owner);
            delBtn = itemView.findViewById(R.id.delete_icon);
            mName = (TextView) itemView.findViewById(R.id.member_name);

            ownerPic.setVisibility(View.GONE);
            delBtn.setVisibility(View.GONE);
        }
    }
}
