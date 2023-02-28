package org.wso2.apk.graphql.api.models;

import java.util.List;

public class ScopeDTO {
    private String id;
    private String name;
    private String displayName;
    private String description;
    private List<String> bindings;
    private Boolean shared;

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getBindings() {
        return bindings;
    }

    public void setBindings(List<String> bindings) {
        this.bindings = bindings;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }
}
