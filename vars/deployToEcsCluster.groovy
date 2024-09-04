def deployToECS(String containerName, String newImage, String region, String cluster, String service) {
    // Read the existing task definition file
    def taskDefinitionFile = new File('../td.json')
    
    // Update the task definition JSON with new values
    def updatedJson = taskDefinitionFile.text.replaceAll(/("image":\s*")[^"]*"/, "\$1${newImage}\"")

    // Write updated JSON to a new file
    def updatedTaskDefinitionFile = new File('td-updated.json')
    updatedTaskDefinitionFile.write(updatedJson)

    // Register the new task definition
    sh """
    aws ecs register-task-definition --region ${region} --cli-input-json file://td-updated.json
    """

    // Get the revision number of the new task definition
    def revision = sh(script: "aws ecs describe-task-definition --task-definition ${containerName} --region ${region} --query taskDefinition.revision --output text", returnStdout: true).trim()

    // Update the ECS service with the new task definition
    sh """
    aws ecs update-service --cluster ${cluster} --service ${service} --region ${region} --task-definition ${containerName}:\${revision}
    """
}
