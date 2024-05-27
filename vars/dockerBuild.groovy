def call() {
        sh """
            docker login --username="${username}" --password="${password}"
        """
    }
}
