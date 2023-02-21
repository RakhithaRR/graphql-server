package org.wso2.apk.graphql.api.datatypes;

import org.wso2.apk.graphql.api.models.*;

import java.util.List;

public class APIDataType {
    private String id;
    private String name;
    private String version;
    private String context;
    private String provider;
    private String organization;
    private String description;
    private String type;
    private List<String> transports;
    private Boolean hasThumbnail;
    private List<String> tags;
    private String endpointConfig;
    private List<OperationDTO> operations;
    private List<String> categories;
    private String lifecycleStatus;
    private String additionalProperties;
    private BusinessInformation businessInformation;
    private Revision revision;
    private List<Deployment> deployments;
    private List<DocumentDTO> documents;
    private String definition;
    private String thumbnail;
    private List<CertificateDTO> clientCertificates;
    private List<CertificateDTO> endpointCertificates;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getTransports() {
        return transports;
    }

    public void setTransports(List<String> transports) {
        this.transports = transports;
    }

    public Boolean getHasThumbnail() {
        return hasThumbnail;
    }

    public void setHasThumbnail(Boolean hasThumbnail) {
        this.hasThumbnail = hasThumbnail;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getEndpointConfig() {
        return endpointConfig;
    }

    public void setEndpointConfig(String endpointConfig) {
        this.endpointConfig = endpointConfig;
    }

    public List<OperationDTO> getOperations() {
        return operations;
    }

    public void setOperations(List<OperationDTO> operationDTOS) {
        this.operations = operationDTOS;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getLifecycleStatus() {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(String lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

    public String getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(String additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public BusinessInformation getBusinessInformation() {
        return businessInformation;
    }

    public void setBusinessInformation(BusinessInformation businessInformation) {
        this.businessInformation = businessInformation;
    }

    public Revision getRevision() {
        return revision;
    }

    public void setRevision(Revision revision) {
        this.revision = revision;
    }

    public List<Deployment> getDeployments() {
        return deployments;
    }

    public void setDeployments(List<Deployment> deployments) {
        this.deployments = deployments;
    }

    public List<DocumentDTO> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentDTO> documents) {
        this.documents = documents;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public List<CertificateDTO> getClientCertificates() {
        return clientCertificates;
    }

    public void setClientCertificates(List<CertificateDTO> clientCertificates) {
        this.clientCertificates = clientCertificates;
    }

    public List<CertificateDTO> getEndpointCertificates() {
        return endpointCertificates;
    }

    public void setEndpointCertificates(List<CertificateDTO> endpointCertificates) {
        this.endpointCertificates = endpointCertificates;
    }
}
