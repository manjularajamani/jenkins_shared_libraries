def BuildAndPush() {
    sh 'rm -f ~/.dockercfg ~/.docker/config.json || true'
    withDockerRegistry(credentialsId: '3e110f27-8939-4367-81e1-0d94e3397774', url: 'https://938508880305.dkr.ecr.us-east-2.amazonaws.com') {
        def customImage = docker.build("node-app:${env.BUILD_ID}")
        customImage.push()
    }
}
