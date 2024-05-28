def BuildAndPush() {
    sh 'rm -f ~/.dockercfg ~/.docker/config.json || true'
    docker.withRegistry('https://938508880305.dkr.ecr.us-east-2.amazonaws.com', 'ecr:eu-west-1:b74f801f-990f-4cb5-8ae5-29117e677e25') {
        def customImage = docker.build("node-app:${env.BUILD_ID}")
        customImage.push()
    }
}

