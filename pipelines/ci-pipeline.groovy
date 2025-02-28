node("maven") {

    def project = "dev"
    def microservice = "project-burndown"

    currentBuild.description = "Build a container from the source, then execute unit and container integration tests before promoting the container as a release candidate for acceptance testing."

    stage("checkout") {
        git branch: "master", url: "https://github.com/stericbro/estafet-microservices-scrum-api-project-burndown"
    }

    stage("reset a-mq to purge topics") {
        openshiftDeploy namespace: project, depCfg: "broker-amq", showBuildLogs: "true",  waitTime: "3000000"
        openshiftVerifyDeployment namespace: project, depCfg: "broker-amq", replicaCount:"1", verifyReplicaCount: "true", waitTime: "300000"
    }

    stage("build & deploy container") {
        openshiftBuild namespace: project, buildConfig: microservice, showBuildLogs: "true",  waitTime: "300000"
        sh "oc set env dc/${microservice} JBOSS_A_MQ_BROKER_URL=tcp://broker-amq-tcp.${project}.svc:61616 -n ${project}"
        openshiftVerifyDeployment namespace: project, depCfg: microservice, replicaCount:"1", verifyReplicaCount: "true", waitTime: "300000"
        sleep time:120
    }

    stage("container tests") {
        withEnv(
            [    "PROJECT_BURNDOWN_REPOSITORY_JDBC_URL=jdbc:postgresql://postgresql.${project}.svc:5432/${project}-${microservice}",
                "PROJECT_BURNDOWN_REPOSITORY_DB_USER=postgres",
                "PROJECT_BURNDOWN_REPOSITORY_DB_PASSWORD=welcome1",
                "PROJECT_BURNDOWN_SERVICE_URI=http://${microservice}.${project}.svc:8080",
                "JBOSS_A_MQ_BROKER_URL=tcp://broker-amq-tcp.${project}.svc:61616",
                "JBOSS_A_MQ_BROKER_USER=amq",
                "JBOSS_A_MQ_BROKER_PASSWORD=amq"
            ]) {
            withMaven(mavenSettingsConfig: 'microservices-scrum') {
                try {
                    sh "mvn clean verify -P integration-test"
                } finally {
                    sh "oc set env dc/${microservice} JBOSS_A_MQ_BROKER_URL=tcp://localhost:61616 -n ${project}"
                }
            }
        }
    }

    stage("deploy snapshots") {
        withMaven(mavenSettingsConfig: 'microservices-scrum') {
             sh "mvn clean deploy -Dmaven.test.skip=true"
        }
    }

    stage("promote to test") {
        openshiftTag namespace: project, srcStream: microservice, srcTag: 'latest', destinationNamespace: 'test', destinationStream: microservice, destinationTag: 'PrepareForTesting'
    }

}

