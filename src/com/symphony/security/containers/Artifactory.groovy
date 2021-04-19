package com.symphony.security.containers

// 1. login with creds
// 2. tag && push image 
class Artifactory {
    
    private def steps
    private Boolean initialized = false
    private String artifactory_url = "artifact.symphony.com"
   
    Artifactory(steps) {
        this.steps = steps
    }

    def init() {
        if (!initialized) {
            steps.withCredentials([steps.usernamePassword(credentialsId: 'artifactory_registry_svc_user', usernameVariable: 'username', passwordVariable: 'password')]) {
                steps.sh (script: "#!/bin/sh -e\n docker login --username ${env.username} --password ${env.password} ${artifactory_url})", returnStdout: true)
            }
        }
        initialized = true
    }

    def push(image_local, image_repository) {
        init()
        steps.sh (script: "#!/bin/sh -e\n docker tag ${image_local} ${artifactory_url}/${image_repository}", returnStdout: true)
        steps.sh (script: "#!/bin/sh -e\n docker push ${artifactory_url}/${image_repository}", returnStdout: true)
    }
}

