package tw.com.chainsea.chat.messagekit.main.viewholder;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.viewbinding.ViewBinding;

import java.io.File;
import java.net.URL;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.client.helper.ClientsHelper;
import tw.com.chainsea.android.common.client.type.Media;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.voice.VoiceHelper;
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.MsgkitVoiceBinding;
import tw.com.chainsea.chat.messagekit.lib.AnimationDrawableManager;
import tw.com.chainsea.chat.messagekit.lib.AudioLib;
import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageBubbleView;
import tw.com.chainsea.chat.util.DownloadUtil;


/**
 * VoiceMessageView
 * Created by Andy on 2016/5/11.
 */
public class VoiceMessageView extends MessageBubbleView<VoiceContent> {

    private VoiceContent voiceContent;
    private TextView voiceView;
    private AnimationDrawable mCurrentVoiceAnim;
    private AnimationDrawable animationDrawable;
    private String audioSavePath;
    private final String downloadDir = DownloadUtil.INSTANCE.getDownloadFileDir();

    ExoPlayer player;

    private final MsgkitVoiceBinding binding;

    public VoiceMessageView(@NonNull ViewBinding binding) {
        super(binding);
        this.binding = MsgkitVoiceBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
        getView(this.binding.getRoot());
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
    protected View getChildView() {
        return binding.getRoot();
    }


    @Override
    protected void bindContentView(VoiceContent v) {
        this.voiceContent = (VoiceContent) getMessage().content();
        if (getMessage().getFrom() == ChannelType.IG) {
            initPlayer(v.getUrl());
        }

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

    private void playRight() {
        String filename = new File(voiceContent.getUrl()).getName();
        String extension = filename.substring(filename.lastIndexOf(".") + 1);

        audioSavePath = downloadDir + getMessage().getId() + "." + extension;
        animationDrawable = (AnimationDrawable) voiceView.getCompoundDrawables()[2];
        //让所有自己发的語音消息都从cache中读取，如果没有，则从新下载
        String audioCachePath = AudioLib.getInstance(binding.getRoot().getContext()).getAudioCachePath(binding.getRoot().getContext())
            + File.separator + getMessage().getId() + "." + extension;
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
        try {
            URL url = new URL(voiceContent.getUrl());
            String path = url.getPath();
            String filename = new File(path).getName();
            String extension = filename.substring(filename.lastIndexOf(".") + 1);
            audioSavePath = downloadDir + getMessage().getId() + "." + extension;
            voiceContent.setRead(true);
            if (this.onMessageControlEventListener != null) {
                this.onMessageControlEventListener.onContentUpdate(getMessage().getId(), voiceContent.getClass().getName(), voiceContent.toStringContent());
            }
            animationDrawable = (AnimationDrawable) voiceView.getCompoundDrawables()[0];
            if (getMessage().getFrom() == ChannelType.IG) {
                startPlayAudioWithInstagram();
            } else if (new File(audioSavePath).exists()) {
                startPlayVoice(audioSavePath);
            } else {
                startDownloadVoice();
            }
        } catch (Exception e) {
            CELog.e("download file failed. reason = " + e.getMessage());
        }
    }

    private void initPlayer(String url) {
        player = new ExoPlayer.Builder(binding.getRoot().getContext()).build();
        MediaItem mediaItem = new MediaItem.Builder()
            .setUri(url)
            .build();

        player.setMediaItem(mediaItem);
        player.prepare();
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == ExoPlayer.STATE_READY) {
                    voiceView.setText(VoiceHelper.strDuration(player.getDuration()));
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
                if (isPlaying) {
                    startAudioAnimation();
                } else {
                    stopAudioAnimation();
                }
            }
        });
    }

    @Override
    public void releasePlayer() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    private void startPlayAudioWithInstagram() {
        if (player != null) {
            player.seekTo(0);
            player.play();
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

        String sToken = TokenPref.getInstance(binding.getRoot().getContext()).getTokenId();

        ClientsHelper.post(false).execute(url + "?tokenId=" + sToken, Media.OCTET_STREAM.get(), "", new tw.com.chainsea.android.common.client.callback.impl.FileCallBack(file.getParent(), file.getName(), false) {

            @Override
            public void onSuccess(String resp, File file) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    voiceContent.setDownLoad(true);
                    if (onMessageControlEventListener != null) {
                        onMessageControlEventListener.onContentUpdate(getMessage().getId(), voiceContent.getClass().getName(), voiceContent.toStringContent());
                    }
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(file);
                    mediaScanIntent.setData(contentUri);
                    binding.getRoot().getContext().sendBroadcast(mediaScanIntent);
                    startPlayVoice(audioSavePath);
                });
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                CELog.e("download file, failed: " + e.toString());
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

    private void startAudioAnimation() {
        //先停止頁面上所有語音動畫
        AnimationDrawableManager.stopAnimations();
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
    }

    private void stopAudioAnimation() {
        AudioLib.getInstance(binding.getRoot().getContext()).stopPlay();
        animationDrawable.stop();
        animationDrawable.selectDrawable(0);
        if (mCurrentVoiceAnim != null) {
            mCurrentVoiceAnim.stop();
            mCurrentVoiceAnim.selectDrawable(0);
        }
    }

    private void startPlayVoice(String path) {
        if (AudioLib.getInstance(binding.getRoot().getContext()).isPlaying(path)) {
            //if the mContent is playing, stop it
            stopAudioAnimation();
        } else {
            startAudioAnimation();
            AudioLib.getInstance(binding.getRoot().getContext()).playAudio(path, isSuccess -> {
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
