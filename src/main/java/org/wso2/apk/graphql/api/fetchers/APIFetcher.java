package org.wso2.apk.graphql.api.fetchers;

import graphql.schema.DataFetcher;
import org.wso2.apk.graphql.api.services.APIService;
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

    public DataFetcher getAPI() {
        return dataFetchingEnvironment -> APIService.getAPI(dataFetchingEnvironment.getArgument("id"));
    }
}
