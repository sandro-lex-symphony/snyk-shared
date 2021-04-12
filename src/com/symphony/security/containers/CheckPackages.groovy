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
    def blacklist
    CheckPackages(steps) {
        this.steps = steps
    }

    def init() {
        if (!initialized) {
            steps.sh 'mkdir -p policy && wget -O policy/blacklist.txt https://raw.githubusercontent.com/sandro-lex-symphony/docker-images/master/packages/blacklist.txt'
            List<String> tmplist(String filePath) {
            File file = new File('policy/blacklist.txt')
            blacklist = file.readLines()
        }
        initialized = true
    }

    def getPackageList(image) {
        def flavor == getImageType(image)
        if (flavor == 'debian' || flavor == 'ubuntu') {
            steps.sh "docker run --rm -i --entrypoint='' ${image} dpkg -l > package-list.txt"
        } else if (flavor == 'centos') {
            steps.sh "docker run --rm -i --entrypoint='' ${image} rpm -qa > package-list.txt"
        } else if (flavor == 'alpine') {
            steps.sh "docker run --rm -i --entrypoint='' ${image} apk info > package-list.txt"
        }
    }

    def getImageType(image) {
        init()
        def ret
        steps.sh "docker run --rm -i --entrypoint='' ${image} cat /etc/os-release > os-release.txt"
        ret = steps.sh(script: "grep Debian os-release.txt", returnStatus: true)
        if (ret == 0) {
            return 'debian'
        }
        ret = steps.sh(script: "grep Ubuntu os-release.txt", returnStatus: true)
        if (ret == 0) {
            return 'ubuntu'
        }
        ret = steps.sh(script: "grep CentOS os-release.txt", returnStatus: true)
        if (ret == 0) {
            return 'centos'
        }
        ret = steps.sh(script: "grep Alpine os-release.txt", returnStatus: true)
        if (ret == 0) {
            return 'alpine'
        }

        for (String item : blacklist) {
            steps.echo item
        }
  
    }

}