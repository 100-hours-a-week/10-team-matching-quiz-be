pipeline {
  agent any

  triggers {
    githubPush()
  }

  environment {
    DOCKER_IMAGE = 'v1999vvv/backend:latest'
    EC2_USER = 'ec2-user'
    EC2_HOST = '43.201.251.197'
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
        withCredentials([
          usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS'),
          file(credentialsId: 'app-secret-yml', variable: 'APP_SECRET_FILE') // Secret File
        ]) {
          sh """
            cd wingterview

            # ✅ Secret file을 application-secret.yml 로 복사
            cp \$APP_SECRET_FILE application-secret.yml

            ./gradlew clean build -x test

            echo "\$DOCKERHUB_PASS" | docker login -u "\$DOCKERHUB_USER" --password-stdin

            docker build -t \$DOCKER_IMAGE .

            docker push \$DOCKER_IMAGE
          """
        }
      }
    }

    stage('Deploy to EC2') {
      steps {
        sshagent(credentials: ['backend-ec2-key']) {
          sh '''
            ssh -o StrictHostKeyChecking=no ec2-user@3.37.14.59 << 'ENDSSH'
              cd /home/ec2-user
              docker compose pull
              docker compose up -d
ENDSSH
          '''
        }
      }
    }
  }
}
