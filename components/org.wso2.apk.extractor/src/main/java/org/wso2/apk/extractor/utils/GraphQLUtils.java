package org.wso2.apk.extractor.utils;

import org.wso2.apk.extractor.GraphQLConstants;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GraphQLUtils {
    public static String getGraphQLSchema() {
        StringBuffer schema = new StringBuffer();
        InputStream inputStream = GraphQLUtils.class.getClassLoader()
                .getResourceAsStream(GraphQLConstants.GRAPHQL_SCHEMA);
        if (inputStream != null) {
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = buffer.readLine()) != null) {
                    schema.append(line);
                }
                return schema.toString();
            } catch (Exception e) {
                return "ERROR123";
            }
        } else {
            return "Invalid schema";
        }
    }
}
