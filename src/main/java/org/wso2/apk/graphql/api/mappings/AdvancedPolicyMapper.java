package org.wso2.apk.graphql.api.mappings;

import org.wso2.apk.graphql.api.models.ratelimit.*;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.policy.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdvancedPolicyMapper {
    private final String organization;
    private final String adminUsername;
    private final APIProvider apiProvider;

    public AdvancedPolicyMapper(APIProvider apiProvider, String adminUsername, String organization) {
        this.apiProvider = apiProvider;
        this.organization = organization;
        this.adminUsername = adminUsername;
    }

    public List<AdvancedPolicyDTO> getAdvancedPolicies(API api) {
        List<AdvancedPolicyDTO> advancedPolicyDTOList = new ArrayList<>();
        if ("WS".equals(api.getType())) {
            return advancedPolicyDTOList;
        }
        try {
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

            Policy[] apiPolicies = apiProvider.getPolicies(adminUsername, PolicyConstants.POLICY_LEVEL_API);
            Set<String> policyNames = new HashSet<>();
            for (URITemplate uriTemplate : uriTemplates) {
                String throttlingTier = uriTemplate.getThrottlingTier();
                // Null check is added fo SOAP APIs
                if (throttlingTier == null || policyNames.contains(throttlingTier)) {
                    continue;
                }
                for (Policy apiPolicy : apiPolicies) {
                    if (throttlingTier.equals(apiPolicy.getPolicyName())) {
                        String policyId = apiPolicy.getUUID();
                        advancedPolicyDTOList.add(getAdvancedPolicyDetails(policyId));
                        policyNames.add(throttlingTier);
                        break;
                    }
                }
            }
        } catch (APIManagementException e) {
            //todo handle exception
        }
        return advancedPolicyDTOList;
    }

    private AdvancedPolicyDTO getAdvancedPolicyDetails(String policyId) throws APIManagementException {
        APIPolicy apiPolicy = apiProvider.getAPIPolicyByUUID(policyId);
        AdvancedPolicyDTO advancedPolicyDTO = new AdvancedPolicyDTO();
        advancedPolicyDTO.setId(apiPolicy.getUUID());
        advancedPolicyDTO.setName(apiPolicy.getPolicyName());
        advancedPolicyDTO.setDescription(apiPolicy.getDescription());
        if (apiPolicy.getDefaultQuotaPolicy() != null) {
            advancedPolicyDTO.setDefaultLimit(getAdvancedPolicyQuotaDetails(apiPolicy.getDefaultQuotaPolicy()));
        }
        if (apiPolicy.getPipelines() != null) {
            List<ConditionalGroupDTO> conditionalGroupDTOList = new ArrayList<>();
            for (Pipeline pipeline : apiPolicy.getPipelines()) {
                conditionalGroupDTOList.add(getConditionalGroupDetails(pipeline));
            }
            advancedPolicyDTO.setConditionalGroups(conditionalGroupDTOList);
        }
        return advancedPolicyDTO;
    }

    private ThrottleLimitDTO getAdvancedPolicyQuotaDetails(QuotaPolicy quotaPolicy) {
        ThrottleLimitDTO throttleLimitDTO = new ThrottleLimitDTO();
        if (PolicyConstants.REQUEST_COUNT_TYPE.equals(quotaPolicy.getType())) {
            RequestCountLimit requestCountLimit = (RequestCountLimit) quotaPolicy.getLimit();
            throttleLimitDTO.setType(PolicyConstants.REQUEST_COUNT_TYPE);
            throttleLimitDTO.setRequestLimit(getRequestLimitDetails(requestCountLimit));
        } else if (PolicyConstants.BANDWIDTH_TYPE.equals(quotaPolicy.getType())) {
            BandwidthLimit bandwidthLimit = (BandwidthLimit) quotaPolicy.getLimit();
            throttleLimitDTO.setType(PolicyConstants.BANDWIDTH_TYPE);
            throttleLimitDTO.setBandwidthLimit(getBandwidthLimitDetails(bandwidthLimit));
        }
        return throttleLimitDTO;
    }

    private RequestLimitDTO getRequestLimitDetails(RequestCountLimit requestCountLimit) {
        RequestLimitDTO requestLimitDTO = new RequestLimitDTO();
        requestLimitDTO.setTimeUnit(requestCountLimit.getTimeUnit());
        requestLimitDTO.setRequestCount(requestCountLimit.getRequestCount());
        requestLimitDTO.setUnitTime(requestCountLimit.getUnitTime());
        return requestLimitDTO;
    }

    private BandwidthDTO getBandwidthLimitDetails(BandwidthLimit bandwidthLimit) {
        BandwidthDTO bandwidthDTO = new BandwidthDTO();
        bandwidthDTO.setTimeUnit(bandwidthLimit.getTimeUnit());
        bandwidthDTO.setUnitTime(bandwidthLimit.getUnitTime());
        bandwidthDTO.setDataAmount(bandwidthLimit.getDataAmount());
        bandwidthDTO.setDataUnit(bandwidthLimit.getDataUnit());
        return bandwidthDTO;
    }

    private ConditionalGroupDTO getConditionalGroupDetails(Pipeline pipeline) {
        ConditionalGroupDTO conditionalGroupDTO = new ConditionalGroupDTO();
        conditionalGroupDTO.setDescription(pipeline.getDescription());
        conditionalGroupDTO.setDefaultLimit(getAdvancedPolicyQuotaDetails(pipeline.getQuotaPolicy()));
        if (pipeline.getConditions() != null) {
            List<ConditionDTO> conditionDTOList = new ArrayList<>();
            for (Condition condition : pipeline.getConditions()) {
                conditionDTOList.add(getConditionDetails(condition));
            }
            conditionalGroupDTO.setConditions(conditionDTOList);
        }
        return conditionalGroupDTO;
    }

    private ConditionDTO getConditionDetails(Condition condition) {
        ConditionDTO conditionDTO = new ConditionDTO();
        conditionDTO.setInvertCondition(condition.isInvertCondition());
        conditionDTO.setType(condition.getType());
        if (condition instanceof IPCondition) {
            conditionDTO.setIpCondition(getIPConditionDetails((IPCondition) condition));
        } else if (condition instanceof HeaderCondition) {
            conditionDTO.setHeaderCondition(getHeaderConditionDetails((HeaderCondition) condition));
        } else if (condition instanceof JWTClaimsCondition) {
            conditionDTO.setJwtClaimsCondition(getJWTClaimsConditionDetails((JWTClaimsCondition) condition));
        } else if (condition instanceof QueryParameterCondition) {
            conditionDTO.setQueryParameterCondition(getQueryParameterConditionDetails((QueryParameterCondition) condition));
        }
        return conditionDTO;
    }

    private IPConditionDTO getIPConditionDetails(IPCondition ipCondition) {
        IPConditionDTO ipConditionDTO = new IPConditionDTO();
        ipConditionDTO.setType(ipCondition.getType());
        ipConditionDTO.setSpecificIP(ipCondition.getSpecificIP());
        ipConditionDTO.setStartingIP(ipCondition.getStartingIP());
        ipConditionDTO.setEndingIP(ipCondition.getEndingIP());
        return ipConditionDTO;
    }

    private HeaderConditionDTO getHeaderConditionDetails(HeaderCondition headerCondition) {
        HeaderConditionDTO headerConditionDTO = new HeaderConditionDTO();
        headerConditionDTO.setHeaderName(headerCondition.getHeaderName());
        headerConditionDTO.setHeaderValue(headerCondition.getValue());
        return headerConditionDTO;
    }

    private JWTClaimsConditionDTO getJWTClaimsConditionDetails(JWTClaimsCondition jwtClaimsCondition) {
        JWTClaimsConditionDTO jwtClaimsConditionDTO = new JWTClaimsConditionDTO();
        jwtClaimsConditionDTO.setClaimUrl(jwtClaimsCondition.getClaimUrl());
        jwtClaimsConditionDTO.setClaimAttribute(jwtClaimsCondition.getAttribute());
        return jwtClaimsConditionDTO;
    }

    private QueryParameterConditionDTO getQueryParameterConditionDetails(QueryParameterCondition
                                                                                 queryParameterCondition) {
        QueryParameterConditionDTO queryParameterConditionDTO = new QueryParameterConditionDTO();
        queryParameterConditionDTO.setParameterName(queryParameterCondition.getParameter());
        queryParameterConditionDTO.setParameterValue(queryParameterCondition.getValue());
        return queryParameterConditionDTO;
    }
}
