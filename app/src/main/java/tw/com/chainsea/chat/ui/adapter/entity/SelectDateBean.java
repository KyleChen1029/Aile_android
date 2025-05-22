package tw.com.chainsea.chat.ui.adapter.entity;

public class SelectDateBean {
    public String name;
    public boolean isCheck;

    public SelectDateBean(String name, boolean isCheck) {
        this.name = name;
        this.isCheck = isCheck;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }
}
