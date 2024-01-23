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
package com.here.platform.artifact.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.authentication.http.HttpHeaderAuthentication

class ArtifactResolver implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.repositories.metaClass.here = { Closure configClosure -> artifactRepo(project) }
    }

    static IvyArtifactRepository artifactRepo(Project project) {
        return project.repositories.ivy({
            name "HERE Artifact Service"
            url ArtifactPropertiesResolver.getInstance().resolveArtifactServiceUrl(HereAuth.getInstance().getTokenEndpointUrl())
            metadataSources {
                artifact()
            }
            patternLayout {
                artifact 'hrn:here:artifact:::[organisation]:[module]:[revision]/[module]-[revision].[ext]'
            }
            credentials(HttpHeaderCredentials) {
                name = 'Authorization'
                value = "Bearer ${HereAuth.getInstance().getToken()}"
            }
            authentication {
                header(HttpHeaderAuthentication)
            }
        })
    }

}