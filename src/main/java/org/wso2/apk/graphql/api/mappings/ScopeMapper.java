package org.wso2.apk.graphql.api.mappings;

import org.wso2.apk.graphql.api.models.ScopeDTO;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;

import java.util.*;

public class ScopeMapper {
    private final String organization;
    private final APIProvider apiProvider;

    public ScopeMapper(APIProvider apiProvider, String organization) {
        this.apiProvider = apiProvider;
        this.organization = organization;
    }

    public List<ScopeDTO> getScopesFromDefinition(String swagger) throws APIManagementException {
        APIDefinition apiDefinition = OASParserUtil.getOASParser(swagger);
        Set<Scope> scopes = apiDefinition.getScopes(swagger);
        List<ScopeDTO> scopeDTOS = new ArrayList<>();
        Set<String> allSharedScopeKeys = apiProvider.getAllSharedScopeKeys(organization);
        for (Scope aScope : scopes) {
            ScopeDTO scopeDTO = new ScopeDTO();
            scopeDTO.setName(aScope.getKey());
            scopeDTO.setDisplayName(aScope.getName());
            scopeDTO.setDescription(aScope.getDescription());
            String roles = aScope.getRoles();
            if (roles == null || roles.isEmpty()) {
                scopeDTO.setBindings(Collections.emptyList());
            } else {
                scopeDTO.setBindings(Arrays.asList((roles).split(",")));
            }
            scopeDTO.setShared(allSharedScopeKeys.contains(aScope.getKey()) ? Boolean.TRUE : Boolean.FALSE);
            scopeDTOS.add(scopeDTO);
        }
        return scopeDTOS;
    }


}
