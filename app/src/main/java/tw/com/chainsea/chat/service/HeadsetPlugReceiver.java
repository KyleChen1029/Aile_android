package tw.com.chainsea.chat.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.widget.Toast;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.widget.floatview.ParentWindowView;

/**
 * Created by sunhui on 2018/4/19.
 */

public class HeadsetPlugReceiver extends BroadcastReceiver {

    private AudioManager audioManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", 0);
            if (state == 0) {
                // Headphone unplugged
                audioManager.setSpeakerphoneOn(true);
                // Pull out the earphones, stick your face off the screen
                ParentWindowView.setIsPickup(true);
            } else if (state == 1) {
                // Headphone insertion
                Toast.makeText(context, R.string.warning_plug_in_earphone, Toast.LENGTH_SHORT).show();
                audioManager.setSpeakerphoneOn(false);
                // Plug in the earphones, cancel the face off
                ParentWindowView.setIsPickup(false);
            }
        }
    }
}
