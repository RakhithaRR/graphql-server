package org.wso2.apk.extractor.datatypes;

import org.wso2.apk.extractor.models.*;
import org.wso2.apk.extractor.models.ratelimit.AdvancedPolicyDTO;

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
    private List<CategoryDTO> categories;
    private String lifecycleStatus;
    private String additionalProperties;
    private BusinessInformation businessInformation;
    private Revision revision;
    private List<Deployment> deployments;
    private List<DocumentDTO> documents;
    private String definition;
    private String graphQLSchema;
    private String thumbnail;
    private List<CertificateDTO> clientCertificates;
    private List<CertificateDTO> endpointCertificates;
    private Boolean isRevision;
    private List<CommentDTO> comments;
    private CorsDTO corsConfiguration;
    private List<MediationDTO> mediationPolicies;
    private List<AdvancedPolicyDTO> advancedPolicies;
    private String authorizationHeader;
    private DesignConfigDTO designConfigurations;
    private List<String> security;
    private String wsdlDefinition;
    private boolean apiProduct = false;
    private List<ProductAPIDTO> productAPIs;

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

    public List<CategoryDTO> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryDTO> categories) {
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

    public boolean isRevision() {
        return isRevision;
    }

    public void setIsRevision(Boolean isRevision) {
        this.isRevision = isRevision;
    }

    public List<CommentDTO> getComments() {
        return comments;
    }

    public void setComments(List<CommentDTO> comments) {
        this.comments = comments;
    }

    public CorsDTO getCorsConfiguration() {
        return corsConfiguration;
    }

    public void setCorsConfiguration(CorsDTO corsConfiguration) {
        this.corsConfiguration = corsConfiguration;
    }

    public List<MediationDTO> getMediationPolicies() {
        return mediationPolicies;
    }

    public void setMediationPolicies(List<MediationDTO> mediationPolicies) {
        this.mediationPolicies = mediationPolicies;
    }

    public List<AdvancedPolicyDTO> getAdvancedPolicies() {
        return advancedPolicies;
    }

    public void setAdvancedPolicies(List<AdvancedPolicyDTO> advancedPolicies) {
        this.advancedPolicies = advancedPolicies;
    }

    public String getGraphQLSchema() {
        return graphQLSchema;
    }

    public void setGraphQLSchema(String graphQLSchema) {
        this.graphQLSchema = graphQLSchema;
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public DesignConfigDTO getDesignConfigurations() {
        return designConfigurations;
    }

    public void setDesignConfigurations(DesignConfigDTO designConfigurations) {
        this.designConfigurations = designConfigurations;
    }

    public List<String> getSecurity() {
        return security;
    }

    public void setSecurity(List<String> security) {
        this.security = security;
    }

    public String getWsdlDefinition() {
        return wsdlDefinition;
    }

    public void setWsdlDefinition(String wsdlDefinition) {
        this.wsdlDefinition = wsdlDefinition;
    }

    public boolean isApiProduct() {
        return apiProduct;
    }

    public void setApiProduct(boolean apiProduct) {
        this.apiProduct = apiProduct;
    }

    public List<ProductAPIDTO> getProductAPIs() {
        return productAPIs;
    }

    public void setProductAPIs(List<ProductAPIDTO> productAPIs) {
        this.productAPIs = productAPIs;
    }
}
