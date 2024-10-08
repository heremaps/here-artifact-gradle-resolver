# Contributing Guide

## Introduction

The team gratefully accepts contributions via [pull requests](https://help.github.com/articles/about-pull-requests/).

## Build
To build the project run the command: `./gradlew build`.
To publish the plugin into the local Maven repository run the command `./gradlew publishToMavenLocal`. The plugin will be published with the `0.0.1-SNAPSHOT` version.


## Unit tests

The project has unit tests based on `junit-jupiter` and `mockito`. To run the tests run the command: `./gradlew test`.

## Continuous Integration

The CI is run on GitHub, meaning that files in the `.github` folder are used to define the workflows.

### Presubmit Verification
The purpose of this verification is to do a check that the code is not broken.

The presubmit verification does the following:

#### `Test` workflow

##### `Validate` step
This step checks the code style in all `.groovy` and `.java` files in the project. This job fails if the code is not formatted properly.

##### `Test` step
This step runs unit tests.

### Submit Verification
The purpose of this workflow is to verify that the `master` branch is always in the `ready for a deploy`
state and release the plugin into the [Maven Central repository](https://repo.maven.apache.org/maven2/com/here/platform/artifact/gradle/gradle-resolver/).

The submit verification runs all the [Presubmit Verification](#presubmit-verification) workflows with the following additional steps:
#### `Release` workflow

##### `Test` step
This step runs unit tests.

##### `Push git tag` step
The step increments the current version and pushes a new git tag.

##### `Deploy Release` step
This step releases the plugin to the [Maven Central repository](https://repo.maven.apache.org/maven2/com/here/platform/artifact/gradle/gradle-resolver/).

## Coding Standards

Styles conventions:

- Each java and groove class should have a **Copyright Notice**:
```text
/*
 * Copyright (C) 20<x>-20<y> HERE Europe B.V.
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
```
replace the `<x>` and `<y>` with numbers to denote the years in which the materials were created and modified.
- The package name should start with `com.here`
- The folder structure should reflect the package name

Source code format controlled by the `spotless` plugin. It's configuration stored in the file `codestyle-formatter-settings.xml`
To apply format run the command `./gradlew spotlessApply`

# Commit Signing

As part of filing a pull request we ask you to sign off the
[Developer Certificate of Origin](https://developercertificate.org/) (DCO) in each commit.
Any Pull Request with commits that are not signed off will be reject by the
[DCO check](https://probot.github.io/apps/dco/).

A DCO is lightweight way for contributors to confirm that they wrote or otherwise have the right
to submit code or documentation to a project. Simply add `Signed-off-by` as shown in the example below
to indicate that you agree with the DCO.

An example signed commit message:

```
    README.md: Fix minor spelling mistake

    Signed-off-by: John Doe <john.doe@example.com>
```

Git has the `-s` flag that can sign a commit for you, see example below:

`$ git commit -s -m 'README.md: Fix minor spelling mistake'`

# GitHub Actions
All opened pull requests are tested by GitHub Actions before they can be merged into the target branch.
After the new code is pushed to `master` GitHub Actions will run the test suite again, build the artifacts and release them
to Maven Central repository. The job will automatically increase patch version during this process.
If you do not want your changes to trigger a release, add the `[skip release]` flag to your commit message,
e.g., `git commit -s -m "[skip release] Fixed proxy configuration"`. We recommend this for example when you update
CI scripts or documentation such as README.md and CONTRIBUTING.md.