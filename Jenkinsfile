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
    stage('Clone Code') {
      steps {
        git branch: 'dev', url: 'https://github.com/100-hours-a-week/10-team-matching-quiz-be.git'
      }
    }
  
  stage('Build & Push Docker Image') {
    steps {
      withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
        sh """
          cd wingterview
          ./gradlew clean build -x test
          echo "$DOCKERHUB_PASS" | docker login -u "$DOCKERHUB_USER" --password-stdin
          docker build -f Dockerfile -t $DOCKER_IMAGE .
          docker push $DOCKER_IMAGE
        """
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
