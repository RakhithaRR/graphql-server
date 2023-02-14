package org.wso2.apk.graphql.api;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.web.bind.annotation.RequestBody;
import org.wso2.apk.graphql.api.utils.GraphQLUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;

@Path("/")
public class GraphQLService {
    @POST
    @Path("/apis")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAPIs(GraphQLRequestBody graphQLRequestBody) {
        GraphQLSchema schema = new GraphQLProvider().getSchema();
        ExecutionInput.Builder builder = ExecutionInput.newExecutionInput()
                .query(graphQLRequestBody.getQuery())
                .operationName(graphQLRequestBody.getOperationName());
        ExecutionInput executionInput = builder.build();
        ExecutionResult executionResult = GraphQL.newGraphQL(schema).build().execute(executionInput);
        if (executionResult.getErrors().size() > 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity(executionResult.getErrors()).build();
        }
        LinkedHashMap<String, Object> result = executionResult.getData();
        return Response.ok(result, MediaType.APPLICATION_JSON).build();
//        String answer = "Hello World";
//        return Response.ok("{\"answer\": \"" + answer + "\"}", MediaType.APPLICATION_JSON).build();
    }
}
