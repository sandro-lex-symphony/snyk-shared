package com.symphony.security.containers

// 1. get ignores from repo
// 2. get dockle bin
// 3. run dockle agains image
// 4. fail not fail
class Dockle {
    
    private def steps
    private Boolean initialized = false
    private String base_image_policy_url = 'https://raw.githubusercontent.com/sandro-lex-symphony/docker-images/master/debian-policy/dockle-base_image'
    private String production_image_policy_url = 'https://raw.githubusercontent.com/sandro-lex-symphony/docker-images/master/debian-policy/dockle-production_image'
    private String policy_file = '.dockleignore'
    private String dockle_bin_url = "https://github.com/sandro-lex-symphony/checkpackages/releases/download/v0.1/dockle"
    private String conf_base_image = "-i DKL-DI-0003 -i CIS-DI-0001  -i CIS-DI-0006 -i CIS-DI-0005"

    Dockle(steps) {
        this.steps = steps
    }

    def init() {
        if (!initialized) {
            steps.sh (script: "#!/bin/sh -e\n mkdir -p policy && wget -q -O policy/dockle_base_image ${base_image_policy_url} && wget -q -O policy/dockle_production_image ${production_image_policy_url}", returnStdout: true)
            steps.sh (script: "#!/bin/sh -e\n wget -q -O dockle ${dockle_bin_url}; chmod +x dockle", returnStdout: true)    
        }
        initialized = true
    }

    def run(image) {
        init()
        def out = steps.sh (script: "#!/bin/sh -e\n cp policy/dockle_production_image .dockleignore && ./dockle --exit-code 0 ${image} | egrep -v IGNORE", returnStdout: true)
        steps.echo out
    }  
    
    def base_image(image) {
        init()
        def out = steps.sh (script: "#!/bin/sh -e\n cp policy/dockle_base_image .dockleignore && ./dockle --exit-code 0 ${image} | egrep -v IGNORE", returnStdout: true)
        steps.echo out
    }
}