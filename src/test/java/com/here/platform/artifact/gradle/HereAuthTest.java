package com.here.platform.artifact.gradle;

import com.here.account.http.HttpException;
import com.here.account.http.HttpProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

class HereAuthTest {

  private HereAuth hereAuth;
  private Properties hereCredentialsMock;

  @BeforeEach
  public void init() {
    hereCredentialsMock = mock(Properties.class);
    CredentialsResolver credentialsResolverMock = mock(CredentialsResolver.class);
    Mockito.doReturn(hereCredentialsMock).when(credentialsResolverMock).resolveCredentials();
    hereAuth = new HereAuth(credentialsResolverMock);
  }

  @Test
  public void testGetTokenEndpointMock() {
    Mockito.when(hereCredentialsMock.getProperty(eq("here.token.endpoint.url"))).thenReturn("someUrl");
    String tokenEndpointUrl = hereAuth.getTokenEndpointUrl();
    assertEquals("someUrl", tokenEndpointUrl);
  }

  @Test
  public void testGetToken() throws HttpException, IOException {
    Mockito.when(hereCredentialsMock.getProperty(eq("here.token.endpoint.url"), any())).thenReturn("someUrl");
    Mockito.when(hereCredentialsMock.getProperty(eq("here.access.key.id"))).thenReturn("accessKey");
    Mockito.when(hereCredentialsMock.getProperty(eq("here.access.key.secret"))).thenReturn("accessKeySecret");
    Mockito.when(hereCredentialsMock.getProperty(eq("here.token.scope"))).thenReturn("scope");
    hereAuth = Mockito.spy(hereAuth);
    HttpProvider httpProviderMock = mock(HttpProvider.class);
    Mockito.when(httpProviderMock.execute(any())).thenReturn(new HttpProvider.HttpResponse() {
      @Override
      public int getStatusCode() {
        return 200;
      }

      @Override
      public long getContentLength() {
        return 0;
      }

      @Override
      public InputStream getResponseBody() throws IOException {
        return new ByteArrayInputStream("{\"access_token\": \"someToken\",\"token_type\" : \"\",\"expires_in\": 0,\"refresh_token\": \"\",\"id_token\" : \"\",\"scope\" : \"\"}".getBytes());
      }
    });
    Mockito.when(hereAuth.createHttpProvider()).thenReturn(httpProviderMock);
    String token = hereAuth.getToken();
    assertEquals("someToken", token);
  }

}