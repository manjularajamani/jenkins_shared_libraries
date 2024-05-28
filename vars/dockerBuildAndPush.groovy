def BuildAndPush() {
    sh 'rm -f ~/.dockercfg ~/.docker/config.json || true'
    withDockerRegistry(credentialsId: 'aws-credentials', url: 'https://938508880305.dkr.ecr.us-east-2.amazonaws.com') {
        def customImage = docker.build("node-app:${env.BUILD_ID}")
        customImage.push()
    }
}
