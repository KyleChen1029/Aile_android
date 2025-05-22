package tw.com.chainsea.android.common.system;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * current by evan on 2020-08-27
 *
 * @author Evan Wang
 * @date 2020-08-27O
 */
public class ThreadExecutorHelper {

    //    private static ScheduledExecutorService WORKER_EXECUTOR = Executors.newScheduledThreadPool(1);
    private static Semaphore mThreadPoolSemaphore = new Semaphore(0);
    private static ExecutorService SOCKET_EXECUTOR = Executors.newCachedThreadPool();
    private static ApiThreadExecutor API_EXECUTOR = getApiExecutor();
    private static ExecutorService IO_EXECUTOR = getIoThreadExecutor();
    //        private static MainThreadExecutor UI_EXECUTOR = getMainThreadExecutor();
    private static MainHandlerThreadExecutor UI_EXECUTOR = getMainThreadExecutor();
    private static MyHandler HANDLER_EXECUTOR = getHandlerExecutor();


    private static boolean isLock = false;

    public static void setIsLock(boolean isLock) {
        ThreadExecutorHelper.isLock = isLock;
    }

    public static ApiThreadExecutor getApiExecutor() {
        if (API_EXECUTOR == null) {
            API_EXECUTOR = ApiThreadExecutor.newInstance();
        }
        return API_EXECUTOR;
    }

    public static ExecutorService getSocketExecutor() {
        if (SOCKET_EXECUTOR == null) {
            SOCKET_EXECUTOR = Executors.newCachedThreadPool();
        }
        return SOCKET_EXECUTOR;
    }

    public static ExecutorService getIoThreadExecutor() {
        if (IO_EXECUTOR == null) {
            IO_EXECUTOR = IoThreadExecutor.newInstance();
        }
        return IO_EXECUTOR;
//        return IoThreadExecutor.newInstance();
    }

    public static MainHandlerThreadExecutor getMainThreadExecutor() {
        if (UI_EXECUTOR == null) {
            UI_EXECUTOR = MainHandlerThreadExecutor.newInstance();
        }
        return UI_EXECUTOR;
    }

    public static MyHandler getHandlerExecutor() {
        if (HANDLER_EXECUTOR == null) {
            HANDLER_EXECUTOR = MyHandler.newInstance();
        }
        return HANDLER_EXECUTOR;
    }

    public static class MyHandler extends Handler {

        public static MyHandler newInstance() {
            return new MyHandler();
        }

        public void execute(Runnable r) {
            if (isLock) {
                return;
            }
//            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
            post(r);
        }

        public void execute(Runnable r, long delayMillis) {
//            if (isLock) {
//                return;
//            }
//            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
            postDelayed(r, delayMillis);
        }

        public void remove(Runnable r) {
            removeCallbacks(r);
        }
    }

    public static class ApiThreadExecutor extends ThreadPoolExecutor {
        public ApiThreadExecutor() {
            super(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(true));
        }

        public static ApiThreadExecutor newInstance() {
            return new ApiThreadExecutor();
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            if (isLock) {
                return;
            }
            super.beforeExecute(t, r);
        }

        @Override
        public void execute(Runnable command) {
//            if (isLock) {
//                return;
//            }
            super.execute(command);
        }


        @Override
        protected void afterExecute(Runnable r, Throwable t) {
//            if (isLock) {
//                return;
//            }
            super.afterExecute(r, t);
        }

        @Override
        protected void terminated() {
            super.terminated();
        }

        @Override
        public boolean isTerminated() {
            return super.isTerminated();
        }

        @Override
        public void shutdown() {
            super.shutdown();
        }
    }

    public static class MainHandlerThreadExecutor implements Executor {
        Handler handler = new Handler(Looper.getMainLooper());

        public MainHandlerThreadExecutor() {
        }

        public static MainHandlerThreadExecutor newInstance() {
            return new MainHandlerThreadExecutor();
        }

        @Override
        public void execute(Runnable r) {
//            if (isLock) {
//                throw new RuntimeException("isLock");
//            }
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
            handler.post(r);
        }

        public void execute(Runnable r, long delayMillis) {
//            if (isLock) {
//                throw new RuntimeException("isLock");
//            }
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
            handler.postDelayed(() -> execute(r), delayMillis);
        }
    }

//    private static LinkedList<Runnable> mTasks = new LinkedList();

    public static class IoThreadExecutor extends ThreadPoolExecutor {
        public IoThreadExecutor() {
            super(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        }

        public static IoThreadExecutor newInstance() {
            return new IoThreadExecutor();
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            if (isLock) {
                return;
            }
            super.beforeExecute(t, r);
        }

        @Override
        public void execute(Runnable command) {

//            mTasks.add(command);
//            try {
//                mThreadPoolSemaphore.acquire();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            execute(mTasks.removeFirst());
//            super.execute(mTasks.removeFirst());
            super.execute(command);

//            mThreadPoolSemaphore.release();
        }


        @Override
        protected void afterExecute(Runnable r, Throwable t) {
//            if (isLock) {
//                return;
//            }
//            mThreadPoolSemaphore.release();
            super.afterExecute(r, t);
        }

        @Override
        protected void terminated() {
            super.terminated();
        }

        @Override
        public boolean isTerminated() {
            return super.isTerminated();
        }

        @Override
        public void shutdown() {
            super.shutdown();
        }
    }

//    public static MainAsyncTask mainAsyncTask = new MainAsyncTask();
//    static public class MainAsyncTask extends AsyncTask<String, Void, String> {
//        @Override protected String doInBackground(String... params) {
//            return "";
//        }
//        @Override protected void onPostExecute(String result) {
//
//        }
//    }
}
