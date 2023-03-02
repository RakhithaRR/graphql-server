package org.wso2.apk.extractor.mappings;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

public class EndpointConfigMapper {

    public static String processEndpointConfig(String endpointConfig) throws APIManagementException {
        if (endpointConfig != null) {
            Gson gson = new Gson();
            JsonObject endpointConfigJson = gson.fromJson(endpointConfig, JsonObject.class);
            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            if (endpointConfigJson.get(APIConstants.ENDPOINT_SECURITY) != null) {
                JsonObject endpointSecurityJson = endpointConfigJson.get(APIConstants.ENDPOINT_SECURITY)
                        .getAsJsonObject();

                // Process production endpoint security
                if (endpointSecurityJson.get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION) != null) {
                    JsonObject productionSecurityJson = endpointSecurityJson.get(APIConstants.OAuthConstants
                            .ENDPOINT_SECURITY_PRODUCTION).getAsJsonObject();
                    if (productionSecurityJson.get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_ENABLED)
                            .getAsBoolean()) {
                        String productionEndpointType = productionSecurityJson.get(APIConstants.OAuthConstants
                                .ENDPOINT_SECURITY_TYPE).getAsString();
                        if (APIConstants.OAuthConstants.OAUTH.equals(productionEndpointType)) {
                            String clientSecret = productionSecurityJson
                                    .get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET).getAsString();
                            if (StringUtils.isNotEmpty(clientSecret)) {
                                try {
                                    productionSecurityJson.addProperty(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET,
                                            new String(cryptoUtil.base64DecodeAndDecrypt(clientSecret)));
                                } catch (CryptoException e) {
                                    throw new APIManagementException("Error while decrypting client secret", e);
                                }
                            }
                        }
                    }
                    endpointSecurityJson.add(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION,
                            productionSecurityJson);
                    endpointConfigJson.add(APIConstants.ENDPOINT_SECURITY, endpointSecurityJson);
                }

                // Process Sandbox endpoint security
                if (endpointSecurityJson.get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX) != null) {
                    JsonObject sandboxSecurityJson = endpointSecurityJson.get(APIConstants.OAuthConstants
                            .ENDPOINT_SECURITY_SANDBOX).getAsJsonObject();
                    if (sandboxSecurityJson.get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_ENABLED).getAsBoolean()) {
                        String sandboxEndpointType = sandboxSecurityJson.get(APIConstants.OAuthConstants
                                .ENDPOINT_SECURITY_TYPE).getAsString();
                        if (APIConstants.OAuthConstants.OAUTH.equals(sandboxEndpointType)) {
                            String clientSecret = sandboxSecurityJson
                                    .get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET).getAsString();
                            if (StringUtils.isNotEmpty(clientSecret)) {
                                try {
                                    sandboxSecurityJson.addProperty(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET,
                                            new String(cryptoUtil.base64DecodeAndDecrypt(clientSecret)));
                                } catch (CryptoException e) {
                                    throw new APIManagementException("Error while decrypting client secret", e);
                                }
                            }
                        }
                    }
                    endpointSecurityJson.add(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX,
                            sandboxSecurityJson);
                    endpointConfigJson.add(APIConstants.ENDPOINT_SECURITY, endpointSecurityJson);
                }
            }
            return endpointConfigJson.toString();
        }
        return null;
    }
}
