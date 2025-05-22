package tw.com.chainsea.android.common.barcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.widget.ImageView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import tw.com.chainsea.android.common.image.BitmapHelper;

/**
 * current by evan on 11/27/20
 *
 * @author Evan Wang
 * @date 11/27/20
 */
public class BarcodeDetectorHelper {


//    public static Bitmap generateQrCode(String myCodeText) throws WriterException {
//        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
//        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // H = 30% damage
//        QRCodeWriter qrCodeWriter = new QRCodeWriter();
//        int size = 256;
//        BitMatrix bitMatrix= qrCodeWriter.encode(myCodeText,BarcodeFormat.QR_CODE, size, size, hintMap);
//        int width = bitMatrix.getWidth();
//        Bitmap bmp = Bitmap.createBitmap(width, width, Bitmap.Config.RGB_565);
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < width; y++) {
//                bmp.setPixel(y, x, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
//            }
//        }
//        return bmp;
//    }


    public static SparseArray<Barcode> detect(Context context, Bitmap bitmap, int... var1) {
        try {
            BarcodeDetector.Builder builder = new BarcodeDetector.Builder(context);

            for (int i : var1) {
                builder.setBarcodeFormats(i);
            }
            int rotate = 0;
            BarcodeDetector detector = builder.build();
            if (!detector.isOperational()) {
                detector.release();
                return new SparseArray<Barcode>();
            }

            // try  90 rotate * 4
            while (rotate != 4) {
                SparseArray<Barcode> array = detector.detect(new Frame.Builder().setBitmap(bitmap).build());
                if (array.size() != 0) {
                    detector.release();
                    return array;
                }
                bitmap = rotate(rotate, bitmap);
                rotate++;
            }
            return new SparseArray<Barcode>();
        } catch (Exception e) {
            return new SparseArray<Barcode>();
        }
    }

    private static Bitmap rotate(int rotate, Bitmap bitmap) {
        Matrix vMatrix = new Matrix();
        vMatrix.setRotate(rotate == 4 ? 45 : 90);
        // width
        // height
        return Bitmap.createBitmap(bitmap, 0, 0
            , bitmap.getWidth()   // width
            , bitmap.getHeight()  // height
            , vMatrix
            , true
        );
    }

    public static SparseArray<Barcode> detect(Context context, String path, int... var1) {
        Bitmap bitmap = BitmapHelper.getBitmapFromLocal(path);
        return detect(context, bitmap, var1);
    }

    public static SparseArray<Barcode> detect(Context context, ImageView view, int... var1) {
        return detect(context, view.getDrawable(), var1);
    }

    public static SparseArray<Barcode> detect(Context context, Drawable drawable, int... var1) {
        Bitmap bitmap = BitmapHelper.drawableToBitmap(drawable);
        return detect(context, bitmap, var1);
    }


}
