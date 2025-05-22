package tw.com.chainsea.android.common.image;

/**
 * ImagesHelper.setResourceByName 使用，
 * 指定 resource 類型
 *
 * @author Evan Wang
 * @version 0.0.1
 * @since 0.0.1
 */
public enum DefType {
    DRAWABLE("drawable"),
    MINMAP("minmap");

    private String type;

    DefType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
