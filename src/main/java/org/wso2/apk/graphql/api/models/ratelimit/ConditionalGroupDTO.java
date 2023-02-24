package org.wso2.apk.graphql.api.models.ratelimit;

import java.util.List;

public class ConditionalGroupDTO {
    private String description;
    private ThrottleLimitDTO defaultLimit;
    private List<ConditionDTO> conditions;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ThrottleLimitDTO getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(ThrottleLimitDTO defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public List<ConditionDTO> getConditions() {
        return conditions;
    }

    public void setConditions(List<ConditionDTO> conditions) {
        this.conditions = conditions;
    }
}
