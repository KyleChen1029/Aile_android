package tw.com.chainsea.ce.sdk.http.cp.respone;

import java.util.List;

import tw.com.chainsea.ce.sdk.http.cp.base.BaseResponse;

public class IndustryScaleResponse extends BaseResponse {

    private List<DictionaryItems> dictionaryItems;

    public List<DictionaryItems> getDictionaryItems() {
        return dictionaryItems;
    }

    public static class DictionaryItems {
        private String treeSerial;
        private Integer treeLevel;
        private Integer index;
        private String dictionaryId;
        private String text;
        private String id;
        private String value;
        private Boolean enabled;
        private Boolean empty;

        public String getTreeSerial() {
            return treeSerial;
        }

        public void setTreeSerial(String treeSerial) {
            this.treeSerial = treeSerial;
        }

        public Integer getTreeLevel() {
            return treeLevel;
        }

        public void setTreeLevel(Integer treeLevel) {
            this.treeLevel = treeLevel;
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public String getDictionaryId() {
            return dictionaryId;
        }

        public void setDictionaryId(String dictionaryId) {
            this.dictionaryId = dictionaryId;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Boolean getEmpty() {
            return empty;
        }

        public void setEmpty(Boolean empty) {
            this.empty = empty;
        }
    }
}
