[![Build Status](https://github.com/heremaps/here-artifact-gradle-resolver/actions/workflows/release.yml/badge.svg)](https://github.com/heremaps/here-artifact-gradle-resolver/actions?query=workflow%3ARelease+branch%3Amaster)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.here.platform.artifact.gradle/gradle-resolver/badge.svg)](https://search.maven.org/artifact/com.here.platform.artifact.gradle/gradle-resolver)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

# HERE Gradle Resolver for Workspace and Marketplace

## Introduction
The HERE platform Gradle resolver plugin provides Java and Scala developers with access to HERE platform
artifacts via Gradle. It uses your HERE platform credentials to generate tokens so that it can pull your
Gradle project dependencies from the HERE platform.

This allows Marketplace and Workspace users to [fetch platform schemas](https://www.here.com/docs/bundle/here-workspace-developer-guide-java-scala/page/proto-schema/README.html).
In addition, the users can [fetch the Java / Scala Data Client Library](https://www.here.com/docs/bundle/data-client-library-developer-guide-java-scala/page/client/get-data.html)
which offer access to data in the HERE Data API.

Go to [the HERE Developer portal](https://developer.here.com/products/platform) to learn more about the HERE platform.

## Limitation
The Gradle resolver plugin is provided 'as is' and not officially part of Workspace or Marketplace.
While there is no official support by HERE, you may still raise issues via GitHub. We may be able to help.

## Prerequisites
To access the libraries and schemas from the HERE platform, you need a HERE Workspace and/or a HERE Marketplace account.
If you don’t have an account yet, go to [Pricing and Plans](https://www.here.com/get-started/pricing) to apply for a free trial.

Once you have enabled your account you need to create the credentials and prepare your environment.
Workspace users can find corresponding guidance [in the documentation for Java and Scala developers](https://www.here.com/docs/bundle/here-workspace-developer-guide-java-scala/page/topics/how-to-use-sdk.html).
Marketplace users can find instructions in the [Marketplace Consumer user guide](https://www.here.com/docs/bundle/marketplace-consumer-user-guide/page/topics/link-catalogs.html#set-up-your-credentials).

Please note, by default the Gradle resolver plugin uses the `credentials.properties` file provided in the `.here` directory in the user home directory.
There are three options to override the credentials:

- The first option is the system property `hereCredentialsFile`, the property should be added to the maven command the following way `-DhereCredentialsFile=/full/path/to/credentials.properties`.
- The second option is the environment variable `HERE_CREDENTIALS_FILE`.  The variable should contain the full file path to the `credentials.properties` file to be used. The variable is taken into account only if there is no system property provided.
- The third option is the environment variable `HERE_CREDENTIALS_STRING`, the variable should have the following format:
```
here.access.key.id=...
here.access.key.secret=...
here.client.id=...
here.user.id=...
here.token.endpoint.url=...
```
Note that providing credentials via `HERE_CREDENTIALS_STRING` variable have the lowest precedence

## How to use it?
This Gradle resolver plugin is published on [Maven Central](https://search.maven.org/artifact/com.here.platform.artifact/gradle-resolver)
so you can conveniently use it from your project.
The `gradle-resolver` plugin can be registered by adding an entry to `build.gradle` file as follows:


    plugins {
        id 'com.here.platform.artifact.gradle' version '1.0.0'
    }
    repositories {
        here()
    }


For example, to fetch the HERE Map Content - Topology Geometry - Protocol Buffers schema and the related Java and Scala bindings set the following dependencies:


    dependencies {
        implementation "com.here.schema.rib:topology-geometry_v2_java:2.109.0"
    }

#### Proxy Setup
To enable Gradle and the HERE Gradle resolver plugin to work behind a proxy,
you need to use standard `http_proxy` and `https_proxy` environment variables, like:
```shell
gradle build -Dhttps.proxyHost=$PROXY_HOST  -Dhttps.proxyPort=$PROXY_PORT -Dhttps.proxyUser=$PROXY_USERNAME -Dhttps.proxyPassword=$PROXY_PASSWORD
```
Alternatively, pass those by setting `JAVA_OPTS` in the environment variable, like:
```shell
export JAVA_OPTS="-Dhttps.proxyHost=$PROXY_HOST  -Dhttps.proxyPort=$PROXY_PORT -Dhttps.proxyUser=$PROXY_USERNAME -Dhttps.proxyPassword=$PROXY_PASSWORD"
```

## License
Copyright (C) 2019-2024 HERE Europe B.V.

Unless otherwise noted in `LICENSE` files for specific files or directories, the [LICENSE](LICENSE) in the root applies to all content in this repository.
