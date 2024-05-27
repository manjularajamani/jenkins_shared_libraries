def call(String project, String imageTag, String hubUser, String password){
    
    sh """
     docker login --username="${hubUser}" --password="${password}"
     docker image build -t ${hubUser}/${project} . 
     docker image tag ${hubUser}/${project} ${hubUser}/${project}:${imageTag}
     docker image tag ${hubUser}/${project} ${hubUser}/${project}:latest
    """
}