package tw.com.chainsea.chat.view.vision;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.tenant.EnvironmentInfo;

/**
 * current by evan on 12/16/20
 *
 * @author Evan Wang
 * date 12/16/20
 */

public class ScanResultBean {
    private String userId;
    private String serviceNumberId;
    private EnvironmentInfo environmentInfo;

    ScanResultBean(String userId, String serviceNumberId, EnvironmentInfo environmentInfo) {
        this.userId = userId;
        this.serviceNumberId = serviceNumberId;
        this.environmentInfo = environmentInfo;
    }

    public static ScanResultBeanBuilder Build() {
        return new ScanResultBeanBuilder();
    }

    //    {"name": "AI3人工智能", "description": "人工智能團隊歡迎您", "url": "http://csce.qbicloud.com:16922", "code": "84459043-01"}


    public int getAction() {
        if (!Strings.isNullOrEmpty(userId)) {
            return 0;
        }

        if (!Strings.isNullOrEmpty(serviceNumberId)) {
            return 1;
        }
        if (environmentInfo != null) {
            return 2;
        }
        return -1;
    }

    public String toJson() {
        return JsonHelper.getInstance().toJson(this);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getServiceNumberId() {
        return serviceNumberId;
    }

    public void setServiceNumberId(String serviceNumberId) {
        this.serviceNumberId = serviceNumberId;
    }

    public EnvironmentInfo getEnvironmentInfo() {
        return environmentInfo;
    }

    public void setEnvironmentInfo(EnvironmentInfo environmentInfo) {
        this.environmentInfo = environmentInfo;
    }

    public ScanResultBeanBuilder toBuilder() {
        return new ScanResultBeanBuilder().userId(this.userId).serviceNumberId(this.serviceNumberId).environmentInfo(this.environmentInfo);
    }

    public static class ScanResultBeanBuilder {
        private String userId;
        private String serviceNumberId;
        private EnvironmentInfo environmentInfo;

        ScanResultBeanBuilder() {
        }

        public ScanResultBeanBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public ScanResultBeanBuilder serviceNumberId(String serviceNumberId) {
            this.serviceNumberId = serviceNumberId;
            return this;
        }

        public ScanResultBeanBuilder environmentInfo(EnvironmentInfo environmentInfo) {
            this.environmentInfo = environmentInfo;
            return this;
        }

        public ScanResultBean build() {
            return new ScanResultBean(userId, serviceNumberId, environmentInfo);
        }

        @NonNull
        public String toString() {
            return "ScanResultBean.ScanResultBeanBuilder(userId=" + this.userId + ", serviceNumberId=" + this.serviceNumberId + ", environmentInfo=" + this.environmentInfo + ")";
        }
    }
}
