package tw.com.chainsea.android.common.multimedia;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import java.lang.ref.WeakReference;

/**
 * current by evan on 2020-10-12
 *
 * @author Evan Wang
 * @date 2020-10-12
 */
public class MediaContentObserver extends ContentObserver {
    private static final String TAG = MediaContentObserver.class.getSimpleName();
    MultimediaHelper.Type type;
    private final WeakReference<Listener> listenerRef;

    public MediaContentObserver(MultimediaHelper.Type type, Handler handler, Listener listener) {
        super(handler);
        this.type = type;
        this.listenerRef = new WeakReference<>(listener);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        Listener listener = listenerRef.get();
        if (listener != null) {
            listener.onChange(this.type, selfChange, uri);
        }
    }

    public interface Listener {
        void onChange(MultimediaHelper.Type type, boolean selfChange, Uri uri);
    }
}
