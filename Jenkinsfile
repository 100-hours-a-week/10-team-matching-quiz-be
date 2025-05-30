pipeline {
  agent any

  environment {
    DOCKER_IMAGE = 'v1999vvv/wingterview-be'
  }

  stages {
    stage('Clone') {
      steps {
        git credentialsId: 'github-pat', url: 'https://github.com/100-hours-a-week/10-team-matching-quiz-be.git', branch: 'dev'
      }
    }

    stage('Build') {
      steps {
        sh './gradlew clean build -x test'
      }
    }

    stage('Docker Build & Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh """
            docker build -t \$DOCKER_IMAGE .
            echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
            docker push \$DOCKER_IMAGE
          """
        }
      }
    }

    stage('Deploy to Backend EC2') {
      steps {
        sshagent (credentials: ['backend-ec2-key']) {
          sh """
            ssh -o StrictHostKeyChecking=no ec2-user@<백엔드-EC2-IP> '
              docker pull \$DOCKER_IMAGE
              docker stop wingterview-be || true
              docker rm wingterview-be || true
              docker run -d --name wingterview-be -p 8081:8080 \$DOCKER_IMAGE
            '
          """
        }
      }
    }
  }
}

