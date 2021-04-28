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
    def testPassed = false
    def policy_url = 'https://raw.githubusercontent.com/sandro-lex-symphony/docker-images/master/policies/snyk/debian-snyk'

    Container(steps, token) {
        this.steps = steps
        this.token = token
    }

    def init() {
        if (!initialized) {
            // install nodejs && install snyk && auth snyk && get policy file
            steps.sh (script: "#!/bin/sh -e\n wget -q ${nodejs_url} && tar -xf node-v${nodejs_version}-linux-x64.tar.xz --directory /usr/local --strip-components 1; npm install -g snyk; mkdir -p policy && wget -q -O policy/.snyk ${policy_url}; snyk auth ${token}; snyk config set disableSuggestions=true", returnStdout: true)
        }
        initialized = true
        }

    def test(image, dockerfile='') {
        init()
        def file_args = ''
        if (!dockerfile.isEmpty()) {
            file_args = " --file=${dockerfile}"
        }
        // steps.sh (script: "#!/bin/sh -e\n snyk container test --severity-threshold=high  --policy-path=policy ${image}", returnStdout: true)
        steps.sh (script: "snyk container test ${file_args} --severity-threshold=high  --policy-path=policy ${image}", returnStdout: true)
        testPassed = true
    }

    def monitor(image) {
        steps.sh(script: "snyk container monitor --policy-path=policy ${image}", returnStdout: true)
    }
}


