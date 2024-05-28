def BuildAndPush() {
    sh 'rm -f ~/.dockercfg ~/.docker/config.json || true'
    docker.withRegistry('https://938508880305.dkr.ecr.us-east-2.amazonaws.com', 'ecr:us-east-2:b15750c6-a77b-4ba8-8202-abc9bbafe24b') {
        def customImage = docker.build("node-app:${env.BUILD_ID}")
        customImage.push()
    }
}

