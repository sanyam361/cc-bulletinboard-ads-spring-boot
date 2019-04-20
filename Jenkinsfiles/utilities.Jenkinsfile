import groovy.json.JsonSlurper
import groovy.json.JsonBuilder



@NonCPS
def parseJson (text) {
    return new JsonSlurper().parseText(text)
}

@NonCPS
def jsonToString (content) {
    return new JsonBuilder(content).toPrettyString()
}

def executeShell(script) {
    return sh (
            script: script,
            returnStdout: true
    ).trim()
}

def reportJunitResultsSureFire() {
    echo 'Reporting **/target/surefire-reports/TEST-*.xml'

    //step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
    junit '**/target/surefire-reports/TEST-*.xml'
}

def reportJunitResultsFailSafe() {
    echo 'Reporting **/target/failsafe-reports/TEST-*.xml'

    //step([$class: 'JUnitResultArchiver', testResults: '**/target/failsafe-reports/TEST-*.xml'])
    junit '**/target/failsafee-reports/TEST-*.xml'
}

def getCommitId(){
    sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
}

def getTimestamp() {
    sh(returnStdout: true, script: 'date +"%Y%m%d%H%M%S"').trim()
}

def unzipFile(zipFile) {
    sh "unzip -j '${zipFile}' -d ."
}

def cleanNode(Closure closure) {
    node {
        deleteDir()
        closure()
    }
}

return this