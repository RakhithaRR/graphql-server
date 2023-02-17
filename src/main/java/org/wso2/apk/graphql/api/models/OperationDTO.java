package org.wso2.apk.graphql.api.models;

import java.util.List;

public class OperationDTO {
    private String target;
    private String verb;
    private Boolean authTypeEnabled;
    private List<String> scopes;
    private String usagePlan;

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

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
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
}
