package tw.com.chainsea.chat.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tw.com.chainsea.ce.sdk.bean.SearchBean;
import tw.com.chainsea.chat.R;

/**
 * Created by Fleming on 2017/1/18.
 */
public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {

    private OnItemClickListener mListener;
    private List<SearchBean> items;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_history_item_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    @SuppressLint("RecyclerView")
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.tvText.setText(items.get(position).getContent());
        holder.rlDelete.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemDelete(position);
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
        return items == null ? 0 : items.size();
    }

//    public void setListData(List<SearchBean> items) {
//        this.items = items;
//        notifyDataSetChanged();
//    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        RelativeLayout rlDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.text);
            rlDelete = itemView.findViewById(R.id.rl_delete);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onItemDelete(int position);
    }
}
