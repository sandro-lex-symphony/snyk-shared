package com.symphony.snyk.shared

// 1. snyk auth token
// 2. snyk container test --severity x --policy-path y image:tag
// 3. snyk container monitor --policy-path abc
class Container {
    def steps
    def token
    def nodejs_version = '14.16.1'
    Container(steps, token) {
        this.steps = steps
        this.token = token
    }

    def init() {
        steps.sh 'wget https://nodejs.org/dist/v10.21.0/node-v10.21.0-linux-x64.tar.xz && tar -xf node-v10.21.0-linux-x64.tar.xz --directory /usr/local --strip-components 1' 
        steps.sh 'npm install -g snyk'
        steps.sh "snyk auth ${token}"
    }

    def hello(param) {
        steps.echo 'scan this ' + param
        steps.sh 'pwd'
    }

    def test(image) {
        init()
        steps.sh "snyk container test ${image} || true"
    }
}


