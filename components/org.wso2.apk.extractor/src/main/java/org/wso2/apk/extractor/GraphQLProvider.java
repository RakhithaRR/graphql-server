package org.wso2.apk.extractor;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import org.wso2.apk.extractor.fetchers.APIFetcher;
import org.wso2.apk.extractor.utils.GraphQLUtils;

public class GraphQLProvider {
    GraphQLSchema schema;

    public GraphQLSchema getSchema() {
        String schemaString = GraphQLUtils.getGraphQLSchema();
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schemaString);
        RuntimeWiring runtimeWiring = buildRuntime();
        schema = new SchemaGenerator().makeExecutableSchema(typeRegistry, runtimeWiring);
        return schema;
    }

    private RuntimeWiring buildRuntime() {
        return RuntimeWiring.newRuntimeWiring()
                .type(queryAllAPIs())
                .type(queryAPI())
                .type(queryAPIsByOrganization())
                .build();
    }

    private TypeRuntimeWiring.Builder queryAllAPIs() {
        return TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("getAPIs", new APIFetcher().getAPIs());
    }

    private TypeRuntimeWiring.Builder queryAPI() {
        return TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("getAPI", new APIFetcher().getAPI());
    }

    private TypeRuntimeWiring.Builder queryAPIsByOrganization() {
        return TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("getAPIsByOrganization", new APIFetcher().getAPIsByOrganization());
    }
}
