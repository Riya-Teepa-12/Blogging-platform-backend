pipeline {
  agent any

  tools {
    maven 'maven3'
  }

  environment {
    SONAR_PROJECT_KEY = 'riya-teepa-12_blogging-platform-backend'
    SONAR_ORG = 'riya-teepa-12'
    DB_HOST = 'inkwell-mysql'
    DB_PORT = '3306'
    DB_NAME = 'inkwell_platform'
    DB_USER = 'root'
    DB_PASS = 'admin'
    AUTH_INTERNAL_API_KEY = 'ghfyfr7t8hgv7yh'
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
	    echo "== DNS =="
	    getent hosts inkwell-mysql || true

	    echo "== Port 3306 (nc) =="
	    nc -zv inkwell-mysql 3306 || true

	    echo "== Env check =="
	    echo "DB_HOST=$DB_HOST"
	    echo "DB_PORT=$DB_PORT"
	    echo "DB_NAME=$DB_NAME"
	  '''
	}
    }

    stage('Build + Test + SonarCloud') {
      steps {
        withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
          sh '''
            mvn -B clean verify sonar:sonar \
	      -Dspring.profiles.active=test \
  	      -Dspring.datasource.url=jdbc:mysql://inkwell-mysql:3306/inkwell_platform \
  	      -Dspring.datasource.username=root \
  	      -Dspring.datasource.password=admin \
  	      -DAUTH_INTERNAL_API_KEY=ghfyfr7t8hgv7yh \
  	      -DINKWELL_INTERNAL_API_KEY=ghfyfr7t8hgv7yh \
	      -Dspring.security.oauth2.client.registration.google.client-id=dummy \
	      -Dspring.security.oauth2.client.registration.google.client-secret=dummy \
 	      -Dspring.security.oauth2.client.registration.github.client-id=dummy \
	      -Dspring.security.oauth2.client.registration.github.client-secret=dummy \
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
