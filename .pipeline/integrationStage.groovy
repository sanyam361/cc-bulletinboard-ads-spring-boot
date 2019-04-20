import com.sap.icd.jenkins.Utils

def call(script) {
  echo "Integration Test"
  def cfSpace = script.globalPipelineEnvironment.getConfigProperty('cfIntegrationSpace')
  def cfManifest = script.globalPipelineEnvironment.getConfigProperty('cfManifest')
  def cfOrg = script.globalPipelineEnvironment.getConfigProperty('cfOrg')
  def cfCredentialsId = script.globalPipelineEnvironment.getConfigProperty('cfCredentialsId')
  def cfApiEndpoint = script.globalPipelineEnvironment.getConfigProperty('cfApiEndpoint')
  def securityEnvironment = script.globalPipelineEnvironment.getConfigProperty('securityEnvironment')
  def systemTestRepo = script.globalPipelineEnvironment.getConfigProperty('systemTestRepo')
  def systemTestProperties = script.globalPipelineEnvironment.getConfigProperty('systemTestProperties')
  
  def nexusUrl
  try {
      nexusUrl=script.nexusUrl
      echo 'Using beviously build artifact found at $nexusUrl'
  }catch(MissingPropertyException mpe) {
      echo 'using just build artifact'
  }
  if (nexusUrl) { 
      sh 'mkdir target'
      sh "curl --insecure --silent --show-error -w %{response_code} -o target/bulletinboard-ads-spring-boot.jar ${nexusUrl}"
  }
  else {
      downloadArtifactsFromNexus script: this, artifactType: 'java', fromStaging: true
  }
  new Utils().unstash 'deployDescriptor'
  deployWithEnvMan(script:script,manifest:cfManifest,space:cfSpace, org:cfOrg,api:cfApiEndpoint,furtherReplaceInFile:['$userRoute':script.usersUrl(cfSpace,cfOrg)])
  sh 'cat modifiedManifest.yml'
  executeHealthCheck testUrl: globalPipelineEnvironment.getConfigProperty('healthCheckUrlIntegration')
  
//  withCredentials([usernamePassword(
//      credentialsId: 'RoleCollectionAdminId',
//      passwordVariable: 'MH_PASSWORD',
//      usernameVariable: 'MH_USERNAME'
//      )]) {
      //manageCloudFoundryEnvironment(script: this,command:"setup-muenchhausen -y $securityEnvironment -s $cfSpace -o $cfOrg")
      
      git url:systemTestRepo
      
      writeSystemTestProperties(systemTestProperties, cfCredentialsId, cfApiEndpoint, cfOrg, cfSpace)
      sh("mvn clean verify -DpropertyFileName=$systemTestProperties")

      //manageCloudFoundryEnvironment(script: this,command:"delete-muenchhausen -y $securityEnvironment -s $cfSpace -o $cfOrg")
//  }
}


def writeSystemTestProperties(String systemTestProperties,String cfCredentialsId,String cfApiEndpoint,String cfOrg,String cfSpace) {
    def VCAP = getVCapServiceJson(cfCredentialsId,cfApiEndpoint,cfOrg,cfSpace,'approuter')
    
    Map xsuaaCreds = VCAP.system_env_json.VCAP_SERVICES.xsuaa[0].credentials
    
    String identityZone = xsuaaCreds.identityzone
    String clientId = xsuaaCreds.clientid
    String clientSecret = xsuaaCreds.clientsecret
    String urlSurfix=".cfapps.sap.hana.ondemand.com"
    String urlPrefix="https://"
    String approuterHostName="cc-refapp-approuter--ads-integration--ccrefapp"
    String approuterUrl=urlPrefix+approuterHostName+urlSurfix

    String configtemplate = readFile 'src/main/resources/config.template'
    
    configtemplate = configtemplate.replace('$userUrl', "${urlPrefix}bulletinboard-users--ads-integration--ccrefapp${urlSurfix}")
    configtemplate = configtemplate.replace('$approuterUrl', approuterUrl)
    configtemplate = configtemplate.replace('$identityZone',identityZone)
    configtemplate = configtemplate.replace('$clientId',clientId)
    configtemplate = configtemplate.replace('$clientSecret',clientSecret)
    
    writeFile file: "src/main/resources/${systemTestProperties}", text: configtemplate
    
    sh "cat src/main/resources/$systemTestProperties"
}

def getJsonFromShellComand(String comand) {
    def jsonResult=sh(returnStdout: true,script:comand).trim()
    //println "jsonResult: $jsonResult"
    return readJSON(text:jsonResult)
}

def getVCapServiceJson(String cfCredentialsId,String cfApiEndpoint,String cfOrg,String cfSpace,String approuterName) {
    def VCAP
    withCredentials([usernamePassword(
        credentialsId: cfCredentialsId,
        passwordVariable: 'cfPassword',
        usernameVariable: 'cfUser'
        )]) {
        sh "cf login -u ${cfUser} -p ${cfPassword} -a ${cfApiEndpoint} -o ${cfOrg} -s ${cfSpace}"
        String appGuid = sh(returnStdout: true,script:"cf app --guid $approuterName").trim()
                
        VCAP = getJsonFromShellComand("cf curl /v2/apps/${appGuid}/env")
    }
    return VCAP
}

return this