package com.symphony.security.containers

 @Library('SnykShared@master')                                                                                                                                                                    
import com.symphony.security.containers.SecurityControl
import com.symphony.security.containers.Artifactory



class Builder {
    def steps
    def buildkit = true
    def buildkit_str= "DOCKER_BUILDKIT=1"
    def contentTrust = true
    def content_trust_str = "DOCKER_CONTENT_TRUST=1"
    def flags = ''
    def cache_args = '--no-cache'
    def pull_args = '--pull'
    
    Builder(steps, contentTrustValue=true, buildkitValue=true) {
        this.steps = steps
        this.buildkit = buildkitValue
        this.contentTrust = contentTrustValue
    }

    // I don't understand why can't it be called from the constructor? 
    def init() {
        buildkit(this.buildkit)
        contentTrust(this.contentTrust)
    }

    def buildkit(v) {
        if (v == true) {
            buildkit = true
            buildkit_str = "DOCKER_BUILDKIT=1"
        } else {
            buildkit = false
            buildkit_str = "DOCKER_BUILDKIT=0"
        }
    }

    def contentTrust(v) {
        if (v == true) {
            contentTrust = true
            content_trust_str = "DOCKER_CONTENT_TRUST=1"
        } else {
            contentTrust  = false
            content_trust_str = "DOCKER_CONTENT_TRUST=0"
        }
    }

    def noCacheArg(v) {
        if (v == true) {
            cache_args = '--no-cache'
        } else {
            cache_args = ''
        }
    }

    def pullArg(v) {
        if (v == true) {
            pull_args = '--pull'
        } else {
            pull_args = ''
        }
    }

    def flags(v) {
        flags = v
    }

    // because of an incompatibility with buildkit and contained (docker < 20.x)
    // if the image is not on local cache, docker build fails
    // if we pull the image first, it works
    def pullRootImage(dockerfile) {
        // get the roor image name from dockerfile and docker pull
        def rootimage = steps.sh(script: "awk -F' ' '/^FROM/ { print \$2 }' ${dockerfile}", returnStdout: true)
        steps.sh(script: "${content_trust_str} docker pull ${rootimage}", returnStdout: true)
    }

    def dockerBuild(image_name, dockerfile, context_path) {
        init()
        steps.echo "### Building container image ${image_name}"
        if (buildkit == true) {
            pullArg(false)
            pullRootImage(dockerfile)
        }
        steps.sh (script: "${buildkit_str} ${content_trust_str} docker build ${cache_args} ${pull_args} ${flags} -f ${dockerfile} -t ${image_name} ${context_path}", returnStdout: true)
        steps.echo "### Done building ${image_name}"
    }

    def buildAndPublish(image_name, dockerfile, context_path, artifactory_repository='') {
        // docker build
        dockerBuild(image_name, dockerfile, context_path)

        // security checks
        steps.echo "### Running Security Checks for ${image_name}"
        def security = new SecurityControl(this.steps)
        security.base_image(image_name, dockerfile)
        steps.echo "### Done security checks for ${image_name}"

        // push to repo
        steps.echo "### Going to push ${image_name} to Artifactory ${artifactory_repository}"
        def artifactory = new Artifactory(this.steps)
        artifactory.push(image_name, artifactory_repository)
        steps.echo "### Done pushing ${image_name} to Artifactory ${artifactory_repository}"
    }
}