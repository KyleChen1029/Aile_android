package tw.com.chainsea.chat.messagekit.theme.viewholder;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.common.base.Strings;
import com.google.common.primitives.Ints;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Objects;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.file.FileHelper;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.content.FileContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.messagekit.enums.FileType;
import tw.com.chainsea.chat.messagekit.theme.viewholder.base.ThemeMessageBubbleView;
import tw.com.chainsea.chat.util.DownloadUtil;


/**
 * FileMessageView
 * Created by Fleming on 2016/5/22.
 */
public class FileThemeMessageView extends ThemeMessageBubbleView {
    private ConstraintLayout rootCL;
    private ImageView iconImage;
    private TextView fileTypeNameTV, fileName, fileSize, tvSign;
    private FileContent fileContent;

    public FileThemeMessageView(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_file;
    }

    @Override
    protected void inflateContentView() {
        rootCL = findView(R.id.rootCL);
        iconImage = findView(R.id.fileIconCIV);
        fileName = findView(R.id.fileNameTV);
        fileSize = findView(R.id.fileSizeTV);
        tvSign = findView(R.id.fileStatusTV);
        fileTypeNameTV = findView(R.id.fileTypeNameTV);
    }

    @Override
    protected boolean showName() {
        return !isRightMessage();
    }

    @Override
    protected int leftBackground() {
        return R.drawable.msgkit_trans_bg;
    }

    @Override
    protected int rightBackground() {
        return R.drawable.msgkit_trans_bg;
    }

    @Override
    protected void bindContentView() {

        int rootWidth = UiHelper.dip2px(getContext(), 200);
        ViewGroup.LayoutParams params = rootCL.getLayoutParams();
        params.width = rootWidth;
        rootCL.setLayoutParams(params);
        rootCL.setMaxWidth(rootWidth);
        fileContent = (FileContent) getMsg().content();

        String fileTyle = FileHelper.getFileTyle(fileContent.getName());
        FileType fileType = FileType.of(fileTyle);

        String progress = fileContent.getProgress();
        MessageStatus status = getMsg().getStatus();
        String size = formatSizeDisplay(fileContent.getSize());

        File file = null;
        if (!Strings.isNullOrEmpty(fileContent.getAndroid_local_path())) {
            file = new File(fileContent.getAndroid_local_path());
        }

        iconImage.setImageResource(fileType.getDrawable());

        String downloadPath = downloadDir + getMsg().getSendTime() + "_" + fileContent.getName();
        File downloadFile = new File(downloadPath);

//        progressBar.setProgress(0);
//        iconImage.setBorder(0, fileType.getDrawable());
        tvSign.setText("");
        fileTypeNameTV.setText(fileType.getName());

        if (isRightMessage()) {
            switch (Objects.requireNonNull(status)) {
                case READ:
                case RECEIVED:
                case SUCCESS:
                    tvSign.setText("已送出");
                    if (file != null && !file.exists() && !downloadFile.exists()) {
                        tvSign.setText("未下載");
                    } else if (progress != null) {
                        Integer pgs = Ints.tryParse(progress);
                        if (pgs != null && pgs < 100) {
                            tvSign.setText("正在下載");
                        }
                    }
                    break;
                case SENDING:
                    tvSign.setText("正在上傳");
                    break;
                case FAILED:
                case ERROR:
                default:
                    tvSign.setText("送出失敗");
                    break;
            }
        } else {
            tvSign.setText(downloadFile.exists() ? "已下載" : "未下載");
        }


//        fileTypeNameTV.setText(fileType.getName());
//        progressBar.setProgress(0);
//        fileIconCIV.setBorder(0, fileType.getDrawable());
//        fileStatusTV.setText("");

//        File file = new File(format.getAndroidLocalPath());
//        String downloadPath = downloadDir + getMessage().getTime() + "_" + format.getName();
//        File downloadFile = new File(downloadPath);


//        String localPath = getMsg().getLocalPath();
//        filePath = TextUtils.isEmpty(localPath) ? downloadDir + msg.getId() + "_" + fileMsgFormat.getAndroidLocalPath() : localPath;
//        int iconId = MToolBox.getFileImage(FileUtil.getNoDirType(filePath));
//        iconImage.setImageDrawable(ContextCompat.getDrawable(getContext(), iconId));
//        String name;
//        String size;
//        if (isRightMessage()) {
////            name = fileMsgFormat.getAndroidLocalPath();
//            size = formatSizeDisplay(fileMsgFormat.getSize());
//            tvSign.setText(getContext().getString(R.string.has_sent));
//        } else {
////            name = fileMsgFormat.getAndroidLocalPath();
//            size = formatSizeDisplay(fileMsgFormat.getSize());
//            File file = new File(filePath);
//            try {
//                localFileSize = FileHelper.getFileSize(file);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            if (file.exists()) {
//                tvSign.setText(getContext().getString(R.string.downloaded));
//            } else {
//                tvSign.setText(getContext().getString(R.string.not_downloaded));
//            }
//        }
        fileName.setText(fileContent.getName());
        fileName.setTextColor(Color.WHITE);
        fileSize.setText(size);
        fileSize.setTextColor(Color.WHITE);
        tvSign.setTextColor(Color.WHITE);
    }

//    @Override
//    protected boolean onBubbleLongClicked(float pressX, float pressY) {
////        showPopup((int) pressX, (int) pressY);
//        return true;
//    }

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

//    @Override
//    protected void onBubbleClicked() {


//        onListDilogItemClickListener.locationMsg(msg);
//        File file = new File(filePath);
//
//        if (file.exists()) {
//            FileHelper.openFile(filePath, getContext());
//        } else {
//            try {
//                startDownloadFile();
//            } catch (Exception e) {
//                e.printStackTrace();
//                getMessage().setStatus(MessageStatus.RECEIVED);
//                CELog.e("download file failed. reason = " + e.getMessage());
//            }
//        }
//    }

    String downloadDir = DownloadUtil.INSTANCE.getDownloadFileDir();

//    private void showPopup(int pressX, int pressY) {
//        final MessageEntity msg = getMsg();
//        final List<String> popupMenuItemList = new ArrayList<>();
//        popupMenuItemList.clear();
//        if (MessageStatus.FAILED.equals(msg.getStatus())) {
//            popupMenuItemList.add(getContext().getString(R.string.retry));
//        } else {
//            popupMenuItemList.add(getContext().getString(R.string.transpond));
//        }
//
//        popupMenuItemList.add(getContext().getString(R.string.item_del));
//        PopupList popupList = new PopupList(view.getContext());
//        popupList.showPopupListWindow(view, pressX, pressY, popupMenuItemList, new PopupList.PopupListListener() {
//            @Override
//            public boolean showPopupList(View adapterView, View contextView) {
//                return true;
//            }
//
//            @Override
//            public void onPopupListClick(View contextView, int position) {
//                String s = popupMenuItemList.get(position);
//                switch (s) {
//                    case "轉發":
//                        onListDilogItemClickListener.tranSend(msg);
//                        break;
//                    case "重發":
//                        onListDilogItemClickListener.retry(msg);
//                        break;
//                    case "刪除":
//                        onListDilogItemClickListener.delete(msg);
//                        break;
//                    case "撤回":
//                        onListDilogItemClickListener.retractMsg(msg);
//                        break;
//                }
//            }
//        });
//    }


    @Override
    public void onClick(View v, MessageEntity message) {
        if (onListDilogItemClickListener != null) {
            onListDilogItemClickListener.locationMsg(msg);
        }
    }

    @Override
    public void onDoubleClick(View v, MessageEntity message) {

    }

    @Override
    public void onLongClick(View v, float x, float y, MessageEntity message) {

    }
}
