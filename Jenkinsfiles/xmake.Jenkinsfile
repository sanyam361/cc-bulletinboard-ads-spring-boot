#!groovy

/*
This function triggers an xmake stage build job on xmake server
Parameters:
 xmakeServer:    Display name of Remote Host. Defined in Jenkins global config, section for 'Parameterized Remote Trigger Plugin'
 jobName:        Full job name of stage/build job on xmake server. CI-, PR-, MS- and Release Jobs gets specified here !
 repoFullname:   GitHub organization + repository name
 commitId:       CommitId of automated versioning
*/
def xmakeStageBuild(xmakeServer, jobName, repoFullName, commitId) {
    echo 'Entering xmakeStageBuildNew'

    //echo '1'

    /*
    def stageBuildHandle = triggerRemoteJob (
        remoteJenkinsName: xmakeServer,
        job: jobName,
        parameters: 'MODE=stage\nTREEISH=' + commitId,
        blockBuildUntilComplete: true,
        enhancedLogging: false,
        pollInterval: 10,
        preventRemoteBuildQueue: false,
        shouldNotFailBuild: false,
        token: ''
    ) //triggerRemoteJob
    */

    def stageBuildHandle = triggerRemoteJob (
        remoteJenkinsName: xmakeServer,
        job: jobName,
        parameters: 'MODE=stage\nTREEISH=' + commitId,
    ) //triggerRemoteJob
    echo 'Remote Status: ' + stageBuildHandle.getBuildStatus().toString()

    /*
    echo '2'
    sh 'ls -la'
    //sh 'env'
    */

    //--- New -----------------------
    echo 'Get Staging Return Parameters'

    //def stageResults = parameterizedRemoteTriggerEnv.readJsonFileFromBuildArchive('build-results.json')
    def stageResults = stageBuildHandle.readJsonFileFromBuildArchive('build-results.json')

    stageId = stageResults.staging_repo_id
    stageUrl = stageResults.stage_repourl
    echo 'stageId: ' + stageId
    echo 'stageUrl: ' + stageUrl

    def projectArchiveUrl = stageResults.projectArchive
    echo 'projectArchiveUrl: ' + projectArchiveUrl

    sh """#!/bin/bash -ev
        echo Downloading build result
        curl -fsSL -o projectArchive.tar.gz ${projectArchiveUrl}
        tar -zxf "projectArchive.tar.gz"
    """

    sh 'ls -la'
    echo '4'

    return [stagingRepoId: stageId, stagingRepoUrl: stageUrl]
}


def xmakePromote(xmakeServer, jobName, stagingRepoId) {
    echo 'Entering xmakePromoteNew'

/*
   def promoteBuildHandle = triggerRemoteJob (
            remoteJenkinsName: xmakeServer,
            job: jobName,
            parameters: 'MODE=promote\nTREEISH=' + commitId + '\nSTAGING_REPO_ID=' + stagingRepoId,
            blockBuildUntilComplete: true,
            enhancedLogging: false,
            pollInterval: 10,
            preventRemoteBuildQueue: false,
            shouldNotFailBuild: false,
            token: ''
        )
*/

   def promoteBuildHandle = triggerRemoteJob (
            remoteJenkinsName: xmakeServer,
            job: jobName,
            parameters: 'MODE=promote\nTREEISH=' + commitId + '\nSTAGING_REPO_ID=' + stagingRepoId,
        )

        echo 'Get Promote Results'
        //def promoteResults = parameterizedRemoteTriggerEnv.readJsonFileFromBuildArchive('build-results.json')
        def promoteResults = promoteBuildHandle.readJsonFileFromBuildArchive('build-results.json')
        releaseMetadataUrl = promoteResults['release.metadata.url']
        echo 'releaseMetadataUrl: ' + releaseMetadataUrl

    return releaseMetadataUrl;
}

/*
 * This function builds the URL to the projects war file in the new release repository (after the xmake PROMOTE)
 */
def getWarUrlFromMetadataUrl(releaseMetadataUrl, releaseVersion, applicationName) {
    return releaseMetadataUrl.replace(
        applicationName + '-' + releaseVersion + '-releaseMetadata.zip',
        applicationName + '-' + releaseVersion + '.war'
        )
}


return this;


//repositories/xmakedeploymilestonesprofile-31209/com/sap/cc/bulletinboard-ads/0.0.1-20170310-140307_5cab40f
