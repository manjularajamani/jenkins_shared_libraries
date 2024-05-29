// Clone the repository
def clonerepo(String repoUrl, String branch = 'master') {
    checkout([
        $class: 'GitSCM',
        branches: [[name: "*/${branch}"]],
        doGenerateSubmoduleConfigurations: false,
        extensions: [[$class: 'CleanCheckout']], // Optional: cleans the workspace before checking out
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

def deploy(cluster, service, task_family, image, region, boolean is_wait = true, String awscli = "aws") {
    sh """
        OLD_TASK_DEF=\$(${awscli} ecs describe-task-definition \
                                --task-definition ${task_family} \
                                --output json --region ${region})

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
                --family ${task_family} \
                --cli-input-json \
                "\$(echo \$FINAL_TASK)" --region "${region}"

        if [ \$? -eq 0 ]
        then
            echo "New task has been registered"
        else
            echo "Error in task registration"
            exit 1
        fi
        
        echo "Now deploying new version..."
                    
        ${awscli} ecs update-service \
            --cluster ${cluster} \
            --service ${service} \
            --force-new-deployment \
            --task-definition ${task_family} \
            --region "${region}"
        
        if ${is_wait}; then
            echo "Waiting for deployment to reflect changes"
            ${awscli} ecs wait services-stable \
                --cluster ${cluster} \
                --service ${service} \
                --region "${region}"
        fi
    """
}

def restart(cluster, service, region, String awscli = "aws") {
    sh """
        ${awscli} ecs update-service \
            --cluster ${cluster} \
            --service ${service} \
            --force-new-deployment \
            --region "${region}"
    """
}

def wait(cluster, service, region, String awscli = "aws") {
    sh """
        ${awscli} ecs wait services-stable \
            --cluster ${cluster} \
            --service ${service} \
            --region "${region}"
    """
}
