// Clone the repository
def clonerepo(String repoUrl, String branch = 'master') {
    checkout([
        $class: 'GitSCM',
        branches: [[name: "*/${branch}"]],
        doGenerateSubmoduleConfigurations: false,
        extensions: [[$class: 'CleanCheckout']], // Cleans the workspace before checking out
        submoduleCfg: [],
        userRemoteConfigs: [[url: repoUrl]]
    ])
}

// Build and Push image on AWS ECR
def buildandpush(String registryUrl, String credentialId) {
    sh 'rm -f ~/.dockercfg ~/.docker/config.json || true'
    docker.withRegistry(registryUrl, credentialId) {
        def customImage = docker.build("sit-ui-dev:${env.BUILD_ID}")
        customImage.push()
    }
}

// Deploy ECS service with new task definition
def deploy(cluster, service, taskFamily, image, region, boolean isWait = true, String awscli = "aws") {
    sh """
        

        NEW_TASK_DEF=\$(echo \$OLD_TASK_DEF | \
                    jq --arg NDI ${image} '.taskDefinition.containerDefinitions[0].image=\$NDI')

        FINAL_TASK=\$(echo \$NEW_TASK_DEF | \
                    jq '.taskDefinition | \
                            {family: .family, \
                            networkMode: .networkMode, \
                            volumes: .volumes, \
                            containerDefinitions: .containerDefinitions, \
                            placementConstraints: .placementConstraints}')

        ${awscli} ecs register-task-definition \
                --family ${taskFamily} \
                --cli-input-json "\$(echo \$FINAL_TASK)" \
                --region "${region}"

        if [ \$? -ne 0 ]; then
            echo "Error in task registration"
            exit 1
        else
            echo "New task has been registered"
        fi
        
        echo "Now deploying new version..."
                    
        ${awscli} ecs update-service \
            --cluster ${cluster} \
            --service ${service} \
            --force-new-deployment \
            --task-definition ${taskFamily} \
            --region "${region}"
        
        if ${isWait}; then
            echo "Waiting for deployment to reflect changes"
            ${awscli} ecs wait services-stable \
                --cluster ${cluster} \
                --service ${service} \
                --region "${region}"
        fi
    """
}

// Restart ECS service
def restart(cluster, service, region, String awscli = "aws") {
    sh """
        ${awscli} ecs update-service \
            --cluster ${cluster} \
            --service ${service} \
            --force-new-deployment \
            --region "${region}"
    """
}

// Wait for ECS service to stabilize
def wait(cluster, service, region, String awscli = "aws") {
    sh """
        ${awscli} ecs wait services-stable \
            --cluster ${cluster} \
            --service ${service} \
            --region "${region}"
    """
}
