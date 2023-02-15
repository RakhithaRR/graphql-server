package org.wso2.apk.graphql.api;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import org.wso2.apk.graphql.api.fetchers.APIFetcher;
import org.wso2.apk.graphql.api.utils.GraphQLUtils;

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
                .type(queryAPI())
                .build();
    }

    private TypeRuntimeWiring.Builder queryAPI() {
        return TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("getAPI", new APIFetcher().getAPIs());
    }
}
