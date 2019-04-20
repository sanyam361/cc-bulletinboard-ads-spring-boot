#!groovy

def automaticVersioningNpm() {
  new_version = getNpmVersion() + "-" + getTimestamp() + "+" + getCommitId()
  echo "New version: ${new_version}"
  setNpmVersion(new_version)
  dir('ui') {
    setNpmVersion(new_version)
  }

  storeVersionInGit("package.json */package.json", version)

  return new_version
}

def executeShell(script) {
  return sh (
    script: script,
    returnStdout: true
  ).trim()
}

def automaticVersioningMvn() {
    def baseVersion = executeShell 'mvn -q -Dexec.executable=\'echo\' -Dexec.args=\'${project.version}\' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec'
    def timestamp = executeShell 'date +"%Y%m%d-%H%M%S"'
    def gitRevision = executeShell 'git rev-parse --short HEAD'
    def version = "${baseVersion}-${timestamp}_${gitRevision}"

    sh "mvn -B versions:set -DnewVersion=${version}"

    storeVersionInGit("pom.xml", version)
    return version
}

def storeVersionInGit(fileString, version) {
    sh """
     git add ${fileString}
     git commit -m "update version"
     git tag "BUILD_${version}"
     git push origin "BUILD_${version}"
    """
    return version
}

def setNpmVersion(version) {
    def packageJson = parseJson(readFile(file: 'package.json'))
    packageJson.version = version
    def json = jsonToString(packageJson)
    packageJson = null
    writeFile file: 'package.json', text: json
}

return this;
