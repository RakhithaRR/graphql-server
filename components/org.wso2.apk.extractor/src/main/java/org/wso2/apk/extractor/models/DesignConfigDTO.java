package org.wso2.apk.extractor.models;

import java.util.List;

public class DesignConfigDTO {
    private String accessControl;
    private List<String> accessControlRoles;
    private String visibility;
    private List<String> visibleRoles;
    private List<String> visibleTenants;
    private Boolean defaultVersion;

    public String getAccessControl() {
        return accessControl;
    }

    public void setAccessControl(String accessControl) {
        this.accessControl = accessControl;
    }

    public List<String> getAccessControlRoles() {
        return accessControlRoles;
    }

    public void setAccessControlRoles(List<String> accessControlRoles) {
        this.accessControlRoles = accessControlRoles;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public List<String> getVisibleRoles() {
        return visibleRoles;
    }

    public void setVisibleRoles(List<String> visibleRoles) {
        this.visibleRoles = visibleRoles;
    }

    public List<String> getVisibleTenants() {
        return visibleTenants;
    }

    public void setVisibleTenants(List<String> visibleTenants) {
        this.visibleTenants = visibleTenants;
    }

    public Boolean getDefaultVersion() {
        return defaultVersion;
    }

    public void setDefaultVersion(Boolean defaultVersion) {
        this.defaultVersion = defaultVersion;
    }
}
