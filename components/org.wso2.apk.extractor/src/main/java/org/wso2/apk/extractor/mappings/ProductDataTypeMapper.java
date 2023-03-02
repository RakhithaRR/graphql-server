package org.wso2.apk.extractor.mappings;

import org.apache.commons.io.IOUtils;
import org.wso2.apk.extractor.datatypes.APIDataType;
import org.wso2.apk.extractor.models.*;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

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
        apiDataType.setClientCertificates(getClientCertificates(product));
        apiDataType.setProductAPIs(getProductAPIsAndResources(product));

        return apiDataType;
    }

    private List<ProductAPIDTO> getProductAPIsAndResources(APIProduct product) {
        Map<String, ProductAPIDTO> aggregatedAPIs = new HashMap<String, ProductAPIDTO>();
        List<APIProductResource> resources = product.getProductResources();
        for (APIProductResource apiProductResource : resources) {
            String uuid = apiProductResource.getApiId();
            URITemplate uriTemplate = apiProductResource.getUriTemplate();
            if (aggregatedAPIs.containsKey(uuid)) {
                ProductAPIDTO productAPIDTO = aggregatedAPIs.get(uuid);
                List<OperationDTO> operations = productAPIDTO.getOperations();
                operations.add(getOperationFromURITemplate(uriTemplate));
            }
            ProductAPIDTO productAPIDTO = new ProductAPIDTO();
            List<OperationDTO> operations = new ArrayList<>();
            productAPIDTO.setUuid(uuid);
            productAPIDTO.setName(apiProductResource.getApiIdentifier().getName());
            productAPIDTO.setVersion(apiProductResource.getApiIdentifier().getVersion());
            operations.add(getOperationFromURITemplate(uriTemplate));
            productAPIDTO.setOperations(operations);
            aggregatedAPIs.put(uuid, productAPIDTO);
        }
        return new ArrayList<>(aggregatedAPIs.values());
    }

    private OperationDTO getOperationFromURITemplate(URITemplate uriTemplate) {
        OperationDTO operationDTO = new OperationDTO();
        operationDTO.setVerb(uriTemplate.getHTTPVerb());
        operationDTO.setTarget(uriTemplate.getUriTemplate());
        operationDTO.setAuthTypeEnabled(!APIConstants.AUTH_NO_AUTHENTICATION.equals(uriTemplate.getAuthType()));
        operationDTO.setThrottlingPolicy(uriTemplate.getThrottlingTier());
        operationDTO.setScopes(uriTemplate.retrieveAllScopes()
                .stream()
                .map(Scope -> {
                    ScopeDTO scopeDTO = new ScopeDTO();
                    scopeDTO.setName(Scope.getKey());
                    return scopeDTO;
                })
                .collect(Collectors.toList()));
        return operationDTO;
    }

    private List<CertificateDTO> getClientCertificates(APIProduct product) throws APIManagementException {
        List<CertificateDTO> certificateDTOList = new ArrayList<>();
        List<ClientCertificateDTO> certificates = apiProvider.searchClientCertificates(tenantId, null,
                product.getId());
        for (ClientCertificateDTO certificate : certificates) {
            CertificateDTO certificateDTO = new CertificateDTO();
            certificateDTO.setAlias(certificate.getAlias());
            certificateDTO.setApiId(certificate.getApiIdentifier().getUUID());
            certificateDTO.setCertificate(certificate.getCertificate());
            certificateDTO.setTierName(certificate.getTierName());
            certificateDTOList.add(certificateDTO);
        }
        return certificateDTOList;
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
