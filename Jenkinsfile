pipeline {
    agent any
    environment {
        // Use pipeline-utility-steps plugin to read information from pom.xml into env variables
        // See 
        //  - https://github.com/jenkinsci/pipeline-utility-steps-plugin/blob/master/docs/STEPS.md
        //  - https://www.jenkins.io/doc/pipeline/steps/pipeline-utility-steps/
        // For waht readMavenPom() returns, see:
        //  - http://maven.apache.org/components/ref/3.3.9/maven-model/apidocs/org/apache/maven/model/Model.html
        ARTIFACTOR_ID = readMavenPom().getArtifactId() // fleashop-server
        VERSION = readMavenPom().getVersion() // 0.0.1-SNAPSHOT
        GROUP_ID = getGroupId() // org.engineer365
    }
    stages {
        stage('Build') {
            steps {
                echo "ARTIFACTOR_ID: ${ARTIFACTOR_ID}" 
                echo "VERSION: ${VERSION}" 
                echo "GROUP_ID: ${GROUP_ID}" 
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
