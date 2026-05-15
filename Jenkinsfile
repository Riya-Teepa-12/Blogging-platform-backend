pipeline {
  agent any

  tools {
    jdk 'jdk17'
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

    stage('Build') {
      steps {
        sh 'mvn -B -DskipTests clean package'
      }
    }

    stage('Test') {
      steps {
        sh 'mvn -B test'
      }
    }

    stage('SonarCloud Scan') {
      steps {
        withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
          sh '''
            mvn -B sonar:sonar \
              -Dsonar.projectKey=$SONAR_PROJECT_KEY \
              -Dsonar.organization=$SONAR_ORG \
              -Dsonar.host.url=https://sonarcloud.io \
              -Dsonar.token=$SONAR_TOKEN
          '''
        }
      }
    }

    stage('Deploy (Docker Compose)') {
      when {
        branch 'main'
      }
      steps {
        sh '''
          docker compose --env-file .env.aws up -d mysql redis zookeeper kafka service-registry
          docker compose --env-file .env.aws up -d auth-service category-service comment-service media-service newsletter-service notification-service post-service
          docker compose --env-file .env.aws up -d api-gateway
        '''
      }
    }
  }

  post {
    always {
      sh 'docker compose --env-file .env.aws ps || true'
    }
    failure {
      echo 'Build failed. Check console logs.'
    }
    success {
      echo 'Build and deploy completed.'
    }
  }
}
