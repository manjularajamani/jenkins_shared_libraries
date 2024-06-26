### Pipeline for Cloning a Repository, Building, Tagging, and Pushing an Image to AWS ECR

The following steps are carried out in the Jenkins dashboard:

**Prerequisite**

- Install the `AWS Credential` and `AWS ECR` plugin 
- To access your AWS account, you need to create credentials using your `ACCESS_KEY` and `SECRET_KEY`
- Use Credential ID on pipeline

**Scripted Pipeline**

```
@Library("shared-lib@master") _

node('vagrant-slave') {

    stage('Clone Repo') {
        checkoutCode.CloneRepo("https://github.com/thejungwon/docker-reactjs.git", "master")
        echo "Working directory: ${env.WORKSPACE}"
    }

    stage('Build and Push Docker Image') {
        def registryUrl = 'https://<aws-account-id>.dkr.ecr.us-east-2.amazonaws.com'
        def credentialId = 'ecr:us-east-2:<credentials-id>'


        dockerBuildAndPush.BuildAndPush(registryUrl, credentialId)
    }
}

```