//package tw.com.chainsea.ce.sdk.http.cp.respone;
//
//
//import com.google.gson.annotations.JsonAdapter;
//import com.squareup.moshi.JsonClass;
//
//import tw.com.chainsea.ce.sdk.http.cp.base.BaseResponse;
//import tw.com.chainsea.ce.sdk.http.cp.model.TransTenantInfo;
//
//@JsonClass(generateAdapter = true, generator = "java")
//public class CheckVersionResponse extends BaseResponse {
//    private String url;
//    private String version;
//    private String fileName;
//    private String versionName;
//    private boolean isToUpdate;
//    private String upgradeKind;
//    private String description;
//    private int size;
//
//    public String getUrl() {
//        return url;
//    }
//
//    public void setUrl(String url) {
//        this.url = url;
//    }
//
//    public String getVersion() {
//        return version;
//    }
//
//    public void setVersion(String version) {
//        this.version = version;
//    }
//
//    public String getFileName() {
//        return fileName;
//    }
//
//    public void setFileName(String fileName) {
//        this.fileName = fileName;
//    }
//
//    public String getVersionName() {
//        return versionName;
//    }
//
//    public void setVersionName(String versionName) {
//        this.versionName = versionName;
//    }
//
//    public boolean isToUpdate() {
//        return isToUpdate;
//    }
//
//    public void setToUpdate(boolean toUpdate) {
//        isToUpdate = toUpdate;
//    }
//
//    public String getUpgradeKind() {
//        return upgradeKind;
//    }
//
//    public void setUpgradeKind(String upgradeKind) {
//        this.upgradeKind = upgradeKind;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public int getSize() {
//        return size;
//    }
//
//    public void setSize(int size) {
//        this.size = size;
//    }
//}
