pipeline {
    agent any
    environment {
        //Use Pipeline Utility Steps plugin to read information from pom.xml into env variables
        IMAGE = readMavenPom().getArtifactId()
        VERSION = readMavenPom().getVersion()
    }
    stages {
        stage('Build') {
            steps {                
                sh './mvnw -B -DskipTests clean package'
            }
        }
        stage('Unit Test') {
            steps {
                sh './mvnw test'
            }
        }
        stage('Integration Test') {
            steps {
                sh './mvnw -DskipTests clean verify -DskipITs=false'
            }
        }
        // stage('Build Image') {
        //    steps {
                // sh "docker build -t engineer365/fleashop-server:${env.BUILD_ID} ."
        //    }
        //}
    }
}
