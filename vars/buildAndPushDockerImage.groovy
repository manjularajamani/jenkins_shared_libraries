def call(String repository, int hubUser, String password, String tagName){
    
    sh """
    //  docker login --username="${hubUser}" --password="${password}"
     docker build -t ${hubUser}/${repository} . 
     docker image tag ${hubUser}/${repository} ${hubUser}/${repository}:${tagName}
     docker push ${hubUser}/${repository}:${imageTag}
    """
}