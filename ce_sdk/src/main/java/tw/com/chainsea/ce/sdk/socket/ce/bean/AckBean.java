package tw.com.chainsea.ce.sdk.socket.ce.bean;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;

/**
 * current by evan on 2020-11-11
 *
 * @author Evan Wang
 * date 2020-11-11
 */
public class AckBean {
    private String event;
    private String action;
    private String code;
    private boolean ack;
    private String deviceName;
    private String content;

    AckBean(String event, String action, String code, boolean ack, String deviceName, String content) {
        this.event = event;
        this.action = action;
        this.code = code;
        this.ack = ack;
        this.deviceName = deviceName;
        this.content = content;
    }

    public static AckBeanBuilder Build() {
        return new AckBeanBuilder();
    }


    public String toJson() {
        return JsonHelper.getInstance().toJson(this);

    }

    public JSONObject toJsonObject() throws JSONException {
        return JsonHelper.getInstance().toJsonObject(this);
    }

    public AckBeanBuilder toBuilder() {
        return new AckBeanBuilder().event(this.event).action(this.action).code(this.code).ack(this.ack).deviceName(this.deviceName).content(this.content);
    }

    public static class AckBeanBuilder {
        private String event;
        private String action;
        private String code;
        private boolean ack;
        private String deviceName;
        private String content;

        AckBeanBuilder() {
        }

        public AckBeanBuilder event(String event) {
            this.event = event;
            return this;
        }

        public AckBeanBuilder action(String action) {
            this.action = action;
            return this;
        }

        public AckBeanBuilder code(String code) {
            this.code = code;
            return this;
        }

        public AckBeanBuilder ack(boolean ack) {
            this.ack = ack;
            return this;
        }

        public AckBeanBuilder deviceName(String deviceName) {
            this.deviceName = deviceName;
            return this;
        }

        public AckBeanBuilder content(String content) {
            this.content = content;
            return this;
        }

        public AckBean build() {
            return new AckBean(event, action, code, ack, deviceName, content);
        }

        @NonNull
        public String toString() {
            return "AckBean.AckBeanBuilder(event=" + this.event + ", action=" + this.action + ", code=" + this.code + ", ack=" + this.ack + ", deviceName=" + this.deviceName + ", content=" + this.content + ")";
        }
    }
}
