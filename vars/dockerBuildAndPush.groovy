// def BuildAndPush(String registryUrl, String credentialId) {
//     sh 'rm -f ~/.dockercfg ~/.docker/config.json || true'
//     docker.withRegistry(registryUrl, credentialId) {
//         def customImage = docker.build("sit-ui-dev:${env.BUILD_ID}")
//         customImage.push()
//     }
// }



// In dockerBuildAndPush.groovy (shared library)
def call(String registryUrl, String credentialId) {
    sh 'rm -f ~/.dockercfg ~/.docker/config.json || true'
    docker.withRegistry(registryUrl, credentialId) {
        def customImage = docker.build("sit-ui-dev:${env.BUILD_ID}")
        customImage.push()
    }
}
