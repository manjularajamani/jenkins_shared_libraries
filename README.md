# Pipeline for Cloning a Repository, Building, Tagging, and Pushing an Image to AWS ECR and Updating the ECS

## Prerequisite

### The following steps are carried out in the Jenkins dashboard:


- Install the **AWS Credential**, **Docker pipeline**, **Pipeline: AWS Steps** and **AWS ECR** plugin 
- To access your AWS account, you need to create a user within the account and use that user's credentials: **ACCESS_KEY** and **SECRET_KEY**
- Use **Credential ID** on pipeline
- To access other shared libraries, the Jenkinsfile needs to use the **@Library** annotation
   -  In the Jenkins dashboard get into the system--> Global Trusted Pipeline Libraries--> click add
   - Enter the details
     ![jenkins](https://github.com/user-attachments/assets/d3732423-20b6-4c22-af14-739c235298d4)
   - Modern SCM (optional)
   - Enter save


### The following steps are carried out in the AWS account:

- First, create the ECR repository
- Create the ECS cluster

## Scripted Pipeline

```
@Library("shared-lib@master") _

node('slave') {
    def region = 'us-east-1'
    def containerName = 'test-dev'
    def imageName = 'sit-ui-dev'
    def registryUrl = 'https://533980823513.dkr.ecr.us-east-1.amazonaws.com'
    def credentialId = 'ecr:us-east-1:aws-test'
    def clusterName = 'test-dev'
    def serviceName = 'test-service-dev'
    def ecsTaskFamily = 'test-task-definition-dev'

    stage('Clone Repo') {
        checkoutCode.CloneRepo("https://github.com/thejungwon/docker-reactjs.git", "master")
        echo "Working directory: ${env.WORKSPACE}"
    }

    stage('Build and Push Docker Image') {
        dockerBuildAndPush.BuildAndPush(registryUrl, credentialId)
    }

    stage('Deploy app into ECS') {
        def newImage = "${registryUrl}/${imageName}:${env.BUILD_ID}"
        
        // Call the shared library function to deploy to ECS
        deployToECSCluster(region, clusterName, serviceName, ecsTaskFamily, imageName)
    }
}

```
