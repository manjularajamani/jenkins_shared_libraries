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
        def customImage = docker.build("testing:${env.BUILD_ID}")
        customImage.push()
    }
}

// Deploy ECS service with new task definition
def deploy(cluster, service, taskFamily, image, region, boolean isWait = true, String awscli = "aws") {
    sh """
        set -e  # Exit on any error

        # Ensure jq is installed, if not, install it
        if ! command -v jq &> /dev/null
        then
            echo "jq could not be found, installing it"
            sudo apt-get update && sudo apt-get install -y jq
        fi

        echo "Updating the image in the task definition"
        // OLD_TASK_DEF=\$(${awscli} ecs describe-task-definition --task-definition ${taskFamily} --region ${region})
        NEW_TASK_DEF=\$(echo "\${OLD_TASK_DEF}" | jq --arg NDI "${image}" '.taskDefinition.containerDefinitions[0].image=\$NDI')

        echo "Creating the final task definition JSON"
        FINAL_TASK=\$(echo "\${NEW_TASK_DEF}" | jq '.taskDefinition | {
                            family: .family,
                            networkMode: .networkMode,
                            volumes: .volumes,
                            containerDefinitions: .containerDefinitions,
                            placementConstraints: .placementConstraints
                        }')

        echo "Validating the final task JSON"
        echo "\${FINAL_TASK}" | jq . > /dev/null
        if [ \$? -ne 0 ]; then
            echo "Invalid JSON created for task definition"
            echo "FINAL_TASK: \${FINAL_TASK}"
            exit 1
        fi

        echo "Registering the new task definition"
        REGISTER_OUTPUT=\$(${awscli} ecs register-task-definition \
                --family ${taskFamily} \
                --cli-input-json "\${FINAL_TASK}" \
                --region "${region}")

        if [ \$? -ne 0 ]; then
            echo "Error registering new task definition"
            echo "REGISTER_OUTPUT: \${REGISTER_OUTPUT}"
            exit 1
        fi

        echo "New task has been registered successfully"
        echo "REGISTER_OUTPUT: \${REGISTER_OUTPUT}"

        echo "Updating the ECS service to use the new task definition"
        UPDATE_OUTPUT=\$(${awscli} ecs update-service \
            --cluster ${cluster} \
            --service ${service} \
            --force-new-deployment \
            --task-definition \${REGISTER_OUTPUT.taskDefinition.taskDefinitionArn} \
            --region "${region}")

        if [ \$? -ne 0]; then
            echo "Error updating the service"
            echo "UPDATE_OUTPUT: \${UPDATE_OUTPUT}"
            exit 1
        fi

        echo "Service update initiated successfully"
        echo "UPDATE_OUTPUT: \${UPDATE_OUTPUT}"

        if ${isWait}; then
            echo "Waiting for deployment to reflect changes"
            WAIT_OUTPUT=\$(${awscli} ecs wait services-stable \
                --cluster ${cluster} \
                --service ${service} \
                --region "${region}")

            if [ \$? -ne 0 ]; then
                echo "Error waiting for service to stabilize"
                echo "WAIT_OUTPUT: \${WAIT_OUTPUT}"
                exit 1
            fi

            echo "Service is stable"
            echo "WAIT_OUTPUT: \${WAIT_OUTPUT}"
        fi
    """
}
