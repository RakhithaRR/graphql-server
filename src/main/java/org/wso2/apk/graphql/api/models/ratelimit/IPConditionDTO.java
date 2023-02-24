package org.wso2.apk.graphql.api.models.ratelimit;

public class IPConditionDTO {
    private String type;
    private String specificIP;
    private String startingIP;
    private String endingIP;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSpecificIP() {
        return specificIP;
    }

    public void setSpecificIP(String specificIP) {
        this.specificIP = specificIP;
    }

    public String getStartingIP() {
        return startingIP;
    }

    public void setStartingIP(String startingIP) {
        this.startingIP = startingIP;
    }

    public String getEndingIP() {
        return endingIP;
    }

    public void setEndingIP(String endingIP) {
        this.endingIP = endingIP;
    }
}
