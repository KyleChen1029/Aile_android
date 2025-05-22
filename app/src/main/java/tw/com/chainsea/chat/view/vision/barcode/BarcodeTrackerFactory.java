package tw.com.chainsea.chat.view.vision.barcode;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

import tw.com.chainsea.chat.view.vision.widgets.GraphicOverlay;

/**
 * current by evan on 11/27/20
 *
 * @author Evan Wang
 * @date 11/27/20
 */
public class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;
    private Context mContext;

    public BarcodeTrackerFactory(GraphicOverlay<BarcodeGraphic> mGraphicOverlay,
                                 Context mContext) {
        this.mGraphicOverlay = mGraphicOverlay;
        this.mContext = mContext;
    }

    @Override
    @NonNull
    public Tracker<Barcode> create(@NonNull Barcode barcode) {
        BarcodeGraphic graphic = new BarcodeGraphic(mGraphicOverlay);
        return new BarcodeGraphicTracker(mGraphicOverlay, graphic, mContext);
    }
}
