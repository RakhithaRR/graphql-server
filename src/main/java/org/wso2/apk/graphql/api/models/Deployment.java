package org.wso2.apk.graphql.api.models;

public class Deployment {
    private String name;
    private String revisionId;
    private String deployedTime;
    private String vhost;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    public String getDeployedTime() {
        return deployedTime;
    }

    public void setDeployedTime(String deployedTime) {
        this.deployedTime = deployedTime;
    }

    public String getVhost() {
        return vhost;
    }

    public void setVhost(String vhost) {
        this.vhost = vhost;
    }
}
