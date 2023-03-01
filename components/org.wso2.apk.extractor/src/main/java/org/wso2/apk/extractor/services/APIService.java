package org.wso2.apk.extractor.services;

import org.wso2.apk.extractor.datatypes.APIDataType;
import org.wso2.apk.extractor.fetchers.dto.APIListDTO;
import org.wso2.apk.extractor.mappings.APIDataTypeMapper;
import org.wso2.apk.extractor.utils.CommonUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.impl.APIConstants;
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
                if (APIConstants.API_PRODUCT.equalsIgnoreCase(api.getType())) {
                    APIProduct detailedAPIProduct = apiProvider.getAPIProductbyUUID(api.getUUID(), organizationName);
                    ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(detailedAPIProduct);
                    apiList.add(apiDataTypeMapper.mapAPIToAPIDataType(apiTypeWrapper));
                } else {
                    API detailedAPI = apiProvider.getAPIbyUUID(api.getUUID(), organizationName);
                    ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(detailedAPI);
                    apiList.add(apiDataTypeMapper.mapAPIToAPIDataType(apiTypeWrapper));
                }

            }
        }
        apiListDTO.setCount(apiList.size());
        apiListDTO.setList(apiList);
        return apiListDTO;
    }

    public static APIListDTO getAPIsByOrganization(String org) throws APIManagementException {
        List<Tenant> organizations = CommonUtils.loadTenants();
        APIListDTO apiListDTO = new APIListDTO();
        List<APIDataType> apiList = new ArrayList<>();
        for (Tenant organization : organizations) {
            if (!organization.getDomain().equals(org)) {
                continue;
            }
            String adminUsername = organization.getAdminName();
            String organizationName = organization.getDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(adminUsername);
            APIDataTypeMapper apiDataTypeMapper = new APIDataTypeMapper(apiProvider, adminUsername,
                    organizationName);
            List<API> apis = apiProvider.getAllAPIs();
            for (API api : apis) {
                if (APIConstants.API_PRODUCT.equalsIgnoreCase(api.getType())) {
                    APIProduct detailedAPIProduct = apiProvider.getAPIProductbyUUID(api.getUUID(), organizationName);
                    ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(detailedAPIProduct);
                    apiList.add(apiDataTypeMapper.mapAPIToAPIDataType(apiTypeWrapper));
                } else {
                    API detailedAPI = apiProvider.getAPIbyUUID(api.getUUID(), organizationName);
                    ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(detailedAPI);
                    apiList.add(apiDataTypeMapper.mapAPIToAPIDataType(apiTypeWrapper));
                }
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
            ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(api);
            return apiDataTypeMapper.mapAPIToAPIDataType(apiTypeWrapper);
        } catch (APIManagementException e) {
            return null;
        }
    }
}
