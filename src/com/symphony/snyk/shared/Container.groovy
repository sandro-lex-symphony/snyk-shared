package com.symphony.snyk.shared

// 1. snyk auth token
// 2. snyk container test --severity x --policy-path y image:tag
// 3. snyk container monitor --policy-path abc
class Container {
    def steps
    def nodeVersion = '14.16.1'

    Container(steps) {
        this.steps = steps
        // instal nodejs LTS
        this.steps.sh 'sh wget https://nodejs.org/dist/v14.16.1/node-v14.16.1-linux-x64.tar.xz && tar -xf node-v14.16.1-linux-x64.tar.xz --directory /usr/local --strip-components 1'
        // install snyk
        this.steps.sh 'npm install -g snyk'
    }

    def hello(param) {
        steps.echo 'scan this ' + param
        steps.sh 'pwd'
    }

    def test(image) {
        steps.withCredentials([string(credentialsId: 'SNYK_API_TOKEN', variable: 'SNYK_TOKEN')]) {
            steps.sh 'snyk auth '+SNYK_TOKEN  
        }
        steps.sh "snyk container test ${image} || true"
    }
}


