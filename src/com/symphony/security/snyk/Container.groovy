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
    def policy_repo = 'https://github.com/sandro-lex-symphony/docker-images'
    Container(steps, token) {
        this.steps = steps
        this.token = token
    }

    def init() {
        if (!initialized) {
            // install nodejs
            steps.sh "wget https://nodejs.org/dist/v${nodejs_version}/node-v${nodejs_version}-linux-x64.tar.xz && tar -xf node-v${nodejs_version}-linux-x64.tar.xz --directory /usr/local --strip-components 1" 
            // install snyk
            steps.sh 'npm install -g snyk'
            // sny auth
            steps.sh "snyk auth ${token}"
        }
        initialized = true
    }

    def hello(param) {
        steps.echo 'scan this ' + param
        steps.sh 'pwd'
    }

    def test(image) {
        init()
        
        steps.sh 'mkdir -p policy && wget -O policy/.snyk https://raw.githubusercontent.com/sandro-lex-symphony/docker-images/master/debian-policy/.snyk'
        steps.sh 'ls -al'
        steps.sh "snyk container test --severity-threshold=high  --policy-path=policy ${image}"
    }
}


