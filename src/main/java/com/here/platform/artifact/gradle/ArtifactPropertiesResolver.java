/*
 * Copyright (C) 2019-2024 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */
package com.here.platform.artifact.gradle;

import com.here.account.util.JsonSerializer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Resolves default artifact service url based on here token url.
 */
public class ArtifactPropertiesResolver {

    private static final String TOKEN_PROD_URL = "https://account.api.here.com/oauth2/token";

    private static final String TOKEN_STAGING_URL = "https://stg.account.api.here.com/oauth2/token";

    private static final String API_LOOKUP_PROD_URL = "https://api-lookup.data.api.platform.here.com/lookup/v1";

    private static final String API_LOOKUP_STAGING_URL = "https://api-lookup.data.api.platform.sit.here.com/lookup/v1";

    private static final String TOKEN_CN_PROD_URL = "https://account.hereapi.cn/oauth2/token";

    private static final String TOKEN_CN_STAGING_URL = "https://account.sit.hereapi.cn/oauth2/token";

    private static final String API_LOOKUP_CN_PROD_URL = "https://api-lookup.data.api.platform.hereolp.cn/lookup/v1/";

    private static final String API_LOOKUP_CN_STAGING_URL =
            "https://api-lookup.data.api.platform.in.hereolp.cn/lookup/v1/";

    // Regional domains are used until the services up on the target domains
    private static final String TOKEN_CN_REGIONAL_PROD_URL =
            "https://elb.cn-northwest-1.account.hereapi.cn/oauth2/token";

    private static final String TOKEN_CN_REGIONAL_STAGING_URL =
            "https://elb.cn-northwest-1.account.sit.hereapi.cn/oauth2/token";

    private static final Map<String, String> URL_MAPPING;

    static {
        Map<String, String> map = new HashMap<>();
        map.put(TOKEN_PROD_URL, API_LOOKUP_PROD_URL);
        map.put(TOKEN_STAGING_URL, API_LOOKUP_STAGING_URL);
        map.put(TOKEN_CN_PROD_URL, API_LOOKUP_CN_PROD_URL);
        map.put(TOKEN_CN_STAGING_URL, API_LOOKUP_CN_STAGING_URL);

        map.put(TOKEN_CN_REGIONAL_PROD_URL, API_LOOKUP_CN_PROD_URL);
        map.put(TOKEN_CN_REGIONAL_STAGING_URL, API_LOOKUP_CN_STAGING_URL);
        URL_MAPPING = Collections.unmodifiableMap(map);
    }

    private static ArtifactPropertiesResolver INSTANCE;

    ArtifactPropertiesResolver() {
    }

    public static ArtifactPropertiesResolver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ArtifactPropertiesResolver();
        }
        return INSTANCE;
    }

    /**
     * Resolves schema default artifact service url based on here token url.
     *
     * @param tokenUrl here token url
     * @return resolved default artifact service url
     */
    public String resolveArtifactServiceUrl(String tokenUrl) throws IOException {
        String artifactApiLookupUrl = getApiLookupUrl(tokenUrl) + "/platform/apis/artifact/v1";
        HttpUriRequest lookupRequest = prepareRequest(artifactApiLookupUrl);
        try (CloseableHttpClient client = buildClient();
                CloseableHttpResponse response = client.execute(lookupRequest)) {
            int statusCode = response.getStatusLine().getStatusCode();
            InputStream content = response.getEntity().getContent();
            if (HTTP_OK == statusCode) {
                return validatedAndParse(content);
            } else {
                throw new RuntimeException(
                    "Unable to resolve Artifact Service URL. Status: " + response.getStatusLine().getReasonPhrase());
            }
        }
    }

    private String getApiLookupUrl(String tokenUrl) {
        String endpoint = tokenUrl.trim();
        String apiLookupUrl = URL_MAPPING.get(endpoint);
        if (apiLookupUrl == null) {
            throw new IllegalArgumentException("Unknown token endpoint: " + endpoint);
        }
        return apiLookupUrl;
    }

    @SuppressWarnings("unchecked")
    private String validatedAndParse(InputStream content) throws IOException {
        List result = JsonSerializer.toPojo(content, List.class);
        Map<String, Object> apiItem = (Map<String, Object>) result.get(0);
        return apiItem.get("baseURL").toString() + "/artifact";
    }

    private HttpUriRequest prepareRequest(String artifactApiLookupUrl) {
        HttpGet httpRequest = new HttpGet(artifactApiLookupUrl);
        String token = HereAuth.getInstance().getToken();
        httpRequest.setHeader("Authorization", "Bearer " + token);
        httpRequest.addHeader("Cache-control", "no-cache");
        httpRequest.addHeader("Cache-store", "no-store");
        httpRequest.addHeader("Pragma", "no-cache");
        httpRequest.addHeader("Expires", "0");
        httpRequest.addHeader("Accept-Encoding", "gzip");
        return httpRequest;
    }

    CloseableHttpClient buildClient() {
        return HttpClientBuilder.create().build();
    }

}
