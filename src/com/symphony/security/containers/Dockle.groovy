package com.symphony.security.containers

// 1. get ignores from repo
// 2. get dockle bin
// 3. run dockle agains image
// 4. fail not fail
class Dockle {
    
    private def steps
    private Boolean initialized = false
    private String policy_url = 'https://raw.githubusercontent.com/sandro-lex-symphony/docker-images/master/packages/blacklist.txt'
    private String policy_file = 'policy/dockleignore'
    private String dockle_bin_url = "https://github.com/sandro-lex-symphony/checkpackages/releases/download/v0.1/dockle"
    private String conf_base_image = "-i DKL-DI-0003 -i CIS-DI-0001  -i CIS-DI-0006"

    Dockle(steps) {
        this.steps = steps
    }

    def init() {
        if (!initialized) {
            //steps.sh (script: "#!/bin/sh -e\n mkdir -p policy && wget -q -O ${policy_file} ${policy_url}", returnStdout: true)
            steps.sh (script: "#!/bin/sh -e\n wget -q -O dockle ${dockle_bin_url}; chmod +x dockle", returnStdout: true)    
        }
        initialized = true
    }

    def run(image, ignores) {
        init()
        def out = steps.sh (script: "#!/bin/sh -e\n ./dockle ${conf_base_image} --exit-code 0 ${image} | egrep -v IGNORE", returnStdout: true)
        steps.echo out
    }  
    
    def base_image(image) {
       run(image, conf_base_image) 
    }
}