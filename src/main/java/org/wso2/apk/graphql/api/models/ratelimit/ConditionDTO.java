package org.wso2.apk.graphql.api.models.ratelimit;

public class ConditionDTO {
    private String type;
    private boolean invertCondition;
    private IPConditionDTO ipCondition;
    private HeaderConditionDTO headerCondition;
    private JWTClaimsConditionDTO jwtClaimsCondition;
    private QueryParameterConditionDTO queryParameterCondition;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isInvertCondition() {
        return invertCondition;
    }

    public void setInvertCondition(boolean invertCondition) {
        this.invertCondition = invertCondition;
    }

    public IPConditionDTO getIpCondition() {
        return ipCondition;
    }

    public void setIpCondition(IPConditionDTO ipCondition) {
        this.ipCondition = ipCondition;
    }

    public HeaderConditionDTO getHeaderCondition() {
        return headerCondition;
    }

    public void setHeaderCondition(HeaderConditionDTO headerCondition) {
        this.headerCondition = headerCondition;
    }

    public JWTClaimsConditionDTO getJwtClaimsCondition() {
        return jwtClaimsCondition;
    }

    public void setJwtClaimsCondition(JWTClaimsConditionDTO jwtClaimsCondition) {
        this.jwtClaimsCondition = jwtClaimsCondition;
    }

    public QueryParameterConditionDTO getQueryParameterCondition() {
        return queryParameterCondition;
    }

    public void setQueryParameterCondition(QueryParameterConditionDTO queryParameterCondition) {
        this.queryParameterCondition = queryParameterCondition;
    }
}
