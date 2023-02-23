package org.wso2.apk.graphql.api.mappings;

import org.apache.commons.io.IOUtils;
import org.wso2.apk.graphql.api.models.MediationDTO;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MediationMapper {
    private APIProvider apiProvider;
    private String adminUsername;
    private String organization;
    private final int tenantId;

    public MediationMapper(APIProvider apiProvider, String adminUsername, String organization, int tenantId) {
        this.apiProvider = apiProvider;
        this.adminUsername = adminUsername;
        this.organization = organization;
        this.tenantId = tenantId;
    }

    public List<MediationDTO> getMediationPolicies(API api) {
        List<MediationDTO> mediationPolicies = new ArrayList<>();
        String inMedPolicyName = api.getInSequence();
        if (inMedPolicyName != null && !inMedPolicyName.isEmpty()) {
            MediationDTO mediationPolicy = getMediationPolicy(api, inMedPolicyName,
                    APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN);
            mediationPolicies.add(mediationPolicy);
        }

        String outMedPolicyName = api.getOutSequence();
        if (outMedPolicyName != null && !outMedPolicyName.isEmpty()) {
            MediationDTO mediationPolicy = getMediationPolicy(api, outMedPolicyName,
                    APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT);
            mediationPolicies.add(mediationPolicy);
        }

        String faultMedPolicyName = api.getFaultSequence();
        if (faultMedPolicyName != null && !faultMedPolicyName.isEmpty()) {
            MediationDTO mediationPolicy = getMediationPolicy(api, faultMedPolicyName,
                    APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT);
            mediationPolicies.add(mediationPolicy);
        }

        return mediationPolicies;
    }

    private MediationDTO getMediationPolicy(API api, String policyName, String type) {
        MediationDTO mediationPolicy = new MediationDTO();
        Map<String, String> mediationPolicyAttributes = getMediationPolicyAttributes(policyName, type,
                api.getId(), tenantId);
        if (mediationPolicyAttributes != null) {
            String mediationPolicyUUID =
                    mediationPolicyAttributes.containsKey("uuid") ? mediationPolicyAttributes.get("uuid") : null;
            String mediationPolicyRegistryPath =
                    mediationPolicyAttributes.containsKey("path") ? mediationPolicyAttributes.get("path") : null;
            boolean sharedStatus = getSharedStatus(mediationPolicyRegistryPath, policyName);
            mediationPolicy.setId(mediationPolicyUUID);
            mediationPolicy.setShared(sharedStatus);
            try {
                mediationPolicy.setContent(getPolicyContent(api, mediationPolicyUUID));
            } catch (APIManagementException e) {
                //todo: handle exception
            }
        }
        mediationPolicy.setName(policyName);
        mediationPolicy.setType(type.toUpperCase());
        return mediationPolicy;
    }

    private Map<String, String> getMediationPolicyAttributes(String sequenceName, String direction,
                                                             APIIdentifier apiIdentifier, int tenantId) {
        try {
            return APIUtil.getMediationPolicyAttributes(sequenceName, tenantId, direction, apiIdentifier);
        } catch (APIManagementException e) {
//            log.error("Error occurred while getting the uuid of the mediation sequence", e);
        }
        return null;
    }

    private boolean getSharedStatus(String resourcePath, String policyName) {

        if (null != resourcePath && resourcePath.contains(APIConstants.API_CUSTOM_SEQUENCE_LOCATION)) {
            return true;
        }
        return false;
    }

    private String getPolicyContent(API api, String mediationPolicyId) throws APIManagementException {
        String content = "";
        String resourcePath = APIUtil.getAPIPath(api.getId());
        resourcePath = resourcePath.substring(0, resourcePath.lastIndexOf("/"));
        Resource mediationResource = apiProvider
                .getApiSpecificMediationResourceFromUuid(api.getId(), mediationPolicyId, resourcePath);
        if (mediationResource != null) {
            String resource = mediationResource.getPath();
            resource = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + resource;
            Map<String, Object> mediationPolicyResourceMap = APIUtil.getDocument(adminUsername, resource, organization);
            Object fileDataStream = mediationPolicyResourceMap.get(APIConstants.DOCUMENTATION_RESOURCE_MAP_DATA);
            try {
                byte[] fileData = IOUtils.toByteArray((InputStream) fileDataStream);
                content = new String(fileData, StandardCharsets.UTF_8);
            } catch (IOException e) {
                //todo: handle exception
            }
        } else {
            // Get global mediation policy if custom one is not there
            mediationResource = apiProvider.getCustomMediationResourceFromUuid(mediationPolicyId);
            String resource = mediationResource.getPath();
            resource = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + resource;
            Map<String, Object> mediationPolicyResourceMap = APIUtil.getDocument(adminUsername, resource, organization);
            Object fileDataStream = mediationPolicyResourceMap.get(APIConstants.DOCUMENTATION_RESOURCE_MAP_DATA);
            try {
                byte[] fileData = IOUtils.toByteArray((InputStream) fileDataStream);
                content = new String(fileData, StandardCharsets.UTF_8);
            } catch (IOException e) {
                //todo: handle exception
            }
        }
        return content;
    }
}
