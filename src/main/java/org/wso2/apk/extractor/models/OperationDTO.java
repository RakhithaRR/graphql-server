package org.wso2.apk.extractor.models;

import java.util.List;

public class OperationDTO {
    private String target;
    private String verb;
    private Boolean authTypeEnabled;
    private List<ScopeDTO> scopes;
    private String usagePlan;
    private String throttlingPolicy;
    private List<OperationMediationDTO> resourceMediationPolicies;

    private OperationPoliciesDTO operationPolicies;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public Boolean getAuthTypeEnabled() {
        return authTypeEnabled;
    }

    public void setAuthTypeEnabled(Boolean authTypeEnabled) {
        this.authTypeEnabled = authTypeEnabled;
    }

    public List<ScopeDTO> getScopes() {
        return scopes;
    }

    public void setScopes(List<ScopeDTO> scopes) {
        this.scopes = scopes;
    }

    public OperationPoliciesDTO getOperationPolicies() {
        return operationPolicies;
    }

    public void setOperationPolicies(OperationPoliciesDTO operationPolicies) {
        this.operationPolicies = operationPolicies;
    }

    public String getUsagePlan() {
        return usagePlan;
    }

    public void setUsagePlan(String usagePlan) {
        this.usagePlan = usagePlan;
    }

    public String getThrottlingPolicy() {
        return throttlingPolicy;
    }

    public void setThrottlingPolicy(String throttlingPolicy) {
        this.throttlingPolicy = throttlingPolicy;
    }

    public List<OperationMediationDTO> getResourceMediationPolicies() {
        return resourceMediationPolicies;
    }

    public void setResourceMediationPolicies(List<OperationMediationDTO> resourceMediationPolicies) {
        this.resourceMediationPolicies = resourceMediationPolicies;
    }
}
