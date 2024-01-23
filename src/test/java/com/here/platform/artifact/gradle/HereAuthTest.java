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
                return new ByteArrayInputStream(
                    "{\"access_token\": \"someToken\",\"token_type\" : \"\",\"expires_in\": 0,\"refresh_token\": \"\",\"id_token\" : \"\",\"scope\" : \"\"}"
                        .getBytes());
            }
        });
        Mockito.when(hereAuth.createHttpProvider()).thenReturn(httpProviderMock);
        String token = hereAuth.getToken();
        assertEquals("someToken", token);
    }

}