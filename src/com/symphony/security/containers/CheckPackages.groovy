package com.symphony.security.containers

// 1. get blacklist from repo
// 2. get image type
// 3. get list of installed packages
// 4. check against policy
// 5. fail not fail
class CheckPackages {
    def policy_repo = 'https://github.com/sandro-lex-symphony/docker-images'
    def steps
    def initialized = false
    CheckPackages(steps) {
        this.steps = steps
    }

    def init() {
        if (!initialized) {
            steps.sh 'mkdir -p policy && wget -O policy/blacklist.txt https://raw.githubusercontent.com/sandro-lex-symphony/docker-images/master/packages/blacklist.txt'
        }
    }

    def getImageType(image) {
        steps.sh "docker run --rm -i --entrypoint='' ${image} cat /etc/os-release > os-release.txt"
        steps.sh 'cat os-release.txt'
        def debian = steps.sh(script: "grep Debian os-release.txt", returnStatus: true)
        println debian.text
    }

}