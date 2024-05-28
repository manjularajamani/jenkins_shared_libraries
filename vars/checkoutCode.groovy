// def CloneRepo(String repoUrl, String branch){
//   def workingDir = "${env.WORKSPACE}"
//     sh "git clone ${repoUrl} ${workingDir}"
//     sh "git checkout ${branch}"
//     return workingDir
// }



// vars/checkoutCode.groovy
def CloneRepo(String repoUrl, String branch = 'master') {
    checkout([
        $class: 'GitSCM',
        branches: [[name: "*/${branch}"]],
        doGenerateSubmoduleConfigurations: false,
        extensions: [[$class: 'CleanCheckout']], // Optional: cleans the workspace before checking out
        submoduleCfg: [],
        userRemoteConfigs: [[url: repoUrl]]
    ])
}
