package tw.com.chainsea.chat.view.qrcode;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.FragmentQrCodeShareBinding;

public class QrCodeShareFragment extends BottomSheetDialogFragment {
    private FragmentQrCodeShareBinding binding;
    private Bitmap qrCodeBitmap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_qr_code_share, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            byte[] byteArray = getArguments().getByteArray(BundleKey.DATA.key());
            qrCodeBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        }
        initListener();
    }

    private void initListener() {
        binding.llShareMessage.setOnClickListener(this::shareToMessage);
        binding.llShareEmail.setOnClickListener(this::shareToMail);
        binding.llShareApplication.setOnClickListener(this::shareToOther);
        binding.tvCancel.setOnClickListener(v -> {
            dismiss();
        });
    }

    private Uri getImageUri() {
        Uri uri = null;
        try {
            String imageFileName = requireContext().getCacheDir() + "/qrcode.jpg";
            File imageFile = new File(imageFileName);
            OutputStream fout = new FileOutputStream(imageFile);
            qrCodeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
            fout.flush();
            fout.close();
            uri = FileProvider.getUriForFile(requireActivity(), requireActivity().getPackageName() + ".fileprovider", imageFile);
        } catch (Exception ignored) {
        }
        return uri;
    }

    private void shareToMessage(View view) {

    }

    @SuppressLint("IntentReset")
    private void shareToMail(View view) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("image/*");
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{""});
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://" + getImageUri()));
        requireActivity().startActivity(Intent.createChooser(emailIntent, "Sharing Options"));
//        requireActivity().startActivity(emailIntent);
    }

    private void shareToOther(View view) {
        Intent otherIntent = new Intent(android.content.Intent.ACTION_SEND);
        otherIntent.setType("image/*");
        otherIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://" + getImageUri()));
        requireActivity().startActivity(otherIntent);
    }
}
