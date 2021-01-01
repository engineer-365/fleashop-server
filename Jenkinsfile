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

        ARTIFACTOR_ID = pom.getArtifactId()
        VERSION = pom.getVersion()
        GROUP_ID = pom.getGroupId()
    }
    options {
        skipStagesAfterUnstable()
        timestamps()
    }
    stages {
        stage('Build') {
            steps {
                sh './mvnw -B -DskipTests clean package'
            }
        }
        stage('Test') {
            steps {
                sh './mvnw verify'
            }
        }
        stage('Quality') {
            steps {
            // See：
                //   - https://www.jenkins.io/doc/pipeline/steps/jacoco/
                //   - https://stackoverflow.com/questions/24077392/how-to-get-jenkins-to-exclude-entire-folders-from-code-coverage/48685604#48685604
                //   - https://testerhome.com/topics/10091 (Chinese)
                //   - https://www.cnblogs.com/yanxinjiang/p/10968297.html (Chinese)
                jacoco(
                    execPattern: 'target/**/*.exec '
                )
                //withSonarQubeEnv('SonarQube') {
                //    sh "${SONAR_SCANNER_HOME}/bin/sonar-scanner \
                //    -Dsonar.projectKey=springboot-demo \
                //    -Dsonar.projectName=springboot-demo \
                //    -Dsonar.sources=.\
                //    -Dsonar.host.url=SonarQube地址 \
                //    -Dsonar.language=java \
                //    -Dsonar.sourceEncoding=UTF-8"
            // }
            }
        }
        // stage('Build Image') {
        //    when {
        //        branch 'main'
        //    }
        //    steps {
                // sh "docker build -t engineer365/fleashop-server:${env.BUILD_ID} ."
        //    }
        //}
    }
    post {
        always {
            junit (
                testResults: "**/target/surefire-reports/**/*.xml",
                allowEmptyResults: true,
                keepLongStdio: true
            )

            // "warnings-ng" plugin
            // https://github.com/jenkinsci/warnings-ng-plugin/blob/master/doc/Documentation.md#pipeline-configuration
            // https://github.com/jenkinsci/warnings-ng-plugin/blob/master/plugin/src/main/java/io/jenkins/plugins/analysis/core/steps/IssuesRecorder.java
            recordIssues enabledForFailure: true, tools: [mavenConsole(), java(), javaDoc()]
            recordIssues enabledForFailure: true, tool: checkStyle()
            recordIssues enabledForFailure: true, tool: spotBugs()
            recordIssues enabledForFailure: true, tool: cpd(pattern: '**/target/cpd.xml')
            recordIssues enabledForFailure: true, tool: pmdParser(pattern: '**/target/pmd.xml')

            // "git-forensics" plugin
            mineRepository
        }
    }
}
