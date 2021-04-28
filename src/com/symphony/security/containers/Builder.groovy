package com.symphony.security.containers

 @Library('SnykShared@master')                                                                                                                                                                    
import com.symphony.security.containers.Control
import com.symphony.security.containers.Artifactory



class Builder {
    def steps
    def artifactory_repo = 'slex-reg-test/'
    def buildkit = "DOCKER_BUILDKIT=1"
    def content_trust = "DOCKER_CONTENT_TRUST=1"
    
    Builder(steps) {
        this.steps = steps
    }

    def buildkit(v) {
        if (v == true) {
            buildkit = "DOCKER_BUILDKIT=1"
        } else {
            buildkit = "DOCKER_BUILDKIT=0"
        }
    }

    def contentTrust(v) {
        if (v == true) {
            content_trust = "DOCKER_CONTENT_TRUST=1"
        } else {
            content_trust = "DOCKER_CONTENT_TRUST=0"
        }
    }

    def dockerBuild(image_name, dockerfile, context_path) {
        steps.echo "### Building container image ${image_name}"
        steps.sh (script: "${this.buildkit} ${this.content_trust} docker build --no-cache --pull -f ${dockerfile} -t ${image_name} ${context_path}", returnStdout: true)
        steps.echo "### finished building"
    }

    def buildAndPublish(image_name, dockerfile, context_path) {
        // docker build
        steps.echo "### Building container image ${image_name}"
        steps.sh (script: "${this.buildkit} ${this.content_trust} docker build --progress plain --no-cache -f ${dockerfile} -t ${image_name} ${context_path}", returnStdout: true)
        steps.echo "### finished building"

        // security checks
        steps.echo "### SECURITY CHECKS"
        def security = new Control(this.steps)
        security.base_image(image_name, dockerfile)
        steps.echo "### END SECURITY"

        // push to repo
        steps.echo "### GOING ARTIFACTORY"
        def artifactory = new Artifactory(this.steps)
        artifactory.push(image_name, artifactory_repo + image_name)
    }
}