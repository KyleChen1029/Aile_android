package tw.com.chainsea.custom.view.surface;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.io.IOException;

/**
 * current by evan on 11/27/20
 *
 * @author Evan Wang
 * @date 11/27/20
 */
@SuppressLint("ViewConstructor")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";
    private final SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
//下面一行適用於Android3.0之前的裝置適配，一般可以省略
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
//制定相機影象的繪製區域為這個SurfaceView，並且啟動相機的預覽
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
//摧毀時釋放相機的資源，如果留空的話則需要在activity裡釋放camera
        mCamera.release();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int w, int h) {
//當SurfaceView尺寸變化時（包括裝置橫屏豎屏改變時時），需要重新設定相關引數
        if (mHolder.getSurface() == null) {
//檢查SurfaceView是否存在
            return;
        }
//改變設定前先關閉相機
        try {
            mCamera.stopPreview();
        } catch (Exception ignored) {
        }
//使用最佳比例配置重啟相機
        try {
            mCamera.setPreviewDisplay(mHolder);
            final Camera.Parameters parameters = mCamera.getParameters();
            final Camera.Size size = getBestPreviewSize(w, h);
            parameters.setPreviewSize(size.width, size.height);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height) {
        Camera.Size result = null;
        final Camera.Parameters p = mCamera.getParameters();
//特別注意此處需要規定rate的比是大的比小的，不然有可能出現rate = height/width，但是後面遍歷的時候，current_rate = width/height,所以我們限定都為大的比小的。
        float rate = (float) Math.max(width, height) / (float) Math.min(width, height);
        float tmp_diff;
        float min_diff = -1f;
        for (Camera.Size size : p.getSupportedPreviewSizes()) {
            float current_rate = (float) Math.max(size.width, size.height) / (float) Math.min(size.width, size.height);
            tmp_diff = Math.abs(current_rate - rate);
            if (min_diff < 0) {
                min_diff = tmp_diff;
                result = size;
            }
            if (tmp_diff < min_diff) {
                min_diff = tmp_diff;
                result = size;
            }
        }
        return result;
    }
}
