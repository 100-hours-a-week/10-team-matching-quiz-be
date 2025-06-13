pipeline {
  agent any

  environment {
    DOCKER_IMAGE = 'v1999vvv/backend:latest'
    EC2_USER = 'ec2-user'
    EC2_HOST = '172.31.1.177'
    REMOTE_WORK_DIR = '/home/ec2-user'
    IMAGE_TAG = 'dev'
  }

  stages {
    stage('Clone Code') {
      steps {
        git branch: 'dev', url: 'https://github.com/100-hours-a-week/10-team-matching-quiz-be.git'
      }
    }

    stage('Build & Push Docker Image') {
      steps {
        script {
          docker.withRegistry('https://index.docker.io/v1/', 'dockerhub') {
            docker.build("${DOCKER_IMAGE}").push()
          }
        }
      }
    }

    stage('Deploy to EC2') {
      steps {
        sshagent(credentials: ['backend-ssh-key']) {
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
