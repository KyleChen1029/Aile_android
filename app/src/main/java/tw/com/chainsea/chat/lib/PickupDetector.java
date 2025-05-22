package tw.com.chainsea.chat.lib;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class PickupDetector {

    private final SensorManager manager;
    private Sensor mProximitysensor;

    private PickupDetectListener listener;

    public PickupDetector(Context context) {

        manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (manager != null) {
            mProximitysensor = manager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

    }

    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                float value = sensorEvent.values[0];
                boolean isPickUp = value == 0.0;
                //打开或者关闭屏幕
                if (listener != null) {
                    listener.onPickupDetected(isPickUp);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };


    public void register(PickupDetectListener listener) {
        this.listener = listener;
        if (manager != null) {
            manager.registerListener(sensorEventListener, mProximitysensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    public void unRegister() {
        if (manager != null) {
            manager.unregisterListener(sensorEventListener);
        }
        listener = null;  // Release the reference.
    }

    public interface PickupDetectListener {
        void onPickupDetected(boolean isPickingUp);
    }
}
