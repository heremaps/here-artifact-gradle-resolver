package com.here.platform.artifact.gradle;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;

class ArtifactPropertiesResolverTest {

  @Test
  public void testResolveArtifactServiceUrl() throws IOException {
    ArtifactPropertiesResolver artifactPropertiesResolver = Mockito.spy(new ArtifactPropertiesResolver());
    CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);
    CloseableHttpResponse responseMock = Mockito.mock(CloseableHttpResponse.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(responseMock.getStatusLine().getStatusCode()).thenReturn(200);
    Mockito.when(responseMock.getEntity().getContent()).thenReturn(new ByteArrayInputStream("[{\"baseURL\": \"mockedBaseUrl\"}]".getBytes()));
    Mockito.when(httpClientMock.execute(any())).thenReturn(responseMock);
    Mockito.when(artifactPropertiesResolver.buildClient()).thenReturn(httpClientMock);
    artifactPropertiesResolver.resolveArtifactServiceUrl("https://account.api.here.com/oauth2/token");
  }

  @Test
  public void testUnknownTokenEndpoint() {
    ArtifactPropertiesResolver artifactPropertiesResolver = new ArtifactPropertiesResolver();
    IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
      artifactPropertiesResolver.resolveArtifactServiceUrl("unknownUrl");
    });
    Assertions.assertEquals("Unknown token endpoint: unknownUrl", exception.getMessage());
  }

  @Test
  public void testErrorResponseFromLookupAPI() throws IOException {
    ArtifactPropertiesResolver artifactPropertiesResolver = Mockito.spy(new ArtifactPropertiesResolver());
    CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);
    CloseableHttpResponse responseMock = Mockito.mock(CloseableHttpResponse.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(responseMock.getStatusLine().getStatusCode()).thenReturn(500);
    Mockito.when(responseMock.getStatusLine().getReasonPhrase()).thenReturn("Some error reason");
    Mockito.when(httpClientMock.execute(any())).thenReturn(responseMock);
    Mockito.when(artifactPropertiesResolver.buildClient()).thenReturn(httpClientMock);
    RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
      artifactPropertiesResolver.resolveArtifactServiceUrl("https://account.api.here.com/oauth2/token");
    });
    Assertions.assertEquals("Unable to resolve Artifact Service URL. Status: Some error reason", exception.getMessage());
  }

  @Test
  public void testInstanceCreation() {
    Assertions.assertNotNull(ArtifactPropertiesResolver.getInstance());
  }

}