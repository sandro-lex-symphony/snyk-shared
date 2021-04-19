package com.symphony.security.containers

 @Library('SnykShared@master')                                                                                                                                                                    
import com.symphony.security.containers.CheckPackages
import com.symphony.security.containers.Dockle
import com.symphony.security.snyk.Container

class Control {
    def steps
    def snyk
    def dockle
    def checkpackages
    

    Control(steps) {
        this.steps = steps
    }

    def init() {
        steps.withCredentials([steps.usernamePassword(credentialsId: 'SNYK_BASEIMAGE_TOKEN', usernameVariable: 'FILLER', passwordVariable: 'SNYK_TOKEN')]) {
            snyk = new Container(steps, steps.env.SNYK_TOKEN)
        }
         checkpackages = new CheckPackages(steps)
         dockle = new Dockle(steps)
    }

    def run(image) {
        init()
        steps.echo "###### Start security checks for container image: ${image}"
        steps.echo "## Running container checkpackages"
        checkpackages.run(image)
        steps.echo "## Running Dockerfile validation (dockle)"
        dockle.run(image)
        steps.echo "## Scanning for vulnerable packages (snyk)"
        snyk.test(image)
        steps.echo "###### End Security Check"
    }
}


