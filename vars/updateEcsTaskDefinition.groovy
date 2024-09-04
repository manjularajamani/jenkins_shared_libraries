def deployToECS(String family, String containerName, String newImage, String region, String logGroup, String executionRoleArn, String taskRoleArn, String cpu, String memory, String cluster, String service, int port) {

    sh """
        # Prepare the JSON for the task definition
        json='{
            "containerDefinitions": [
                {
                    "name": "${containerName}",
                    "image": "${newImage}",
                    "cpu": 0,
                    "portMappings": [
                        {
                            "containerPort": ${port},
                            "hostPort": ${port},
                            "protocol": "tcp"
                        }
                    ],
                    "essential": true,
                    "logConfiguration": {
                        "logDriver": "awslogs",
                        "options": {
                            "awslogs-group": "${logGroup}",
                            "awslogs-region": "${region}",
                            "awslogs-create-group": "true",
                            "awslogs-stream-prefix": "ecs"
                        }
                    }
                }
            ],
            "family": "test-task-definition-dev",
            "taskRoleArn": "${taskRoleArn}",
            "executionRoleArn": "${executionRoleArn}",
            "networkMode": "awsvpc",
            "volumes": [],
            "cpu": "${cpu}",
            "memory": "${memory}",
            "placementConstraints": [],
            "requiresCompatibilities": ["FARGATE"]
        }'

        echo \$json > td.json

        # Register the new task definition
        aws ecs register-task-definition --region ${region} --cli-input-json file://td.json
    
        # Get the revision number
        REVISION=\$(aws ecs describe-task-definition --task-definition ${family} --region ${region} --query taskDefinition.revision --output text)
        
        # Update the service with the new task definition
        aws ecs update-service --cluster ${cluster} --service ${service} --region ${region} --task-definition ${family}:\$REVISION
    """
}
