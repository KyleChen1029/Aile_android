package tw.com.chainsea.chat.pchat.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tw.com.chainsea.android.common.text.KeyWordHelper;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.http.ce.request.ServiceNumberSearchRequest;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.RvSearchHistoryItemViewBinding;

/**
 * tw.com.chainsea.agententerprise.ui.adapter
 * Created by andy on 17-2-10.
 */

public class SearchHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context ctx;
    private List<ServiceNumberSearchRequest.Resp.Item> mDatas;
    private static final int TYPE_HINT = 0;     //文字提示
    private static final int TYPE_VIPNC = 1;     //匹配到的公众号
    private String keyWord = "";

    public SearchHistoryAdapter() {

    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<ServiceNumberSearchRequest.Resp.Item> data) {
        this.mDatas = data;
        notifyDataSetChanged();
    }

    //返回当前位置的类型
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HINT;
        } else {
            return TYPE_VIPNC;
        }
    }


    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.ctx = parent.getContext();
        switch (viewType) {
            case TYPE_HINT:
                return new HintHolder(LayoutInflater.from(this.ctx).inflate(R.layout.rv_head_search_history_item_view, parent, false));
            case TYPE_VIPNC:
                RvSearchHistoryItemViewBinding binding = RvSearchHistoryItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new VipcnHolder(binding);
        }
        return null;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        switch (itemViewType) {
            case TYPE_HINT:
                break;
            case TYPE_VIPNC:
                VipcnHolder vipcnHolder = (VipcnHolder) holder;
                ServiceNumberSearchRequest.Resp.Item itemsBean = mDatas.get(position - 1);
                Spanned text = KeyWordHelper.matcherSearchTitle(this.ctx.getResources().getColor(R.color.key_word), itemsBean.getName(), keyWord);
                vipcnHolder.binding.txtVipcnName.setText(text);
                vipcnHolder.binding.txtVipcnDesc.setText(itemsBean.getDescription());
                if (position == mDatas.size() + 1) {
                    vipcnHolder.binding.line.setVisibility(View.GONE);
                }
                if (mListener != null) {
                    vipcnHolder.binding.rlItem.setOnClickListener(v -> mListener.onItemClick(position - 1));
                }
                AvatarService.post(this.ctx, itemsBean.getServiceNumberAvatarId(), PicSize.SMALL, vipcnHolder.binding.imgVipcnHead, R.drawable.custom_default_avatar);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size() + 1;
    }


    public SearchHistoryAdapter setKeyWord(String keyWord) {
        this.keyWord = keyWord;
        return this;
    }

    public static class HintHolder extends RecyclerView.ViewHolder {

        public HintHolder(View itemView) {
            super(itemView);
        }
    }

    public static class VipcnHolder extends RecyclerView.ViewHolder {

        private RvSearchHistoryItemViewBinding binding;

        public VipcnHolder(RvSearchHistoryItemViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
//            unbinder = ButterKnife.bind(this, itemView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }


}
