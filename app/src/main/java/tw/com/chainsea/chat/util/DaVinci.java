package tw.com.chainsea.chat.util;

import android.content.Context;
import cn.hadcn.davinci.base.VolleyManager;
import cn.hadcn.davinci.http.impl.HttpRequest;
import cn.hadcn.davinci.http.impl.PersistentCookieStore;
import cn.hadcn.davinci.image.VinciImageLoader;
import cn.hadcn.davinci.other.impl.VinciDownload;
import cn.hadcn.davinci.other.impl.VinciUpload;
import cn.hadcn.davinci.volley.RequestQueue;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;

public class DaVinci {
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;
    private static RequestQueue mRequestQueue;
    private static VinciImageLoader mDaImageLoader;
    private static RequestQueue mDefaultRequestQueue;
    private static VinciImageLoader mDefaultDaImageLoader;
    private static DaVinci sDaVinci = null;
    private boolean isEnableCookie = false;
    private CookieManager mCookieManager = null;
    private static Context mContext = null;
    private Map<String, RequestQueue> queues = new HashMap();
    private Map<String, VinciImageLoader> loaders = new HashMap();
    private int mMaxRetries = 0;
    private int mTimeOut = 0;
    private String gBody;

    public static DaVinci with(Context context) {
        mContext = context.getApplicationContext();
        if (sDaVinci == null) {
            sDaVinci = new DaVinci(0);
        }

        mRequestQueue = mDefaultRequestQueue;
        mDaImageLoader = mDefaultDaImageLoader;
        return sDaVinci;
    }

    public static DaVinci with() {
        if (sDaVinci == null) {
            throw new RuntimeException("DaVinci instance has not been initialized yet, please use DaVinci.init() first");
        } else {
            mRequestQueue = mDefaultRequestQueue;
            mDaImageLoader = mDefaultDaImageLoader;
            return sDaVinci;
        }
    }

    public DaVinci tag(String tag) {
        if (!this.queues.containsKey(tag)) {
            throw new RuntimeException("The pool has not been initialized");
        } else {
            mRequestQueue = (RequestQueue)this.queues.get(tag);
            mDaImageLoader = (VinciImageLoader)this.loaders.get(tag);
            return this;
        }
    }

    public void addThreadPool(String tag, int size) {
        if (size <= 0) {
            throw new RuntimeException("pool size at least one");
        } else {
            RequestQueue requestQueue = VolleyManager.newRequestQueue(mContext, size);
            VinciImageLoader imageLoader = new VinciImageLoader(mContext, requestQueue);
            this.queues.put(tag, requestQueue);
            this.loaders.put(tag, imageLoader);
        }
    }

    public static void init(Context context) {
        init(0, context);
    }

    public static void init(int poolSize, Context context) {
        mContext = context.getApplicationContext();
        sDaVinci = new DaVinci(poolSize);
    }

    private DaVinci(int poolSize) {
        if (poolSize <= 0) {
            poolSize = 4;
        }

        mDefaultRequestQueue = VolleyManager.newRequestQueue(mContext, poolSize);
        mDefaultDaImageLoader = new VinciImageLoader(mContext, mDefaultRequestQueue);
    }

    public void enableCookie() {
        this.isEnableCookie = true;
        if (this.mCookieManager == null) {
            this.mCookieManager = new CookieManager(new PersistentCookieStore(mContext), CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(this.mCookieManager);
        }

    }

    public void setHttpGlobal(int maxRetires, int timeout) {
        this.mMaxRetries = maxRetires;
        this.mTimeOut = timeout;
    }

    public HttpRequest getHttpRequest() {
        String cookieString = null;
        if (this.isEnableCookie) {
            StringBuilder cookieBuilder = new StringBuilder();
            String divider = "";

            for(HttpCookie cookie : this.mCookieManager.getCookieStore().getCookies()) {
                cookieBuilder.append(divider);
                divider = ";";
                cookieBuilder.append(cookie.getName());
                cookieBuilder.append("=");
                cookieBuilder.append(cookie.getValue());
            }

            cookieString = cookieBuilder.toString();
        }

        HttpRequest request = new HttpRequest(mRequestQueue, this.isEnableCookie, cookieString);
        if (this.mMaxRetries != 0) {
            request.maxRetries(this.mMaxRetries);
        }

        if (this.mTimeOut != 0) {
            request.timeOut(this.mTimeOut);
        }

        return request;
    }

    public VinciImageLoader getImageLoader() {
        return mDaImageLoader;
    }

    public VinciUpload getUploader() {
        return new VinciUpload(mRequestQueue);
    }

    public void setDownloadBody(String body) {
        this.gBody = body;
    }

    public VinciDownload getDownloader() {
        VinciDownload download = new VinciDownload(mRequestQueue);
        download.body(this.gBody);
        return download;
    }
}
