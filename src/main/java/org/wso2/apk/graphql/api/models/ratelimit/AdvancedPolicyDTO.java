package org.wso2.apk.graphql.api.models.ratelimit;

import java.util.List;

public class AdvancedPolicyDTO {
    private String id;
    private String name;
    private String description;
    private ThrottleLimitDTO defaultLimit;
    private List<ConditionalGroupDTO> conditionalGroups;

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

    public List<ConditionalGroupDTO> getConditionalGroups() {
        return conditionalGroups;
    }

    public void setConditionalGroups(List<ConditionalGroupDTO> conditionalGroups) {
        this.conditionalGroups = conditionalGroups;
    }
}
