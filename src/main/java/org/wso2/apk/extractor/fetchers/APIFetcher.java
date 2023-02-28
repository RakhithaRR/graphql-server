package org.wso2.apk.extractor.fetchers;

import graphql.schema.DataFetcher;
import org.wso2.apk.extractor.services.APIService;
import org.wso2.carbon.apimgt.api.APIManagementException;

public class APIFetcher {

    public DataFetcher getAPIs() {
        return dataFetchingEnvironment -> {
            try {
                return APIService.getAPIs();
            } catch (APIManagementException e) {
                throw new graphql.GraphQLException(e.getMessage());
            }
        };
    }

    public DataFetcher getAPIsByOrganization() {
        return dataFetchingEnvironment -> {
            try {
                return APIService.getAPIsByOrganization(dataFetchingEnvironment.getArgument("org"));
            } catch (APIManagementException e) {
                throw new graphql.GraphQLException(e.getMessage());
            }
        };
    }

    public DataFetcher getAPI() {
        return dataFetchingEnvironment -> APIService.getAPI(dataFetchingEnvironment.getArgument("id"));
    }
}
