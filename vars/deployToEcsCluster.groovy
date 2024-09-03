def deployToECS(String clusterName, String serviceName, String taskDefinitionArn, String region) {
    def result = sh(script: "aws ecs update-service --cluster ${clusterName} --service ${serviceName} --task-definition ${taskDefinitionArn} --region ${region}", returnStdout: true).trim()
    echo "ECS Service Updated: ${result}"
}
