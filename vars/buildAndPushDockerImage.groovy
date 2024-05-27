def call(String aws_account_id, String region, String ecr_repoName, String tagName){
    
    sh """

     aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${aws_account_id}.dkr.ecr.${region}.amazonaws.com  
     docker build -t ${ecr_repoName} .
     docker tag ${ecr_repoName}:latest ${aws_account_id}.dkr.ecr.${region}.amazonaws.com/${ecr_repoName}:${tagName}
     docker push ${aws_account_id}.dkr.ecr.${region}.amazonaws.com/${ecr_repoName}:${tagName}
    """
}