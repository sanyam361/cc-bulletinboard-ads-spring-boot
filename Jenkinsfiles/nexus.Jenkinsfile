#!groovy

NEXUS_MILESTONE_REPOSITORY = 'https://nexus.wdf.sap.corp:8443/nexus/content/groups/build.milestones'
NEXUS_RELEASE_REPOSITORY = 'https://nexus.wdf.sap.corp:8443/nexus/content/groups/build.releases'

def downloadArtifact(Map overrideParams = [:]) {
    Map defaultMap = [groupId: 'com.sap.cc', artifactId: null, version: 'LATEST', packaging: 'war', destination: null, remoteRepositories: 'https://nexus.wdf.sap.corp:8443/nexus/content/groups/build.milestone']
    params = defaultMap << overrideParams

    if (params.artifactId == null) {
        throw new Exception("artifactId needs to be given")
    }
    if (params.destination == null) {
        throw new Exception("destination needs to be given")
    }

    def completeGav = "${params.groupId}:${params.artifactId}:${params.version}:${params.packaging}"

    sh """
        mvn org.apache.maven.plugins:maven-dependency-plugin:2.10:get -U -Dartifact=${completeGav} -DremoteRepositories=${params.remoteRepositories} -Ddest=${params.destination} -Dtransitive=false
    """
}

def downloadArtifactWithCurl(Map overrideParams = [:]) {
    Map defaultMap = [groupId: 'com.sap.cc', artifactId: null, version: 'LATEST', packaging: 'war', destination: null, remoteRepository: NEXUS_MILESTONE_REPOSITORY]
    params = defaultMap << overrideParams

    if (params.artifactId == null) {
        throw new Exception("artifactId needs to be given")
    }
    if (params.destination == null) {
        throw new Exception("destination needs to be given")
    }

    def artifactFilename = "${params.artifactId}-${params.version}"
    def groupIdDirectory = params.groupId.replaceAll("\\.", '/')

    sh """
        curl -fsSL -o ${params.destination} ${params.remoteRepository}/${groupIdDirectory}/${params.artifactId}/${params.version}/${artifactFilename}.${params.packaging}
    """
}

return this
