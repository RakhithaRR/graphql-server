package org.wso2.apk.graphql.api.mappings;

import org.apache.commons.lang3.StringUtils;
import org.wso2.apk.graphql.api.datatypes.APIDataType;
import org.wso2.apk.graphql.api.models.OperationDTO;
import org.wso2.apk.graphql.api.models.OperationPoliciesDTO;
import org.wso2.apk.graphql.api.models.OperationPolicyDTO;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.utils.OperationPolicyComparator;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;

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
            setOperationPoliciesToOperationsDTO(uriTemplates, operationsDTOList);
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

    private static void setOperationPoliciesToOperationsDTO(Set<URITemplate> uriTemplates,
                                                            List<OperationDTO> apiOperationsDTO) {

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
}
