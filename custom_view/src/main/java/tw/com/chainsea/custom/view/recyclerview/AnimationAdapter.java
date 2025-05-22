package tw.com.chainsea.custom.view.recyclerview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * current by evan on 2020-06-17
 *
 * @author Evan Wang
 * @date 2020-06-17
 */
public abstract class AnimationAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private BaseAnimation adapterAnimation = new CustomAnimation();
    protected boolean animationEnable = true;
    private boolean ignoreTriggerType = false;
    private int mLastPosition = -1;
    protected int triggerType = 99;


    private AnimationType type = AnimationType.CUSTOM;

    public enum AnimationType {
        CUSTOM,
        SHAKE
    }

    public void setAnimationEnable(boolean animationEnable) {
        this.animationEnable = animationEnable;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VH holder) {
//        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();

        if (holder.itemView.getTag() != null && this.ignoreTriggerType) {
            addAnimation(holder);
        }

        if (type == this.triggerType) {
            addAnimation(holder);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VH holder) {
//        super.onViewDetachedFromWindow(holder);
//        Log.e(TAG, "" + type);

    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    private BaseAnimation getAnimationAdapter() {
        switch (this.type) {
            case SHAKE:
                if (adapterAnimation != null) {
                    if (adapterAnimation instanceof AnimationAdapter.ShakeAnimation) {
                    } else {
                        adapterAnimation = new ShakeAnimation();
                    }
                } else {
                    adapterAnimation = new ShakeAnimation();
                }
                break;
            case CUSTOM:
            default:
                if (adapterAnimation != null) {
                    if (adapterAnimation instanceof AnimationAdapter.CustomAnimation) {
                    } else {
                        adapterAnimation = new CustomAnimation();
                    }
                } else {
                    adapterAnimation = new CustomAnimation();
                }
                break;
        }

        return adapterAnimation;
    }

    private void addAnimation(RecyclerView.ViewHolder holder) {
        if (animationEnable) {
            if (holder.getLayoutPosition() > mLastPosition) {
                for (Animator animator : getAnimationAdapter().animators(holder.itemView)) {
                    startAnim(animator, holder.getLayoutPosition());
                }
                mLastPosition = holder.getLayoutPosition();
            }
        }
    }

    protected void startAnim(Animator anim, int position) {
//        Log.e(TAG, "Position:: " + position);
//        anim.setStartDelay(500L);
        anim.start();
        executeAnimatorEnd(position);
    }


    public abstract void executeAnimatorEnd(int position);

    interface BaseAnimation {
        Animator[] animators(View view);
    }

    /**
     * 搖動
     */
    static class ShakeAnimation implements BaseAnimation {

        @Override
        public Animator[] animators(View view) {
            Animator translationX = ObjectAnimator.ofFloat(view, "translationX", 0f, 200f, 0f, -200f, 0f);
            translationX.setDuration(400L);
            translationX.setInterpolator(new LinearInterpolator());
            return new Animator[]{translationX};
        }
    }


    /**
     * 客制
     */
    static class CustomAnimation implements BaseAnimation {
        @Override
        public Animator[] animators(View view) {


//            Animator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.1f, 1f);
//            scaleX.setDuration(300L);
//            scaleX.setInterpolator(new DecelerateInterpolator());

            Animator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.7f, 1.0f);
            scaleY.setDuration(500L);
            scaleY.setInterpolator(new LinearInterpolator());

            @SuppressLint("Recycle") Animator translationY = ObjectAnimator.ofFloat(view, "translationY", view.getMeasuredHeight(), 0f);
            translationY.setDuration(250L);
            translationY.setInterpolator(new LinearInterpolator());


            Animator alpha = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1f);
            alpha.setDuration(500L);
            alpha.setInterpolator(new LinearInterpolator());


            ObjectAnimator rotationX = ObjectAnimator.ofFloat(view, "rotationX", 30, 0);
            rotationX.setDuration(400L);
            rotationX.setInterpolator(new LinearInterpolator());

            return new Animator[]{alpha, scaleY, rotationX};


//            Animator animator = ObjectAnimator.ofFloat(view, "translationY", view.getMeasuredHeight(), 0f);
//            animator.setDuration(800L);
//            animator.setInterpolator(new DecelerateInterpolator());
//            return new Animator[]{animator};


//            Animator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1f);
//            Animator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1f);
////            Animator alpha = ObjectAnimator.ofFloat(view, "alpha", 0, 1f);
//
//            scaleY.setDuration(300);
//            scaleX.setDuration(300);
////            alpha.setDuration(350);
//
//            scaleY.setInterpolator(new DecelerateInterpolator());
//            scaleX.setInterpolator(new DecelerateInterpolator());
//
//            return new Animator[]{scaleY, scaleX};
        }
    }


}
