package org.wso2.apk.graphql.api.mappings;

import org.apache.commons.lang3.StringUtils;
import org.wso2.apk.graphql.api.datatypes.APIDataType;
import org.wso2.apk.graphql.api.models.*;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.utils.OperationPolicyComparator;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class APIDataTypeMapper {

    private static final String ORGANIZATION = "carbon.super";

    public static APIDataType mapAPIToAPIDataType(API api) {
        APIDataType apiDataType = new APIDataType();
        // Attributes required for runtime API
        apiDataType.setId(api.getUuid());
        apiDataType.setName(api.getId().getName());
        apiDataType.setVersion(api.getId().getVersion());
        apiDataType.setContext(api.getContextTemplate());
        apiDataType.setProvider(api.getId().getProviderName());
        //TODO: get the organization from the current tenant
        apiDataType.setOrganization(ORGANIZATION);
        apiDataType.setType(api.getType());
        apiDataType.setEndpointConfig(api.getEndpointConfig());

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
        apiDataType.setBusinessInformation(mapBusinessInformation(api));
        getRevisionDetails(apiDataType, api.getUuid());
        apiDataType.setDocuments(getDocumentationDetails(api.getUuid()));

        return apiDataType;
    }

    private static List<OperationDTO> getOperationsFromSwaggerDef(API api) {
        try {
            String swaggerDefinition;
            if (api.getSwaggerDefinition() != null) {
                swaggerDefinition = api.getSwaggerDefinition();
            } else {
                APIProvider apiProvider = RestApiCommonUtil.getProvider("admin");
                swaggerDefinition = apiProvider.getOpenAPIDefinition(api.getUuid(), ORGANIZATION);
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
                    OperationDTO operationsDTO = getOperationFromURITemplate(uriTemplate);
                    operationsDTOList.add(operationsDTO);
                }
            }
            setOperationPoliciesToOperationsDTO(api, operationsDTOList);
            return operationsDTOList;
        } catch (APIManagementException e) {
            return null;
        }
    }

    private static OperationDTO getOperationFromURITemplate(URITemplate uriTemplate) {

        OperationDTO operationsDTO = new OperationDTO();
        operationsDTO.setVerb(uriTemplate.getHTTPVerb());
        operationsDTO.setTarget(uriTemplate.getUriTemplate());
        operationsDTO.setScopes(uriTemplate.retrieveAllScopes().stream().map(Scope::getKey).collect(
                Collectors.toList()));
        operationsDTO.setAuthTypeEnabled(!APIConstants.AUTH_NO_AUTHENTICATION.equals(uriTemplate.getAuthType()));
        return operationsDTO;
    }

    private static void setOperationPoliciesToOperationsDTO(API api, List<OperationDTO> apiOperationsDTO) {

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

    private static OperationPoliciesDTO fromOperationPolicyListToDTO(List<OperationPolicy> operationPolicyList) {

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

    private static OperationPolicyDTO fromOperationPolicyToDTO(OperationPolicy operationPolicy) {

        OperationPolicyDTO dto = new OperationPolicyDTO();
        dto.setPolicyName(operationPolicy.getPolicyName());
        dto.setPolicyVersion(operationPolicy.getPolicyVersion());
        dto.setPolicyId(operationPolicy.getPolicyId());
        dto.setParameters(operationPolicy.getParameters().toString());
        return dto;
    }

    private static List<String> getCategoryNames(List<APICategory> apiCategories) {

        List<String> categoryNames = new ArrayList<>();
        if (apiCategories != null && !apiCategories.isEmpty()) {
            for (APICategory category : apiCategories) {
                categoryNames.add(category.getName());
            }
        }
        return categoryNames;
    }

    private static BusinessInformation mapBusinessInformation(API api) {

        BusinessInformation businessInformation = new BusinessInformation();
        businessInformation.setBusinessOwner(api.getBusinessOwner());
        businessInformation.setBusinessOwnerEmail(api.getBusinessOwnerEmail());
        businessInformation.setTechnicalOwner(api.getTechnicalOwner());
        businessInformation.setTechnicalOwnerEmail(api.getTechnicalOwnerEmail());
        return businessInformation;
    }

    private static void getRevisionDetails(APIDataType apiDataType, String apiId) {
        List<APIRevision> apiDeployedRevisions = new ArrayList<>();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getProvider("admin");
            List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiId);
            for (APIRevision apiRevision : apiRevisions) {
                if (!apiRevision.getApiRevisionDeploymentList().isEmpty()) {
                    apiDeployedRevisions.add(apiRevision);
                }
            }
        } catch (APIManagementException e) {
            // todo: handle exception
        }
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

    private static Deployment fromAPIRevisionDeploymentToDeployment(
            APIRevisionDeployment apiRevisionDeployment) {
        Deployment revisionDeployment = new Deployment();
        revisionDeployment.setName(apiRevisionDeployment.getDeployment());
        revisionDeployment.setVhost(apiRevisionDeployment.getVhost());
        if (apiRevisionDeployment.getRevisionUUID() != null) {
            revisionDeployment.setRevisionId(apiRevisionDeployment.getRevisionUUID());
        }
        return revisionDeployment;
    }

    private static Date parseStringToDate(String time) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.parse(time);
        } catch (ParseException e) {
            return null;
            // todo: handle exception
        }
    }

    private static List<DocumentDTO> getDocumentationDetails(String apiId) {
        List<DocumentDTO> documentDTOList = new ArrayList<>();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getProvider("admin");
            List<Documentation> documentationList = apiProvider.getAllDocumentation(apiId, "carbon.super");

            for (Documentation documentation : documentationList) {
                DocumentDTO documentDTO = new DocumentDTO();
                documentDTO.setDocumentId(documentation.getId());
                documentDTO.setName(documentation.getName());
                documentDTO.setSummary(documentation.getSummary());
                documentDTO.setType(documentation.getType().toString());
                documentDTO.setSourceType(documentation.getSourceType().toString());
                documentDTO.setOtherTypeName(documentation.getOtherTypeName());
                documentDTO.setSourceUrl(documentation.getSourceUrl());
                documentDTO.setVisibility(documentation.getVisibility().toString());
                if (Documentation.DocumentSourceType.INLINE.equals(documentation.getSourceType())
                        || Documentation.DocumentSourceType.MARKDOWN.equals(documentation.getSourceType())) {
                    DocumentationContent documentationContent = apiProvider
                            .getDocumentationContent(apiId, documentation.getId(), "carbon.super");
                    if (documentationContent != null) {
                        documentDTO.setInlineContent(documentationContent.getTextContent());
                    }
                }
                documentDTO.setFileName(documentation.getFilePath());
                documentDTOList.add(documentDTO);
            }
        } catch (APIManagementException e) {
            return documentDTOList;
            // todo: handle exception
        }
        return documentDTOList;
    }
}
