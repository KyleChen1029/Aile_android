package tw.com.chainsea.ce.sdk.bean.business;

import android.os.Parcel;

import java.io.Serializable;
import java.util.List;

public class BusinessItems implements Serializable {
    private static final long serialVersionUID = -8830526166947866510L;

    /**
     * serviceNumberId : 167cfae3-b900-0062-2c04-000c2994b3ab
     */
    private String serviceNumberId;
    private List<BusinessItemsBean> businessItems;

    public String getServiceNumberId() {
        return serviceNumberId;
    }

    public void setServiceNumberId(String serviceNumberId) {
        this.serviceNumberId = serviceNumberId;
    }

    public List<BusinessItemsBean> getBusinessItems() {
        return businessItems;
    }

    public void setBusinessItems(List<BusinessItemsBean> businessItems) {
        this.businessItems = businessItems;
    }


    public static class BusinessItemsBean implements Serializable {
        /**
         * businessCode : Ecp.Task
         */
        private String businessCode;
        private String name;
        private List<FieldsBean> fields;

        public String getBusinessCode() {
            return businessCode;
        }

        public void setBusinessCode(String businessCode) {
            this.businessCode = businessCode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<FieldsBean> getFields() {
            return fields;
        }

        public void setFields(List<FieldsBean> fields) {
            this.fields = fields;
        }

        public static class FieldsBean implements Serializable {
            /**
             * visible : true
             * name : serialNumber
             * index : 2
             * control : String
             * title : 問題編號
             * maxLength : 0
             * hasImage : true
             * hasVoice : true
             * hasQRCode : true
             */

            private boolean visible;
            private String name;
            private int index;
            private String control;
            private String title;
            private int maxLength;
            private boolean hasImage;
            private boolean hasVoice;
            private boolean hasQRCode;

            protected FieldsBean(Parcel in) {
                visible = in.readByte() != 0;
                name = in.readString();
                index = in.readInt();
                control = in.readString();
                title = in.readString();
                maxLength = in.readInt();
                hasImage = in.readByte() != 0;
                hasVoice = in.readByte() != 0;
                hasQRCode = in.readByte() != 0;
            }

            public boolean isVisible() {
                return visible;
            }

            public void setVisible(boolean visible) {
                this.visible = visible;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public int getIndex() {
                return index;
            }

            public void setIndex(int index) {
                this.index = index;
            }

            public String getControl() {
                return control;
            }

            public void setControl(String control) {
                this.control = control;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public int getMaxLength() {
                return maxLength;
            }

            public void setMaxLength(int maxLength) {
                this.maxLength = maxLength;
            }

            public boolean isHasImage() {
                return hasImage;
            }

            public void setHasImage(boolean hasImage) {
                this.hasImage = hasImage;
            }

            public boolean isHasVoice() {
                return hasVoice;
            }

            public void setHasVoice(boolean hasVoice) {
                this.hasVoice = hasVoice;
            }

            public boolean isHasQRCode() {
                return hasQRCode;
            }

            public void setHasQRCode(boolean hasQRCode) {
                this.hasQRCode = hasQRCode;
            }

        }
    }
}
