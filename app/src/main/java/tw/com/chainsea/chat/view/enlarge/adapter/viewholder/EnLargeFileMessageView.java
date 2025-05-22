package tw.com.chainsea.chat.view.enlarge.adapter.viewholder;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;
import com.google.common.primitives.Ints;

import java.io.File;
import java.text.DecimalFormat;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.file.FileHelper;
import tw.com.chainsea.android.common.text.KeyWordHelper;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.content.FileContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemEnLargeFileMessageBinding;
import tw.com.chainsea.chat.messagekit.enums.FileType;
import tw.com.chainsea.chat.util.DownloadUtil;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.base.EnLargeMessageBaseView;

/**
 * current by evan on 2020-04-15
 *
 * @author Evan Wang
 * @date 2020-04-15
 */
public class EnLargeFileMessageView extends EnLargeMessageBaseView<FileContent> {
    ItemEnLargeFileMessageBinding enLargeFileMessageBinding;

    private String downloadDir = DownloadUtil.INSTANCE.getDownloadFileDir();

    public EnLargeFileMessageView(@NonNull ItemEnLargeFileMessageBinding binding) {
        super(binding.getRoot());
        enLargeFileMessageBinding = binding;
    }

    @Override
    public void onBind(MessageEntity entity, FileContent fileContent, int position) {

        String fileTyle = FileHelper.getFileTyle(fileContent.getName());
        FileType fileType = FileType.of(fileTyle);

        String progress = fileContent.getProgress();
        MessageStatus status = entity.getStatus();

        enLargeFileMessageBinding.tvFileTypeName.setText(fileType.getName());
        String size = formatSizeDisplay(fileContent.getSize());
        enLargeFileMessageBinding.progress.setProgress(0);
//        civFileIcon.setBorder(0, fileType.getDrawable());
        enLargeFileMessageBinding.civFileIcon.setImageResource(fileType.getDrawable());
        enLargeFileMessageBinding.tvFileStatus.setText("");


        enLargeFileMessageBinding.tvFileName.setText(KeyWordHelper.matcherSearchBackground(0xFFFFF039, fileContent.getName(), ""), TextView.BufferType.NORMAL);
        enLargeFileMessageBinding.tvFileSize.setText(size);

        File file = null;
        if (!Strings.isNullOrEmpty(fileContent.getAndroid_local_path())) {
            file = new File(fileContent.getAndroid_local_path());
        }
        // getAndroidLocalPath == null
        String downloadPath = downloadDir + entity.getSendTime() + "_" + fileContent.getName();
        File downloadFile = new File(downloadPath);

        if (isRightMessage(entity)) {
            switch (status) {
                case READ:
                case RECEIVED:
                case SUCCESS:
                    enLargeFileMessageBinding.tvFileStatus.setText("已送出");
                    if (file != null && !file.exists() && !downloadFile.exists()) {
                        enLargeFileMessageBinding.tvFileStatus.setText("未下載");
                    }
                    if (!Strings.isNullOrEmpty(progress)) {
                        int pgs = Ints.tryParse(progress);
                        if (pgs >= 100) {
                        } else {
                            enLargeFileMessageBinding.tvFileStatus.setText(R.string.warning_downloading);
                            enLargeFileMessageBinding.progress.setProgress(pgs);
                        }
                    }
                    break;
                case SENDING:
                    enLargeFileMessageBinding.tvFileStatus.setText(R.string.warning_uploading);
                    if (!Strings.isNullOrEmpty(progress)) {
                        int pgs = Ints.tryParse(progress);
                        if (pgs >= 100) {
                            enLargeFileMessageBinding.progress.setProgress(0);
                            enLargeFileMessageBinding.tvFileStatus.setText("已送出");
                        } else {
                            enLargeFileMessageBinding.progress.setProgress(pgs);
                        }
                    }
                    break;
                case FAILED:
                case ERROR:
                default:
                    enLargeFileMessageBinding.tvFileStatus.setText("送出失敗");
                    break;
            }
        } else {
            if (downloadFile.exists()) {
                enLargeFileMessageBinding.tvFileStatus.setText("已下載");
                enLargeFileMessageBinding.progress.setProgress(0);
            } else {
                enLargeFileMessageBinding.tvFileStatus.setText("未下載");
                enLargeFileMessageBinding.progress.setProgress(0);
            }
            if (!Strings.isNullOrEmpty(progress)) {
                enLargeFileMessageBinding.tvFileStatus.setText(R.string.warning_downloading);
                if (!Strings.isNullOrEmpty(progress)) {
                    int pgs = Ints.tryParse(progress);
                    if (pgs >= 100) {
                        enLargeFileMessageBinding.progress.setProgress(0);
                        enLargeFileMessageBinding.tvFileStatus.setText("已下載");
                    } else {
                        enLargeFileMessageBinding.progress.setProgress(pgs);
                    }
                }
            }
        }

    }

    public static String formatSizeDisplay(long size) {
        String sizeString;
        DecimalFormat df = new DecimalFormat("#.0");
        if (size < 1024) {
            sizeString = size + "B";
        } else if (size < 1048576) {
            sizeString = df.format(size / 1024.0) + "K";
        } else {
            sizeString = df.format(size / 1048576.0) + "M";
        }
        return sizeString;
    }
}
