package tw.com.chainsea.ce.sdk.event;

import com.google.common.base.Strings;

public class EventMsg<T> {
    private int code;
    private T data;
    private String str = "";


    public EventMsg(int code) {
        this.code = code;
    }

    public EventMsg(int code, T data) {
        if (data instanceof String) {
            str = (String) data;
        }
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public String getString() {
        return Strings.isNullOrEmpty(str) ? "" : str;
    }

    public void setData(T data) {
        this.data = data;
    }

}
