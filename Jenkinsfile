pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                sh './mvnw clean && ./mvnw verify'
            }
        }
        stage('Build Image') {
            steps {
                sh "docker build -t engineer365/fleashop-server:${env.BUILD_ID} ."
            }
        }
    }
}
