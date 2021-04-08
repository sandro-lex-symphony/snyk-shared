package com.symphony.snyk.shared

// 1. snyk auth token
// 2. snyk container test --severity x --policy-path y image:tag
// 3. snyk container monitor --policy-path abc
class ContainerScan {
    static def hello(param) {
        echo 'scan this ' + param
        sh 'pwd'
    }
}


