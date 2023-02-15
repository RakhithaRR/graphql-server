package org.wso2.apk.graphql.api.services;

import org.wso2.apk.graphql.api.datatypes.APIDataType;
import org.wso2.apk.graphql.api.mappings.APIDataTypeMapper;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.api.APIProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIService {
    public static APIDataType getAPIs() {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getProvider("admin");
            List<API> apis = apiProvider.getAllAPIs();
            API firstAPI = apis.get(0);
            API detailedAPI = apiProvider.getAPIbyUUID(firstAPI.getUuid(), "carbon.super");
//            Map<String,String> apiMap = new HashMap<>();
//            apiMap.put("name", firstAPI.getId().getApiName());
//            apiMap.put("version", firstAPI.getId().getVersion());
//            apiMap.put("context", firstAPI.getContext());
            return APIDataTypeMapper.mapAPIToAPIDataType(detailedAPI);
        } catch (APIManagementException e) {
            return null;
        }
//        return "Hello World";
    }
}
