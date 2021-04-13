package com.symphony.security.containers

// 1. get blacklist from repo
// 2. get image type
// 3. get list of installed packages
// 4. check against policy
// 5. fail not fail
class CheckPackages {
    private String policy_repo = 'https://github.com/sandro-lex-symphony/docker-images'
    private String policy_file = 'policy/blacklist.txt'
    private def steps
    private Boolean initialized = false
    private String[] blacklist
    private String[] pkgList

    CheckPackages(steps) {
        this.steps = steps
    }

    def run(image) {
        init()
        getPackageList(image)
        return compare()
    }

    def init() {
        if (!initialized) {
            steps.sh "mkdir -p policy && wget -O ${policy_file} https://raw.githubusercontent.com/sandro-lex-symphony/docker-images/master/packages/blacklist.txt"
            def tmp_file
            if (steps.fileExists(policy_file)) {
                tmp_file = steps.readFile policy_file
            }
            blacklist = tmp_file.split("\n");
        }
        initialized = true
    }

    def getPackageList(image) {
        def flavor = getImageType(image)
        if (flavor == 'debian' || flavor == 'ubuntu') {
            steps.sh "docker run --rm -i --entrypoint='' ${image} dpkg -l > package-list.txt"
        } else if (flavor == 'centos') {
            steps.sh "docker run --rm -i --entrypoint='' ${image} rpm -qa > package-list.txt"
        } else if (flavor == 'alpine') {
            steps.sh "docker run --rm -i --entrypoint='' ${image} apk info > package-list.txt"
        }
        def tmp_file
        if (steps.fileExists('package-list.txt')) {
            tmp_file = steps.readFile 'package-list.txt'
        }
        pkgList = tmp_file.split("\n");
    }

    def compare() {
        def bad_list
        for (String blacklisted_package: blacklist) {
            for (String installed_pkg: pkgList) {
                if (installed_pkg.contains(blacklisted_package)) {
                    bad_list += ' ' + blacklisted_package
                }
            }
        }
        if (!bad_list.isEmpty()) {
            steps.echo "Found non authorized packages: ${bad_list}"
            return false
        }
        return true
    }

    def getImageType(image) {
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
    }

    def t() {
        steps.withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'sym-aws-dev', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
            steps.sh "set +x; echo 'Logging into docker repo'; `aws --region us-east-1 ecr get-login --no-include-email`"
            steps.sh 'docker pull 189141687483.dkr.ecr.us-east-1.amazonaws.com/slex-reg-test/debian:buster-slim'
            steps.sh 'docker run 189141687483.dkr.ecr.us-east-1.amazonaws.com/slex-reg-test/debian:buster-slim cat /etc/os-release'
         }
    }

}