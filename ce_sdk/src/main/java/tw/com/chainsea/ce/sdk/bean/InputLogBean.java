package tw.com.chainsea.ce.sdk.bean;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import tw.com.chainsea.android.common.json.JsonHelper;

/**
 * current by evan on 2020-06-16
 *
 * @author Evan Wang
 * date 2020-06-16
 */
public class InputLogBean {

    @SerializedName("type")
    private InputLogType type;
    private boolean isTheme = false;
    private String id;
    private String text;

    InputLogBean(InputLogType type, boolean isTheme, String id, String text) {
        this.type = type;
        this.isTheme = isTheme;
        this.id = id;
        this.text = text;
    }

    private static boolean $default$isTheme() {
        return false;
    }

    public static InputLogBeanBuilder Build() {
        return new InputLogBeanBuilder();
    }


    public String toJson() {
        return JsonHelper.getInstance().toJson(this);
    }

    public static InputLogBean from(String json) {
        if (Strings.isNullOrEmpty(json)) {
            return InputLogBean.Build().id("").type(InputLogType.TEXT).text("").build();
        }
        try {
            InputLogBean bean = JsonHelper.getInstance().from(json, InputLogBean.class);
            if (bean == null) {
                throw new JsonSyntaxException("");
            }
            return bean;
        } catch (JsonSyntaxException e) {
            return InputLogBean.Build().id("").type(InputLogType.TEXT).text(json).build();
        }
    }

    public InputLogType getType() {
        return type;
    }

    public void setType(InputLogType type) {
        this.type = type;
    }

    public boolean isTheme() {
        return isTheme;
    }

    public void setTheme(boolean theme) {
        isTheme = theme;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public InputLogBeanBuilder toBuilder() {
        return new InputLogBeanBuilder().type(this.type).isTheme(this.isTheme).id(this.id).text(this.text);
    }

    public static class InputLogBeanBuilder {
        private InputLogType type;
        private boolean isTheme$value;
        private boolean isTheme$set;
        private String id;
        private String text;

        InputLogBeanBuilder() {
        }

        public InputLogBeanBuilder type(InputLogType type) {
            this.type = type;
            return this;
        }

        public InputLogBeanBuilder isTheme(boolean isTheme) {
            this.isTheme$value = isTheme;
            this.isTheme$set = true;
            return this;
        }

        public InputLogBeanBuilder id(String id) {
            this.id = id;
            return this;
        }

        public InputLogBeanBuilder text(String text) {
            this.text = text;
            return this;
        }

        public InputLogBean build() {
            boolean isTheme$value = this.isTheme$value;
            if (!this.isTheme$set) {
                isTheme$value = InputLogBean.$default$isTheme();
            }
            return new InputLogBean(type, isTheme$value, id, text);
        }

        @NonNull
        public String toString() {
            return "InputLogBean.InputLogBeanBuilder(type=" + this.type + ", isTheme$value=" + this.isTheme$value + ", id=" + this.id + ", text=" + this.text + ")";
        }
    }
}
