package com.symphony.security.snyk

// 1. snyk auth token
// 2. snyk container test --severity x --policy-path y image:tag
// 3. snyk container monitor --policy-path abc
// TODO: add --file=Dockerfile
// TODO: add --exclude base image
class Container {
    def steps
    def token
    def nodejs_version = '14.16.1'
    def nodejs_url = "https://nodejs.org/dist/v${nodejs_version}/node-v${nodejs_version}-linux-x64.tar.xz"
    def initialized = false
    def policy_url = 'https://raw.githubusercontent.com/sandro-lex-symphony/docker-images/master/debian-policy/.snyk'

    Container(steps, token) {
        this.steps = steps
        this.token = token
    }

    def init() {
        if (!initialized) {
            // install nodejs && install snyk && auth snyk && get policy file
            steps.sh (script: "#!/bin/sh -e\n wget -q ${nodejs_url} && tar -xf node-v${nodejs_version}-linux-x64.tar.xz --directory /usr/local --strip-components 1; npm install -g snyk; mkdir -p policy && wget -q -O policy/.snyk ${policy_url}; snyk auth ${token}", returnStdout: true)
        }
        initialized = true
        }

    def test(image) {
        init()
        // steps.sh (script: "#!/bin/sh -e\n snyk container test --severity-threshold=high  --policy-path=policy ${image}", returnStdout: true)
        steps.sh (script: "snyk container test --severity-threshold=high  --policy-path=policy ${image}", returnStdout: true)
    }

    def test2(image) {
        steps.sh "mkdir -p policy && wget -q -O policy/.snyk ${policy_url};"
        steps.sh "docker pull snyk/snyk-cli:docker"
        steps.sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v `pwd`:/projects -e SNYK_TOKEN=${token} -e MONITOR=false snyk/snyk-cli:docker test --docker ${image}"
    }

    def monitor() {

    }
}


