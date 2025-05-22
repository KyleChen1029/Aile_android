package tw.com.chainsea.chat.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.List;

import tw.com.chainsea.android.common.file.FileHelper;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.messagekit.lib.FileUtil;
import tw.com.chainsea.chat.messagekit.lib.Global;
import tw.com.chainsea.chat.messagekit.lib.MToolBox;
import tw.com.chainsea.chat.ui.adapter.entity.FileDisData;


public class FileExplorerAdapter extends BaseAdapter {

    private List<FileDisData> mFileModels;
    Activity mActivity;

    public FileExplorerAdapter(Activity activity, List<FileDisData> fileModels) {
        mActivity = activity;
        mFileModels = fileModels;
    }

    @Override
    public int getCount() {
        return mFileModels.size();
    }

    @Override
    public FileDisData getItem(int a_Index) {
        return mFileModels.get(a_Index);
    }

    @Override
    public long getItemId(int a_Pos) {
        return a_Pos;
    }

    public void setFileNames(List<FileDisData> fileModels) {
        if (fileModels == null) {
            mFileModels.clear();
            return;
        }
        mFileModels = fileModels;
    }

    @Override
    @SuppressLint("SetTextI18n")
    public View getView(final int pos, View view, ViewGroup parent) {
        if (mFileModels == null) {
            return null;
        }
        final ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = mActivity.getLayoutInflater().inflate(R.layout.item_file_explorer, parent, false);

            viewHolder.tvFileName = view.findViewById(R.id.explorer_name);
            viewHolder.cbCheckBox = view.findViewById(R.id.explorer_check);
            viewHolder.ivFileIcon = view.findViewById(R.id.explorer_logo);
            viewHolder.tvFileSize = view.findViewById(R.id.explorer_size);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        String l_FilePath = getItem(pos).getFilePath();
        String l_FileName = FileHelper.getFileName(l_FilePath);

        viewHolder.tvFileName.setText(l_FileName);

        int l_FileType = FileUtil.getFileType(l_FilePath);
        int l_ResourceID = MToolBox.getFileImage(l_FileType);

        Drawable l_Drawable = ResourcesCompat.getDrawable(mActivity.getResources(), l_ResourceID, null);
        viewHolder.ivFileIcon.setImageDrawable(l_Drawable);

        if (l_FileType == Global.FileType_Dir) {
            viewHolder.cbCheckBox.setVisibility(View.GONE);
            viewHolder.tvFileSize.setVisibility(View.GONE);
        } else {
            viewHolder.cbCheckBox.setVisibility(View.VISIBLE);
            viewHolder.tvFileSize.setVisibility(View.VISIBLE);
            try {
                FileInputStream fis = new FileInputStream(new File(l_FilePath));
                DecimalFormat df = new DecimalFormat("#.0");
                double size = fis.available();
                String sizeDis;
                if (size < 1024) {
                    sizeDis = size + "B";
                } else if (size < 1048576) {
                    sizeDis = df.format(size / 1024.0) + "K";
                } else {
                    sizeDis = df.format(size / 1048576.0) + "M";
                }
                viewHolder.tvFileSize.setText(sizeDis);
            } catch (Exception e) {
                viewHolder.tvFileSize.setText("0.0M");
            }
        }
        viewHolder.cbCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (listenner != null) {
                    listenner.onCheckedChanged(pos, isChecked);
                }
            }
        });
        viewHolder.cbCheckBox.setChecked(getItem(pos).isChecked());
        return view;
    }

    private static final class ViewHolder {
        public ImageView ivFileIcon;
        public TextView tvFileName;
        public CheckBox cbCheckBox;
        public TextView tvFileSize;
    }

    Listenner listenner;

    public interface Listenner {
        void onCheckedChanged(int pos, boolean isChecked);
    }

    public void setListenner(Listenner listenner) {
        this.listenner = listenner;
    }
}
