pipeline {
  agent any

  environment {
    DOCKER_IMAGE = 'v1999vvv/wingterview-be'
    IMAGE_TAG = 'prod'
  }
    
  stage('Prepare Secret Config') {
      steps {
        withCredentials([file(credentialsId: 'app-secret-yml', variable: 'APP_SECRET_YML')]) {
          sh 'cp $APP_SECRET_YML ./wingterview/src/main/resources/application-secret.yml'
        }
      }
    }
  
  stages {
    stage('Clone Code') {
      steps {
        git 'https://github.com/your-org/your-backend-repo.git'
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

    stage('Deploy to EC2 via SSH') {
      steps {
        sshagent(credentials: ['backend-ssh-key']) {
          sh """
            docker build -t $DOCKER_IMAGE:$IMAGE_TAG ./wingterview
            echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
            docker push $DOCKER_IMAGE:$IMAGE_TAG
          """
        }
      }
    }
  }
}
