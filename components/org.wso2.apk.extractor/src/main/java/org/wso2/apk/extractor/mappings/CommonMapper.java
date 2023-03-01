package org.wso2.apk.extractor.mappings;

import org.apache.commons.lang3.StringUtils;
import org.wso2.apk.extractor.models.CategoryDTO;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonMapper {
    private final String organization;
    private final APIProvider apiProvider;

    public CommonMapper(APIProvider apiProvider, String organization) {
        this.apiProvider = apiProvider;
        this.organization = organization;
    }

    public List<String> getTransports(String transportsString) {
        if (StringUtils.isEmpty(transportsString)) {
            List<String> transports = new ArrayList<>();
            transports.add(APIConstants.HTTPS_PROTOCOL);
            return transports;
        } else {
            return Arrays.asList(transportsString.split(","));
        }
    }

    public List<CategoryDTO> getCategories(List<APICategory> apiCategories) {

        List<CategoryDTO> categoryNames = new ArrayList<>();
        if (apiCategories != null && !apiCategories.isEmpty()) {
            for (APICategory category : apiCategories) {
                CategoryDTO categoryDTO = new CategoryDTO();
                categoryDTO.setName(category.getName());
                categoryDTO.setDescription(category.getDescription());
                categoryDTO.setOrganization(organization);
                categoryNames.add(categoryDTO);
            }
        }
        return categoryNames;
    }

    public String getAPIDefinition(Identifier identifier) throws APIManagementException {
        return apiProvider.getOpenAPIDefinition(identifier);
    }
}
