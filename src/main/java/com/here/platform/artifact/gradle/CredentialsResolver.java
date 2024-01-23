package com.here.platform.artifact.gradle;

import java.io.*;
import java.util.Properties;

import static org.apache.http.util.TextUtils.isEmpty;

/**
 * Resolves credentials based on system configuration. Credentials precedence:
 * 1) -DhereCredentialsFile system property
 * 2) HERE_CREDENTIALS_FILE environment variable
 * 3) HERE_CREDENTIALS_STRING environment variable
 * 4) ~/.here/credentials.properties file
 */
public class CredentialsResolver {

  private static final String HERE_CREDENTIALS_PROPERTY = "hereCredentialsFile";
  private static final String HERE_CREDENTIALS_STRING_ENV = "HERE_CREDENTIALS_STRING";
  private static final String HERE_CREDENTIALS_PATH = ".here/credentials.properties";
  private static final String HERE_CREDENTIALS_ENV = "HERE_CREDENTIALS_FILE";

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
    return properties;
  }

  private File resolveFile() {
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

  private void loadCredentialsFromFile(Properties properties, File file) {
    if (file.exists() && file.canRead()) {
      try (InputStream in = new FileInputStream(file)) {
        properties.load(in);
      } catch (IOException exp) {
        throw new RuntimeException("Unable to read client credentials at " + file.getAbsolutePath(), exp);
      }
    }
  }

  private void loadCredentialsFromString(Properties properties, String credentialsString) {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(credentialsString.getBytes());
    try {
      properties.load(byteArrayInputStream);
    } catch (IOException exp) {
      throw new RuntimeException("Unable to create client credentials from environment variable " + HERE_CREDENTIALS_STRING_ENV, exp);
    }
  }
}
