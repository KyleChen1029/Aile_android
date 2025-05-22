package tw.com.chainsea.custom.view.image.animation;

import android.view.animation.Interpolator;

/**
 * current by evan on 2019-11-01
 */
public class SpringInterpolator implements Interpolator {

    private float factor;

    public SpringInterpolator(float factor) {
        this.factor = factor;
    }

    @Override
    public float getInterpolation(float input) {

        return (float) (Math.pow(2, -10 * input) * Math.sin((input - factor / 4) * (2 * Math.PI) / factor) + 1);
    }

}

