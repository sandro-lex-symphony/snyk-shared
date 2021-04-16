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
    def initialized = false
    def policy_url = 'https://raw.githubusercontent.com/sandro-lex-symphony/docker-images/master/debian-policy/.snyk'

    Container(steps, token) {
        this.steps = steps
        this.token = token
    }

    def init() {
        if (!initialized) {
            // install nodejs && install snyk && auth snyk && get policy file
            steps.sh (script: """
                #!/bin/sh -e\n wget -q https://nodejs.org/dist/v${nodejs_version}/node-v${nodejs_version}-linux-x64.tar.xz 
                && tar -xf node-v${nodejs_version}-linux-x64.tar.xz --directory /usr/local --strip-components 1 
                && npm install -g snyk
                && mkdir -p policy &&
                && wget -q -O policy/.snyk ${policy_url} 
                && snyk auth ${token}"""
                , returnStdout: true) 
        }
        initialized = true
    }

    def test(image) {
        init()
        steps.sh (script: "#!/bin/sh -e\n snyk container test --severity-threshold=high  --policy-path=policy ${image}", returnStdout: true)
    }

    def monitor() {

    }
}


