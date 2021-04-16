package com.symphony.security.containers

// 1. get blacklist from repo
// 2. get checkpackages bin
// 3. test image
// 4. fail not fail
class CheckPackages {
    
    private def steps
    private Boolean initialized = false
    private String policy_url = 'https://raw.githubusercontent.com/sandro-lex-symphony/docker-images/master/packages/blacklist.txt'
    private String policy_file = 'policy/blacklist.txt'
    private String checkpackages_bin_url = "https://github.com/sandro-lex-symphony/checkpackages/releases/download/v0.1/checkpackages"

    CheckPackages(steps) {
        this.steps = steps
    }

    def init() {
        if (!initialized) {
            // get policy file and get checkpackage binary file
            steps.sh (script: "#!/bin/sh -e\n mkdir -p policy && wget -q -O ${policy_file} ${policy_url} && wget -q -O checkpackages ${checkpackages_bin_url}; chmod +x checkpackages", returnStdout: true)    
        }
        initialized = true
    }

    def run(image) {
        init()
        steps.sh (script: "#!/bin/sh -e\n ./checkpackages ${image} ${policy_file}", returnStdout: true)
    }  
}