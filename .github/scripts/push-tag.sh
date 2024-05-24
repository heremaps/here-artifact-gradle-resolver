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

# Prepare release
PREVIOUS_RELEASE_TAG=$(git describe --abbrev=0)
if [[ $PREVIOUS_RELEASE_TAG =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  # Set current released version
  RELEASE_TAG=$(echo ${PREVIOUS_RELEASE_TAG} | awk -F. -v OFS=. '{$NF += 1 ; print}')
else
  echo "Cannot parse the latest release tag: ${PREVIOUS_RELEASE_TAG}"
  exit 1
fi

git config user.name "GitHub CI"
git config user.email "ARTIFACT_SERVICE_SUPPORT@here.com"

git tag -a "${RELEASE_TAG}" -m "Release ${RELEASE_TAG} from build ${GITHUB_JOB}"

git push origin --tags