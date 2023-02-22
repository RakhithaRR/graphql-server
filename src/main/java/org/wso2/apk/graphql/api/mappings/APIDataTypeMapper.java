package org.wso2.apk.graphql.api.mappings;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.wso2.apk.graphql.api.datatypes.APIDataType;
import org.wso2.apk.graphql.api.models.*;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.OperationPolicyComparator;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class APIDataTypeMapper {

    private final String organization;
    private final APIProvider apiProvider;
    private final int tenantId;
    private final DocumentMapper documentMapper;
    private final ScopeMapper scopeMapper;

    public APIDataTypeMapper(APIProvider apiProvider, String organization) throws APIManagementException {
        this.apiProvider = apiProvider;
        this.organization = organization;
        this.tenantId = APIUtil.getInternalOrganizationId(organization);
        this.documentMapper = new DocumentMapper(apiProvider, organization);
        this.scopeMapper = new ScopeMapper(apiProvider, organization);
    }


    public APIDataType mapAPIToAPIDataType(API api) {
        APIDataType apiDataType = new APIDataType();
        // Attributes required for runtime API
        apiDataType.setId(api.getUuid());
        apiDataType.setName(api.getId().getName());
        apiDataType.setVersion(api.getId().getVersion());
        apiDataType.setContext(api.getContextTemplate());
        apiDataType.setProvider(api.getId().getProviderName());
        apiDataType.setOrganization(organization);
        apiDataType.setType(api.getType());
        apiDataType.setEndpointConfig(api.getEndpointConfig());
        apiDataType.setIsRevision(api.isRevision());

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
        // Attributes required for Backoffice API
        apiDataType.setCategories(getCategoryNames(api.getApiCategories()));
        apiDataType.setLifecycleStatus(api.getStatus());
        apiDataType.setAdditionalProperties(api.getAdditionalProperties().toJSONString());
        apiDataType.setDefinition(getAPIDefinition(api.getUuid()));
        apiDataType.setBusinessInformation(mapBusinessInformation(api));
        getRevisionDetails(apiDataType, api.getUuid());
        apiDataType.setDocuments(documentMapper.getDocumentationDetails(api.getUuid()));
        apiDataType.setThumbnail(getThumbnail(api.getUuid()));
        apiDataType.setClientCertificates(getClientCertificates(api));
        apiDataType.setEndpointCertificates(getEndpointCertificates(api.getEndpointConfig()));
        apiDataType.setComments(getComments(api.getUuid()));
        apiDataType.setCorsConfiguration(getCorsConfiguration(api));

        return apiDataType;
    }

    private List<OperationDTO> getOperationsFromSwaggerDef(API api) {
        try {
            String swaggerDefinition;
            if (api.getSwaggerDefinition() != null) {
                swaggerDefinition = api.getSwaggerDefinition();
            } else {
                APIProvider apiProvider = RestApiCommonUtil.getProvider("admin");
                swaggerDefinition = apiProvider.getOpenAPIDefinition(api.getUuid(), organization);
            }
            APIDefinition apiDefinition = OASParserUtil.getOASParser(swaggerDefinition);
            Set<URITemplate> uriTemplates;
            if (APIConstants.GRAPHQL_API.equals(api.getType())) {
                uriTemplates = api.getUriTemplates();
            } else {
                uriTemplates = apiDefinition.getURITemplates(swaggerDefinition);
            }

            List<OperationDTO> operationsDTOList = new ArrayList<>();
            if (!StringUtils.isEmpty(swaggerDefinition)) {
                for (URITemplate uriTemplate : uriTemplates) {
                    OperationDTO operationsDTO = getOperationFromURITemplate(uriTemplate, swaggerDefinition);
                    operationsDTOList.add(operationsDTO);
                }
            }
            setOperationPoliciesToOperationsDTO(api, operationsDTOList);
            return operationsDTOList;
        } catch (APIManagementException e) {
            return null;
        }
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
        return operationsDTO;
    }

    private void setOperationPoliciesToOperationsDTO(API api, List<OperationDTO> apiOperationsDTO) {

        Set<URITemplate> uriTemplates = api.getUriTemplates();
        Map<String, URITemplate> uriTemplateMap = new HashMap<>();
        for (URITemplate uriTemplate : uriTemplates) {
            String key = uriTemplate.getUriTemplate() + ":" + uriTemplate.getHTTPVerb();
            uriTemplateMap.put(key, uriTemplate);
        }

        for (OperationDTO operationsDTO : apiOperationsDTO) {
            String key = operationsDTO.getTarget() + ":" + operationsDTO.getVerb();
            if (uriTemplateMap.get(key) != null) {
                List<OperationPolicy> operationPolicies = uriTemplateMap.get(key).getOperationPolicies();
                if (!operationPolicies.isEmpty()) {
                    operationsDTO.setOperationPolicies(fromOperationPolicyListToDTO(operationPolicies));
                }
            }
        }
    }

    private OperationPoliciesDTO fromOperationPolicyListToDTO(List<OperationPolicy> operationPolicyList) {

        OperationPoliciesDTO dto = new OperationPoliciesDTO();
        List<OperationPolicyDTO> request = new ArrayList<>();
        List<OperationPolicyDTO> response = new ArrayList<>();
        List<OperationPolicyDTO> fault = new ArrayList<>();
        operationPolicyList.sort(new OperationPolicyComparator());
        for (OperationPolicy op : operationPolicyList) {
            OperationPolicyDTO policyDTO = fromOperationPolicyToDTO(op);
            if (APIConstants.OPERATION_SEQUENCE_TYPE_REQUEST.equals(op.getDirection())) {
                request.add(policyDTO);
            } else if (APIConstants.OPERATION_SEQUENCE_TYPE_RESPONSE.equals(op.getDirection())) {
                response.add(policyDTO);
            } else if (APIConstants.OPERATION_SEQUENCE_TYPE_FAULT.equals(op.getDirection())) {
                fault.add(policyDTO);
            }
        }
        dto.setRequest(request);
        dto.setResponse(response);
        dto.setFault(fault);
        return dto;
    }

    private OperationPolicyDTO fromOperationPolicyToDTO(OperationPolicy operationPolicy) {

        OperationPolicyDTO dto = new OperationPolicyDTO();
        dto.setPolicyName(operationPolicy.getPolicyName());
        dto.setPolicyVersion(operationPolicy.getPolicyVersion());
        dto.setPolicyId(operationPolicy.getPolicyId());
        dto.setParameters(operationPolicy.getParameters().toString());
        return dto;
    }

    private List<String> getCategoryNames(List<APICategory> apiCategories) {

        List<String> categoryNames = new ArrayList<>();
        if (apiCategories != null && !apiCategories.isEmpty()) {
            for (APICategory category : apiCategories) {
                categoryNames.add(category.getName());
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

    private void getRevisionDetails(APIDataType apiDataType, String apiId) {
        List<APIRevision> apiDeployedRevisions = new ArrayList<>();
        try {
            List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiId);
            for (APIRevision apiRevision : apiRevisions) {
                if (!apiRevision.getApiRevisionDeploymentList().isEmpty()) {
                    apiDeployedRevisions.add(apiRevision);
                }
            }
        } catch (APIManagementException e) {
            // todo: handle exception
        }
        if (!apiDeployedRevisions.isEmpty()) {
            APIRevision deployedRevision = apiDeployedRevisions.get(0);
            Revision revision = new Revision();
            revision.setId(deployedRevision.getRevisionUUID());
            revision.setCreatedTime(parseStringToDate(deployedRevision.getCreatedTime()).toString());
            revision.setDisplayName("Revision " + deployedRevision.getId());
            revision.setDescription(deployedRevision.getDescription());

            List<Deployment> revisionDeployments = new ArrayList<>();
            if (deployedRevision.getApiRevisionDeploymentList() != null) {
                for (APIRevisionDeployment apiRevisionDeployment : deployedRevision.getApiRevisionDeploymentList()) {
                    revisionDeployments.add(fromAPIRevisionDeploymentToDeployment(apiRevisionDeployment));
                }
            }

            apiDataType.setRevision(revision);
            apiDataType.setDeployments(revisionDeployments);
        }
    }

    private Deployment fromAPIRevisionDeploymentToDeployment(
            APIRevisionDeployment apiRevisionDeployment) {
        Deployment revisionDeployment = new Deployment();
        revisionDeployment.setName(apiRevisionDeployment.getDeployment());
        revisionDeployment.setVhost(apiRevisionDeployment.getVhost());
        if (apiRevisionDeployment.getRevisionUUID() != null) {
            revisionDeployment.setRevisionId(apiRevisionDeployment.getRevisionUUID());
        }
        return revisionDeployment;
    }

    private Date parseStringToDate(String time) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.parse(time);
        } catch (ParseException e) {
            return null;
            // todo: handle exception
        }
    }

    private String getAPIDefinition(String apiId) {
        String apiDefinition = "";
        try {
            apiDefinition = apiProvider.getOpenAPIDefinition(apiId, organization);
        } catch (APIManagementException e) {
            return apiDefinition;
        }
        return apiDefinition;
    }

    private String getThumbnail(String apiId) {
        String base64EncodedThumbnail = "";
        try {
            ResourceFile thumbnail = apiProvider.getIcon(apiId, organization);
            InputStream thumbnailStream = thumbnail.getContent();
            byte[] bytes = IOUtils.toByteArray(thumbnailStream);
            base64EncodedThumbnail = Base64.getEncoder().encodeToString(bytes);
            return base64EncodedThumbnail;
        } catch (Exception e) {
            return base64EncodedThumbnail;
        }
    }

    private List<CertificateDTO> getClientCertificates(API api) {
        List<CertificateDTO> certificateDTOList = new ArrayList<>();
        try {
            List<ClientCertificateDTO> certificates = apiProvider
                    .searchClientCertificates(tenantId, null, api.getId(), organization);
            for (ClientCertificateDTO certificate : certificates) {
                CertificateDTO certificateDTO = new CertificateDTO();
                certificateDTO.setAlias(certificate.getAlias());
                certificateDTO.setApiId(certificate.getApiIdentifier().getUUID());
                certificateDTO.setCertificate(certificate.getCertificate());
                certificateDTO.setTierName(certificate.getTierName());
                certificateDTOList.add(certificateDTO);
            }
        } catch (APIManagementException e) {
            return certificateDTOList;
        }
        return certificateDTOList;
    }

    private List<CertificateDTO> getEndpointCertificates(String config) {
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
        try {
            List<CertificateMetadataDTO> certificates;
            for (String endpoint : endpoints) {
                certificates = apiProvider.searchCertificates(tenantId, null, endpoint);
                for (CertificateMetadataDTO certificate : certificates) {
                    CertificateDTO certificateDTO = new CertificateDTO();
                    certificateDTO.setAlias(certificate.getAlias());
                    certificateDTO.setEndpoint(endpoint);
                    certificateDTO.setCertificate(certificate.getCertificate());
                    certificateDTOList.add(certificateDTO);
                }
            }
        } catch (Exception e) {
            return certificateDTOList;
        }
        return certificateDTOList;
    }

    private List<CommentDTO> getComments(String apiId) {
        List<CommentDTO> commentDTOList = new ArrayList<>();
        try {
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
            CommentList comments = apiProvider.getComments(apiTypeWrapper, null, 100, 0);
            commentDTOList = fromCommentListToCommentDTOList(comments);
        } catch (APIManagementException e) {
            return commentDTOList;
        }
        return commentDTOList;
    }

    private List<CommentDTO> fromCommentListToCommentDTOList(CommentList comments) {
        List<CommentDTO> commentDTOList = new ArrayList<>();
        for (Comment comment : comments.getList()) {
            CommentDTO commentDTO = new CommentDTO();
            commentDTO.setId(comment.getId());
            commentDTO.setContent(comment.getText());
            commentDTO.setUser(comment.getUser());
            commentDTO.setEntrypoint(comment.getEntryPoint());
            commentDTO.setCategory(comment.getCategory());
            commentDTO.setParentCommentId(comment.getParentCommentID());
            commentDTO.setReplies(fromCommentListToCommentDTOList(comment.getReplies()));
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
