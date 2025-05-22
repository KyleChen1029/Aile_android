package tw.com.chainsea.chat.messagekit.main.viewholder;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.viewbinding.ViewBinding;

import com.google.common.base.Strings;
import com.google.common.primitives.Ints;
import com.luck.picture.lib.utils.ToastUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Objects;

import okhttp3.Call;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.client.callback.impl.FileCallBack;
import tw.com.chainsea.android.common.client.helper.ClientsHelper;
import tw.com.chainsea.android.common.file.FileHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.text.KeyWordHelper;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.content.FileContent;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.MsgkitFileBinding;
import tw.com.chainsea.chat.messagekit.enums.FileType;
import tw.com.chainsea.chat.messagekit.lib.FileUtil;
import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageBubbleView;
import tw.com.chainsea.chat.util.DownloadUtil;


/**
 * FileMessageView
 * Created by Fleming on 2016/5/22.
 */
public class FileMessageView extends MessageBubbleView<FileContent> {
    private final MsgkitFileBinding binding;
    private DownloadingStatus newestStatus = DownloadingStatus.UNDEF;
    private enum DownloadingStatus {
        UNDEF, SUCCESS, CANCELED, FAILURE
    }
    private final String downloadDir = DownloadUtil.INSTANCE.getDownloadFileDir();

    private Call downloadRequestCall = null;
    //file_message_icon_excel


    public FileMessageView(@NonNull ViewBinding binding) {
        super(binding);
        this.binding = MsgkitFileBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
        getView(this.binding.getRoot());
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_file;
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
    protected View getChildView() {
        return binding.getRoot();
    }

    @Override
    protected void bindContentView(FileContent fileContent) {
//        Log.i(TAG, JsonHelper.getInstance().toJson(format));
        int rootWidth = UiHelper.dip2px(binding.getRoot().getContext(), 200);
        ViewGroup.LayoutParams params = binding.rootCL.getLayoutParams();
        params.width = rootWidth;
        binding.rootCL.setLayoutParams(params);
        binding.rootCL.setMaxWidth(rootWidth);
        if (fileContent.getName() != null) {
            String fileTyle = FileHelper.getFileTyle(fileContent.getName());
            FileType fileType = FileType.of(fileTyle);
            binding.fileTypeNameTV.setText(fileType.getName());
            binding.fileIconCIV.setImageResource(fileType.getDrawable());
        } else {
            fileContent.setName("Unknown");
            binding.fileTypeNameTV.setVisibility(View.GONE);
            binding.fileIconCIV.setImageResource(R.drawable.file_message_icon_file);
        }

        binding.progressBar.setOnFileClickListener(() -> {
            if (!Strings.isNullOrEmpty(fileContent.getProgress())) {
                newestStatus = DownloadingStatus.CANCELED;
                canceledOrFailureHandle(getMessage(), fileContent, "", newestStatus);
                ToastUtils.showToast(binding.getRoot().getContext(), binding.getRoot().getContext().getString(R.string.canceled_downloading));
                if (downloadRequestCall != null) {
                    downloadRequestCall.cancel();
                }
                binding.progressBar.isCanceledLoading = false;
            }
        });


        String progress = fileContent.getProgress();
        MessageStatus status = getMessage().getStatus();
        String size = formatSizeDisplay(fileContent.getSize());
        binding.progressBar.setVisibility((Strings.isNullOrEmpty(progress) || "100".equals(progress)) ? View.GONE : View.VISIBLE);
        binding.fileStatusTV.setText("");

//        if (getMessage().isAnimator()) {
//            AnimatorHelper.shakeAnimation(itemView, true, (animator, statuss) -> {
//                if (statuss.equals(AnimatorHelper.Status.END)) {
//                    getMessage().setAnimator(false);
//                }
//            });
//        }

        binding.fileNameTV.setText(KeyWordHelper.matcherSearchBackground(0xFFFFF039, fileContent.getName(), getKeyword()), TextView.BufferType.NORMAL);
        binding.fileSizeTV.setText(size);
        File file = null;
        if(!Strings.isNullOrEmpty(fileContent.getAndroid_local_path())) {
            file = new File(fileContent.getAndroid_local_path());
        }
        // getAndroidLocalPath == null
        String downloadPath = DownloadUtil.INSTANCE.getDownloadFileDir() +fileContent.getName();
        File downloadFile = new File(downloadPath);

        getBackgroundImage();

        if (isRightMessage()) {
            switch (Objects.requireNonNull(status)) {
                case READ:
                case RECEIVED:
                case SUCCESS:
                    binding.fileStatusTV.setText("已送出");
                    if (file != null && !file.exists() && !downloadFile.exists()) {
                        binding.fileStatusTV.setText("未下載");
                    }
                    if (!Strings.isNullOrEmpty(progress)) {
                        Integer pgs = Ints.tryParse(progress);
                        if (pgs != null && pgs >= 100) {
                            binding.progressBar.setVisibility(View.GONE);
                            setBackgroundTint(false);
                        } else {
                            binding.fileStatusTV.setText("正在下載");
                            binding.progressBar.setProgress(pgs!=null ? pgs : 0);
                            setBackgroundTint(newestStatus != DownloadingStatus.CANCELED);
                        }
                    }
                    break;
                case SENDING:
                    binding.fileStatusTV.setText("正在上傳");
                    if (!Strings.isNullOrEmpty(progress)) {
                        Integer pgs = Ints.tryParse(progress);
                        if (pgs!=null && pgs >= 100) {
                            binding.progressBar.setProgress(0);
                            binding.progressBar.setVisibility(View.GONE);
                            setBackgroundTint(false);
                            binding.fileStatusTV.setText("已送出");
                        } else {
                            binding.progressBar.setProgress(pgs!=null ? pgs : 0);
                            binding.progressBar.setVisibility(View.VISIBLE);
                            setBackgroundTint(newestStatus != DownloadingStatus.CANCELED);

                        }
                    }
                    break;
                case IS_REMOTE:
                    if ((file != null && file.exists()) || downloadFile.exists()) {
                        binding.fileStatusTV.setText("已送出");
                    } else {
                        binding.fileStatusTV.setText("檔案不存在");
                    }
                    break;
                case FAILED:
                case ERROR:
                default:
                    binding.fileStatusTV.setText("送出失敗");
                    break;
            }
        } else {
            if (downloadFile.exists()) {
                binding.fileStatusTV.setText("已下載");
                binding.progressBar.setProgress(0);
                binding.progressBar.setVisibility(View.GONE);
                setBackgroundTint(false);
            } else {
                binding.fileStatusTV.setText("未下載");
                binding.progressBar.setProgress(0);
                binding.progressBar.setVisibility(View.GONE);
                setBackgroundTint(false);
            }
            if (!Strings.isNullOrEmpty(progress)) {
                binding.fileStatusTV.setText("正在下載");
                if (!Strings.isNullOrEmpty(progress)) {
                    Integer pgs = Ints.tryParse(progress);
                    if (pgs!=null && pgs >= 100) {
                        binding.progressBar.setProgress(0);
                        binding.progressBar.setVisibility(View.GONE);
                        binding.fileStatusTV.setText("已下載");
                        setBackgroundTint(false);
                    } else {
                        binding.progressBar.setProgress(pgs!=null ? pgs : 0);
                        binding.progressBar.setVisibility(View.VISIBLE);
                        setBackgroundTint(newestStatus!=DownloadingStatus.CANCELED);
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

    private void setBackgroundTint(boolean isLoading) {
        if(isLoading) {
            getBackgroundImage();
            ColorStateList tint = ColorStateList.valueOf(ContextCompat.getColor(binding.getRoot().getContext(), R.color.loading_background));
            ViewCompat.setBackgroundTintList(binding.rootCL, tint);
        } else {
            ViewCompat.setBackgroundTintList(binding.rootCL, null);
        }
    }

    private void getBackgroundImage() {
        binding.rootCL.setBackgroundResource(isRightMessage() ? R.drawable.bubble_send : R.drawable.bubble_receive);
    }


    public void startDownloadFile(MessageEntity message, FileContent fileContent, String filePath) throws Exception {
        fileContent.setProgress("0");
        fileContent.setDownload(true);
        File file = new File(filePath);
        String url = fileContent.getUrl();
        File dir = new File(downloadDir);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new Exception("mkdir failed.");
            }
        }
        String sToken = TokenPref.getInstance(binding.getRoot().getContext()).getTokenId();

        binding.progressBar.setProgress(0);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.fileStatusTV.setText("正在下載");
        setBackgroundTint(newestStatus!=DownloadingStatus.CANCELED);


        downloadRequestCall = ClientsHelper.buildDownloadGet(url + "?tokenId=" + sToken, new FileCallBack(file.getParent(), file.getName(), true) {
            @Override
            public void onSuccess(String resp, File file) {
                fileContent.setProgress(null);
                fileContent.setDownload(true);
                binding.progressBar.setProgress(0);
                binding.progressBar.setVisibility(View.GONE);
                binding.fileStatusTV.setText("已下載");
                fileContent.setProgress("100");
                setBackgroundTint(false);
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UPDATE_MESSAGE_STATUS, message));
                message.setContent(fileContent.toStringContent());
                if (onMessageControlEventListener != null) {
                    onMessageControlEventListener.onContentUpdate(getMessage().getId(), fileContent.getClass().getName(), fileContent.toStringContent());
                }
                //handling the behavior of cancel downloading
                binding.progressBar.isCanceledLoading = false;
                if(newestStatus == DownloadingStatus.CANCELED)
                    canceledOrFailureHandle(message, fileContent, "", newestStatus);

                newestStatus = DownloadingStatus.SUCCESS;
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                canceledOrFailureHandle(message, fileContent, errorMsg, newestStatus = DownloadingStatus.FAILURE);
            }
        }, (bytesRead, contentLength, done) -> {
            int progress = (fileContent.getSize() == 0)? 100 : (int) Math.abs(bytesRead * 100 / fileContent.getSize());
            fileContent.setProgress("" + progress);
            message.setContent(fileContent.toStringContent());
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UPDATE_MESSAGE_STATUS, message));
        });
    }

    private void canceledOrFailureHandle(MessageEntity message, FileContent fileContent, String e, DownloadingStatus status) {
        fileContent.setProgress(null);
        fileContent.setDownload(false);
        message.setContent(fileContent.toStringContent());
        binding.progressBar.setProgress(0);
        binding.progressBar.setVisibility(View.GONE);
        if(status == DownloadingStatus.CANCELED)
            binding.fileStatusTV.setText("未下載");
        else
            binding.fileStatusTV.setText("下載失敗");
        setBackgroundTint(false);
        if (onMessageControlEventListener != null) {
            onMessageControlEventListener.onContentUpdate(getMessage().getId(), fileContent.getClass().getName(), fileContent.toStringContent());
        }
        try {
            String downloadPath = downloadDir + message.getSendTime() + "_" + fileContent.getName();
            File downloadFile = new File(downloadPath);
            boolean isDeleted = downloadFile.delete();
            CELog.e("下載檔案失敗，刪除檔案成功");
        } catch (Exception e1) {
            CELog.e("下載檔案失敗，刪除檔案失敗", e);
        }
    }
    @Override
    public void onClick(View v, MessageEntity message) {
        super.onClick(v, message);
        File file = null;
        FileContent fileContent = (FileContent) getMessage().content();

        if(!Strings.isNullOrEmpty(fileContent.getAndroid_local_path())){
            file = new File(fileContent.getAndroid_local_path());
        } else {
            if(fileContent.getSize() == 0){  //影片防呆
                return;
            }
        }
        String downloadPath = DownloadUtil.INSTANCE.getDownloadFileDir() +fileContent.getName();
        File downloadFile = new File(downloadPath);

        if (isRightMessage()) {
            if (file != null && file.exists()) {
                try {
                    FileUtil.openFile(fileContent.getAndroid_local_path(), binding.getRoot().getContext());
                } catch (Exception e) {
                    CELog.e("download file failed. reason = " + e.getMessage());
                }
            } else if (downloadFile.exists()) {
                try {
                    FileUtil.openFile(downloadPath, binding.getRoot().getContext());
                } catch (Exception e) {
                    CELog.e("download file failed. reason = " + e.getMessage());
                }
            } else {
                try {
                    startDownloadFile(message, fileContent, downloadPath);
                } catch (Exception e) {
                    CELog.e("download file failed. reason = " + e.getMessage());
                    getMessage().setStatus(MessageStatus.RECEIVED);
                }
            }
        } else {
            if (downloadFile.exists()) {
                try {
                    FileUtil.openFile(downloadPath, binding.getRoot().getContext());
                } catch (Exception e) {
                    CELog.e("download file failed. reason = " + e.getMessage());
                }
            } else {

                try {
                    startDownloadFile(message, fileContent, downloadPath);
                } catch (Exception e) {
                    CELog.e("download file failed. reason = " + e.getMessage());
                    getMessage().setStatus(MessageStatus.RECEIVED);
                }
            }
        }
    }

//    private String getFileTyle(String path) {
//        int startIndex = path.lastIndexOf(46) + 1;
//        int endIndex = path.length();
//        return path.substring(startIndex, endIndex);
//    }

    @Override
    public void onDoubleClick(View v, MessageEntity message) {
//        onListDilogItemClickListener.enLarge(getMessage());
    }

    @Override
    public void onLongClick(View v, float x, float y, MessageEntity message) {
        if (onMessageControlEventListener != null) {
            onMessageControlEventListener.onLongClick(getMessage(), (int) x, (int) y);
        }
    }

    public interface OnFileClickListener {
        void performClick();
    }
}
