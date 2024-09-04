// def deployToECS(String containerName, String newImage, String region, String cluster, String service) {
//     // Read the existing task definition file
//     def taskDefinitionFilePath = '../td.json'
    
//     // Update the task definition JSON with new values
//     def updatedJson = taskDefinitionFile.text.replaceAll(/("image":\s*")[^"]*"/, "\$1${newImage}\"")

//     // Write updated JSON to a new file
//     def updatedTaskDefinitionFile = new File('td-updated.json')
//     updatedTaskDefinitionFile.write(updatedJson)

//     // Register the new task definition
//     sh """
//     aws ecs register-task-definition --region ${region} --cli-input-json file://td-updated.json
//     """

//     // Get the revision number of the new task definition
//     def revision = sh(script: "aws ecs describe-task-definition --task-definition ${containerName} --region ${region} --query taskDefinition.revision --output text", returnStdout: true).trim()

//     // Update the ECS service with the new task definition
//     sh """
//     aws ecs update-service --cluster ${cluster} --service ${service} --region ${region} --task-definition ${containerName}:\${revision}
//     """
// }


def deployToECS(String containerName, String newImage, String region, String cluster, String service) {
    // Define the path to the existing task definition file
    def taskDefinitionFilePath = '../td.json'

    // Print the current working directory for debugging
    sh 'pwd'
    
    // Check if the file exists and print debug information
    sh "ls -l ${taskDefinitionFilePath}"
    
    // Read the existing task definition file
    def taskDefinitionFile = new File(taskDefinitionFilePath)
    if (!taskDefinitionFile.exists()) {
        error "Task definition file not found at path: ${taskDefinitionFilePath}"
    }
    
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
