package org.wso2.apk.graphql.api.models.ratelimit;

public class ThrottleLimitDTO {
    private RequestLimitDTO requestLimit;
    private BandwidthDTO bandwidthLimit;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RequestLimitDTO getRequestLimit() {
        return requestLimit;
    }

    public void setRequestLimit(RequestLimitDTO requestLimit) {
        this.requestLimit = requestLimit;
    }

    public BandwidthDTO getBandwidthLimit() {
        return bandwidthLimit;
    }

    public void setBandwidthLimit(BandwidthDTO bandwidthLimit) {
        this.bandwidthLimit = bandwidthLimit;
    }
}
