package tw.com.chainsea.chat.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.FragmentRecordBinding;
import tw.com.chainsea.chat.messagekit.lib.AudioLib;
import tw.com.chainsea.chat.util.FileUtils;
import tw.com.chainsea.custom.view.alert.AlertView;


/**
 * Created by jerry.yang on 2017/12/3.
 * desc:
 */
public class RecordFragment extends Fragment {

    private FragmentRecordBinding binding;

    private boolean isStop;  // 录音是否结束的标志
    private long mStartTime;
    private long mEndTime;
    private long mRecordTime;
    private boolean isRecording = false;
    private String mVoicePath;
//    private MaterialDialog mMaterialDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        mMaterialDialog = new MaterialDialog(getContext());
        binding = FragmentRecordBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initListener();
    }

    private void initListener() {
        binding.btnCancel.setOnClickListener(this::onClick);
        binding.btnRecord.setOnClickListener(this::onClick);
        binding.btnCommit.setOnClickListener(this::onClick);
    }

    //用于定时器
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };
    //定时器
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // handler自带方法实现定时器
            try {
                long endTime = System.currentTimeMillis();
                int time = (int) ((endTime - mStartTime) / 1000);
                //  限制录音时间
                if (time <= recordTime) {
                    count--;
                    isStop = false;
                    mHandler.postDelayed(this, 1000);
                    mRecordTime = time;
                    binding.btnRecord.setText("停止錄音");
                    Log.e("jerry", "录音计时" + count);
                } else {
                    Toast.makeText(getContext(), "您已達到最長錄音時間!", Toast.LENGTH_SHORT).show();
                    isStop = true;
                    stopRecord();
                    Log.e("jerry", "錄音結束");
                }
            } catch (Exception ignored) {

            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @SuppressLint("NonConstantResourceId")
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                deleteSoundFileUnSend(mVoicePath);
                getActivity().finish();
                break;
            case R.id.btn_record:
                if (!isRecording) {
                    //開始录音
                    startRecord();
                } else {
                    //停止錄音
                    isStop = true;
                    stopRecord();
                }
                isRecording = !isRecording;
                break;
            case R.id.btn_commit:
                //发送录音文件
                sendRecordMedia();
                break;
        }
    }

    int recordTime = 119;  //录音时长
    int count = 119;  //计时
    private View mBtnCancel;
    private View mBtnRecord;
    private View mBtnCommit;

    private void startRecord() {
        count = 119;
        isStop = true;
        binding.btnCancel.setVisibility(View.INVISIBLE);
        binding.btnCommit.setVisibility(View.INVISIBLE);
        binding.recording.show(1);
        // 获取声音路径
        mVoicePath = AudioLib.getInstance(getContext()).generatePath(getActivity());
        AudioLib.getInstance(getContext()).start(mVoicePath, new AudioListener());
        // 开启定时器(时长)，如果有动画可以开启动画
        mStartTime = System.currentTimeMillis();
        mHandler.postDelayed(runnable, 1000);
        Log.e("jerry", "開始录音");
    }


    private void stopRecord() {
        if (isStop) {
            mEndTime = System.currentTimeMillis();
            mRecordTime = (int) ((mEndTime - mStartTime) / 1000);
            int period = AudioLib.getInstance(getContext()).complete();
            if (period < 0) {
                deleteSoundFileUnSend(mVoicePath);
                binding.btnRecord.setText("開始錄音");
                showDialog("錄音時間太短，請重新錄音!", new String[]{"取消", "繼續"}, true);
                Log.e("jerry", "錄音時間太短，請重新錄音");
            } else if (period >= 0) {
                binding.btnRecord.setText("錄音結束");
                binding.btnRecord.setVisibility(View.INVISIBLE);
                binding.btnCancel.setVisibility(View.VISIBLE);
                binding.btnCommit.setVisibility(View.VISIBLE);
            }
            Log.e("jerry", "停止錄音");
        }
        binding.recording.hide();
        mHandler.removeCallbacks(runnable); //移除定时器
    }

    private void sendRecordMedia() {
        LocalMedia localMedia;
        final File file = new File(mVoicePath);
        //广播行动:请求媒体扫描仪扫描一个文件,并将它添加到媒体数据库

        localMedia = new LocalMedia();
        localMedia.setPath(mVoicePath);
        String pictureType = FileUtils.fileToType(file);
        localMedia.setMimeType(FileUtils.pictureToVideo(pictureType));
        localMedia.setDuration(mRecordTime);
        Intent intent = new Intent();
        intent.putExtra(BundleKey.IS_SEND_VIDEO.key(), localMedia);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    class AudioListener implements AudioLib.OnAudioListener {

        @Override
        public void onDbChange(double db) {
            int level = 0;
            if (db > 40) {
                level = ((int) db - 40) / 7;
            }
            if (null != binding.recording) {
                binding.recording.setVoiceLevel(level);
            }
        }
    }

    /**
     * 录音完毕后，若不发送，则删除文件
     */
    public void deleteSoundFileUnSend(String mVoicePath) {
        if (!"".equals(mVoicePath)) {
            try {
                File file = new File(mVoicePath);
                file.delete();
                mVoicePath = "";
            } catch (Exception ignored) {

            }
        }
    }

    public void showDialog(String context, String[] btnTexts, final boolean finish) {
        new AlertView.Builder()
            .setContext(getContext())
            .setStyle(AlertView.Style.Alert)
//                .setTitle("")
            .setMessage(context)
            .setOthers(btnTexts)
            .setOnItemClickListener((o, position) -> {
                if (position == 0) {
                    if (finish) {
                        getActivity().finish();
                        getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                    }
                } else {

                }
            })
            .build()
            .show();


//        if (mMaterialDialog != null && !mMaterialDialog.isShowing()) {
//            mMaterialDialog.setCanceledOnTouchOutside(false);
//            mMaterialDialog.setCancelable(false);
//            mMaterialDialog.isTitleShow(false)
//                    .btnNum(2)
//                    .content(context)
//                    .btnText(btnTexts)
//                    .show();
//            mMaterialDialog.setOnBtnClickL(new OnBtnClickL() {
//                @Override
//                public void onBtnClick() {
//                    mMaterialDialog.dismiss();
//                    if (finish) {
//                        getActivity().finish();
//                        getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
//                    }
//                }
//            }, new OnBtnClickL() {
//                @Override
//                public void onBtnClick() {
//                    mMaterialDialog.dismiss();
//                    // 數據初始化，重新錄音
//                }
//            });
//        }
    }

}
