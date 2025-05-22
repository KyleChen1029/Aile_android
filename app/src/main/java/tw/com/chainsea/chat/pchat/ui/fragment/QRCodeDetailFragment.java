package tw.com.chainsea.chat.pchat.ui.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.FragmentQrcodeDetailBinding;
import tw.com.chainsea.chat.view.vision.ScanResultBean;
import tw.com.chainsea.chat.zxing.encoding.EncodingHandler;


public class QRCodeDetailFragment extends Fragment {

    private String serviceNumberId;

    private FragmentQrcodeDetailBinding binding;

    public static QRCodeDetailFragment newInstance(String serviceNumberId) {
        QRCodeDetailFragment fragment = new QRCodeDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(BundleKey.SERVICE_NUMBER_ID.key(), serviceNumberId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serviceNumberId = getArguments().getString(BundleKey.SERVICE_NUMBER_ID.key());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQrcodeDetailBinding.inflate(inflater, container, false);

        getVipcnDetail();
        return binding.getRoot();
    }

    private void getVipcnDetail() {
//        String tokenId = TokenPref.getInstance(getContext()).getTokenId();
        ApiManager.doServiceNumberItem(getContext(), serviceNumberId, new ApiListener<ServiceNumberEntity>() {

            @Override
            public void onSuccess(ServiceNumberEntity entity) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    refreashResult(entity);
                });
            }

            @Override
            public void onFailed(String errorMessage) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT)::show);
            }
        });
//        RequestManager.getInstance().requestGetVipcnDetail(getContext(), tokenId, serviceNumberId, new ServiceNumberItemRequest.VipcnDetailListener() {
//            @Override
//            public void onSearchVipcnSuccess(VipcnDetailBean bean) {
//                refreashResult(bean);
//            }
//
//            @Override
//            public void onSearchVipcnFailed(ErrCode code, String errorMessage) {
//                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void refreashResult(ServiceNumberEntity entity) {
        if (entity == null) {
            return;
        }
        binding.tvName.setText(entity.getName());
        AvatarService.post(getContext(), entity.getAvatarId(), PicSize.SMALL, binding.ivAvatar, R.drawable.custom_default_avatar);

        try {
//            jsonObject.put(BundleKey.QR_SERVICE_NUMBER_ID.key(), serviceNumberId);
            String json = ScanResultBean.Build().serviceNumberId(serviceNumberId).build().toJson();
            Bitmap codeBitmap = EncodingHandler.createQRCode(json, 600, 600, null);
            binding.qrCode.setImageBitmap(codeBitmap);
        } catch (Exception ignored) {
            CELog.d("save qrCode id error");
        }

    }
}
