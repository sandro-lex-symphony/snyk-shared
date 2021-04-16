package com.symphony.security.containers

 @Library('SnykShared@master')                                                                                                                                                                    
import com.symphony.security.containers.CheckPackages
import com.symphony.security.containers.Dockle
import com.symphony.security.snyk.Container

// 1. snyk auth token
// 2. snyk container test --severity x --policy-path y image:tag
// 3. snyk container monitor --policy-path abc
// TODO: add --file=Dockerfile
// TODO: add --exclude base image
class Control {
    def steps
    def snyk
    def dockle
    def checkpackages
    

    Control(steps) {
        this.steps = steps
    }

    def init() {
        steps.withCredentials([steps.string(credentialsId: 'SNYK_API_TOKEN', variable: 'SNYK_TOKEN')]) {
            steps.echo 'XXXX ' + steps.env.SNYK_TOKEN
            // snyk = new Container(steps, ${env.SNYK_TOKEN})
        }
         checkpackages = new CheckPackages(steps)
         dockle = new Dockle(steps)
    }

    def run(image) {
        init()
        checkpackages.run(image)
        dockle.run(image)
        // snyk.test(image)
    }
}


