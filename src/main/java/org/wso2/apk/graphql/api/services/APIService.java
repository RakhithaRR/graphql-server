package org.wso2.apk.graphql.api.services;

import org.wso2.apk.graphql.api.datatypes.APIDataType;
import org.wso2.apk.graphql.api.fetchers.dto.APIListDTO;
import org.wso2.apk.graphql.api.mappings.APIDataTypeMapper;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.api.APIProvider;

import java.util.ArrayList;
import java.util.List;


public class APIService {
    public static APIListDTO getAPIs() {
        try {
            APIListDTO apiListDTO = new APIListDTO();
            List<APIDataType> apiList = new ArrayList<>();
            APIProvider apiProvider = RestApiCommonUtil.getProvider("admin");
            List<API> apis = apiProvider.getAllAPIs();
            for (API api : apis) {
                API detailedAPI = apiProvider.getAPIbyUUID(api.getUuid(), "carbon.super");
                apiList.add(APIDataTypeMapper.mapAPIToAPIDataType(detailedAPI));
            }
            apiListDTO.setCount(apiList.size());
            apiListDTO.setList(apiList);
            return apiListDTO;
        } catch (APIManagementException e) {
            return null;
        }
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
