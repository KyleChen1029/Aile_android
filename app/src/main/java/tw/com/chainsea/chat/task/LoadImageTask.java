package tw.com.chainsea.chat.task;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.bumptech.glide.request.FutureTarget;

import java.util.concurrent.ExecutionException;

/**
 * current by evan on 2020-02-24
 */
public class LoadImageTask extends AsyncTask<FutureTarget<Bitmap>, Void, Bitmap> {
    private LoadImageTask.OnSuccess onSuccess;

    public interface OnSuccess {
        void onSuccess(Bitmap bitmap);
    }

    public LoadImageTask(LoadImageTask.OnSuccess onSuccess) {
        this.onSuccess = onSuccess;
    }

    @SafeVarargs
    @Override
    protected final Bitmap doInBackground(FutureTarget<Bitmap>... futureTargets) {
        try {
            return futureTargets[0].get();
        } catch (ExecutionException | InterruptedException ignored) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap != null)
            onSuccess.onSuccess(bitmap);
    }
}

