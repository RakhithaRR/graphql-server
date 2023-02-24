package org.wso2.apk.graphql.api.services;

import org.wso2.apk.graphql.api.datatypes.APIDataType;
import org.wso2.apk.graphql.api.fetchers.dto.APIListDTO;
import org.wso2.apk.graphql.api.mappings.APIDataTypeMapper;
import org.wso2.apk.graphql.api.utils.CommonUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.user.api.Tenant;

import java.util.ArrayList;
import java.util.List;


public class APIService {
    public static APIListDTO getAPIs() throws APIManagementException {
        List<Tenant> organizations = CommonUtils.loadTenants();
        APIListDTO apiListDTO = new APIListDTO();
        List<APIDataType> apiList = new ArrayList<>();
        for (Tenant organization : organizations) {
            String adminUsername = organization.getAdminName();
            String organizationName = organization.getDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(adminUsername);
            APIDataTypeMapper apiDataTypeMapper = new APIDataTypeMapper(apiProvider, adminUsername,
                    organizationName);
            List<API> apis = apiProvider.getAllAPIs();
            for (API api : apis) {
                API detailedAPI = apiProvider.getAPIbyUUID(api.getUUID(), organizationName);
                apiList.add(apiDataTypeMapper.mapAPIToAPIDataType(detailedAPI));
            }
        }
        apiListDTO.setCount(apiList.size());
        apiListDTO.setList(apiList);
        return apiListDTO;
    }

    public static APIDataType getAPI(String id) {
        try {
            APIProvider apiProvider = RestApiUtil.getProvider("admin");
            API api = apiProvider.getAPIbyUUID(id, "carbon.super");
            APIDataTypeMapper apiDataTypeMapper = new APIDataTypeMapper(apiProvider, "admin", "carbon.super");
            return apiDataTypeMapper.mapAPIToAPIDataType(api);
        } catch (APIManagementException e) {
            return null;
        }
    }
}
