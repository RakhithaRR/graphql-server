package org.wso2.apk.extractor.mappings;

import org.apache.commons.io.IOUtils;
import org.wso2.apk.extractor.datatypes.APIDataType;
import org.wso2.apk.extractor.models.BusinessInformation;
import org.wso2.apk.extractor.models.DesignConfigDTO;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

public class ProductDataTypeMapper {
    private final String organization;
    private final APIProvider apiProvider;
    private final String adminUsername;
    private final int tenantId;
    private final DocumentMapper documentMapper;
    private final CommonMapper commonMapper;

    public ProductDataTypeMapper(APIProvider apiProvider, String adminUsername, String organization) {
        this.apiProvider = apiProvider;
        this.adminUsername = adminUsername;
        this.organization = organization;
        this.tenantId = APIUtil.getTenantIdFromTenantDomain(organization);
        this.documentMapper = new DocumentMapper(apiProvider, adminUsername, organization);
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
        apiDataType.setThumbnail(getThumbnail(product));
        apiDataType.setDesignConfigurations(getDesignConfigDetails(product));
        apiDataType.setDocuments(documentMapper.getProductDocumentationDetails(product));

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

    private String getThumbnail(APIProduct product) throws APIManagementException {
        String base64EncodedThumbnail;
        try {
            ResourceFile thumbnail = apiProvider.getProductIcon(product.getId());
            if (thumbnail == null) {
                return null;
            }
            InputStream thumbnailStream = thumbnail.getContent();
            byte[] bytes = IOUtils.toByteArray(thumbnailStream);
            base64EncodedThumbnail = Base64.getEncoder().encodeToString(bytes);
            return base64EncodedThumbnail;
        } catch (IOException e) {
            throw new APIManagementException("Error while getting thumbnail for API Product "
                    + product.getId().getName(), e);
        }
    }

    private DesignConfigDTO getDesignConfigDetails(APIProduct product) {
        DesignConfigDTO designConfigDTO = new DesignConfigDTO();
        designConfigDTO.setAccessControl(product.getAccessControl());
        if (product.getAccessControlRoles() != null) {
            designConfigDTO.setAccessControlRoles(Arrays.asList(product.getAccessControlRoles().split(",")));
        }
        designConfigDTO.setVisibility(product.getVisibility());
        if (product.getVisibleRoles() != null) {
            designConfigDTO.setVisibleRoles(Arrays.asList(product.getVisibleRoles().split(",")));
        }
        if (product.getVisibleTenants() != null) {
            designConfigDTO.setVisibleTenants(Arrays.asList(product.getVisibleTenants().split(",")));
        }
        return designConfigDTO;
    }
}
