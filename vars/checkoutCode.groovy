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
