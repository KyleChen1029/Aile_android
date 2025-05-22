package tw.com.chainsea.chat.messagekit.main.adapter;

/**
 * current by evan on 2020/5/5
 *
 * @author Evan Wang
 * date 2020/5/5
 */
public enum MessageAdapterMode {
    DEFAULT("default"),
    SELECTION("selection"),
    RANGE_SELECTION("rangeSelection");

    private String mode;

    MessageAdapterMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
