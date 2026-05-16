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
    
   stage('Verify MySQL Connectivity') {
      steps {
          sh '''
      set +e
      echo "== DNS/host check =="
      getent hosts host.docker.internal || true
      getent hosts mysql || true

      echo "== Port check 3306 =="
      (echo > /dev/tcp/host.docker.internal/3306) >/dev/null 2>&1 && echo "host.docker.internal:3306 OPEN" || echo "host.docker.internal:3306 CLOSED"
      (echo > /dev/tcp/mysql/3306) >/dev/null 2>&1 && echo "mysql:3306 OPEN" || echo "mysql:3306 CLOSED"

      echo "== MySQL login check (if client exists) =="
      mysql --version || true
      mysql -h host.docker.internal -P 3306 -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1;" || true
      mysql -h mysql -P 3306 -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1;" || true
    '''
  }
}

    stage('Build + Test + SonarCloud') {
      steps {
        withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
          sh '''
            mvn -B clean verify sonar:sonar \
	      -DINKWELL_LOG_CONFIG=file:/var/jenkins_home/workspace/inkwell-backend/common-logback-spring.xml \
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
