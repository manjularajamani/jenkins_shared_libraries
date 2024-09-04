def call(String awsCredentialsId, String awsRegion, String ecsClusterName, String ecsServiceName, String ecsTaskFamily, String dockerImageTag) {
    withAWS(credentials: awsCredentialsId, region: awsRegion) {
        script {
            // Step 1: Fetch the current task definition
            def taskDefinition = sh(
                script: "aws ecs describe-task-definition --task-definition ${ecsTaskFamily} --region ${awsRegion}",
                returnStdout: true
            ).trim()

            def taskDefJson = new groovy.json.JsonSlurperClassic().parseText(taskDefinition).taskDefinition
            def containerDef = taskDefJson.containerDefinitions[0]

            // Step 2: Update the container image
            containerDef.image = "${dockerImageTag}"

            // Step 3: Register a new revision of the task definition
            def registerTaskDefResult = sh(
                script: """
                    aws ecs register-task-definition \
                    --family ${ecsTaskFamily} \
                    --container-definitions '${groovy.json.JsonOutput.toJson([containerDef])}' \
                    --region ${awsRegion}
                """,
                returnStdout: true
            ).trim()

            def newTaskDefArn = new groovy.json.JsonSlurperClassic().parseText(registerTaskDefResult).taskDefinition.taskDefinitionArn
            echo "New Task Definition ARN: ${newTaskDefArn}"

            // Step 4: Update the ECS service with the new task definition
            sh """
                aws ecs update-service \
                --cluster ${ecsClusterName} \
                --service ${ecsServiceName} \
                --task-definition ${newTaskDefArn} \
                --region ${awsRegion} \
                --force-new-deployment
            """
        }
    }
}
