package org.wso2.apk.graphql.api.mappings;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.wso2.apk.graphql.api.datatypes.APIDataType;
import org.wso2.apk.graphql.api.models.*;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class APIDataTypeMapper {

    private final String organization;
    private final APIProvider apiProvider;
    private final String adminUsername;
    private final int tenantId;
    private final DocumentMapper documentMapper;
    private final ScopeMapper scopeMapper;
    private final MediationMapper mediationMapper;
    private final AdvancedPolicyMapper advancedPolicyMapper;

    public APIDataTypeMapper(APIProvider apiProvider, String adminUsername, String organization)
            throws APIManagementException {
        this.apiProvider = apiProvider;
        this.organization = organization;
        this.adminUsername = adminUsername;
        this.tenantId = APIUtil.getTenantIdFromTenantDomain(organization);
        this.documentMapper = new DocumentMapper(apiProvider, adminUsername, organization);
        this.scopeMapper = new ScopeMapper(apiProvider, organization);
        this.mediationMapper = new MediationMapper(apiProvider, adminUsername, organization, tenantId);
        this.advancedPolicyMapper = new AdvancedPolicyMapper(apiProvider, adminUsername, organization);
    }


    public APIDataType mapAPIToAPIDataType(API api) throws APIManagementException {
        APIDataType apiDataType = new APIDataType();
        // Attributes required for runtime API
        apiDataType.setId(api.getUUID());
        apiDataType.setName(api.getId().getName());
        apiDataType.setVersion(api.getId().getVersion());
        apiDataType.setContext(getContext(api.getContextTemplate()));
        apiDataType.setProvider(api.getId().getProviderName());
        apiDataType.setOrganization(organization);
        apiDataType.setType(api.getType());
        apiDataType.setEndpointConfig(api.getEndpointConfig());
        // Not available in 3.2.0
        // apiDataType.setIsRevision(api.isRevision());

        apiDataType.setDescription(api.getDescription());
        if (StringUtils.isEmpty(api.getTransports())) {
            List<String> transports = new ArrayList<>();
            transports.add(APIConstants.HTTPS_PROTOCOL);
            apiDataType.setTransports(transports);
        } else {
            apiDataType.setTransports(Arrays.asList(api.getTransports().split(",")));
        }
        apiDataType.setTags(new ArrayList<>(api.getTags()));
        apiDataType.setOperations(getOperationsFromSwaggerDef(api));
        apiDataType.setAuthorizationHeader(api.getAuthorizationHeader());
        // Attributes required for Backoffice API
        apiDataType.setCategories(getCategories(api.getApiCategories()));
        apiDataType.setLifecycleStatus(api.getStatus());
        apiDataType.setAdditionalProperties(api.getAdditionalProperties().toJSONString());
        apiDataType.setDefinition(getAPIDefinition(api));
        apiDataType.setBusinessInformation(mapBusinessInformation(api));
//        getRevisionDetails(apiDataType, api.getUUID());
        apiDataType.setDocuments(documentMapper.getDocumentationDetails(api));
        apiDataType.setThumbnail(getThumbnail(api));
        apiDataType.setClientCertificates(getClientCertificates(api));
        apiDataType.setEndpointCertificates(getEndpointCertificates(api.getEndpointConfig()));
        apiDataType.setComments(getComments(api.getUUID()));
        apiDataType.setCorsConfiguration(getCorsConfiguration(api));
        apiDataType.setMediationPolicies(mediationMapper.getMediationPolicies(api));
        apiDataType.setAdvancedPolicies(advancedPolicyMapper.getAdvancedPolicies(api));
        apiDataType.setGraphQLSchema(getGraphqlSchemaFromAPI(api));

        return apiDataType;
    }

    private String getContext(String context) {
        if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
            return context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
        } else {
            return context;
        }
    }

    private List<OperationDTO> getOperationsFromSwaggerDef(API api) throws APIManagementException {
        List<OperationDTO> operationsDTOList = new ArrayList<>();
        if (!"WS".equals(api.getType())) {
            String swaggerDefinition;
            if (api.getSwaggerDefinition() != null) {
                swaggerDefinition = api.getSwaggerDefinition();
            } else {
                swaggerDefinition = apiProvider.getOpenAPIDefinition(api.getId());
            }
            APIDefinition apiDefinition = OASParserUtil.getOASParser(swaggerDefinition);
            Set<URITemplate> uriTemplates;
            if (APIConstants.GRAPHQL_API.equals(api.getType())) {
                uriTemplates = api.getUriTemplates();
            } else {
                uriTemplates = apiDefinition.getURITemplates(swaggerDefinition);
            }

            if (!StringUtils.isEmpty(swaggerDefinition)) {
                for (URITemplate uriTemplate : uriTemplates) {
                    OperationDTO operationsDTO = getOperationFromURITemplate(uriTemplate, swaggerDefinition);
                    operationsDTOList.add(operationsDTO);
                }
            }
//            Not available in 3.2.0
//            setOperationPoliciesToOperationsDTO(api, operationsDTOList);
        } else {
            return operationsDTOList;
        }
        return operationsDTOList;
    }

    private OperationDTO getOperationFromURITemplate(URITemplate uriTemplate, String definition)
            throws APIManagementException {

        OperationDTO operationsDTO = new OperationDTO();
        operationsDTO.setVerb(uriTemplate.getHTTPVerb());
        operationsDTO.setTarget(uriTemplate.getUriTemplate());
        operationsDTO.setScopes(scopeMapper
                .getScopesFromDefinition(definition)
                .stream()
                .filter(scope -> uriTemplate.retrieveAllScopes().
                        stream()
                        .map(Scope::getKey)
                        .collect(Collectors.toList())
                        .contains(scope.getName()))
                .collect(Collectors.toList()));
        operationsDTO.setAuthTypeEnabled(!APIConstants.AUTH_NO_AUTHENTICATION.equals(uriTemplate.getAuthType()));
        operationsDTO.setThrottlingPolicy(uriTemplate.getThrottlingTier());
        return operationsDTO;
    }

//    Not available in 3.2.0
//    private void setOperationPoliciesToOperationsDTO(API api, List<OperationDTO> apiOperationsDTO) {
//
//        Set<URITemplate> uriTemplates = api.getUriTemplates();
//        Map<String, URITemplate> uriTemplateMap = new HashMap<>();
//        for (URITemplate uriTemplate : uriTemplates) {
//            String key = uriTemplate.getUriTemplate() + ":" + uriTemplate.getHTTPVerb();
//            uriTemplateMap.put(key, uriTemplate);
//        }
//
//        for (OperationDTO operationsDTO : apiOperationsDTO) {
//            String key = operationsDTO.getTarget() + ":" + operationsDTO.getVerb();
//            if (uriTemplateMap.get(key) != null) {
//                List<OperationPolicy> operationPolicies = uriTemplateMap.get(key).getOperationPolicies();
//                if (!operationPolicies.isEmpty()) {
//                    operationsDTO.setOperationPolicies(fromOperationPolicyListToDTO(operationPolicies));
//                }
//            }
//        }
//    }

//    private OperationPoliciesDTO fromOperationPolicyListToDTO(List<OperationPolicy> operationPolicyList) {
//
//        OperationPoliciesDTO dto = new OperationPoliciesDTO();
//        List<OperationPolicyDTO> request = new ArrayList<>();
//        List<OperationPolicyDTO> response = new ArrayList<>();
//        List<OperationPolicyDTO> fault = new ArrayList<>();
//        operationPolicyList.sort(new OperationPolicyComparator());
//        for (OperationPolicy op : operationPolicyList) {
//            OperationPolicyDTO policyDTO = fromOperationPolicyToDTO(op);
//            if (APIConstants.OPERATION_SEQUENCE_TYPE_REQUEST.equals(op.getDirection())) {
//                request.add(policyDTO);
//            } else if (APIConstants.OPERATION_SEQUENCE_TYPE_RESPONSE.equals(op.getDirection())) {
//                response.add(policyDTO);
//            } else if (APIConstants.OPERATION_SEQUENCE_TYPE_FAULT.equals(op.getDirection())) {
//                fault.add(policyDTO);
//            }
//        }
//        dto.setRequest(request);
//        dto.setResponse(response);
//        dto.setFault(fault);
//        return dto;
//    }
//
//    private OperationPolicyDTO fromOperationPolicyToDTO(OperationPolicy operationPolicy) {
//
//        OperationPolicyDTO dto = new OperationPolicyDTO();
//        dto.setPolicyName(operationPolicy.getPolicyName());
//        dto.setPolicyVersion(operationPolicy.getPolicyVersion());
//        dto.setPolicyId(operationPolicy.getPolicyId());
//        dto.setParameters(operationPolicy.getParameters().toString());
//        return dto;
//    }

    private List<CategoryDTO> getCategories(List<APICategory> apiCategories) {

        List<CategoryDTO> categoryNames = new ArrayList<>();
        if (apiCategories != null && !apiCategories.isEmpty()) {
            for (APICategory category : apiCategories) {
                CategoryDTO categoryDTO = new CategoryDTO();
                categoryDTO.setName(category.getName());
                categoryDTO.setDescription(category.getDescription());
                categoryDTO.setOrganization(organization);
                categoryNames.add(categoryDTO);
            }
        }
        return categoryNames;
    }

    private BusinessInformation mapBusinessInformation(API api) {

        BusinessInformation businessInformation = new BusinessInformation();
        businessInformation.setBusinessOwner(api.getBusinessOwner());
        businessInformation.setBusinessOwnerEmail(api.getBusinessOwnerEmail());
        businessInformation.setTechnicalOwner(api.getTechnicalOwner());
        businessInformation.setTechnicalOwnerEmail(api.getTechnicalOwnerEmail());
        return businessInformation;
    }

//    Not available in 3.2.0
//    private void getRevisionDetails(APIDataType apiDataType, String apiId) {
//        List<APIRevision> apiDeployedRevisions = new ArrayList<>();
//        try {
//            List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiId);
//            for (APIRevision apiRevision : apiRevisions) {
//                if (!apiRevision.getApiRevisionDeploymentList().isEmpty()) {
//                    apiDeployedRevisions.add(apiRevision);
//                }
//            }
//        } catch (APIManagementException e) {
//        }
//        if (!apiDeployedRevisions.isEmpty()) {
//            APIRevision deployedRevision = apiDeployedRevisions.get(0);
//            Revision revision = new Revision();
//            revision.setId(deployedRevision.getRevisionUUID());
//            revision.setCreatedTime(parseStringToDate(deployedRevision.getCreatedTime()).toString());
//            revision.setDisplayName("Revision " + deployedRevision.getId());
//            revision.setDescription(deployedRevision.getDescription());
//
//            List<Deployment> revisionDeployments = new ArrayList<>();
//            if (deployedRevision.getApiRevisionDeploymentList() != null) {
//                for (APIRevisionDeployment apiRevisionDeployment : deployedRevision.getApiRevisionDeploymentList()) {
//                    revisionDeployments.add(fromAPIRevisionDeploymentToDeployment(apiRevisionDeployment));
//                }
//            }
//
//            apiDataType.setRevision(revision);
//            apiDataType.setDeployments(revisionDeployments);
//        }
//    }
//
//    private Deployment fromAPIRevisionDeploymentToDeployment(
//            APIRevisionDeployment apiRevisionDeployment) {
//        Deployment revisionDeployment = new Deployment();
//        revisionDeployment.setName(apiRevisionDeployment.getDeployment());
//        revisionDeployment.setVhost(apiRevisionDeployment.getVhost());
//        if (apiRevisionDeployment.getRevisionUUID() != null) {
//            revisionDeployment.setRevisionId(apiRevisionDeployment.getRevisionUUID());
//        }
//        return revisionDeployment;
//    }

//    private Date parseStringToDate(String time) {
//        try {
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            return dateFormat.parse(time);
//        } catch (ParseException e) {
//            return null;
//            // todo: handle exception
//        }
//    }

    private String getAPIDefinition(API api) throws APIManagementException {
        return apiProvider.getOpenAPIDefinition(api.getId());
    }

    private String getGraphqlSchemaFromAPI(API api) throws APIManagementException {
        String graphqlSchema = "";
        if (!APIConstants.GRAPHQL_API.equals(api.getType())) {
            return graphqlSchema;
        }
        graphqlSchema = apiProvider.getGraphqlSchema(api.getId());
        return graphqlSchema;
    }

    private String getThumbnail(API api) throws APIManagementException {
        String base64EncodedThumbnail;
        try {
            ResourceFile thumbnail = apiProvider.getIcon(api.getId());
            if (thumbnail == null) {
                return null;
            }
            InputStream thumbnailStream = thumbnail.getContent();
            byte[] bytes = IOUtils.toByteArray(thumbnailStream);
            base64EncodedThumbnail = Base64.getEncoder().encodeToString(bytes);
            return base64EncodedThumbnail;
        } catch (Exception e) {
            throw new APIManagementException("Error while getting thumbnail for API " + api.getId().getApiName(), e);
        }
    }

    private List<CertificateDTO> getClientCertificates(API api) throws APIManagementException {
        List<CertificateDTO> certificateDTOList = new ArrayList<>();
        List<ClientCertificateDTO> certificates = apiProvider.searchClientCertificates(tenantId, null, api.getId());
        for (ClientCertificateDTO certificate : certificates) {
            CertificateDTO certificateDTO = new CertificateDTO();
            certificateDTO.setAlias(certificate.getAlias());
            certificateDTO.setApiId(certificate.getApiIdentifier().getUUID());
            certificateDTO.setCertificate(certificate.getCertificate());
            certificateDTO.setTierName(certificate.getTierName());
            certificateDTOList.add(certificateDTO);
        }
        return certificateDTOList;
    }

    private List<CertificateDTO> getEndpointCertificates(String config) throws APIManagementException {
        List<CertificateDTO> certificateDTOList = new ArrayList<>();
        Set<String> endpoints = new HashSet<>();
        if (config.isBlank()) {
            return certificateDTOList;
        }
        JsonObject configObject = JsonParser.parseString(config).getAsJsonObject();
        JsonObject endpointConfig = configObject.getAsJsonObject("production_endpoints");
        endpoints.add(endpointConfig.get("url").getAsString());
        endpointConfig = configObject.getAsJsonObject("sandbox_endpoints");
        endpoints.add(endpointConfig.get("url").getAsString());
        List<CertificateMetadataDTO> certificates;
        for (String endpoint : endpoints) {
            certificates = apiProvider.searchCertificates(tenantId, null, endpoint);
            for (CertificateMetadataDTO certificate : certificates) {
                CertificateDTO certificateDTO = new CertificateDTO();
                certificateDTO.setAlias(certificate.getAlias());
                certificateDTO.setEndpoint(endpoint);
                certificateDTO.setCertificate(getEndpointCertificateContent(certificate.getAlias()));
                certificateDTOList.add(certificateDTO);
            }
        }
        return certificateDTOList;
    }

    // This method is only required in APIM 3.2.0
    private String getEndpointCertificateContent(String alias) throws APIManagementException {
        String cert;
        ByteArrayInputStream content = apiProvider.getCertificateContent(alias);
        try {
            String stringContent = new String(Base64.getEncoder().encode(IOUtils.toByteArray(content)));
            String certificateContent = APIConstants.BEGIN_CERTIFICATE_STRING.concat(stringContent).concat("\n"
            ).concat(APIConstants.END_CERTIFICATE_STRING);
            cert = Base64.getEncoder().encodeToString(certificateContent.getBytes());
        } catch (IOException e) {
            throw new APIManagementException("Error while getting certificate content for alias " + alias, e);
        }
        return cert;
    }

    private List<CommentDTO> getComments(String apiId) throws APIManagementException {
        List<CommentDTO> commentDTOList;
        APIConsumer apiConsumer = RestApiUtil.getConsumer(adminUsername);
        ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
        Comment[] comments = apiConsumer.getComments(apiTypeWrapper);
        commentDTOList = fromCommentListToCommentDTOList(comments);
        return commentDTOList;
    }

    private List<CommentDTO> fromCommentListToCommentDTOList(Comment[] comments) {
        List<CommentDTO> commentDTOList = new ArrayList<>();
        for (Comment comment : comments) {
            CommentDTO commentDTO = new CommentDTO();
            commentDTO.setId(comment.getId());
            commentDTO.setContent(comment.getText());
            commentDTO.setUser(comment.getUser());
            commentDTOList.add(commentDTO);
        }
        return commentDTOList;
    }

    private CorsDTO getCorsConfiguration(API api) {
        CORSConfiguration corsConfiguration = api.getCorsConfiguration();
        CorsDTO corsDTO = new CorsDTO();
        corsDTO.setEnabled(corsConfiguration.isCorsConfigurationEnabled());
        corsDTO.setAccessControlAllowOrigins(corsConfiguration.getAccessControlAllowOrigins());
        corsDTO.setAccessControlAllowCredentials(corsConfiguration.isAccessControlAllowCredentials());
        corsDTO.setAccessControlAllowHeaders(corsConfiguration.getAccessControlAllowHeaders());
        corsDTO.setAccessControlAllowMethods(corsConfiguration.getAccessControlAllowMethods());
        return corsDTO;
    }
}
