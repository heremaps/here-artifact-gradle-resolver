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
            url ArtifactPropertiesResolver.resolveArtifactServiceUrl(HereAuth.getTokenEndpointUrl())
            metadataSources {
                artifact()
            }
            patternLayout {
                artifact 'hrn:here:artifact:::[organisation]:[module]:[revision]/[module]-[revision].[ext]'
            }
            credentials(HttpHeaderCredentials) {
                name = 'Authorization'
                value = "Bearer ${HereAuth.getToken()}"
            }
            authentication {
                header(HttpHeaderAuthentication)
            }
        })
    }

}