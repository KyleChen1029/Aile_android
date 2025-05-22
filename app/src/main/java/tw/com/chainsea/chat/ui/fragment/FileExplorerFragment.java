package tw.com.chainsea.chat.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import com.google.common.collect.Lists;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tw.com.chainsea.android.common.file.FileHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.messagekit.lib.FileUtil;
import tw.com.chainsea.chat.messagekit.lib.Global;
import tw.com.chainsea.chat.messagekit.lib.MToolBox;
import tw.com.chainsea.chat.ui.adapter.FileExplorerAdapter;
import tw.com.chainsea.chat.ui.adapter.entity.FileDisData;


/**
 * FileExplorerFragment
 * Created by 90Chris on 2015/3/12.
 */
public class FileExplorerFragment extends ListFragment implements View.OnClickListener, FileExplorerAdapter.Listenner {
    String m_RootPath;
    ArrayList<String> selectedFiles = new ArrayList<>();
    Map<String, List<FileDisData>> filesMap = new HashMap<>();
    List<FileDisData> curFileList = null;
    TextView tvSendButton;
    View mView = null;
    int maxFileNum = 5;

    public static FileExplorerFragment newInstance(int limit) {
        FileExplorerFragment fragment = new FileExplorerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BundleKey.LIMIT_MAX.key(), limit);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_RootPath = MToolBox.getSdcardPath();
        FileUtil.setCurPath(m_RootPath);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            maxFileNum = bundle.getInt(BundleKey.LIMIT_MAX.key());
        }
        mView = inflater.inflate(R.layout.fragment_file_explorer, container, false);
        setListAdapter(new FileExplorerAdapter(getActivity(), getFileList(m_RootPath)));
        ((FileExplorerAdapter) getListAdapter()).setListenner(this);
        tvSendButton = (TextView) mView.findViewById(R.id.explorer_send);
        tvSendButton.setOnClickListener(this);
        return mView;
    }

    private List<FileDisData> getFileList(String dirPath) {
        List<FileDisData> fileModels;
        CELog.d("currentDirFileName" + FileUtil.getCurDirFileNames());
        if (FileUtil.getCurDirFileNames() == null) {
            return Lists.newArrayList();
        }
        if (null == filesMap.get(dirPath)) {
            fileModels = new ArrayList<>();
            for (String filePath : FileUtil.getCurDirFileNames()) {
                if (!FileHelper.getFileName(filePath).startsWith(".")) {
                    if (FileUtil.getFileType(filePath) == Global.FileType_Dir || isFormatOk(filePath)) {
                        fileModels.add(new FileDisData(filePath, false));
                    }
                }
            }
            filesMap.put(dirPath, fileModels);
        } else {
            fileModels = filesMap.get(dirPath);
        }
        curFileList = fileModels;
        return curFileList;
    }

    public void updateFileList(String a_DirPath) {
        boolean l_Rc = FileUtil.setCurPath(a_DirPath);
        if (!l_Rc) {
            return;
        }
        ((FileExplorerAdapter) getListAdapter()).setFileNames(getFileList(a_DirPath));
        ((FileExplorerAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    @SuppressLint("NotifyDataSetChanged")
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String l_FilePath = curFileList.get(position).getFilePath();
        int l_Type = FileUtil.getFileType(l_FilePath);
        if (l_Type == Global.FileType_Dir) {
            updateFileList(l_FilePath);
        } else {
//            if (!isSizeOK(l_FilePath)) {
//                return;
//            }
            if (!isFormatOk(l_FilePath)) {
                return;
            }
            if (curFileList.get(position).isChecked()) {
                curFileList.get(position).setChecked(false);
                selectedFiles.remove(l_FilePath);
            } else if (selectedFiles.size() < maxFileNum) {
                curFileList.get(position).setChecked(true);
                selectedFiles.add(l_FilePath);
            } else {
                Toast.makeText(getActivity(), getString(R.string.have_selected_files, maxFileNum + ""), Toast.LENGTH_SHORT).show();
            }
            ((FileExplorerAdapter) Objects.requireNonNull(getListAdapter())).notifyDataSetChanged();
            tvSendButton.setEnabled(!selectedFiles.isEmpty());
            tvSendButton.setText(getString(R.string.send_file, "(" + selectedFiles.size() + "/" + maxFileNum + ")"));
        }
    }

    private final static int FILE_MAX_SIZE = 100;  // max size of file megabits

    boolean isSizeOK(String path) {
        int size = 0;
        try (FileInputStream fis = new FileInputStream(path)) {
            size = fis.available();
        } catch (Exception ignored) {
        }
        if (0 == size) {
            Toast.makeText(getActivity(), "檔案錯誤", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (size > FILE_MAX_SIZE * 1024 * 1024) {
            Toast.makeText(getActivity(), getString(R.string.file_too_big, FILE_MAX_SIZE + ""), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    boolean isFormatOk(String path) {
        switch (FileUtil.getFileType(path)) {
            case Global.FileType_docx:
            case Global.FileType_doc:
            case Global.FileType_ppt:
            case Global.FileType_pptx:
            case Global.FileType_xls:
            case Global.FileType_xlsx:
            case Global.FileType_pdf:
            case Global.FileType_Jpg:
            case Global.FileType_jpeg:
            case Global.FileType_bmp:
            case Global.FileType_txt:
            case Global.FileType_mp3:
            case Global.FileType_wav:
            case Global.FileType_3gp:
            case Global.FileType_Png:
            case Global.FileType_mp4:
            case Global.FileType_m4a:
            case Global.FileType_aac:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.explorer_send) {
            getActivity().setResult(Activity.RESULT_OK,
                new Intent().putStringArrayListExtra(BundleKey.FILE_PATH_LIST.key(), selectedFiles));
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
        }
    }

    @Override
    public void onCheckedChanged(int pos, boolean isChecked) {
        Log.d("onCheckedChanged", "pos:" + pos + " Checked:" + isChecked);
        String l_FilePath = curFileList.get(pos).getFilePath();
        int l_Type = FileUtil.getFileType(l_FilePath);
        if (l_Type == Global.FileType_Dir) {
//            updateFileList(l_FilePath);
            return;
        }
        if (!isFormatOk(l_FilePath)) {
            return;
        }
        FileDisData data = curFileList.get(pos);
        data.setChecked(isChecked);
        if (data.isChecked()) {
            if (selectedFiles.size() < maxFileNum) {
                if (!selectedFiles.contains(l_FilePath)) {
                    selectedFiles.add(l_FilePath);
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.have_selected_files, maxFileNum + ""), Toast.LENGTH_SHORT).show();
            }
        } else {
            selectedFiles.remove(l_FilePath);
        }

        tvSendButton.setEnabled(!selectedFiles.isEmpty());
        tvSendButton.setText(getString(R.string.send_file, "(" + selectedFiles.size() + "/" + maxFileNum + ")"));
    }
}
