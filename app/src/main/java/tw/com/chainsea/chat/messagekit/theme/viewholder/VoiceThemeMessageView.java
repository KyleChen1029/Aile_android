package tw.com.chainsea.chat.messagekit.theme.viewholder;

import android.annotation.SuppressLint;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

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
import tw.com.chainsea.chat.messagekit.theme.viewholder.base.ThemeMessageBubbleView;
import tw.com.chainsea.chat.util.DownloadUtil;


/**
 * VoiceMessageView
 * Created by Andy on 2016/5/11.
 */
public class VoiceThemeMessageView extends ThemeMessageBubbleView {

    private ImageView ivVoiceDot;
    private TextView voiceContentRight;
    private TextView voiceContentLeft;
    //    private VoiceMsgFormat format;
    private VoiceContent voiceContent;
    private TextView voiceView;
    private AnimationDrawable mCurrentVoiceAnim;
    private AnimationDrawable animationDrawable;
    private String audioSavePath;
    private final String downloadDir = DownloadUtil.INSTANCE.getDownloadFileDir();

    public VoiceThemeMessageView(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    protected boolean showName() {
        return !isRightMessage();
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_reply_voice;
    }

    @Override
    protected void inflateContentView() {
        voiceContentRight = findView(R.id.msg_voice_right_content);
        voiceContentLeft = findView(R.id.msg_voice_left_content);
        ivVoiceDot = findView(R.id.message_voice_dot);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void bindContentView() {
        voiceContent = (VoiceContent) getMsg().content();
        if (isRightMessage()) {
            voiceContentRight.setVisibility(View.VISIBLE);
            voiceContentLeft.setVisibility(View.GONE);
            ivVoiceDot.setVisibility(View.INVISIBLE);
            voiceView = voiceContentRight;
            voiceContent.setRead(true);
        } else {
            voiceContentRight.setVisibility(View.GONE);
            voiceContentLeft.setVisibility(View.VISIBLE);
            voiceView = voiceContentLeft;
            if (voiceContent.isRead()) {
                ivVoiceDot.setVisibility(View.INVISIBLE);
            } else {
                ivVoiceDot.setVisibility(View.VISIBLE);
            }
        }
        voiceView.setText(VoiceHelper.strDuration(voiceContent.getDuration() * 1000) + "\"");
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

//    private void playRight() {
//        audioSavePath = downloadDir + getMsg().getId() + ".amr";
//        animationDrawable = (AnimationDrawable) voiceView.getCompoundDrawables()[2];
//        //让所有自己发的語音消息都从cache中读取，如果没有，则从新下载
//        String audioCachePath = AudioLib.getInstance(getContext()).getAudioCachePath(getContext())
//                + File.separator + getMsg().getId() + ".amr";
//        File file = new File(audioCachePath);
//        if (file.exists()) {
//            startPlayVoice(audioCachePath);
//        } else {
//            try {
//                startDownloadVoice();
//            } catch (Exception e) {
//                CELog.e("download file failed. reason = " + e.getMessage());
//            }
//        }
//    }

    public void playLeft() {
        audioSavePath = downloadDir + getMsg().getId() + ".amr";
        voiceContent.setRead(true);
        onListDilogItemClickListener.onContentUpdate(getMsg().getId(), voiceContent.getClass().getName(), voiceContent.toStringContent());
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
        CELog.d("start download " + url);

//        String sToken = TokenSave.getInstance(getContext()).getToken();
        String sToken = TokenPref.getInstance(getContext()).getTokenId();
//        ClientsHelper.ASYNC_GET.execute(url + "?tokenId=" + sToken, new FileCallBack(file.getParent(), file.getName()) {
//            @Override
//            public void onSuccess(File file) {
//                format.setDownloadStatus(true);
//                onAvatarListener.onContentUpdate(getMessage().getId(), format.getClass().getName(), format.toJsonString());
//                startPlayVoice(audioSavePath);
//            }
//
//            @Override
//            public void onFailure(Exception e, String errorMsg) {
//                CELog.e("download file, failed: " + e.toString());
//                if (isRightMessage()) {
//                    getMessage().setStatus(MessageStatus.FAILED);
//                }
//            }
//
//            @Override
//            public void progress(float progress, long total) {
//                CELog.d(String.valueOf(progress));
//            }
//        });


        ClientsHelper.post(false).execute(url + "?tokenId=" + sToken, Media.OCTET_STREAM.get(), "", new tw.com.chainsea.android.common.client.callback.impl.FileCallBack(file.getParent(), file.getName(), false) {

            @Override
            public void onSuccess(String resp, File file) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    voiceContent.setDownLoad(true);
                    onListDilogItemClickListener.onContentUpdate(getMsg().getId(), voiceContent.getClass().getName(), voiceContent.toStringContent());
                    startPlayVoice(audioSavePath);
                });
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                CELog.e("download file, failed: " + e.toString());
                if (isRightMessage()) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> getMsg().setStatus(MessageStatus.FAILED));
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
//                            getMsg().setStatus(MessageStatus.FAILED);
//                        }
//                    }
//
//                    @Override
//                    public void onResponse(File response, int id) {
//                        voiceContent.setDownLoad(true);
//                        onListDilogItemClickListener.onContentUpdate(getMsg().getId(), voiceContent.getClass().getName(), voiceContent.toStringContent());
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
                ivVoiceDot.setVisibility(View.INVISIBLE);
            }

            if (!animationDrawable.isRunning()) {
                animationDrawable.start();
                AnimationDrawableManager.addDrawable(animationDrawable);
            }

            AudioLib.getInstance(getContext()).playAudio(path, isSuccess -> {
                animationDrawable.stop();
                animationDrawable.selectDrawable(0);
                AnimationDrawableManager.removeDrawable(animationDrawable);
                onListDilogItemClickListener.onPlayComplete(msg);
            });
        }
        mCurrentVoiceAnim = animationDrawable;
    }


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
//        String userId = TokenPref.getInstance(getContext()).getUserId();
//        if (msg.getSenderId().equals(userId)) {
//            int value = msg.getStatus().getValue();
////            ChatRoomEntity iSession = ChatRoomReference.getInstance().findById(msg.getRoomId());
//            ChatRoomEntity iSession = ChatRoomReference.getInstance().findById2( userId, msg.getRoomId(), false, false, false, false, false);
//            if (value > 0 && value != 2 && !iSession.getType().equals(ChatRoomType.SERVICES) && !iSession.getType().equals(ChatRoomType.SUBSCRIBE)) {
//                //TODO 1.5.3 隐藏撤回
//                popupMenuItemList.add(getContext().getString(R.string.recover));
//            }
//        }
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


//    @Override
//    protected boolean onBubbleLongClicked(float pressX, float pressY) {
////        showListDialog();
////        showPopup((int) pressX, (int) pressY);
//        return true;
//    }
//
//    @Override
//    protected void onBubbleClicked() {
//        onListDilogItemClickListener.locationMsg(msg);
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
