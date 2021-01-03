// See
//  - https://www.jenkins.io/doc/book/pipeline/
//  - https://builder.engineer365.org:40443/pipeline-syntax/

pipeline {
    agent any
    environment {
        // "pipeline-utility-steps": read information from pom.xml into env variables
        // See 
        //  - https://github.com/jenkinsci/pipeline-utility-steps-plugin/blob/master/docs/STEPS.md
        //  - https://www.jenkins.io/doc/pipeline/steps/pipeline-utility-steps/
        // For waht readMavenPom() returns, see:
        //  - http://maven.apache.org/components/ref/3.3.9/maven-model/apidocs/org/apache/maven/model/Model.html
        pom = readMavenPom()

        PRJ_ID = pom.getArtifactId()
        PRJ_VER = pom.getVersion()
        ORG_ID = pom.getGroupId()

        //COMMIT_ID = sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()
        COMMIT_ID = GIT_COMMIT.take(7)
    
        DOCKER_REG = 'docker.engineer365.org:40444'
        DOCKER_REG_CRED = 'docker-engineer365-builder'
        DOCKER_PRJ = "${ORG_ID}/${PRJ_ID}"
        DOCKER_PRJ_FQ = "${DOCKER_REG}/${DOCKER_PRJ}"
        DOCKER_IMG_VER = "${PRJ_VER}-${COMMIT_ID}-${env.BUILD_ID}"

        K8S_GIT = "github.com/${ORG_ID}/${PRJ_ID}-k8s.git"
    }
    options {
        skipStagesAfterUnstable()
        timestamps()
    }
    stages {
        stage('Clean & Compile') {
            steps {
                sh './mvnw clean'
                sh './mvnw compile'
            }
        }
        stage('Unit test & Integration test') {
            steps {
                sh './mvnw verify'
                archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true 
            }
        }
        stage('Quality check') {
            steps {
                // "jacoco" plugin
                // See：
                //   - https://www.jenkins.io/doc/pipeline/steps/jacoco/
                //   - https://stackoverflow.com/questions/24077392/how-to-get-jenkins-to-exclude-entire-folders-from-code-coverage/48685604#48685604
                //   - https://testerhome.com/topics/10091 (Chinese)
                //   - https://www.cnblogs.com/yanxinjiang/p/10968297.html (Chinese)
                jacoco(
                    execPattern: 'target/**/*.exec '
                )

                /*withSonarQubeEnv('SonarQube') {
                    sh "${SONAR_SCANNER_HOME}/bin/sonar-scanner \
                    -Dsonar.projectKey=springboot-demo \
                    -Dsonar.projectName=springboot-demo \
                    -Dsonar.sources=.\
                    -Dsonar.host.url=SonarQube-host \
                    -Dsonar.language=java \
                    -Dsonar.sourceEncoding=UTF-8"
                }

                // "javadoc" plugin
                javadoc javadocDir: "", keepAll: "true"
                */
            }
        }
        stage('Build docker image') {
        //    when {
        //        branch 'main'
        //    }
            steps {
                script {
                    docker.withRegistry("https://" + DOCKER_REG, DOCKER_REG_CRED) {
                        sh "docker build -t ${DOCKER_PRJ_FQ}:latest -f Dockerfile ."
                        sh "docker tag ${DOCKER_PRJ_FQ}:latest ${DOCKER_PRJ_FQ}:${DOCKER_IMG_VER}"
                        sh "docker push ${DOCKER_PRJ_FQ}:${DOCKER_IMG_VER}"
                        sh "docker push ${DOCKER_PRJ_FQ}:latest"
                    }
                }
            }
        }
        stage('Update k8s deployment for test env') {
            steps {
                dir('k8s') {
                    git branch: 'main', credentialsId: 'github-engineer365-builder', url: K8S_GIT

                    dir('overlays/test') {
                        sh 'yq eval \'.images[].newTag="' + DOCKER_IMG_VER + '"\' kustomization.yaml -i'
                    }

                    withCredentials([usernameColonPassword(credentialsId: 'github-engineer365-builder', variable: 'USERPASS')]) {
                        sh 'git config user.email "engineer365-builder@mail.engineer365.org"'
                        sh 'git config user.name "Engineer365 Builder"'
                        sh "git commit -a -m 'Updating test env image version to ${DOCKER_IMG_VER}'"
                        sh "git push https://${USERPASS}@${K8S_GIT}"
                    }
                }
            }
        }
    }
    post {
        always {
            // "chucknorris" plugin
            chuckNorris()

            // "junit" plugin
            junit (
                testResults: "**/target/surefire-reports/**/*.xml",
                allowEmptyResults: true,
                keepLongStdio: true
            )

            // "warnings-ng" plugin
            // https://github.com/jenkinsci/warnings-ng-plugin/blob/master/doc/Documentation.md#pipeline-configuration
            // https://github.com/jenkinsci/warnings-ng-plugin/blob/master/plugin/src/main/java/io/jenkins/plugins/analysis/core/steps/IssuesRecorder.java
            recordIssues enabledForFailure: true, tools: [mavenConsole(), java(), javaDoc()]
            // recordIssues enabledForFailure: true, tool: checkStyle()
            // recordIssues enabledForFailure: true, tool: spotBugs()
            // recordIssues enabledForFailure: true, tool: cpd(pattern: '**/target/cpd.xml')
            // recordIssues enabledForFailure: true, tool: pmdParser(pattern: '**/target/pmd.xml')

            // "git-forensics" plugin
            // TODO: not yet enabled, actually
            mineRepository()
        }
    }
}
