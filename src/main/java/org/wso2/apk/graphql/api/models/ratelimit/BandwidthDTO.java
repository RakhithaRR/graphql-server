package org.wso2.apk.graphql.api.models.ratelimit;

public class BandwidthDTO {
    private String timeUnit;
    private int unitTime;
    private Long dataAmount;
    private String dataUnit;

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    public int getUnitTime() {
        return unitTime;
    }

    public void setUnitTime(int unitTime) {
        this.unitTime = unitTime;
    }

    public Long getDataAmount() {
        return dataAmount;
    }

    public void setDataAmount(Long dataAmount) {
        this.dataAmount = dataAmount;
    }

    public String getDataUnit() {
        return dataUnit;
    }

    public void setDataUnit(String dataUnit) {
        this.dataUnit = dataUnit;
    }
}
