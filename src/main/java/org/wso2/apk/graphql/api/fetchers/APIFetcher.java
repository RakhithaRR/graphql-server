package org.wso2.apk.graphql.api.fetchers;

import graphql.schema.DataFetcher;
import org.wso2.apk.graphql.api.services.APIService;

public class APIFetcher {

    public DataFetcher getAPIs() {
        return dataFetchingEnvironment -> APIService.getAPIs();
    }

    public DataFetcher getAPI() {
        return dataFetchingEnvironment -> APIService.getAPI(dataFetchingEnvironment.getArgument("id"));
    }
}
