package com.symphony.snyk.shared

// 1. snyk auth token
// 2. snyk container test --severity x --policy-path y image:tag
// 3. snyk container monitor --policy-path abc
class Container {
    def steps
    def nodejs_version = '14.16.1'
    Container(steps) {
        this.steps = steps
    }

    def init() {
        steps.sh 'wget https://nodejs.org/dist/v10.21.0/node-v10.21.0-linux-x64.tar.xz && tar -xf node-v10.21.0-linux-x64.tar.xz --directory /usr/local --strip-components 1' 
        steps.sh 'npm install -g snyk'
    }

    def hello(param) {
        steps.echo 'scan this ' + param
        steps.sh 'pwd'
    }

    def test(image) {
        init()
        steps.withCredentials([string(credentialsId: 'SNYK_API_TOKEN', variable: 'SNYK_TOKEN')]) {
            steps.sh 'snyk auth '+SNYK_TOKEN  
        }
        steps.sh "snyk container test ${image} || true"
    }
}


