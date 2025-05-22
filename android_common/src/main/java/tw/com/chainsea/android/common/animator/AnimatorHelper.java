package tw.com.chainsea.android.common.animator;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;

/**
 * current by evan on 2020-06-30
 *
 * @author Evan Wang
 * @date 2020-06-30
 */
public class AnimatorHelper {

    public enum Status {
        START,
        END,
        CANCEL,
        REPEAT
    }

    public static Animator shakeAnimation(View view, boolean isStart, long delayed, CallBack<Animator, Status> callBack) {
        Animator translationX = ObjectAnimator.ofFloat(view, "translationX", 0f, 100f, -75f, 50f, -25f, 0f);
        translationX.setDuration(300L);
        translationX.setInterpolator(new LinearInterpolator());
        translationX.setStartDelay(delayed);
        translationX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                if (callBack != null) {
                    callBack.callback(animation, Status.START);
                }
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                if (callBack != null) {
                    callBack.callback(animation, Status.END);
                }
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
                if (callBack != null) {
                    callBack.callback(animation, Status.CANCEL);
                }
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {
                if (callBack != null) {
                    callBack.callback(animation, Status.REPEAT);
                }
            }
        });
        if (isStart) {
            translationX.start();
        }
        return translationX;
    }


    public interface CallBack<T, E> {
        void callback(T t, E e);
    }
}
