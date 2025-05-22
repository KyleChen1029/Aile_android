package tw.com.chainsea.chat.messagekit.child.viewholder;

import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import java.io.File;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.client.helper.ClientsHelper;
import tw.com.chainsea.android.common.client.type.Media;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.voice.VoiceHelper;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.messagekit.lib.AnimationDrawableManager;
import tw.com.chainsea.chat.messagekit.lib.AudioLib;
import tw.com.chainsea.chat.util.DownloadUtil;


/**
 * VoiceMessageView
 * Created by Andy on 2016/5/11.
 */
public class ChildVoiceMessageView extends ChildMessageBubbleView<VoiceContent> {

    //    private VoiceMsgFormat format;
    private VoiceContent voiceContent;
    private TextView voiceView;
    private AnimationDrawable mCurrentVoiceAnim;
    private AnimationDrawable animationDrawable;
    private String audioSavePath;
    private final String downloadDir = DownloadUtil.INSTANCE.getDownloadFileDir();
    private tw.com.chainsea.chat.databinding.MsgkitVoiceBinding binding;

    public ChildVoiceMessageView(@NonNull ViewBinding binding) {
        super(binding);
    }

    @Override
    protected boolean showName() {
        return !isRightMessage();
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_voice;
    }

    @Override
    protected void bindView(View itemView) {
        binding = tw.com.chainsea.chat.databinding.MsgkitVoiceBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
    }

    @Override
    protected void bindContentView(VoiceContent v) {
        this.voiceContent = (VoiceContent) getMessage().content();
        if (isRightMessage()) {
            binding.msgVoiceRightContent.setVisibility(View.VISIBLE);
            binding.msgVoiceLeftContent.setVisibility(View.GONE);
            binding.messageVoiceDot.setVisibility(View.INVISIBLE);
            voiceView = binding.msgVoiceRightContent;
            voiceContent.setRead(true);
        } else {
            binding.msgVoiceRightContent.setVisibility(View.GONE);
            binding.msgVoiceLeftContent.setVisibility(View.VISIBLE);
            voiceView = binding.msgVoiceLeftContent;
            if (voiceContent.isRead()) {
                binding.messageVoiceDot.setVisibility(View.INVISIBLE);
            } else {
                binding.messageVoiceDot.setVisibility(View.VISIBLE);
            }
        }

        voiceView.setText(VoiceHelper.strDuration(voiceContent.getDuration() * 1000));
    }


//    private boolean validateMicAvailability() {
//        Boolean available = true;
//        AudioRecord recorder =
//                new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
//                        AudioFormat.CHANNEL_IN_MONO,
//                        AudioFormat.ENCODING_DEFAULT, 44100);
//        try {
//            if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
//                available = false;
//
//            }
//
//            recorder.startRecording();
//            if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
//                recorder.stop();
//                available = false;
//
//            }
//            recorder.stop();
//        } finally {
//            recorder.release();
//            recorder = null;
//        }
//
//        return available;
//    }

    private void playRight() {
        audioSavePath = downloadDir + getMessage().getId() + ".amr";
        animationDrawable = (AnimationDrawable) voiceView.getCompoundDrawables()[2];
        //让所有自己发的語音消息都从cache中读取，如果没有，则从新下载
        String audioCachePath = AudioLib.getInstance(getContext()).getAudioCachePath(getContext())
                + File.separator + getMessage().getId() + ".amr";
        File file = new File(audioCachePath);
        if (file.exists()) {
            startPlayVoice(audioCachePath);
        } else {
            try {
                startDownloadVoice();
            } catch (Exception e) {
                CELog.e("download file failed. reason = " + e.getMessage());
            }
        }
    }

    public void playLeft() {
        audioSavePath = downloadDir + getMessage().getId() + ".amr";
        voiceContent.setRead(true);
        if (this.onMessageControlEventListener != null) {
            this.onMessageControlEventListener.onContentUpdate(getMessage().getId(), voiceContent.getClass().getName(), voiceContent.toStringContent());
        }
        animationDrawable = (AnimationDrawable) voiceView.getCompoundDrawables()[0];
        if (voiceContent.isDownLoad()) {
            startPlayVoice(audioSavePath);
        } else {
            try {
                startDownloadVoice();
            } catch (Exception e) {
                CELog.e("download file failed. reason = " + e.getMessage());
            }
        }
    }

    private void startDownloadVoice() throws Exception {

        File dir = new File(downloadDir);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new Exception("mkdir failed.");
            }
        }

        File file = new File(audioSavePath);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new Exception(" create file failed.");
            }
        }
        String url = voiceContent.getUrl();

        String sToken = TokenPref.getInstance(getContext()).getTokenId();

        ClientsHelper.post(false).execute(url + "?tokenId=" + sToken, Media.OCTET_STREAM.get(), "", new tw.com.chainsea.android.common.client.callback.impl.FileCallBack(file.getParent(), file.getName(), false) {

            @Override
            public void onSuccess(String resp, File file) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    voiceContent.setDownLoad(true);
                    if (onMessageControlEventListener != null) {
                        onMessageControlEventListener.onContentUpdate(getMessage().getId(), voiceContent.getClass().getName(), voiceContent.toStringContent());
                    }
                    startPlayVoice(audioSavePath);
                });
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                CELog.e("download file, failed: " + e.toString());
                CELog.e("download file, failed: " + e);
                if (isRightMessage()) {
                    getMessage().setStatus(MessageStatus.FAILED);
                }
            }

            @Override
            public void progress(float progress, long total) {
                super.progress(progress, total);
                CELog.d(String.valueOf(progress));
            }
        });


//        OkHttpUtils
//                .get()
//                .url(url + "?tokenId=" + sToken)
//                .build()
//                .execute(new FileCallBack(file.getParent(), file.getName()) {
//
//                    @Override
//                    public void onError(Call call, Exception e, int id) {
//                        CELog.e("download file, failed: " + e.toString());
//                        if (isRightMessage()) {
//                            getMessage().setStatus(MessageStatus.FAILED);
//                        }
//                    }
//
//                    @Override
//                    public void onResponse(File response, int id) {
//                        voiceContent.setDownLoad(true);
//                        if (onMessageControlEventListener != null) {
//                            onMessageControlEventListener.onContentUpdate(getMessage().getId(), voiceContent.getClass().getName(), voiceContent.toStringContent());
//                        }
//                        startPlayVoice(audioSavePath);
//                    }
//
//                    @Override
//                    public void inProgress(float progress, long total, int id) {
//                        CELog.d(String.valueOf(progress));
//                    }
//
//                });
    }

    private void startPlayVoice(String path) {
        if (AudioLib.getInstance(getContext()).isPlaying(path)) {
            //if the mContent is playing, stop it
            AudioLib.getInstance(getContext()).stopPlay();
            animationDrawable.stop();
            animationDrawable.selectDrawable(0);
            if (mCurrentVoiceAnim != null) {
                mCurrentVoiceAnim.stop();
                mCurrentVoiceAnim.selectDrawable(0);
            }
        } else {
            //先停止頁面上所有語音動畫
            AnimationDrawableManager.stopAnimations();
            //stop the playing voice anim, start the click one
            if (mCurrentVoiceAnim != null) {
                mCurrentVoiceAnim.stop();
                mCurrentVoiceAnim.selectDrawable(0);
            }

            if (voiceContent.isRead()) {
                binding.messageVoiceDot.setVisibility(View.INVISIBLE);
            }

            if (!animationDrawable.isRunning()) {
                animationDrawable.start();
                AnimationDrawableManager.addDrawable(animationDrawable);
            }

            AudioLib.getInstance(getContext()).playAudio(path, isSuccess -> {
                animationDrawable.stop();
                animationDrawable.selectDrawable(0);
                AnimationDrawableManager.removeDrawable(animationDrawable);
                if (onMessageControlEventListener != null) {
                    onMessageControlEventListener.onPlayComplete(getMessage());
                }
            });
        }
        mCurrentVoiceAnim = animationDrawable;
    }

//    public static String getVoiceDisplay(boolean isSend, double voiceLength) {
//        String content = String.format(Locale.CHINESE, "''", voiceLength);
////        String content = String.format(Locale.CHINESE, "%d''", voiceLength);
//        if (voiceLength > 20) {
//            if (isSend) {
//                return String.format("%20s%s", "", content);
//            } else {
//                return String.format("%s%20s", content, "");
//            }
//        } else {
//            if (isSend) {
//                return String.format("%" + voiceLength + "s%s", "", content);
//            } else {
//                return String.format("%s%" + voiceLength + "s", content, "");
//            }
//        }
//    }


    private void showPopup(int pressX, int pressY) {
        final MessageEntity msg = getMessage();
        if (this.onMessageControlEventListener != null) {
            this.onMessageControlEventListener.onLongClick(msg, pressX, pressY);
        }

       /* final List<String> popupMenuItemList = new ArrayList<>();
        popupMenuItemList.clear();
        if (message.getStatus() == MessageStatus.FAILED) {
            popupMenuItemList.add(getContext().getString(R.string.retry));
        } else {
            popupMenuItemList.add(getContext().getString(R.string.transpond));
        }

        if (message.getSenderId().equals(UserPref.getInstance(getContext()).getUserId())) {
            int value = message.getStatus().getValue();
            ISession iSession = DBManager.getInstance().querySession(message.getSessionId());
            if (value > 0 && value != 2 && iSession.getTodoOverviewType() != SessionType.SERVICES
                    && iSession.getTodoOverviewType() != SessionType.SUBSCRIBE) {
                //TODO 1.5.3 隐藏撤回
       popupMenuItemList.add(getContext().getString(R.string.retractMsg));
            }
        }
        popupMenuItemList.add(getContext().getString(R.string.reply));
        popupMenuItemList.add(getContext().getString(R.string.item_del));
        PopupList popupList = new PopupList(view.getContext());
        popupList.showPopupListWindow(view, pressX, pressY, popupMenuItemList, new PopupList.PopupListListener() {
            @Override
            public boolean showPopupList(View adapterView, View contextView) {
                return true;
            }

            @Override
            public void onPopupListClick(View contextView, int position) {
                String s = popupMenuItemList.get(position);
                switch (s) {
                    case "轉發":
                        onListDilogItemClickListener.tranSend(message);
                        break;
                    case "重發":
                        onListDilogItemClickListener.retry(message);
                        break;
                    case "刪除":
                        onListDilogItemClickListener.delete(message);
                        break;
                    case "撤回":
                        onListDilogItemClickListener.retractMsg(message);
                        break;
                    case "回復":
                        onListDilogItemClickListener.replyText(message);
                        break;
                    default:
                        break;
                }
            }
        });*/
    }

    @Override
    public void onClick(View v, MessageEntity message) {
        super.onClick(v, message);
        if (isRightMessage()) {
            playRight();
        } else {
            playLeft();
        }
    }

    @Override
    public void onDoubleClick(View v, MessageEntity message) {
    }

    @Override
    public void onLongClick(View v, float x, float y, MessageEntity message) {
        showPopup((int) x, (int) y);
    }

}
