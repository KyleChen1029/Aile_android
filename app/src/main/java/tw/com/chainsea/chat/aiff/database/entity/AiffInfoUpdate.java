package tw.com.chainsea.chat.aiff.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AiffInfoUpdate {
    @PrimaryKey
    @ColumnInfo(name = "id")
    private String id = "";
    @ColumnInfo(name = "tenantId")
    private String tenantId;
    @ColumnInfo(name = "index")
    private Integer index;
    @ColumnInfo(name = "displayLocation")
    private String displayLocation;
    @ColumnInfo(name = "pictureId")
    private String pictureId;
    @ColumnInfo(name = "url")
    private String url;
    @ColumnInfo(name = "userType")
    private String userType;
    @ColumnInfo(name = "embedLocation")
    private String embedLocation;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "displayType")
    private String displayType;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "aiffURL")
    private String aiffURL;
    @ColumnInfo(name = "applyType")
    private String applyType;
    @ColumnInfo(name = "closeInAiff")
    private Boolean closeInAiff;
    @ColumnInfo(name = "tenantId$")
    private String tenantId$;
    @ColumnInfo(name ="incomingAiff")
    private String incomingAiff;
    @ColumnInfo(name="serviceNumberIds")
    private String serviceNumberIds;
    @ColumnInfo(name="userType_APP")
    private String userType_APP;
    @ColumnInfo(name="serviceNumberNames")
    private String serviceNumberNames;
    @ColumnInfo(name="status")
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getDisplayLocation() {
        return displayLocation;
    }

    public void setDisplayLocation(String displayLocation) {
        this.displayLocation = displayLocation;
    }

    public String getPictureId() {
        return pictureId;
    }

    public void setPictureId(String pictureId) {
        this.pictureId = pictureId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getEmbedLocation() {
        return embedLocation;
    }

    public void setEmbedLocation(String embedLocation) {
        this.embedLocation = embedLocation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayType() {
        return displayType;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAiffURL() {
        return aiffURL;
    }

    public void setAiffURL(String aiffURL) {
        this.aiffURL = aiffURL;
    }

    public String getApplyType() {
        return applyType;
    }

    public void setApplyType(String applyType) {
        this.applyType = applyType;
    }

    public Boolean getCloseInAiff() {
        return closeInAiff;
    }

    public void setCloseInAiff(Boolean closeInAiff) {
        this.closeInAiff = closeInAiff;
    }

    public String getTenantId$() {
        return tenantId$;
    }

    public void setTenantId$(String tenantId$) {
        this.tenantId$ = tenantId$;
    }

    public String getIncomingAiff() {
        return incomingAiff;
    }

    public void setIncomingAiff(String incomingAiff) {
        this.incomingAiff = incomingAiff;
    }

    public String getServiceNumberIds() {
        return serviceNumberIds;
    }

    public void setServiceNumberIds(String serviceNumberIds) {
        this.serviceNumberIds = serviceNumberIds;
    }

    public String getServiceNumberNames() {
        return serviceNumberNames;
    }
    public void setServiceNumberNames(String serviceNumberNames) {
        this.serviceNumberNames = serviceNumberNames;
    }
    public String getUserType_APP() {
        return userType_APP;

    }
    public void setUserType_APP(String userType_APP) {
        this.userType_APP = userType_APP;
    }
    public String getStatus() {
        return status;
    }

    public AiffInfoUpdate(@NonNull String id, String tenantId, Integer index, String displayLocation,
                          String pictureId, String url, String userType, String embedLocation, String title,
                          String displayType, String name, String aiffURL, String applyType, Boolean closeInAiff,
                          String tenantId$, String incomingAiff, String serviceNumberIds, String userType_APP,
                          String serviceNumberNames, String status) {
        this.id = id;
        this.tenantId = tenantId;
        this.index = index;
        this.displayLocation = displayLocation;
        this.pictureId = pictureId;
        this.url = url;
        this.userType = userType;
        this.embedLocation = embedLocation;
        this.title = title;
        this.displayType = displayType;
        this.name = name;
        this.aiffURL = aiffURL;
        this.applyType = applyType;
        this.closeInAiff = closeInAiff;
        this.tenantId$ = tenantId$;
        this.incomingAiff = incomingAiff;
        this.serviceNumberIds = serviceNumberIds;
        this.userType_APP = userType_APP;
        this.serviceNumberNames = serviceNumberNames;
        this.status = status;
    }
}
