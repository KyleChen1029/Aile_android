package tw.com.chainsea.chat.widget.floatview;

import static android.content.Context.POWER_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.PowerManager;
import android.widget.LinearLayout;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.lib.PickupDetector;

/**
 * Created by sunhui on 2018/5/25.
 */

public class ParentWindowView extends LinearLayout implements PickupDetector.PickupDetectListener {

    public SoundPool mSoundPool;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private PickupDetector mPickupDetector;
    public static boolean isPickup = true;
    private int inRingtone;
    private int outRingtone;

    @SuppressLint("InvalidWakeLockTag")
    public ParentWindowView(Context context) {
        super(context);

        mPowerManager = (PowerManager) getContext().getSystemService(POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "TAG");
        mPickupDetector = new PickupDetector(getContext());
        mPickupDetector.register(this);

        mSoundPool = new SoundPool(2, AudioManager.STREAM_RING, 0);
        outRingtone = mSoundPool.load(getContext(), R.raw.outcall_ringtone, 1);
        inRingtone = mSoundPool.load(getContext(), R.raw.incoming_ringtone, 1);
    }

    public void playOutRingtone() {
        mSoundPool.setLoop(outRingtone, -1);
        mSoundPool.play(outRingtone, 1, 1, 0, 0, 1);
    }

    public void playInRingtone() {
        mSoundPool.setLoop(inRingtone, -1);
        mSoundPool.play(inRingtone, 1, 1, 0, 0, 1);
    }

    public void stopRingtone() {
        mSoundPool.autoPause();
        mSoundPool.release();
    }

    @Override
    public void onPickupDetected(boolean isPickingUp) {
        if (mWakeLock == null) {
            System.out.println(" No PROXIMITY_SCREEN_OFF_WAKE_LOCK");
            return;
        }
        if (isPickingUp && !mWakeLock.isHeld() && isPickup) {
            mWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        }
        if (!isPickingUp && mWakeLock.isHeld()) {
            try {
                mWakeLock.setReferenceCounted(false);
                mWakeLock.release();
            } catch (Exception e) {

            }
        }
    }

    protected void removeView() {
        if (mPickupDetector != null) {
            mPickupDetector.unRegister();
        }
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.setReferenceCounted(false);
            mWakeLock.release();
        }
    }

    public static void setIsPickup(boolean isPickup) {
        ParentWindowView.isPickup = isPickup;
    }
}
