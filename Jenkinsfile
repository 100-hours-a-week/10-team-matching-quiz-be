pipeline {
  agent any

  environment {
    DOCKER_IMAGE = 'v1999vvv/backend:latest'
    EC2_USER = 'ec2-user'
    EC2_HOST = '172.31.1.177'
    REMOTE_WORK_DIR = '/home/ec2-user'
    IAMGE_TAG = 'dev'
  }

  stages {
    stage('Clone') {
      steps {
        git credentialsId: 'github-pat', url: 'https://github.com/100-hours-a-week/10-team-matching-quiz-be.git', branch: 'main'
      }
    }

    stage('Prepare Secret Config') {
      steps {
        withCredentials([file(credentialsId: 'app-secret-yml', variable: 'APP_SECRET_YML')]) {
          sh 'cp $APP_SECRET_YML ./wingterview/src/main/resources/application-secret.yml'
        }
      }
    }

    stage('Build') {
      steps {
        dir('wingterview') {
          sh './gradlew clean build -x test'
        }
      }
    }

    stage('Docker Build & Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh """
            ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} << EOF
              cd ${REMOTE_WORK_DIR}
              docker compose pull
              docker compose up -d
            EOF
          """
        }
      }
    }
  }
}
