package tw.com.chainsea.chat.messagekit.lib;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import tw.com.chainsea.android.common.log.CELog;

/**
 * AudioLib
 * Created by 90Chris on 2015/12/2.
 */
public class AudioLib {
    private static AudioLib sAudioLib = null;
    private MediaRecorder recorder;
    private String mPath;

    private int mPeriod = 0;
    private static final int MIN_LENGTH = 2;


    private AudioManager mAudioManager;
//    private final BluetoothAdapter ba;


    public static AudioLib getInstance(Context context) {
        if (sAudioLib == null) {
            sAudioLib = new AudioLib(context);
        }
        return sAudioLib;
    }

    public AudioLib(Context context) {
        new Timer().schedule(new AudioTimerTask(), 0, 1000);

        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

//        ba = BluetoothAdapter.getDefaultAdapter();
//        if (ba == null) {
////         isBlueCon = -1;     //error
//            //return -1;
//        } else if (ba.isEnabled()) {
//            int a2dp = ba.getProfileConnectionState(BluetoothProfile.A2DP);              //可操控蓝牙设备，如带播放暂停功能的蓝牙耳机
//            int headset = ba.getProfileConnectionState(BluetoothProfile.HEADSET);        //蓝牙头戴式耳机，支持语音输入输出
//            int health = ba.getProfileConnectionState(BluetoothProfile.HEALTH);          //蓝牙穿戴式设备
//
//            //查看是否蓝牙是否连接到三种设备的一种，以此来判断是否处于连接状态还是打开并没有连接的状态
//            int flag = -1;
//            if (a2dp == BluetoothProfile.STATE_CONNECTED) {
//                flag = a2dp;
//            } else if (headset == BluetoothProfile.STATE_CONNECTED) {
//                flag = headset;
//            } else if (health == BluetoothProfile.STATE_CONNECTED) {
//                flag = health;
//            }
//            //说明连接上了三种设备的一种
//            if (flag != -1) {
////            isBlueCon = 1;            //connected
//                //  return 2;
////                changeToSpeaker();//蓝牙
//
//            }
//        }
//

    }

    private class AudioTimerTask extends TimerTask {

        @Override
        public void run() {
            ++mPeriod;
        }
    }

    public synchronized void start(String path, OnAudioListener listener) {
        CELog.d("start recording");
        mPeriod = 0;

        mListener = listener;
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(path);

        try {
            recorder.prepare();
            recorder.start();
            updateMicStatus();
            CELog.d("record start success");
        } catch (IllegalStateException ignored) {
            CELog.e("IllegalStateException");
        } catch (IOException e) {
            CELog.e("IOException:" + e.getMessage());
        }

        mPath = path;
    }

    /**
     * cancel, not save the file
     *
     * @return true, cancel success, false, cancel failed
     */
    public synchronized boolean cancel() {
//        CELog.d("cancel recording");
        if (recorder == null) {
//            CELog.e("recorder is null ");
            return false;
        }
        try {
            stopRecord();
        } catch (IllegalStateException ignored) {
//            CELog.e("illegal state happened when cancel");
        }

        File file = new File(mPath);
        return file.exists() && file.delete();
    }


    /**
     * complete the recording
     *
     * @return recording last time
     */
    public synchronized int complete() {
        CELog.i("complete recording");

        if (recorder == null) {
            CELog.e("recorder is null ");
            return -1;
        }

        try {
            stopRecord();
        } catch (IllegalStateException ignored) {
            CELog.e("illegal state happened when complete");
            return -1;
        }

        if (mPeriod < MIN_LENGTH) {
            CELog.i("record time is too short");
            return -1;
        }

        return mPeriod;
    }

    public String generatePath(Context context) {
        boolean isSuccess = true;
        String cachePath = getAudioCachePath(context);
        File file = new File(cachePath);
        if (!file.exists()) {
            isSuccess = file.mkdirs();
        }
        if (isSuccess) {
            //为了通过msgId来播放語音消息，故将語音文件名字改为uuid，用做自己发的消息cache的msgId
            return cachePath + File.separator + UUID.randomUUID() + ".m4a";
        } else {
            return null;
        }
    }

    public String getAudioCachePath(Context context) {
        final String CACHE_DIR_NAME = "audioCache";
        return context.getCacheDir().getAbsolutePath() + File.separator + CACHE_DIR_NAME;
    }

    private synchronized void stopRecord() throws IllegalStateException {
        //mHandler.removeCallbacks(mUpdateMicStatusTimer);
        try {
            recorder.stop();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
        recorder.release();
        recorder = null;
    }

    private final Handler mHandler = new Handler();
    private Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            updateMicStatus();
        }
    };

    private void updateMicStatus() {
        if (recorder != null) {
            double ratio = (double) recorder.getMaxAmplitude();
            double db = 0;
            if (ratio > 1) {
                db = 20 * Math.log10(ratio);
            }
            if (mListener != null) {
                mListener.onDbChange(db);
            }
            mHandler.postDelayed(mUpdateMicStatusTimer, 500);
        }
    }

    private OnAudioListener mListener = null;

    public interface OnAudioListener {
        void onDbChange(double db);
    }

    private MediaPlayer mMediaPlayer = null;
    private String mCurrentPlayingAudioPath = null;

    public String currentPath() {
        return this.mPath;
    }

    public int currentDuration() {
        return this.mPeriod;
    }

    /**
     * play the audio
     *
     * @param path path of the audio file
     */
    public synchronized void playAudio(String path, OnMediaPlayComplete listener) {
//        changeToReceiver();
        if (mMediaPlayer != null) {
            stopPlay();
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (listener != null) {
                    listener.onPlayComplete(!mp.isPlaying());
                }
                stopPlay();
            }
        });
        try {
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
        } catch (IOException ignored) {
        }

        mMediaPlayer.start();

        mCurrentPlayingAudioPath = path;


//        handler.postSticker(runnable);
    }

    public synchronized void stopPlay() {


//        handler.removeCallbacks(runnable);


        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentPlayingAudioPath = null;
        }
    }

    public boolean isPlaying(String path) {
        return (mMediaPlayer != null) && mMediaPlayer.isPlaying() && (path.equals(mCurrentPlayingAudioPath));
    }

    public boolean isPlaying() {
        if (mMediaPlayer == null) {
            return false;
        }
        return mMediaPlayer.isPlaying();
    }

    public interface OnMediaPlayComplete {
        void onPlayComplete(boolean isSuccess);
    }


//    Handler handler = new Handler();
//
//    Runnable runnable = new Runnable() {
//        int count = 0;
//
//        @Override
//        public void run() {
//            switch (count % 3) {
//                case 0:
//                    changeToSpeaker();
//                    break;
//                case 1:
//                    changeToHeadset();
//                    break;
//                case 2:
//                    changeToReceiver();
//                    break;
//            }
//            count++;
//            handler.postDelayed(this, 1500L);
//        }
//    };

    /**
     * 切换到外放  连接设备
     */
    public void changeToSpeaker() {
        Log.e("tag", "切换到外放");
//        mAudioManager.setMode(AudioManager.MODE_RINGTONE);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);

        mAudioManager.setSpeakerphoneOn(true);
    }

    /**
     * 切换到耳机模式
     */
    public void changeToHeadset() {
        Log.e("tag", "切换到内放");
        mAudioManager.setSpeakerphoneOn(false);
    }

    /**
     * 切换到听筒  手机自带喇叭
     */
    public void changeToReceiver() {
        mAudioManager.setSpeakerphoneOn(false);
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }


}
