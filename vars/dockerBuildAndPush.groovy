def BuildAndPush() {
    sh 'rm -f ~/.dockercfg ~/.docker/config.json || true'
    docker.withRegistry('https://938508880305.dkr.ecr.us-east-2.amazonaws.com', 'ecr:us-east-2:19f5744c-02cd-4180-bb91-4e9e1e268bfd') {
        def customImage = docker.build("node-app:${env.BUILD_ID}")
        customImage.push()
    }
}

