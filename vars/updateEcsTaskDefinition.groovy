def call(String clusterName, String serviceName, String containerName, String newImage, String region) {
    // Define the task definition JSON
    def ecsTaskDef = [
        "containerDefinitions": [
            [
                "name": containerName,
                "image": newImage,
                "cpu": 0,
                "portMappings": [
                    [
                        "containerPort": 80,
                        "hostPort": 80,
                        "protocol": "tcp"
                    ]
                ],
                "essential": true,
                "logConfiguration": [
                    "logDriver": "awslogs",
                    "options": [
                        "awslogs-group": "/ecs/test-log-group",
                        "awslogs-region": region,
                        "awslogs-stream-prefix": "ecs"
                    ]
                ]
            ]
        ],
        "family": "test-task-definition-dev",
        "taskRoleArn": "arn:aws:iam::533980823513:role/test-task-role",
        "executionRoleArn": "arn:aws:iam::533980823513:role/test-execution-role",
        "networkMode": "awsvpc",
        "cpu": "512",
        "memory": "2048",
        "requiresCompatibilities": ["FARGATE"]
    ]

    def ecsJson = new groovy.json.JsonBuilder(ecsTaskDef).toPrettyString()
    
    // Register the new task definition
    def registerResult = sh(script: "aws ecs register-task-definition --cli-input-json '${ecsJson}' --region ${region}", returnStdout: true).trim()
    echo "Task Definition Registered: ${registerResult}"
    
    def jsonResult = new groovy.json.JsonSlurper().parseText(registerResult)
    def newTaskDefinitionArn = jsonResult.taskDefinition.taskDefinitionArn

    // Update the ECS service with the new task definition
    def deployResult = sh(script: "aws ecs update-service --cluster ${clusterName} --service ${serviceName} --task-definition ${newTaskDefinitionArn} --region ${region}", returnStdout: true).trim()
    echo "ECS Service Updated: ${deployResult}"

    return newTaskDefinitionArn
}
