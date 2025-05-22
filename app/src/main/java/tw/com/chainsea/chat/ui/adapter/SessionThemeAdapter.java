package tw.com.chainsea.chat.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.custom.view.image.CircleImageView;


/**
 * MessageListAdapter
 * Created by 90Chris on 2015/7/7.
 */
public class SessionThemeAdapter extends RecyclerView.Adapter<SessionThemeAdapter.ViewHolder> {
    private OnItemClickListener mListener;
//    private Context mContext;

    public SessionThemeAdapter() {
//        mContext = context;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session_theme, parent, false);
        return new ViewHolder(view);

    }

    @Override
    @SuppressLint("RecyclerView")
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mMsgTheme.setOnClickListener(v -> mListener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mMsgThemeContent;
        CircleImageView mMsgThemeAvatar;
        private final RelativeLayout mMsgTheme;

        public ViewHolder(View itemView) {
            super(itemView);
            mMsgTheme = itemView.findViewById(R.id.msg_theme);
            mMsgThemeAvatar = itemView.findViewById(R.id.msg_theme_avatar);
            mMsgThemeContent = itemView.findViewById(R.id.msg_theme_content);
        }

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
