pipeline {
    agent any
    environment {
        // Use pipeline-utility-steps plugin to read information from pom.xml into env variables
        // See 
        //  - https://github.com/jenkinsci/pipeline-utility-steps-plugin/blob/master/docs/STEPS.md
        //  - https://www.jenkins.io/doc/pipeline/steps/pipeline-utility-steps/
        // For waht readMavenPom() returns, see:
        //  - http://maven.apache.org/components/ref/3.3.9/maven-model/apidocs/org/apache/maven/model/Model.html
        pom = readMavenPom()
        ARTIFACTOR_ID = pom.getArtifactId() // fleashop-server
        VERSION = pom.getVersion() // 0.0.1-SNAPSHOT
        BUILD = pom.getBuild()
        GROUP_ID = pom.getGroupId() // org.engineer365
        ID = pom.getId() //
        ORG = pom.getOrganization()
    }
    stages {
        stage('Build') {
            steps {
                echo "ARTIFACTOR_ID: ${ARTIFACTOR_ID}" 
                echo "VERSION: ${VERSION}" 
                echo "BUILD: ${BUILD}" 
                echo "GROUP_ID: ${GROUP_ID}" 
                echo "ID: ${ID}" 
                echo "ORG: ${ORG}" 
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
