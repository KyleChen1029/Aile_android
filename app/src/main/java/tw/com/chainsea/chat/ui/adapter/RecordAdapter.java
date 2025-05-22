package tw.com.chainsea.chat.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import tw.com.chainsea.chat.R;

/**
 * Created by sunhui on 2017/10/30.
 */

public class RecordAdapter extends BaseAdapter {
    private List<String> mRecords;

    public RecordAdapter(List<String> records) {
        mRecords = records;
    }

    @Override
    public int getCount() {
        return mRecords == null ? 0 : mRecords.size();
    }

    @Override
    public String getItem(int position) {
        return mRecords.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.record = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.delete = (ImageView) convertView.findViewById(R.id.iv_del);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.record.setText(getItem(position));
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDeleteRecordListener.onDelete(position);
            }
        });
        return convertView;
    }

    private OnDeleteRecordListener mDeleteRecordListener;

    public void setDeleteRecordListener(OnDeleteRecordListener deleteRecordListener) {
        mDeleteRecordListener = deleteRecordListener;
    }

    public interface OnDeleteRecordListener {
        void onDelete(int position);
    }

    static class ViewHolder {
        ImageView delete;
        TextView record;
    }
}
