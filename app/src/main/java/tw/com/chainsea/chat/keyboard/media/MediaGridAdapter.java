package tw.com.chainsea.chat.keyboard.media;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import tw.com.chainsea.chat.R;


public class MediaGridAdapter extends BaseAdapter {
    private ArrayList<MediaBean> mediaModels;
    Context mContext;
    int mSize = 0;

    /**
     * MediaGridAdapter
     *
     * @param context     context
     * @param mediaModels data
     */
    public MediaGridAdapter(Context context, ArrayList<MediaBean> mediaModels, int size) {
        this.mContext = context;
        this.mediaModels = mediaModels;
        mSize = size;
    }

    public void resizeItem(int size) {
        mSize = size;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.media_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            viewHolder.ivImage.setLayoutParams(new LinearLayout.LayoutParams(mSize, mSize));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.ivImage.setImageDrawable(ContextCompat.getDrawable(mContext, getItem(position).getDrawableId()));
        viewHolder.tvText.setText(getItem(position).getText());
        convertView.setOnClickListener(v -> getItem(position).getMediaListener().onMediaClick(getItem(position).getId()));

        return convertView;
    }

    static class ViewHolder {
        public ImageView ivImage;
        public TextView tvText;

        public ViewHolder(View view) {
            ivImage = (ImageView) view.findViewById(R.id.media_item_image);
            tvText = (TextView) view.findViewById(R.id.media_item_text);
        }
    }

    @Override
    public int getCount() {
        return mediaModels.size();
    }

    @Override
    public MediaBean getItem(int position) {
        return mediaModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
