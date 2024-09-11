#!/bin/bash
# Copyright (C) 2019-2024 HERE Global B.V. and its affiliate(s).
# All rights reserved.
#
# This software and other materials contain proprietary information
# controlled by HERE and are protected by applicable copyright legislation.
# Any use and utilization of this software and other materials and
# disclosure to any third parties is conditional upon having a separate
# agreement with HERE for the access, use, utilization or disclosure of this
# software. In the absence of such agreement, the use of the software is not
# allowed.
#

set -ev

export GPG_TTY=$(tty)

gpg --version

export ORG_GRADLE_PROJECT_asciiArmoredSigningKey=$(echo $GPG_PRIVATE_KEY | base64 -d)
export ORG_GRADLE_PROJECT_signingPassword=$GPG_PASSPHRASE
export ORG_GRADLE_PROJECT_ossrhUsername=$OSSRH_USERNAME
export ORG_GRADLE_PROJECT_ossrhPassword=$OSSRH_PASSWORD

RELEASE_TAG=$(git describe --abbrev=0)
./gradlew clean publish closeAndReleaseStagingRepositories -PprojVersion=$RELEASE_TAG --info
