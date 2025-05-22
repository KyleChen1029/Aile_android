package tw.com.chainsea.chat.lib;

import static android.media.AudioManager.FLAG_SHOW_UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;

/**
 * Created by sunhui on 2017/9/5.
 */

public class PlayerManager {
    /**
     * External mode
     */
    public static final int MODE_SPEAKER = 0;
    /**
     * Headphone mode
     */
    public static final int MODE_HEADSET = 1;
    /**
     * Handset mode
     */
    public static final int MODE_EARPIECE = 2;
    @SuppressLint("StaticFieldLeak")
    private static volatile PlayerManager playerManager;
    private AudioManager audioManager;
    private Context context;
    private int currentMode = MODE_SPEAKER;

    public static PlayerManager getInstence() {
        if (playerManager == null) {
            synchronized (PlayerManager.class) {
                playerManager = new PlayerManager();
            }
        }
        return playerManager;
    }

    public void init(Context context) {
        this.context = context;
        initAudioManager();
    }

    /**
     * Initialize the audio manager
     */
    private void initAudioManager() {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        // The default is speaker playback
        audioManager.setSpeakerphoneOn(true);
    }

    /**
     * Get current play mode * @return
     */
    public int getCurrentMode() {
        return currentMode;
    }

    /**
     * Switch to handset mode
     */
    public void changeToEarpieceMode() {
        currentMode = MODE_EARPIECE;
        audioManager.setSpeakerphoneOn(false);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.MODE_IN_COMMUNICATION), AudioManager.FLAG_SHOW_UI);
    }

    /**
     * Switch to headset mode
     */
    public void changeToHeadsetMode() {
        currentMode = MODE_HEADSET;
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * Switch to external mode
     */
    public void changeToSpeakerMode() {
        currentMode = MODE_SPEAKER;
        audioManager.setSpeakerphoneOn(true);
    }

    /**
     * Turn up the volume
     */
    public void raiseVolume() {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        }
    }

    /**
     * Turn down the volume
     */
    public void lowerVolume() {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume > 0) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        }
    }

    public void raiseVolume(Context context) {
        setVolume(context, true);
    }

    public void lowerVolume(Context context) {
        setVolume(context, false);
    }

    private void setVolume(Context context, boolean upVolume) {
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (upVolume) {
            volume += 1;
        } else {
            volume -= 1;
        }
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (volume >= 0 && volume <= maxVolume) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, FLAG_SHOW_UI);
        }
    }
}

