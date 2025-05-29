pipeline {
  agent any

  environment {
    DOCKER_IMAGE = 'v1999vvv/wingterview-be'  // 수정
  }

  stages {
    stage('Clone Repository') {
      steps {
        git credentialsId: 'github-pat', url: 'https://github.com/100-hours-a-week/10-team-matching-quiz-be.git', branch: 'dev'
      }
    }

    stage('Gradle Build') {
      steps {
        sh './gradlew clean build -x test'
      }
    }

    stage('Docker Build & Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
          sh """
            docker build -t $DOCKER_IMAGE .
            echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin
            docker push $DOCKER_IMAGE
          """
        }
      }
    }

    stage('Deploy to Backend EC2') {
      steps {
        sshagent (credentials: ['backend-ec2-key']) {
          sh """
            ssh -o StrictHostKeyChecking=no ec2-user@172.31.2.153 '
              docker pull $DOCKER_IMAGE
              docker stop backend || true
              docker rm backend || true
              docker run -d --name backend -p 8080:8080 $DOCKER_IMAGE
            '
          """
        }
      }
    }
  }
}
