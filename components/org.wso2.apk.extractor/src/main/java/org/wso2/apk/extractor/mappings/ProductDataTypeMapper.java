package org.wso2.apk.extractor.mappings;

import org.wso2.apk.extractor.datatypes.APIDataType;
import org.wso2.apk.extractor.models.BusinessInformation;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class ProductDataTypeMapper {
    private final String organization;
    private final APIProvider apiProvider;
    private final String adminUsername;
    private final int tenantId;
    private final CommonMapper commonMapper;

    public ProductDataTypeMapper(APIProvider apiProvider, String adminUsername, String organization) {
        this.apiProvider = apiProvider;
        this.adminUsername = adminUsername;
        this.organization = organization;
        this.tenantId = APIUtil.getTenantIdFromTenantDomain(organization);
        this.commonMapper = new CommonMapper(apiProvider, organization);
    }

    public APIDataType mapProductToAPIDataType(APIProduct product) throws APIManagementException {
        APIDataType apiDataType = new APIDataType();
        apiDataType.setApiProduct(true);
        apiDataType.setId(product.getUuid());
        apiDataType.setName(product.getId().getName());
        apiDataType.setVersion(product.getId().getVersion());
        apiDataType.setContext(product.getContext());
        apiDataType.setOrganization(organization);
        apiDataType.setProvider(product.getId().getProviderName());
        apiDataType.setType(product.getType());
        apiDataType.setDescription(product.getDescription());
        apiDataType.setTransports(commonMapper.getTransports(product.getTransports()));
        apiDataType.setBusinessInformation(mapBusinessInformation(product));
        apiDataType.setTags(new ArrayList<>(product.getTags()));
        apiDataType.setAuthorizationHeader(product.getAuthorizationHeader());
        apiDataType.setSecurity(Arrays.asList(product.getApiSecurity().split(",")));
        apiDataType.setCategories(commonMapper.getCategories(product.getApiCategories()));
        apiDataType.setAdditionalProperties(product.getAdditionalProperties().toJSONString());
        apiDataType.setDefinition(commonMapper.getAPIDefinition(product.getId()));

        return apiDataType;
    }

    private BusinessInformation mapBusinessInformation(APIProduct product) {

        BusinessInformation businessInformation = new BusinessInformation();
        businessInformation.setBusinessOwner(product.getBusinessOwner());
        businessInformation.setBusinessOwnerEmail(product.getBusinessOwnerEmail());
        businessInformation.setTechnicalOwner(product.getTechnicalOwner());
        businessInformation.setTechnicalOwnerEmail(product.getTechnicalOwnerEmail());
        return businessInformation;
    }
}
