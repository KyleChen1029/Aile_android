package tw.com.chainsea.ce.sdk.bean.msg.content;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.Serializable;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.business.BusinessCode;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;

/**
 * current by evan on 2020-03-26
 *
 * @author Evan Wang
 * date 2020-03-26
 */

public class BusinessContent implements IMessageContent<MessageType>, Serializable {
    private static final String TAG = BusinessContent.class.getSimpleName();
    private static final long serialVersionUID = 721431533704289886L;

    @SerializedName(value = "businessId", alternate = {"id"})
    private String id;

    @SerializedName(value = "businessName", alternate = {"name"})
    private String name;
    @SerializedName(value = "businessPrimaryName", alternate = {"primaryName"})
    private String businessPrimaryName = "";
    @SerializedName(value = "businessEndTime", alternate = {"endTime"})
    private String endTime;
    @SerializedName(value = "businessEndtimestamp", alternate = {"endTimestamp"})
    private long endTimestamp;
    private String description;

    private String businessPrimaryId;
    @SerializedName(value = "businessManagerId", alternate = {"businessManager"})
    private String businessManagerId;
    private String businessManagerName = "";
    private String businessManagerAvatarId;

    @SerializedName(value = "businessExecutorId", alternate = {"businessExecutor"})
    private String businessExecutorId;
    private String businessExecutorName = "";
    private String businessExecutorAvatarId;

    @SerializedName(value = "businessCustomerId", alternate = {"businessCustomer"})
    private String businessCustomerId;
    private String businessCustomerName;
    private String businessCustomerAvatarId;

    @SerializedName(value = "businessCode", alternate = {"code"})
    private BusinessCode code = BusinessCode.TASK;


    // local control
    private String avatarUrl;

    public BusinessContent(String id, String name, BusinessCode code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public BusinessContent(String name, String businessPrimaryName, String endTime, long endTimestamp, String description, String businessManagerId, String businessManagerName, String businessManagerAvatarId, String businessExecutorId, String businessExecutorName, String businessExecutorAvatarId, BusinessCode code) {
        this.name = name;
        this.businessPrimaryName = businessPrimaryName;
        this.endTime = endTime;
        this.endTimestamp = endTimestamp;
        this.description = description;
        this.businessManagerId = businessManagerId;
        this.businessManagerName = businessManagerName;
        this.businessManagerAvatarId = businessManagerAvatarId;
        this.businessExecutorId = businessExecutorId;
        this.businessExecutorName = businessExecutorName;
        this.businessExecutorAvatarId = businessExecutorAvatarId;
        this.code = code;
    }

    @Override
    public MessageType getType() {
        return MessageType.BUSINESS;
    }

    @Override
    public String toStringContent() {
        return JsonHelper.getInstance().toJson(this);
    }


    public String toSendContent() {
        try {
            return new JSONObject()
                    .put("businessId", this.id)
                    .put("name", this.name)
                    .put("endTime", this.endTime)

//                    .put("businessCategory", "Task")
                    .put("code", this.code.getCode())

                    .put("businessManagerId", this.businessManagerId)
                    .put("businessManagerName", this.businessManagerName)
                    .put("businessManagerAvatarId", this.businessManagerAvatarId)

                    .put("businessExecutorId", this.businessExecutorId)
                    .put("businessExecutorName", this.businessExecutorName)
                    .put("businessExecutorAvatarId", this.businessExecutorAvatarId)

                    .put("businessCustomerId", this.businessCustomerId)
                    .put("businessCustomerName", this.businessCustomerName)
                    .put("businessCustomerAvatarId", this.businessCustomerAvatarId)

                    .put("description", this.description)

                    .put("endTimestamp", this.endTimestamp)
                    .put("businessPrimaryName", this.businessPrimaryName)

                    .toString();
        } catch (Exception e) {
            return "";
        }
    }


    @Override
    public String simpleContent() {
        return "[物件]";
    }

    @Override
    public String getFilePath() {
        return null;
    }

    @Override
    public JSONObject getSendObj() {
        return null;
    }

    public static String getTAG() {
        return TAG;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getBusinessPrimaryId() {
        return businessPrimaryId;
    }

    public void setBusinessPrimaryId(String businessPrimaryId) {
        this.businessPrimaryId = businessPrimaryId;
    }

    public String getBusinessPrimaryName() {
        return businessPrimaryName;
    }

    public void setBusinessPrimaryName(String businessPrimaryName) {
        this.businessPrimaryName = businessPrimaryName;
    }

    public String getBusinessManagerId() {
        return businessManagerId;
    }

    public void setBusinessManagerId(String businessManagerId) {
        this.businessManagerId = businessManagerId;
    }

    public String getBusinessManagerAvatarId() {
        return businessManagerAvatarId;
    }

    public void setBusinessManagerAvatarId(String businessManagerAvatarId) {
        this.businessManagerAvatarId = businessManagerAvatarId;
    }

    public String getBusinessManagerName() {
        return businessManagerName;
    }

    public void setBusinessManagerName(String businessManagerName) {
        this.businessManagerName = businessManagerName;
    }

    public String getBusinessExecutorId() {
        return businessExecutorId;
    }

    public void setBusinessExecutorId(String businessExecutorId) {
        this.businessExecutorId = businessExecutorId;
    }

    public String getBusinessExecutorAvatarId() {
        return businessExecutorAvatarId;
    }

    public void setBusinessExecutorAvatarId(String businessExecutorAvatarId) {
        this.businessExecutorAvatarId = businessExecutorAvatarId;
    }

    public String getBusinessExecutorName() {
        return businessExecutorName;
    }

    public void setBusinessExecutorName(String businessExecutorName) {
        this.businessExecutorName = businessExecutorName;
    }

    public String getBusinessCustomerId() {
        return businessCustomerId;
    }

    public void setBusinessCustomerId(String businessCustomerId) {
        this.businessCustomerId = businessCustomerId;
    }

    public String getBusinessCustomerName() {
        return businessCustomerName;
    }

    public void setBusinessCustomerName(String businessCustomerName) {
        this.businessCustomerName = businessCustomerName;
    }

    public String getBusinessCustomerAvatarId() {
        return businessCustomerAvatarId;
    }

    public void setBusinessCustomerAvatarId(String businessCustomerAvatarId) {
        this.businessCustomerAvatarId = businessCustomerAvatarId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BusinessCode getCode() {
        return code;
    }

    public void setCode(BusinessCode code) {
        this.code = code;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}