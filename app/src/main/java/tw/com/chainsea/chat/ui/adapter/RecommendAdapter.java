package tw.com.chainsea.chat.ui.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.Recommend;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.R;


/**
 * MessageListAdapter
 * Created by 90Chris on 2015/7/7.
 */
public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.ViewHolder> {

    private List<Recommend> mRecommends;
    private OnItemClickListener mListener;
    private OnDelClickListenner mDelClickListenner;
    private Context mContext;

    public RecommendAdapter(Context context, List<Recommend> recommends) {
        mRecommends = recommends;
        mContext = context;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommend_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Recommend recommend = mRecommends.get(position);

        holder.mName.setText(recommend.getName());
        AvatarService.post(mContext, recommend.getAvatarUrl(), PicSize.SMALL, holder.mAvatar, R.drawable.custom_default_avatar);


        holder.mClose.setOnClickListener(v -> {
            if (mDelClickListenner != null) {
                mDelClickListenner.onDelClick(position);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRecommends == null ? 0 : mRecommends.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mAvatar;
        TextView mName;
        ImageView mClose;

        public ViewHolder(View itemView) {
            super(itemView);
            mAvatar = (ImageView) itemView.findViewById(R.id.recommend_avatar);
            mName = (TextView) itemView.findViewById(R.id.recommend_name);
            mClose = (ImageView) itemView.findViewById(R.id.recommend_close);
        }

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void setDelClickListenner(OnDelClickListenner delClickListenner) {
        mDelClickListenner = delClickListenner;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnDelClickListenner {
        void onDelClick(int position);
    }
}
