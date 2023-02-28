package org.wso2.apk.extractor.models.ratelimit;

public class JWTClaimsConditionDTO {
    private String claimUrl;
    private String claimAttribute;

    public String getClaimUrl() {
        return claimUrl;
    }

    public void setClaimUrl(String claimUrl) {
        this.claimUrl = claimUrl;
    }

    public String getClaimAttribute() {
        return claimAttribute;
    }

    public void setClaimAttribute(String claimAttribute) {
        this.claimAttribute = claimAttribute;
    }
}
