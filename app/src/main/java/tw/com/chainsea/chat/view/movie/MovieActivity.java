package tw.com.chainsea.chat.view.movie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.common.base.Strings;

import java.io.File;

import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.android.common.video.VideoHelper;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.ActivityMovieBinding;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.custom.view.progress.IosProgressBar;

public class MovieActivity extends AppCompatActivity {
    private ActivityMovieBinding binding;
    public static final int UPDATA_VIDEO_NUM = 1;
    private AudioManager audioManager;//音量控制器
    private boolean screen_flag = true;//判断屏幕转向
    private int screen_width, screen_height;
    private int currentPosition;
    private IosProgressBar progressBar;
    ExoPlayer player;
    private boolean isGreenTheme = false;
    /**
     * 通过handler对播放进度和时间进行更新
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATA_VIDEO_NUM) {
                int currentTime = 0;
                int totally = 0;
                if (player != null) {
                    currentTime = (int) player.getContentPosition();
                    totally = (int) player.getDuration();
                }

                //格式化显示时间
                updateTimeFormat(binding.totallyTimeTV, totally);
                updateTimeFormat(binding.currentTimeTV, currentTime);
                //设置播放进度
                binding.seekSB.setMax(totally);
                binding.seekSB.setProgress(currentTime);
                //自己通知自己更新
                handler.sendEmptyMessageDelayed(UPDATA_VIDEO_NUM, 500);//500毫秒刷新
            }
        }
    };


    private void showLoadingView() {
        progressBar = IosProgressBar.show(this, getString(R.string.text_video_loading), true, true, dialog -> {
        });
    }

    private void hideLoadingView() {
        try {
            if (progressBar != null && progressBar.isShowing())
                progressBar.dismiss();
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
        //实例化音量控制器
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie);
        Window w = this.getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // 初始化聲音
        int volumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        binding.volumeControlSB.setMax(volumeMax);
        //获取设置当前音量
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        binding.volumeControlSB.setProgress(currentVolume);
        //实例化控制器
        MediaController controller = new MediaController(this);
        controller.setVisibility(View.GONE);

        initListener();

        String url = getIntent().getStringExtra(BundleKey.VIDEO_URL.key());
        String path = getIntent().getStringExtra(BundleKey.VIDEO_PATH.key());


        /**
         * 初始畫面比例
         */
        binding.videoCVV.post(() -> {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            screen_width = dm.widthPixels;
            screen_height = dm.heightPixels;

            int inWidth = getIntent().getIntExtra(BundleKey.VIDEO_WIDTH.key(), 0);
            int inHeight = getIntent().getIntExtra(BundleKey.VIDEO_HEIGHT.key(), 0);
            int[] size = (inWidth == 0 || inHeight == 0) ? VideoHelper.size(url) : zoomImage(inWidth, inHeight, screen_width, screen_height);

            ViewGroup.LayoutParams vParams = binding.videoCVV.getLayoutParams();
            vParams.width = size[0];
            vParams.height = size[1];
            binding.videoCVV.setLayoutParams(vParams);

            ViewGroup.LayoutParams tParams = binding.thumbnailIV.getLayoutParams();
            tParams.width = size[0];
            tParams.height = size[1];
            binding.thumbnailIV.setLayoutParams(tParams);

            String load = url;
            if (!Strings.isNullOrEmpty(path) && new File(path).exists()) {
                load = path;
            }
            /**
             * 縮略圖製作
             */
            try {
                Glide.with(this)
                    .load(load)
                    .apply(new RequestOptions()
                        .frame(1000)
                        .override(screen_width)
                        .error(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(this.binding.thumbnailIV);
            } catch (Exception ignored) {
            }

        });
        init(!Strings.isNullOrEmpty(path) && new File(path).exists() ? path : url);

        //视频播放时开始刷新
        binding.playIV.setImageResource(R.drawable.icon_play_blue);
        /*
         * 将控制器和播放器进行互相关联
         */
        controller.setMediaPlayer(binding.videoCVV);
        binding.videoCVV.setMediaController(controller);

        // 如果有偵數
        int position = getIntent().getIntExtra(BundleKey.VIDEO_POSITION.key(), 0);
        if (position != 0) {
            binding.thumbnailIV.setAlpha(0.0f);
            binding.videoCVV.seekTo(position);
            binding.videoCVV.start();
            binding.playIV.setImageResource(R.drawable.icon_pause_blue);
        }
        doPlayControllerAction(binding.playIV); //autoPlay
    }


    private void init(String path) {
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter.Builder(this).build();
        DefaultTrackSelector defaultTrackSelector = new DefaultTrackSelector(this);
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(path));
        player = new ExoPlayer.Builder(this)
            .setTrackSelector(defaultTrackSelector)
            .setBandwidthMeter(defaultBandwidthMeter)
            .build();
        player.setVideoSurfaceView(binding.videoCVV);
        player.setMediaItem(mediaItem);
        player.prepare();
        showLoadingView();
        player.addAnalyticsListener(new AnalyticsListener() {
            @Override
            public void onVideoDecoderInitialized(@NonNull EventTime eventTime, @NonNull String decoderName, long initializedTimestampMs, long initializationDurationMs) {
                AnalyticsListener.super.onVideoDecoderInitialized(eventTime, decoderName, initializedTimestampMs, initializationDurationMs);
                hideLoadingView();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding.videoCVV != null) {
            binding.videoCVV.seekTo(currentPosition);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentPosition = binding.videoCVV.getCurrentPosition();
        handler.removeMessages(UPDATA_VIDEO_NUM);

        if (binding.videoCVV != null) {
            //视频暂停
            binding.videoCVV.pause();
            binding.playIV.setImageResource(R.drawable.icon_play_blue);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (binding.videoCVV != null) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
        handler.removeMessages(UPDATA_VIDEO_NUM);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 判断当前屏幕的横竖屏状态
        int screenOritentation = getResources().getConfiguration().orientation;
        if (screenOritentation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏时处理
            setVideoScreenSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            screen_flag = false;
            //清除全屏标记，重新添加
            getWindow().clearFlags((WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN));
            getWindow().addFlags((WindowManager.LayoutParams.FLAG_FULLSCREEN));
        } else {
            //竖屏时处理
            setVideoScreenSize(ViewGroup.LayoutParams.MATCH_PARENT, UiHelper.dip2px(MovieActivity.this, 240));
            screen_flag = true;
            //清除全屏标记，重新添加
            getWindow().clearFlags((WindowManager.LayoutParams.FLAG_FULLSCREEN));
            getWindow().addFlags((WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN));
        }
    }


    /**
     * 设置横竖屏时的视频大小
     *
     * @param width
     * @param height
     */
    private void setVideoScreenSize(int width, int height) {
        //获取视频控件的布局参数
        ViewGroup.LayoutParams videoViewLayoutParams = binding.videoCVV.getLayoutParams();
        //设置视频范围
        videoViewLayoutParams.width = width;
        videoViewLayoutParams.height = height;
        binding.videoCVV.setLayoutParams(videoViewLayoutParams);
    }

    /**
     * 时间格式化
     *
     * @param textView    时间控件
     * @param millisecond 总时间 毫秒
     */
    @SuppressLint("DefaultLocale")
    private void updateTimeFormat(TextView textView, int millisecond) {
        //将毫秒转换为秒
        int second = millisecond / 1000;
        //计算小时
        int hh = second / 3600;
        //计算分钟
        int mm = second % 3600 / 60;
        //计算秒
        int ss = second % 60;
        //判断时间单位的位数
        String str = null;
        if (hh != 0) {//表示时间单位为三位
            str = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            str = String.format("%02d:%02d", mm, ss);
        }
        //将时间赋值给控件
        textView.setText(str);
    }

    /**
     * 按钮点击事件
     */
    private void initListener() {

        binding.expandIV.setOnClickListener(this::doScreenExpandAction);
        binding.playIV.setOnClickListener(this::doPlayControllerAction);
        // 播放完成
        binding.videoCVV.setOnCompletionListener(mp -> binding.playIV.setImageResource(R.drawable.icon_play_blue));

        //播放进度条事件
        binding.seekSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //设置当前的播放时间
                updateTimeFormat(binding.currentTimeTV, progress);
                if (player != null && player.getDuration() == progress) {
                    binding.playIV.setImageResource(R.drawable.icon_play_blue);
                    pausePlayer();
                    player.seekTo(0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //拖动视频进度时，停止刷新
                handler.removeMessages(UPDATA_VIDEO_NUM);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //停止拖动后，获取总进度
                int totall = seekBar.getProgress();
                //设置VideoView的播放进度
                if (player != null) {
                    player.seekTo(totall);
                }
                //重新handler刷新
                handler.sendEmptyMessage(UPDATA_VIDEO_NUM);

            }
        });

        //音量控制条事件
        binding.volumeControlSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //设置音量变动后系统的值
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    //播放按钮事件
    void doPlayControllerAction(View view) {
        if (view.getTag() == null) {
            view.setTag("isPlay");
            binding.playIV.setImageResource(R.drawable.icon_pause_blue);
            startPlayer();
            binding.thumbnailIV.setAlpha(0.0f);
            //当视频播放时，通知刷新
            handler.sendEmptyMessage(UPDATA_VIDEO_NUM);
        } else {
            view.setTag(null);
            binding.playIV.setImageResource(R.drawable.icon_play_blue);
            //视频暂停
            pausePlayer();
            //当视频处于暂停状态，停止handler的刷新
            handler.removeMessages(UPDATA_VIDEO_NUM);
        }
    }

    private void pausePlayer() {
        player.setPlayWhenReady(false);
        player.pause();
        player.getPlaybackState();
    }

    private void startPlayer() {
        player.setPlayWhenReady(true);
        player.play();
        player.getPlaybackState();
    }


    //设置全屏按钮点击事件
    void doScreenExpandAction(View view) {
        if (screen_flag) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//控制屏幕竖屏
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);//控制屏幕横屏
        }
        screen_flag = !screen_flag;
    }


    public static int[] zoomImage(int inWidth, int inHeight, int widthPixels, int screen_height) {
        int outWidth;
        int outHeight;
        if (inWidth > inHeight) {
            outWidth = widthPixels;
            outHeight = (inHeight * widthPixels) / inWidth;
        } else {
            outHeight = screen_height;
            outWidth = (inWidth * screen_height) / inHeight;
        }
        int[] sizes = new int[2];
        sizes[0] = outWidth;
        sizes[1] = outHeight;
        return sizes;
    }

//    public static void start(Context context, String path, String url, int width, int height, int position) {
//        context.startActivity(new Intent(context, MovieActivity.class)
//            .putExtra(BundleKey.VIDEO_PATH.key(), path)
//            .putExtra(BundleKey.VIDEO_URL.key(), url)
//            .putExtra(BundleKey.VIDEO_WIDTH.key(), width)
//            .putExtra(BundleKey.VIDEO_HEIGHT.key(), height)
//            .putExtra(BundleKey.VIDEO_POSITION.key(), position));
//    }

    public static Intent getIntent(Context context, String path, String url, int width, int height, int position) {
        return new Intent(context, MovieActivity.class)
            .putExtra(BundleKey.VIDEO_PATH.key(), path)
            .putExtra(BundleKey.VIDEO_URL.key(), url)
            .putExtra(BundleKey.VIDEO_WIDTH.key(), width)
            .putExtra(BundleKey.VIDEO_HEIGHT.key(), height)
            .putExtra(BundleKey.VIDEO_POSITION.key(), position);
    }
}
