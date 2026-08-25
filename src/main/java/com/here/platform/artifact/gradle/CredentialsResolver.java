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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

import static org.apache.http.util.TextUtils.isEmpty;

/**
 * Resolves credentials based on system configuration. Credentials precedence:
 * <ol>
 * <li>-DhereCredentialsFile system property
 * <li>HERE_CREDENTIALS_FILE environment variable
 * <li>HERE_CREDENTIALS_STRING environment variable
 * <li>~/.here/credentials.properties file
 * </ol>
 */
public class CredentialsResolver {

    public static final String ACCESS_KEY_ID = "here.access.key.id";
    public static final String ACCESS_KEY_SECRET = "here.access.key.secret";
    public static final String TOKEN_ENDPOINT_URL = "here.token.endpoint.url";
    public static final String CLIENT_ID = "here.client.id";

    private static final Logger LOG = LoggerFactory.getLogger(CredentialsResolver.class);

    private static final String HERE_CREDENTIALS_PROPERTY = "hereCredentialsFile";
    private static final String HERE_CREDENTIALS_STRING_ENV = "HERE_CREDENTIALS_STRING";
    private static final String HERE_CREDENTIALS_PATH = ".here/credentials.properties";
    private static final String HERE_CREDENTIALS_ENV = "HERE_CREDENTIALS_FILE";

    /**
     * Resolve credentials based on a precedence:
     * <ol>
     * <li>-DhereCredentialsFile system property
     * <li>HERE_CREDENTIALS_FILE environment variable
     * <li>HERE_CREDENTIALS_STRING environment variable
     * <li>~/.here/credentials.properties file
     * </ol>
     * 
     * @return resolved credentials
     */
    public Properties resolveCredentials() {
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
        validateCredentials(properties);
        return properties;
    }

    private File resolveFile() {
        File file = null;
        String systemPropertyFile = System.getProperty(HERE_CREDENTIALS_PROPERTY);
        if (!isEmpty(systemPropertyFile)) {
            LOG
                .debug("Found property file value at System Property {}: {}", HERE_CREDENTIALS_PROPERTY,
                    systemPropertyFile);
        } else {
            systemPropertyFile = System.getenv(HERE_CREDENTIALS_ENV);
            if (!isEmpty(systemPropertyFile)) {
                LOG
                    .debug("Found property file at Environment Property {}: {}", HERE_CREDENTIALS_ENV,
                        systemPropertyFile);
            }
        }
        if (!isEmpty(systemPropertyFile)) {
            file = new File(systemPropertyFile);
        }
        return file;
    }

    private void loadCredentialsFromFile(Properties properties, File file) {
        LOG.debug("Using here credentials file: {}", file.getAbsolutePath());
        if (file.exists() && file.canRead()) {
            LOG.debug("Attempting to read credentials file at: {}", file.getAbsolutePath());
            try (InputStream in = new FileInputStream(file)) {
                properties.load(in);
            } catch (IOException exp) {
                LOG.error("Unable to read client credentials at {}", file.getAbsolutePath(), exp);
                throw new RuntimeException("Unable to read client credentials at " + file.getAbsolutePath(), exp);
            }
        } else {
            LOG.warn("Unable to read configured file: {}", file.getAbsolutePath());
        }
    }

    private void loadCredentialsFromString(Properties properties, String credentialsString) {
        LOG.debug("Attempting to create credentials from environment variable");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(credentialsString.getBytes());
        try {
            properties.load(byteArrayInputStream);
        } catch (IOException exp) {
            LOG
                .error("Unable to create client credentials from environment variable {}", HERE_CREDENTIALS_STRING_ENV,
                    exp);
            throw new RuntimeException(
                "Unable to create client credentials from environment variable " + HERE_CREDENTIALS_STRING_ENV, exp);
        }
    }

    void validateCredentials(Properties properties) {
        propertyMustExists(properties, ACCESS_KEY_ID);
        propertyMustExists(properties, ACCESS_KEY_SECRET);
        propertyMustExists(properties, TOKEN_ENDPOINT_URL);
        propertyMustExists(properties, CLIENT_ID);
    }

    private void propertyMustExists(Properties properties, String propertyName) {
        if (properties.getProperty(propertyName) == null) {
            throw new RuntimeException("Credentials don't contain the property: " + propertyName);
        }
    }
}
