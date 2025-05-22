package tw.com.chainsea.chat.messagekit.lib;

import android.graphics.drawable.AnimationDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunhui on 2018/5/14.
 */

public class AnimationDrawableManager {
    public static List<AnimationDrawable> sAnimationDrawables = new ArrayList<>();

    public static void addDrawable(AnimationDrawable drawable) {
        sAnimationDrawables.add(drawable);
    }

    public static void removeDrawable(AnimationDrawable drawable) {
        sAnimationDrawables.remove(drawable);
    }

    public static void stopAnimations() {
        if (isHaveAnimation()) {
            for (AnimationDrawable animationDrawable : sAnimationDrawables) {
                animationDrawable.selectDrawable(0);
                animationDrawable.stop();
            }
        }
    }

    public static boolean isHaveAnimation() {
        return !sAnimationDrawables.isEmpty();
    }
}
