package tw.com.chainsea.ce.sdk.bean.servicenumber;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import tw.com.chainsea.ce.sdk.bean.statistics.ServiceNumberStatType;

/**
 * current by evan on 2020-09-17
 *
 * @author Evan Wang
 * date 2020-09-17
 */
public class ServiceNumberStat implements Serializable {
    private static final long serialVersionUID = 3089556831316861362L;

    @SerializedName("lastDay") // all agents
    private Stat lastDay;
    @SerializedName("last30Day") // all agents 30 day
    private Stat last30Day;
    @SerializedName("lastDayMember") // self
    private Stat lastDayMember;
    @SerializedName("last30DayMember") // self 30 day
    private Stat last30DayMember;

    private int internalSubscribeCount;  // number of internal subscriptions
    private int externalSubscribeCount; // Number of external subscriptions

    public Stat getLastDay() {
        return lastDay;
    }

    public void setLastDay(Stat lastDay) {
        this.lastDay = lastDay;
    }

    public Stat getLast30Day() {
        return last30Day;
    }

    public void setLast30Day(Stat last30Day) {
        this.last30Day = last30Day;
    }

    public Stat getLastDayMember() {
        return lastDayMember;
    }

    public void setLastDayMember(Stat lastDayMember) {
        this.lastDayMember = lastDayMember;
    }

    public Stat getLast30DayMember() {
        return last30DayMember;
    }

    public void setLast30DayMember(Stat last30DayMember) {
        this.last30DayMember = last30DayMember;
    }

    public int getInternalSubscribeCount() {
        return internalSubscribeCount;
    }

    public void setInternalSubscribeCount(int internalSubscribeCount) {
        this.internalSubscribeCount = internalSubscribeCount;
    }

    public int getExternalSubscribeCount() {
        return externalSubscribeCount;
    }

    public void setExternalSubscribeCount(int externalSubscribeCount) {
        this.externalSubscribeCount = externalSubscribeCount;
    }

    public static class Stat {
        private int internalSubscribeCount;  // Number of internal subscriptions
        private int externalSubscribeCount; // Number of external subscriptions

        private int validServiceCount; // Effective service times
        private int serviceCount;  // Service times
        private int totalServiceCount; // All services

        private double firstAvgReplyTime; // Average first response time
        private double avgReplyTime; // Average response time

        private String statDate; // Statistics Time

        // local
        private ServiceNumberStatType type;

        public int getInternalSubscribeCount() {
            return internalSubscribeCount;
        }

        public void setInternalSubscribeCount(int internalSubscribeCount) {
            this.internalSubscribeCount = internalSubscribeCount;
        }

        public int getExternalSubscribeCount() {
            return externalSubscribeCount;
        }

        public void setExternalSubscribeCount(int externalSubscribeCount) {
            this.externalSubscribeCount = externalSubscribeCount;
        }

        public int getValidServiceCount() {
            return validServiceCount;
        }

        public void setValidServiceCount(int validServiceCount) {
            this.validServiceCount = validServiceCount;
        }

        public int getServiceCount() {
            return serviceCount;
        }

        public void setServiceCount(int serviceCount) {
            this.serviceCount = serviceCount;
        }

        public int getTotalServiceCount() {
            return totalServiceCount;
        }

        public void setTotalServiceCount(int totalServiceCount) {
            this.totalServiceCount = totalServiceCount;
        }

        public double getFirstAvgReplyTime() {
            return firstAvgReplyTime;
        }

        public void setFirstAvgReplyTime(double firstAvgReplyTime) {
            this.firstAvgReplyTime = firstAvgReplyTime;
        }

        public double getAvgReplyTime() {
            return avgReplyTime;
        }

        public void setAvgReplyTime(double avgReplyTime) {
            this.avgReplyTime = avgReplyTime;
        }

        public String getStatDate() {
            return statDate;
        }

        public void setStatDate(String statDate) {
            this.statDate = statDate;
        }

        public ServiceNumberStatType getType() {
            return type;
        }

        public void setType(ServiceNumberStatType type) {
            this.type = type;
        }
    }
}
