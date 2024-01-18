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

import java.io.*;
import java.util.Properties;

import static org.apache.http.util.TextUtils.isEmpty;

public class HereAuth {

  private static final int OAUTH_CONNECTION_TIMEOUT_IN_MS = 20000;

  private static final int OAUTH_REQUEST_TIMEOUT_IN_MS = 20000;

  private static final String HERE_CREDENTIALS_PROPERTY = "hereCredentialsFile";
  private static final String HERE_CREDENTIALS_STRING_ENV = "HERE_CREDENTIALS_STRING";
  private static final String HERE_CREDENTIALS_ENV = "HERE_CREDENTIALS_FILE";
  private static final String HERE_CREDENTIALS_PATH = ".here/credentials.properties";
  private static final String HERE_ENDPOINT_URL_KEY = "here.token.endpoint.url";

  private static final Properties hereCredentials = loadHereProperties();

  private static String token;

  public static String getTokenEndpointUrl() {
    return hereCredentials.getProperty(HERE_ENDPOINT_URL_KEY);
  }

  public static String getToken() {
    if (token == null) {
      TokenEndpoint tokenEndpoint = HereAccount.getTokenEndpoint(
          createHttpProvider(),
          new FromProperties(new SettableSystemClock(), hereCredentials)
      );
      token = tokenEndpoint.requestToken(new ClientCredentialsGrantRequest()).getAccessToken();
    }
    return token;
  }

  private static HttpProvider createHttpProvider() {
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


  protected static Properties loadHereProperties() {
    Properties properties = new Properties();
    File file = resolveFile();
    if (file != null) {
      loadCredentialsFromFile(properties, file);
    }
    String credentialsString = System.getenv(HERE_CREDENTIALS_STRING_ENV);
    if (properties.isEmpty() && !isEmpty(credentialsString)) {
      loadCredentialsFromString(properties, credentialsString);
    } else {
      loadCredentialsFromFile(properties, new File(System.getProperty("user.home"), HERE_CREDENTIALS_PATH));
    }
    return properties;
  }

  private static File resolveFile() {
    File file = null;
    String systemPropertyFile = System.getProperty(HERE_CREDENTIALS_PROPERTY);
    if (isEmpty(systemPropertyFile)) {
      systemPropertyFile = System.getenv(HERE_CREDENTIALS_ENV);
    }
    if (!isEmpty(systemPropertyFile)) {
      file = new File(systemPropertyFile);
    }
    return file;
  }

  private static void loadCredentialsFromFile(Properties properties, File file) {
    if (file.exists() && file.canRead()) {
      try (InputStream in = new FileInputStream(file)) {
        properties.load(in);
      } catch (IOException exp) {
        throw new RuntimeException("Unable to read client credentials at " + file.getAbsolutePath(), exp);
      }
    }
  }

  private static void loadCredentialsFromString(Properties properties, String credentialsString) {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(credentialsString.getBytes());
    try {
      properties.load(byteArrayInputStream);
    } catch (IOException exp) {
      throw new RuntimeException("Unable to create client credentials from environment variable " + HERE_CREDENTIALS_STRING_ENV, exp);
    }
  }
}
