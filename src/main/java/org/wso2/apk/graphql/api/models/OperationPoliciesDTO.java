package org.wso2.apk.graphql.api.models;

import java.util.ArrayList;
import java.util.List;

public class OperationPoliciesDTO {
    private List<OperationPolicyDTO> request = new ArrayList<OperationPolicyDTO>();
    private List<OperationPolicyDTO> response = new ArrayList<OperationPolicyDTO>();
    private List<OperationPolicyDTO> fault = new ArrayList<OperationPolicyDTO>();

    public List<OperationPolicyDTO> getRequest() {
        return request;
    }

    public void setRequest(List<OperationPolicyDTO> request) {
        this.request = request;
    }

    public List<OperationPolicyDTO> getResponse() {
        return response;
    }

    public void setResponse(List<OperationPolicyDTO> response) {
        this.response = response;
    }

    public List<OperationPolicyDTO> getFault() {
        return fault;
    }

    public void setFault(List<OperationPolicyDTO> fault) {
        this.fault = fault;
    }
}
