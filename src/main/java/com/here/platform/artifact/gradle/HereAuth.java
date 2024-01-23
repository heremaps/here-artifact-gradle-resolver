package com.here.platform.artifact.gradle;

import com.here.account.auth.provider.FromProperties;
import com.here.account.http.HttpProvider;
import com.here.account.http.apache.ApacheHttpClientProvider;
import com.here.account.oauth2.ClientCredentialsGrantRequest;
import com.here.account.oauth2.HereAccount;
import com.here.account.oauth2.TokenEndpoint;
import com.here.account.util.SettableSystemClock;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.Properties;

public class HereAuth {

  private static final int OAUTH_CONNECTION_TIMEOUT_IN_MS = 20000;

  private static final int OAUTH_REQUEST_TIMEOUT_IN_MS = 20000;

  private static final String HERE_ENDPOINT_URL_KEY = "here.token.endpoint.url";

  private static HereAuth INSTANCE;

  private Properties hereCredentials;

  HereAuth(CredentialsResolver credentialsResolver) {
    this.hereCredentials = credentialsResolver.resolveCredentials();
  }

  public static HereAuth getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new HereAuth(new CredentialsResolver());
    }
    return INSTANCE;
  }

  public String getTokenEndpointUrl() {
    return hereCredentials.getProperty(HERE_ENDPOINT_URL_KEY);
  }

  public String getToken() {
    TokenEndpoint tokenEndpoint = HereAccount.getTokenEndpoint(
        createHttpProvider(),
        new FromProperties(new SettableSystemClock(), hereCredentials)
    );
    return tokenEndpoint.requestToken(new ClientCredentialsGrantRequest()).getAccessToken();
  }

  HttpProvider createHttpProvider() {
    RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
        .setConnectTimeout(OAUTH_CONNECTION_TIMEOUT_IN_MS)
        .setConnectionRequestTimeout(OAUTH_REQUEST_TIMEOUT_IN_MS);
    HttpClientBuilder clientBuilder = HttpClientBuilder.create().useSystemProperties()
        .setDefaultRequestConfig(requestConfigBuilder.build());
    return ApacheHttpClientProvider.builder()
        .setHttpClient(clientBuilder.build())
        .setDoCloseHttpClient(true)
        .build();
  }

}
