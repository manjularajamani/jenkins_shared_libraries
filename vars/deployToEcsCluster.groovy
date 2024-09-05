def deployToECS(String awsRegion, String ecsClusterName, String ecsServiceName, String ecsTaskFamily) {
    withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'aws-test', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
        script {
            sh """
            # Fetch the current task definition for the family
            aws ecs describe-task-definition --task-definition ${ecsTaskFamily} --region ${awsRegion}

            # Register a new task definition with the updated image
            registerTaskDefResult=\$(aws ecs register-task-definition \
                --family ${ecsTaskFamily} \
                --network-mode awsvpc \
                --requires-compatibilities FARGATE \
                --cpu '256' \
                --memory '512' \
                --container-definitions "[{\\"name\\":\\"sleep\\",\\"image\\":\\"myrepo/sleep:latest\\",\\"cpu\\":10,\\"memory\\":10,\\"essential\\":true,\\"command\\":[\\"sleep\\",\\"360\\"]}]" \
                --region ${awsRegion})

            # Parse the ARN of the new task definition
            newTaskDefArn=\$(echo \$registerTaskDefResult | python3 -c "import sys, json; print(json.load(sys.stdin)['taskDefinition']['taskDefinitionArn'])")

            echo "New Task Definition ARN: \${newTaskDefArn}"

            # Update the ECS service with the new task definition and force a new deployment
            aws ecs update-service \
                --cluster ${ecsClusterName} \
                --service ${ecsServiceName} \
                --task-definition \${newTaskDefArn} \
                --region ${awsRegion} \
                --force-new-deployment
            """
        }
    }
}
