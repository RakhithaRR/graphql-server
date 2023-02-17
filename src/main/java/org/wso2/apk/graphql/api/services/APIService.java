package org.wso2.apk.graphql.api.services;

import org.wso2.apk.graphql.api.datatypes.APIDataType;
import org.wso2.apk.graphql.api.fetchers.dto.APIListDTO;
import org.wso2.apk.graphql.api.mappings.APIDataTypeMapper;
import org.wso2.apk.graphql.api.utils.CommonUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.user.api.Tenant;

import java.util.ArrayList;
import java.util.List;


public class APIService {
    public static APIListDTO getAPIs() {
        List<Tenant> organizations = CommonUtils.loadTenants();
        APIListDTO apiListDTO = new APIListDTO();
        for (Tenant organization : organizations) {
            String adminUsername = organization.getAdminName();
            String organizationName = organization.getDomain();
            try {
                List<APIDataType> apiList = new ArrayList<>();
                APIProvider apiProvider = RestApiCommonUtil.getProvider(adminUsername);
                List<API> apis = apiProvider.getAllAPIs();
                for (API api : apis) {
                    API detailedAPI = apiProvider.getAPIbyUUID(api.getUuid(), organizationName);
                    apiList.add(APIDataTypeMapper.mapAPIToAPIDataType(detailedAPI));
                }
                apiListDTO.setCount(apiList.size());
                apiListDTO.setList(apiList);
            } catch (APIManagementException e) {
                return null;
            }
        }
        return apiListDTO;
    }

    public static APIDataType getAPI(String id) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getProvider("admin");
            API api = apiProvider.getAPIbyUUID(id, "carbon.super");
            return APIDataTypeMapper.mapAPIToAPIDataType(api);
        } catch (APIManagementException e) {
            return null;
        }
    }
}
