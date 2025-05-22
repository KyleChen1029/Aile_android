package tw.com.chainsea.ce.sdk.bean.business;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.google.common.collect.ComparisonChain;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import tw.com.chainsea.ce.sdk.bean.common.EnableType;
import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.database.DBContract;

/**
 * current by evan on 2020-09-07
 *
 * @author Evan Wang
 * date 2020-09-07
 */
public class BusinessEntity implements Serializable, Comparable<BusinessEntity> {
    private static final long serialVersionUID = 8896643575180393271L;

    @SerializedName(value = "businessId", alternate = {"id"})
    private String id;

    @SerializedName(value = "businessCode", alternate = {"code"})
    private BusinessCode code = BusinessCode.TASK;

    @SerializedName(value = "name")
    private String name;
    @SerializedName(value = "description")
    private String description;
    @SerializedName(value = "endTimestamp")
    private long endTimestamp;
    private String endTime;

    @SerializedName(value = "businessManagerId", alternate = {"businessManager"})
    private String managerId = "";
    @SerializedName(value = "businessManagerAvatarId")
    private String managerAvatarId = "";
    @SerializedName(value = "businessManagerName")
    private String managerName = "";

    @SerializedName(value = "businessExecutorId", alternate = {"businessExecutor"})
    private String executorId = "";
    @SerializedName(value = "businessExecutorAvatarId")
    private String executorAvatarId = "";
    @SerializedName(value = "businessExecutorName")
    private String executorName = "";

    @SerializedName(value = "businessCustomerId", alternate = {"businessCustomer"})
    private String customerId = "";
    @SerializedName(value = "businessCustomerAvatarId")
    private String customerAvatarId = "";
    @SerializedName(value = "businessCustomerName")
    private String customerName = "";


    @SerializedName(value = "businessPrimaryId")
    private String primaryId = "";
    @SerializedName(value = "businessPrimaryName")
    private String primaryName = "";

    // local control
    private String avatarUrl;
    private long updateTime;

    private EnableType enable = EnableType.Y;

    public BusinessEntity(String id, BusinessCode code, String name, String description, long endTimestamp, String endTime, String managerId, String managerAvatarId, String managerName, String executorId, String executorAvatarId, String executorName, String customerId, String customerAvatarId, String customerName, String primaryId, String primaryName, String avatarUrl, long updateTime, EnableType enable) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.endTimestamp = endTimestamp;
        this.endTime = endTime;
        this.managerId = managerId;
        this.managerAvatarId = managerAvatarId;
        this.managerName = managerName;
        this.executorId = executorId;
        this.executorAvatarId = executorAvatarId;
        this.executorName = executorName;
        this.customerId = customerId;
        this.customerAvatarId = customerAvatarId;
        this.customerName = customerName;
        this.primaryId = primaryId;
        this.primaryName = primaryName;
        this.avatarUrl = avatarUrl;
        this.updateTime = updateTime;
        this.enable = enable;
    }

    public BusinessEntity() {
    }


    public static BusinessEntity formatByCursor(Cursor cursor, long max) {
        BusinessEntity entity = BusinessEntity.Build()
            .id(Tools.getDbString(cursor, DBContract.BusinessEntry._ID))
            .name(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_NAME))
            .code(BusinessCode.valueOf(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_BUSINESS_CODE)))
            .endTime(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_END_TIME))
            .endTimestamp(Tools.getDbLong(cursor, DBContract.BusinessEntry.COLUMN_END_TIMESTAMP))

            .primaryId(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_PRIMARY_ID))
            .primaryName(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_PRIMARY_NAME))

            .managerId(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_MANAGER_ID))
            .managerAvatarId(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_MANAGER_AVATAR_ID))
            .managerName(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_MANAGER_NAME))

            .executorId(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_EXECUTOR_ID))
            .executorAvatarId(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_EXECUTOR_AVATAR_ID))
            .executorName(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_EXECUTOR_NAME))

            .customerId(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_CUSTOMER_ID))
            .customerAvatarId(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_CUSTOMER_AVATAR_ID))
            .customerName(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_CUSTOMER_NAME))

            .description(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_DESCRIPTION))
            .enable(EnableType.valueOf(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_IS_ENABLE)))
            .build();


        entity.setUpdateTime(max > 0 ? max : -entity.getEndTimestamp());

        return entity;

    }


    public static ContentValues getContentValues(BusinessEntity entity) {
        ContentValues values = new ContentValues();

        values.put(DBContract.BusinessEntry._ID, entity.getId());
        values.put(DBContract.BusinessEntry.COLUMN_NAME, entity.getName());
        values.put(DBContract.BusinessEntry.COLUMN_END_TIME, entity.getEndTime());
        values.put(DBContract.BusinessEntry.COLUMN_END_TIMESTAMP, entity.getEndTimestamp());
        values.put(DBContract.BusinessEntry.COLUMN_BUSINESS_CODE, entity.getCode() == null ? BusinessCode.TASK.name() : entity.getCode().name());
        values.put(DBContract.BusinessEntry.COLUMN_PRIMARY_ID, entity.getPrimaryId());
        values.put(DBContract.BusinessEntry.COLUMN_PRIMARY_NAME, entity.getPrimaryName());

        values.put(DBContract.BusinessEntry.COLUMN_MANAGER_ID, entity.getManagerId());
        values.put(DBContract.BusinessEntry.COLUMN_MANAGER_AVATAR_ID, entity.getManagerAvatarId());
        values.put(DBContract.BusinessEntry.COLUMN_MANAGER_NAME, entity.getManagerName());

        values.put(DBContract.BusinessEntry.COLUMN_EXECUTOR_ID, entity.getExecutorId());
        values.put(DBContract.BusinessEntry.COLUMN_EXECUTOR_AVATAR_ID, entity.getExecutorAvatarId());
        values.put(DBContract.BusinessEntry.COLUMN_EXECUTOR_NAME, entity.getExecutorName());

        values.put(DBContract.BusinessEntry.COLUMN_CUSTOMER_ID, entity.getCustomerId());
        values.put(DBContract.BusinessEntry.COLUMN_CUSTOMER_AVATAR_ID, entity.getCustomerAvatarId());
        values.put(DBContract.BusinessEntry.COLUMN_CUSTOMER_NAME, entity.getCustomerName());

        values.put(DBContract.BusinessEntry.COLUMN_DESCRIPTION, entity.getDescription());
        values.put(DBContract.BusinessEntry.COLUMN_IS_ENABLE, EnableType.Y.name());
        return values;
    }

    private static BusinessCode $default$code() {
        return BusinessCode.TASK;
    }

    private static String $default$managerId() {
        return "";
    }

    private static String $default$managerAvatarId() {
        return "";
    }

    private static String $default$managerName() {
        return "";
    }

    private static String $default$executorId() {
        return "";
    }

    private static String $default$executorAvatarId() {
        return "";
    }

    private static String $default$executorName() {
        return "";
    }

    private static String $default$customerId() {
        return "";
    }

    private static String $default$customerAvatarId() {
        return "";
    }

    private static String $default$customerName() {
        return "";
    }

    private static String $default$primaryId() {
        return "";
    }

    private static String $default$primaryName() {
        return "";
    }

    private static EnableType $default$enable() {
        return EnableType.Y;
    }

    public static BusinessEntityBuilder Build() {
        return new BusinessEntityBuilder();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BusinessEntity other = (BusinessEntity) obj;
        if (this.id == null || other.getId() == null) {
            return false;
        } else return this.id.equals(other.getId());
    }

    public long getWeights() {
        if (this.updateTime > 0) {
            return this.updateTime;
        } else {
            return -this.endTimestamp;
        }
    }


    @Override
    public int compareTo(BusinessEntity o) {
        return ComparisonChain.start()
            .compare(o.getWeights(), this.getWeights())
            .result();
    }

    public String getId() {
        return this.id;
    }

    public BusinessCode getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public long getEndTimestamp() {
        return this.endTimestamp;
    }

    public String getEndTime() {
        return this.endTime;
    }

    public String getManagerId() {
        return this.managerId;
    }

    public String getManagerAvatarId() {
        return this.managerAvatarId;
    }

    public String getManagerName() {
        return this.managerName;
    }

    public String getExecutorId() {
        return this.executorId;
    }

    public String getExecutorAvatarId() {
        return this.executorAvatarId;
    }

    public String getExecutorName() {
        return this.executorName;
    }

    public String getCustomerId() {
        return this.customerId;
    }

    public String getCustomerAvatarId() {
        return this.customerAvatarId;
    }

    public String getCustomerName() {
        return this.customerName;
    }

    public String getPrimaryId() {
        return this.primaryId;
    }

    public String getPrimaryName() {
        return this.primaryName;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public long getUpdateTime() {
        return this.updateTime;
    }

    public EnableType getEnable() {
        return this.enable;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCode(BusinessCode code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public void setManagerAvatarId(String managerAvatarId) {
        this.managerAvatarId = managerAvatarId;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public void setExecutorId(String executorId) {
        this.executorId = executorId;
    }

    public void setExecutorAvatarId(String executorAvatarId) {
        this.executorAvatarId = executorAvatarId;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setCustomerAvatarId(String customerAvatarId) {
        this.customerAvatarId = customerAvatarId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    public void setPrimaryName(String primaryName) {
        this.primaryName = primaryName;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public void setEnable(EnableType enable) {
        this.enable = enable;
    }

    @NonNull
    public String toString() {
        return "BusinessEntity(id=" + this.getId() + ", code=" + this.getCode() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", endTimestamp=" + this.getEndTimestamp() + ", endTime=" + this.getEndTime() + ", managerId=" + this.getManagerId() + ", managerAvatarId=" + this.getManagerAvatarId() + ", managerName=" + this.getManagerName() + ", executorId=" + this.getExecutorId() + ", executorAvatarId=" + this.getExecutorAvatarId() + ", executorName=" + this.getExecutorName() + ", customerId=" + this.getCustomerId() + ", customerAvatarId=" + this.getCustomerAvatarId() + ", customerName=" + this.getCustomerName() + ", primaryId=" + this.getPrimaryId() + ", primaryName=" + this.getPrimaryName() + ", avatarUrl=" + this.getAvatarUrl() + ", updateTime=" + this.getUpdateTime() + ", enable=" + this.getEnable() + ")";
    }

    public BusinessEntityBuilder toBuilder() {
        return new BusinessEntityBuilder().id(this.id).code(this.code).name(this.name).description(this.description).endTimestamp(this.endTimestamp).endTime(this.endTime).managerId(this.managerId).managerAvatarId(this.managerAvatarId).managerName(this.managerName).executorId(this.executorId).executorAvatarId(this.executorAvatarId).executorName(this.executorName).customerId(this.customerId).customerAvatarId(this.customerAvatarId).customerName(this.customerName).primaryId(this.primaryId).primaryName(this.primaryName).avatarUrl(this.avatarUrl).updateTime(this.updateTime).enable(this.enable);
    }

    public static class BusinessEntityBuilder {
        private String id;
        private BusinessCode code$value;
        private boolean code$set;
        private String name;
        private String description;
        private long endTimestamp;
        private String endTime;
        private String managerId$value;
        private boolean managerId$set;
        private String managerAvatarId$value;
        private boolean managerAvatarId$set;
        private String managerName$value;
        private boolean managerName$set;
        private String executorId$value;
        private boolean executorId$set;
        private String executorAvatarId$value;
        private boolean executorAvatarId$set;
        private String executorName$value;
        private boolean executorName$set;
        private String customerId$value;
        private boolean customerId$set;
        private String customerAvatarId$value;
        private boolean customerAvatarId$set;
        private String customerName$value;
        private boolean customerName$set;
        private String primaryId$value;
        private boolean primaryId$set;
        private String primaryName$value;
        private boolean primaryName$set;
        private String avatarUrl;
        private long updateTime;
        private EnableType enable$value;
        private boolean enable$set;

        BusinessEntityBuilder() {
        }

        public BusinessEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BusinessEntityBuilder code(BusinessCode code) {
            this.code$value = code;
            this.code$set = true;
            return this;
        }

        public BusinessEntityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public BusinessEntityBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BusinessEntityBuilder endTimestamp(long endTimestamp) {
            this.endTimestamp = endTimestamp;
            return this;
        }

        public BusinessEntityBuilder endTime(String endTime) {
            this.endTime = endTime;
            return this;
        }

        public BusinessEntityBuilder managerId(String managerId) {
            this.managerId$value = managerId;
            this.managerId$set = true;
            return this;
        }

        public BusinessEntityBuilder managerAvatarId(String managerAvatarId) {
            this.managerAvatarId$value = managerAvatarId;
            this.managerAvatarId$set = true;
            return this;
        }

        public BusinessEntityBuilder managerName(String managerName) {
            this.managerName$value = managerName;
            this.managerName$set = true;
            return this;
        }

        public BusinessEntityBuilder executorId(String executorId) {
            this.executorId$value = executorId;
            this.executorId$set = true;
            return this;
        }

        public BusinessEntityBuilder executorAvatarId(String executorAvatarId) {
            this.executorAvatarId$value = executorAvatarId;
            this.executorAvatarId$set = true;
            return this;
        }

        public BusinessEntityBuilder executorName(String executorName) {
            this.executorName$value = executorName;
            this.executorName$set = true;
            return this;
        }

        public BusinessEntityBuilder customerId(String customerId) {
            this.customerId$value = customerId;
            this.customerId$set = true;
            return this;
        }

        public BusinessEntityBuilder customerAvatarId(String customerAvatarId) {
            this.customerAvatarId$value = customerAvatarId;
            this.customerAvatarId$set = true;
            return this;
        }

        public BusinessEntityBuilder customerName(String customerName) {
            this.customerName$value = customerName;
            this.customerName$set = true;
            return this;
        }

        public BusinessEntityBuilder primaryId(String primaryId) {
            this.primaryId$value = primaryId;
            this.primaryId$set = true;
            return this;
        }

        public BusinessEntityBuilder primaryName(String primaryName) {
            this.primaryName$value = primaryName;
            this.primaryName$set = true;
            return this;
        }

        public BusinessEntityBuilder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public BusinessEntityBuilder updateTime(long updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public BusinessEntityBuilder enable(EnableType enable) {
            this.enable$value = enable;
            this.enable$set = true;
            return this;
        }

        public BusinessEntity build() {
            BusinessCode code$value = this.code$value;
            if (!this.code$set) {
                code$value = BusinessEntity.$default$code();
            }
            String managerId$value = this.managerId$value;
            if (!this.managerId$set) {
                managerId$value = BusinessEntity.$default$managerId();
            }
            String managerAvatarId$value = this.managerAvatarId$value;
            if (!this.managerAvatarId$set) {
                managerAvatarId$value = BusinessEntity.$default$managerAvatarId();
            }
            String managerName$value = this.managerName$value;
            if (!this.managerName$set) {
                managerName$value = BusinessEntity.$default$managerName();
            }
            String executorId$value = this.executorId$value;
            if (!this.executorId$set) {
                executorId$value = BusinessEntity.$default$executorId();
            }
            String executorAvatarId$value = this.executorAvatarId$value;
            if (!this.executorAvatarId$set) {
                executorAvatarId$value = BusinessEntity.$default$executorAvatarId();
            }
            String executorName$value = this.executorName$value;
            if (!this.executorName$set) {
                executorName$value = BusinessEntity.$default$executorName();
            }
            String customerId$value = this.customerId$value;
            if (!this.customerId$set) {
                customerId$value = BusinessEntity.$default$customerId();
            }
            String customerAvatarId$value = this.customerAvatarId$value;
            if (!this.customerAvatarId$set) {
                customerAvatarId$value = BusinessEntity.$default$customerAvatarId();
            }
            String customerName$value = this.customerName$value;
            if (!this.customerName$set) {
                customerName$value = BusinessEntity.$default$customerName();
            }
            String primaryId$value = this.primaryId$value;
            if (!this.primaryId$set) {
                primaryId$value = BusinessEntity.$default$primaryId();
            }
            String primaryName$value = this.primaryName$value;
            if (!this.primaryName$set) {
                primaryName$value = BusinessEntity.$default$primaryName();
            }
            EnableType enable$value = this.enable$value;
            if (!this.enable$set) {
                enable$value = BusinessEntity.$default$enable();
            }
            return new BusinessEntity(id, code$value, name, description, endTimestamp, endTime, managerId$value, managerAvatarId$value, managerName$value, executorId$value, executorAvatarId$value, executorName$value, customerId$value, customerAvatarId$value, customerName$value, primaryId$value, primaryName$value, avatarUrl, updateTime, enable$value);
        }

        @NonNull
        public String toString() {
            return "BusinessEntity.BusinessEntityBuilder(id=" + this.id + ", code$value=" + this.code$value + ", name=" + this.name + ", description=" + this.description + ", endTimestamp=" + this.endTimestamp + ", endTime=" + this.endTime + ", managerId$value=" + this.managerId$value + ", managerAvatarId$value=" + this.managerAvatarId$value + ", managerName$value=" + this.managerName$value + ", executorId$value=" + this.executorId$value + ", executorAvatarId$value=" + this.executorAvatarId$value + ", executorName$value=" + this.executorName$value + ", customerId$value=" + this.customerId$value + ", customerAvatarId$value=" + this.customerAvatarId$value + ", customerName$value=" + this.customerName$value + ", primaryId$value=" + this.primaryId$value + ", primaryName$value=" + this.primaryName$value + ", avatarUrl=" + this.avatarUrl + ", updateTime=" + this.updateTime + ", enable$value=" + this.enable$value + ")";
        }
    }
}
