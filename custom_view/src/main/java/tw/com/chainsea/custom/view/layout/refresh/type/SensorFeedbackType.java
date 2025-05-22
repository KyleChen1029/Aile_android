package tw.com.chainsea.custom.view.layout.refresh.type;

/**
 * current by evan on 2019-12-26
 */
public enum SensorFeedbackType {
    _NONE(0),
    _BOUNCE(1),
    _END(2);

    private int type;

    SensorFeedbackType(int type) {
        this.type = type;
    }


    public int getType() {
        return type;
    }

    public static SensorFeedbackType typeOf(int type) {
        switch (type) {
            case 0:
                return _NONE;
            case 1:
                return _BOUNCE;
            case 2:
                return _END;
            default:
                return _NONE;
        }
    }
}
