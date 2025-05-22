package tw.com.chainsea.custom.view.video;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * current by evan on 2020-01-09
 */
public class RoundVideoView extends VideoView {

    private final static String TAG = "VideoSurfaceView";
    private boolean inOtherShape;
    private Path shapePath;

    public RoundVideoView(Context context) {
        super(context);
    }

    public RoundVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (this.inOtherShape)
            canvas.clipPath(shapePath);
        super.dispatchDraw(canvas);
    }

    /**
     * Crops the view in circular shape
     *
     * @param centerX
     * @param centerY
     * @param radius
     */
    public void cropCircle(float centerX, float centerY, int radius) {
        shapePath = new Path();
        shapePath.addCircle(centerX, centerY, radius, Path.Direction.CW);
    }

    /**
     * Crops the view in oval shape
     *
     * @param left
     * @param top
     * @param width
     * @param height
     */
    public void cropOval(float left, float top, int width, int height) {
        RectF rectF = new RectF(left, top, width, height);
        shapePath = new Path();
        shapePath.addOval(rectF, Path.Direction.CW);
    }

    /**
     * Crops the view in rectangular shape
     *
     * @param left
     * @param top
     * @param width
     * @param height
     */
    public void cropRect(float left, float top, int width, int height) {
        RectF rectF = new RectF(left, top, width, height);
        rectF.round(new Rect(10, 10, 10, 10));
        shapePath = new Path();
        shapePath.addRect(rectF, Path.Direction.CW);
    }

    public void roundRect() {
        shapePath = new Path();
        RectF rectF = new RectF(10, 10, 0, 0);
        shapePath.addRoundRect(rectF, new float[]{30, 30, 30, 30, 30, 30, 30, 30}, Path.Direction.CW);
    }

    /**
     * Sets the flag for cropping the view in shape
     *
     * @param inOtherShape
     */
    public void setOtherShape(boolean inOtherShape) {
        this.inOtherShape = inOtherShape;
        invalidate();
    }
}
