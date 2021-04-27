package com.symphony.security.containers

 @Library('SnykShared@master')                                                                                                                                                                    
import com.symphony.security.containers.Control
import com.symphony.security.containers.Artifactory



class Builder {
    def steps
    def artifactory_repo = 'slex-reg-test/'
    
    Builder(steps) {
        this.steps = steps
    }

    def buildAndPublish(image_name, dockerfile, context_path) {
        // docker build
        steps.echo "### Building container image ${image}"
        steps.sh (script: "DOCKER_BUILDKIT=1 DOCKER_CONTENT_TRUST=1 docker build --no-cache -f ${dockerfile} -t ${image_name} ${context_path}", returnStdout: true)

        // security checks
        def security = new Control(this)
        security.base_image(image_name)

        // push to repo
        def artifactory = new Artifactory(this)
        artifactory.push(image_name, artifactory_repo + image_name)
    }
}