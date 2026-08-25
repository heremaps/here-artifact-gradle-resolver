/*
 * Copyright (C) 2019-2026 HERE Europe B.V.
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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Properties;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialsResolverTest {

    private CredentialsResolver credentialsResolver = new CredentialsResolver();

    @Test
    public void testValidateCredentials() {
        Properties hereCredentialsMock = mock(Properties.class);
        Mockito.when(hereCredentialsMock.getProperty(eq("here.token.endpoint.url"))).thenReturn("someUrl");
        Mockito.when(hereCredentialsMock.getProperty(eq("here.access.key.id"))).thenReturn("accessKey");
        Mockito.when(hereCredentialsMock.getProperty(eq("here.access.key.secret"))).thenReturn("accessKeySecret");
        Mockito.when(hereCredentialsMock.getProperty(eq("here.client.id"))).thenReturn("clientId");
        credentialsResolver.validateCredentials(hereCredentialsMock);
    }

    @Test
    public void testValidateCredentialsWithoutEndpointUrl() {
        Properties hereCredentialsMock = mock(Properties.class);
        Mockito.when(hereCredentialsMock.getProperty(eq("here.access.key.id"))).thenReturn("accessKey");
        Mockito.when(hereCredentialsMock.getProperty(eq("here.access.key.secret"))).thenReturn("accessKeySecret");
        Mockito.when(hereCredentialsMock.getProperty(eq("here.client.id"))).thenReturn("clientId");
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> credentialsResolver.validateCredentials(hereCredentialsMock));
        assertEquals("Credentials don't contain the property: here.token.endpoint.url", exception.getMessage());
    }

}