#!/usr/bin/env groovy
@Library(['EnvMan-lib','bulletinboard-JSL']) _
import com.sap.icd.jenkins.Utils
import com.sap.cc.jenkins.FileUtils

Utils utils = new Utils()

try {
    stage('Central Build') {
        lock(resource: "${env.JOB_NAME}/10", inversePrecedence: true) {
            milestone 10
            node {
                deleteDir()
                checkout scm

                setupPipelineEnvironment script: this, storeGithubStatistics: true, gitBranch: utils.SCMBranchName

                measureDuration(script: this, measurementName: 'build_duration') {
                    def version = setVersion script: this, buildTool: 'maven'
                    echo "version set to: $version"
                    stashFiles(script: this) {
                        executeBuild script: this, buildType: 'xMakeStage'
                    }
                    publishCheckResults archive: true, tasks: true, pmd: true, cpd: true, findbugs: true, checkstyle: true, aggregation: [thresholds: [fail: [high: 0]]]
                    publishTestResults junit: [updateResults: true, archive: true], jacoco: [archive: true]
                }
            }
        }
    }

    stage('add. Unit Tests') {
        lock(resource: "${env.JOB_NAME}/20", inversePrecedence: true) {
            milestone 20
            parallel(
                    'OPA5': {
                        node {
                            if (globalPipelineEnvironment.getConfigProperty('runOpaTests').toBoolean()) {
                                deleteDir()
                                measureDuration(script: this, measurementName: 'opa_duration') {
                                    executeOnePageAcceptanceTests script: this, buildTool: 'npm'
                                    publishTestResults junit: [pattern: '**/target/karma/**/TEST-*.xml']
                                }
                            }
                        }
                    }, 'PerformanceUnit': {
                node {
                    if (globalPipelineEnvironment.getConfigProperty('runPerformanceUnitTests').toBoolean()) {
                        deleteDir()
                        measureDuration(script: this, measurementName: 'perfunit_duration') {
                            executePerformanceUnitTests script: this
                            publishTestResults contiperf: [archive: true]
                        }
                    }
                }
            }, failFast: false
            )
        }
    }
    
    stage('Integration') {
        lock(resource: "${env.JOB_NAME}/30", inversePrecedence: true) {
            milestone 30
            node {
                //custom integration tests
                if (!globalPipelineEnvironment.getConfigProperty('extensionIntegrationTestScript').isEmpty()) {
                    deleteDir()
                    executeCustomIntegrationTests script: this
                }
            }
        }
    }

    stage('Security (VULAS only)') {
        lock(resource: "${env.JOB_NAME}/40", inversePrecedence: true) {
            milestone 40
            parallel(
                    'Fortify': {
//                            node {
//                                deleteDir()
//                                measureDuration(script: this, measurementName: 'fortify_duration') {
//                                    executeFortifyScan script: this
//                                }
//                            }
                   },
                    'Checkmarx': {
//                            node {
//                                deleteDir()
//                                measureDuration(script: this, measurementName: 'checkmarx_duration') {
//                                    executeCheckmarxScan script: this
//                                }
//                            }
                    },
                    'OpenSourceVulnerability': {
                        node {
                            deleteDir()
                            try {
                            	measureDuration(script: this, measurementName: 'opensourcevulnerability_duration') {
                                	executeOpenSourceDependencyScan script: this, vulas: true
                            	}
                            }
                            catch(err) {
                                echo "$err"
                            }

                        }
                    }, failFast: false
            )
        }
    }

    stage('Performance') {
        lock(resource: "${env.JOB_NAME}/50", inversePrecedence: true) {
            milestone 50
            node {
                deleteDir()
                measureDuration(script: this, measurementName: 'deploy_perf_duration') {
                    downloadArtifactsFromNexus script: this, artifactType: 'java', fromStaging: true
                    def cfOrg = globalPipelineEnvironment.getConfigProperty('cfOrg')
                    def perfManifest = globalPipelineEnvironment.getConfigProperty('cfManifest') 
                    def perfSpace = globalPipelineEnvironment.getConfigProperty('cfPerfSpace')
					deployWithEnvMan(script:this,manifest:perfManifest,space:perfSpace,org:cfOrg,furtherReplaceInFile:['$userRoute':usersUrl(perfSpace,cfOrg)])
                    executeHealthCheck testUrl: globalPipelineEnvironment.getConfigProperty('healthCheckUrlPerformance')
                }
                if (globalPipelineEnvironment.getConfigProperty('runPerformanceJMeterTests').toBoolean()) {
                    deleteDir()
                    measureDuration(script: this, measurementName: 'jmeter_duration') {
                        executePerformanceJMeterTests script: this
                        publishTestResults jmeter: [archive: true], allowUnstableBuilds: false
                    }
                }
                if (globalPipelineEnvironment.getConfigProperty('runPerformanceSUT').toBoolean()) {

                    deleteDir()
                    measureDuration(script: this, measurementName: 'supa_duration') {
                        executePerformanceSingleUserTests script: this
                        publishTestResults supa: [archive: true]
                    }
                }
            }
        }
    }

    stage('IPScan and PPMS') {
        lock(resource: "${env.JOB_NAME}/60", inversePrecedence: true) {
            milestone 60
            parallel(
                    'whitesource': {
//                            node {
//                                deleteDir()
//                                measureDuration(script: this, measurementName: 'whitesource_duration') {
//                                    executeWhitesourceScan script: this, scanType: 'maven'
//                                }
//                            }
                    },'PPMS': {
                node {
                    if (!globalPipelineEnvironment.getConfigProperty('ppmsCredentialsId').isEmpty()) {
                        deleteDir()
                        measureDuration(script: this, measurementName: 'ppmscheck_duration') {
                            executePPMSComplianceCheck script: this
                        }
                    }
                }
            }, failFast: false
            )
        }
    }
    
    stage('Acceptance') {
        lock(resource: "${env.JOB_NAME}/70", inversePrecedence: true) {
            milestone 70
            node {
                deleteDir()
                measureDuration(script: this, measurementName: 'deploy_test_duration') {
                    downloadArtifactsFromNexus script: this, artifactType: 'java', fromStaging: true
                    def cfOrg = globalPipelineEnvironment.getConfigProperty('cfOrg')
                    def acceManifest = globalPipelineEnvironment.getConfigProperty('cfManifest') 
                    def acceSpace = globalPipelineEnvironment.getConfigProperty('cfAccSpace')
                    deployWithEnvMan(script:this,manifest:acceManifest,space:acceSpace,org:cfOrg, deployType: 'blue-green',cfAppName:"bulletinboard-ads--$acceSpace--$cfOrg",furtherReplaceInFile:['$userRoute':usersUrl(acceSpace,cfOrg)])

                    executeHealthCheck testUrl: globalPipelineEnvironment.getConfigProperty('healthCheckUrlAcceptance')
                }
                if (globalPipelineEnvironment.getConfigProperty('runFunctionalAcceptanceTests').toBoolean()) {
                    measureDuration(script: this, measurementName: 'gauge_duration') {
                        executeGaugeTests script: this
                        publishTestResults gauge: [archive: true], allowUnstableBuilds: false
                    }
                }
            }
        }
    }

    stage('Promote') {
        //input message: 'Shall we proceed to promotion & release?'
        lock(resource: "${env.JOB_NAME}/80", inversePrecedence: true) {
            milestone 80
            node {
                deleteDir()
                measureDuration(script: this, measurementName: 'promote_duration') {
                    executeBuild script: this, buildType: 'xMakePromote'
                }
            }
        }
    }

    stage('Release') {
        lock(resource: "${env.JOB_NAME}/90", inversePrecedence: true) {
            milestone 90
            node {
                measureDuration(script: this, measurementName: 'release_duration') {
                    deleteDir()
                    downloadArtifactsFromNexus script: this, artifactType: 'java'
                    
					def prodOrg = globalPipelineEnvironment.getConfigProperty('cfProdOrg').toLowerCase()
                    def prodManifest = globalPipelineEnvironment.getConfigProperty('cfManifest') 
                    def prodSpace = globalPipelineEnvironment.getConfigProperty('cfProdSpace').toLowerCase()
					def prodApiEndpoint = globalPipelineEnvironment.getConfigProperty('cfProdApiEndpoint')
					String modifiedManifest="modifiedManifest.yml"
					utils.unstash 'deployDescriptor'
					new FileUtils(this).replaceInFile(prodManifest,modifiedManifest,['$space':prodSpace,'$org':prodOrg,'$userRoute':usersUrl(prodSpace,prodOrg)]) 
                    manageCloudFoundryEnvironment(script: this,command:"setup-environment -y modifiedManifest.yml -s $prodSpace -o $prodOrg -a $prodApiEndpoint")
                    
                    deployToCloudFoundry script: this, deployTool: 'cf_native', deployType: 'blue-green', cfApiEndpoint: prodApiEndpoint, cfOrg: prodOrg, cfSpace: prodSpace, cfManifest: 'modifiedManifest.yml', cfAppName:"bulletinboard-ads--$prodSpace--$prodOrg"
                    executeHealthCheck testUrl: globalPipelineEnvironment.getConfigProperty('healthCheckUrlProduction')
                    currentBuild.result = 'SUCCESS'
                }
            }
        }
    }
} catch (Throwable err) { // catch all exceptions
    globalPipelineEnvironment.addError(this, err)
    throw err
} finally {
    node{
        writeInflux script: this , influxServer:""
        sendNotificationMail script: this
    }
}


String usersUrl(String cfSpace, String org) {
	return 'https://bulletinboard-users--$cfSpace--$org.cfapps.sap.hana.ondemand.com'.replace('$cfSpace',cfSpace).replace('$org',org)                                          
}
