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

  /**
   * Resolves schema default artifact service url based on here token url.
   *
   * @param tokenUrl here token url
   * @return resolved default artifact service url
   */

  public static String resolveArtifactServiceUrl(String tokenUrl) throws IOException {
    String artifactApiLookupUrl = getApiLookupUrl(tokenUrl) + "/platform/apis/artifact/v1";
    HttpGet httpGet = new HttpGet(artifactApiLookupUrl);
    try(CloseableHttpResponse response = executeRequest(httpGet)) {
      int statusCode = response.getStatusLine().getStatusCode();
      InputStream content = response.getEntity().getContent();
      if (HTTP_OK == statusCode) {
        return validatedAndParse(content);
      } else {
        throw new RuntimeException("Unable to resolve Artifact Service URL. Status: " + response.getStatusLine().getReasonPhrase());
      }
    }
  }

  private static String getApiLookupUrl(String tokenUrl) {
    String endpoint = tokenUrl.trim();
    String apiLookupUrl = URL_MAPPING.get(endpoint);
    if (apiLookupUrl == null) {
      throw new IllegalArgumentException("Unknown token endpoint: " + endpoint);
    }
    return apiLookupUrl;
  }

  private static String validatedAndParse(InputStream content) throws IOException {
    List result = JsonSerializer.toPojo(content, List.class);
    Map<String, Object> apiItem = (Map<String, Object>) result.get(0);
    return apiItem.get("baseURL").toString() + "/artifact";
  }

  private static CloseableHttpResponse executeRequest(HttpUriRequest httpRequest) throws IOException {
    setBearer(httpRequest);
    httpRequest.addHeader("Cache-control", "no-cache");
    httpRequest.addHeader("Cache-store", "no-store");
    httpRequest.addHeader("Pragma", "no-cache");
    httpRequest.addHeader("Expires", "0");
    httpRequest.addHeader("Accept-Encoding", "gzip");
    CloseableHttpClient client = HttpClientBuilder.create().build();
    return client.execute(httpRequest);
  }

  private static void setBearer(HttpUriRequest httpRequest) {
    String token = HereAuth.getToken();
    httpRequest.setHeader("Authorization", "Bearer " + token);
  }

}
