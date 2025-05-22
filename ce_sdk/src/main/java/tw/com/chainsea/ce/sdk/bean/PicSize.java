package tw.com.chainsea.ce.sdk.bean;
/**
 * PicSize
 * Created by 90Chris on 2016/7/1.
 */
public enum PicSize {
    SMALL("s", 80), //80
    MED("m", 200), // 200
    LARGE("l", 320); // 320

    private final String value;
    private final int size;

    public static PicSize ofValue(String value) {
        for (PicSize item : values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return LARGE;
    }

    PicSize(String value, int size) {
        this.value = value;
        this.size = size;
    }

    public String getValue() {
        return value;
    }

    public int getSize() {
        return size;
    }
}
