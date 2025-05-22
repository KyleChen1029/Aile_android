package tw.com.chainsea.custom.view.image.animation;

import android.view.View;

/**
 * current by evan on 2019-11-01
 */
public class AnimCompat {
    private static final int SIXTY_FPS_INTERVAL = 1000 / 60;

    public static void postOnAnimation(View view, Runnable runnable) {
        view.postOnAnimation(runnable);
    }
}
