def call(String repository, String hubUser, String password, String imageTag){
    
    sh """
     docker login --username="${hubUser}" --password="${password}"
     docker image build -t ${hubUser}/${prorepositoryject} . 
     docker image tag ${hubUser}/${repository} ${hubUser}/${repository}:${imageTag}
     docker push ${hubUser}/${repository}:${imageTag}
    """
}