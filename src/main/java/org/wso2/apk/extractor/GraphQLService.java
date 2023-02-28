package org.wso2.apk.extractor;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.wso2.apk.extractor.utils.FileUtils;

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
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveAPI(GraphQLRequestBody graphQLRequestBody) {
        GraphQLSchema schema = new GraphQLProvider().getSchema();
        ExecutionInput.Builder builder = ExecutionInput.newExecutionInput()
                .query(graphQLRequestBody.getQuery())
                .operationName(graphQLRequestBody.getOperationName());
        ExecutionInput executionInput = builder.build();
        ExecutionResult executionResult = GraphQL.newGraphQL(schema).build().execute(executionInput);
        if (executionResult.getErrors().size() > 0) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(executionResult.getErrors()).build();
        }
        LinkedHashMap<String, Object> result = executionResult.getData();
        try {
            FileUtils.saveExtractedData(result);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }
}
