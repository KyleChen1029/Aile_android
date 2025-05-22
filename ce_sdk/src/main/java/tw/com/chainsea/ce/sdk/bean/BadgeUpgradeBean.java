package tw.com.chainsea.ce.sdk.bean;

public class BadgeUpgradeBean {
    private int num;
    private String tag;

    public BadgeUpgradeBean(int num, String tag) {
        this.num = num;
        this.tag = tag;
    }

    public int getNum() {
        return num;
    }

    public String getTag() {
        return tag;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
