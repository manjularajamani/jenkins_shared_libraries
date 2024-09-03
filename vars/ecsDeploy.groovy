def deployToECS(cluster, service, registryUrl, imageName, region, String awsCli = 'aws') {
    sh """
        set -e

        echo "Fetching current task definition for service ${service}"
        CURRENT_TASK_DEF_ARN=\$(${awsCli} ecs describe-services --cluster ${cluster} --services ${service} --query "services[0].taskDefinition" --output text --region ${region})

        echo "Describing current task definition"
        CURRENT_TASK_DEF=\$(${awsCli} ecs describe-task-definition --task-definition \${CURRENT_TASK_DEF_ARN} --output json --region ${region})

        echo "Updating container image in task definition"
        NEW_TASK_DEF=\$(echo "\${CURRENT_TASK_DEF}" | jq --arg IMAGE "${registryUrl}/${imageName}:latest" '.taskDefinition.containerDefinitions[0].image=\$IMAGE')

        echo "Creating new task definition"
        FINAL_TASK_DEF=\$(echo "\${NEW_TASK_DEF}" | jq '.taskDefinition | { 
                                family: .family, 
                                networkMode: .networkMode, 
                                volumes: .volumes, 
                                containerDefinitions: .containerDefinitions, 
                                placementConstraints: .placementConstraints
                            }')

        echo "Registering new task definition"
        REGISTER_OUTPUT=\$(${awsCli} ecs register-task-definition \
            --cli-input-json "\${FINAL_TASK_DEF}" \
            --region ${region} \
            --output json)

        NEW_TASK_DEF_ARN=\$(echo "\${REGISTER_OUTPUT}" | jq -r '.taskDefinition.taskDefinitionArn')

        echo "Updating ECS service to use the new task definition"
        ${awsCli} ecs update-service --cluster ${cluster} --service ${service} --task-definition \${NEW_TASK_DEF_ARN} --region ${region} --force-new-deployment

        echo "Waiting for the service to stabilize"
        ${awsCli} ecs wait services-stable --cluster ${cluster} --services ${service} --region ${region}

        echo "Service is now stable"
    """
}
