#!groovy

def determineHostname(appname, spacename) {
  return "${appname}-${spacename}"
}

def determineUrl(hostname, domain=CF_DOMAIN, protocol='https') {
  return "${protocol}://${hostname}.${domain}"
}

def deployApplicationWithAppDeployer(appname, apppath, spacename, cf_domain, manifestFile = 'manifest.yml') {
  hostname = determineHostname(appname, spacename)
  sh """
      app_deployer deploy-blue-green ${appname} --manifest=${manifestFile} --host=${hostname} --app-path=${apppath} --domain=${cf_domain}
  """
}

def withCfCredentials(Closure closure) {
  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'CF_CREDENTIAL', passwordVariable: 'CF_PASSWORD', usernameVariable: 'CF_USERNAME']]) {
    closure();
  }
}

def withCloudFoundryEnv(spacename, environmentFile, Closure closure) {
  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'CF_CREDENTIAL', passwordVariable: 'CF_PASSWORD', usernameVariable: 'CF_USERNAME']]) {
    withEnv(["CF_REST_API=false", "CF_HOME=${pwd()}", "CF_DOMAIN=cfapps.sap.hana.ondemand.com", "CF_API_ENDPOINT=https://api.cf.sap.hana.ondemand.com", "CF_SPACE=${spacename}", "CF_ORGANIZATION=cloudrefapp", "CF_ENVIRONMENT_CONFIGURATION=${environmentFile}"]) {
      closure()
    }
  }
}

def withinExistingCfSpace(spacename, environmentFile, Closure closure) {
  withCloudFoundryEnv(spacename, environmentFile) {
    sh "cf login -u \${CF_USERNAME} -p \${CF_PASSWORD} -o \${CF_ORGANIZATION} -s \${CF_SPACE} -a \${CF_API_ENDPOINT}"
    try {
      closure();
    } finally {
      sh "cf logout"
    }
  }
}

def blueGreenDeployViaShell(org, space, version, cf_domain, mainhost="bulletinboard-users-${space}") {
  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'CF_CREDENTIAL', passwordVariable: 'CF_PASSWORD', usernameVariable: 'CF_USERNAME']]) {
    withEnv([ "CF_HOME=${pwd()}" ]) {
      sh "./deploy.sh -o ${org} -s ${space} -u \$CF_USERNAME -p \$CF_PASSWORD -d ${cf_domain} -a ${CF_API_ENDPOINT} -n ${mainhost}"
    }
  }
}

def deleteSpace() {
  // Hack for working with the dependency between uaa-bulletinboard-cc-sap and uaa-bulletinboard
  sh "cf login -u \${CF_USERNAME} -p \${CF_PASSWORD} -o \${CF_ORGANIZATION} -s \${CF_SPACE} -a \${CF_API_ENDPOINT}"
  try { sh "cf unbind-service approuter uaa-bulletinboard-cc-sap" } catch(e) { /* intentional */ }
  try { sh "cf delete-service-key -f uaa-bulletinboard-cc-sap testServiceKey" } catch(e) { /* intentional */ }  
  try { sh "cf unbind-service bulletinboard-ads-green uaa-bulletinboard-cc-sap" } catch(e) { /* intentional */ }
  try { sh "cf unbind-service bulletinboard-ads-blue uaa-bulletinboard-cc-sap" } catch(e) { /* intentional */ }
  try { sh "cf delete-service -f uaa-bulletinboard-cc-sap" } catch(e) { /* intentional */ }
  try { sh "cf delete-service -f uaa-bulletinboard" } catch(e) { /* intentional */ }
  sh "cf delete-space -f ${CF_SPACE}"
  sh "cf logout"
}


def withinTemporaryCfSpace(spacename, environmentFile, spaceManagerDir, Closure closure) {
  withCloudFoundryEnv(spacename, environmentFile) {
    try {
      deleteSpace()
    } catch(e) {
      //try to clean up space, no error if not exists
    }
    dir(spaceManagerDir) {
      sh """
        space_manager setup-environment
        cf login -u \${CF_USERNAME} -p \${CF_PASSWORD} -o \${CF_ORGANIZATION} -s \${CF_SPACE} -a \${CF_API_ENDPOINT}
      """
    }
    closure();
    deleteSpace()
  }
}

return this
