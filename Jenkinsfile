pipeline {
  agent any

  tools {
    maven 'maven3'
  }

  environment {
    SONAR_PROJECT_KEY = 'riya-teepa-12_blogging-platform-backend'
    SONAR_ORG = 'riya-teepa-12'
  }

  options {
    disableConcurrentBuilds()
    timestamps()
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build + Test + SonarCloud') {
      steps {
        withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
          sh '''
            mvn -B clean verify sonar:sonar \
              -Dsonar.host.url=https://sonarcloud.io \
              -Dsonar.projectKey=$SONAR_PROJECT_KEY \
              -Dsonar.organization=$SONAR_ORG \
              -Dsonar.token=$SONAR_TOKEN \
              -Dsonar.qualitygate.wait=true \
              -Dsonar.coverage.jacoco.xmlReportPaths=**/target/site/jacoco/jacoco.xml \
              -Dsonar.coverage.exclusions=**/dto/**,**/entity/**
          '''
        }
      }
    }
  }

  post {
    failure {
      echo 'Build failed. Check console logs.'
    }
    success {
      echo 'Build, test, and SonarCloud scan completed.'
    }
  }
}
