pipeline {
  agent any

  environment {
    DOCKER_IMAGE = 'v1999vvv/wingterview-be'
    IMAGE_TAG = 'prod'
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
            docker build -t $DOCKER_IMAGE:$IMAGE_TAG ./wingterview
            echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
            docker push $DOCKER_IMAGE:$IMAGE_TAG
          """
        }
      }
    }

    stage('Deploy with Ansible') {
      steps {
        sshagent (credentials: ['backend-ec2-key']) {
          sh """
            ssh -o StrictHostKeyChecking=no ec2-user@43.203.77.116 '
              ssh -o StrictHostKeyChecking=no ubuntu@172.30.11.7 "
                cd ~/ansible-deploy &&
                ansible-playbook -i inventory.ini deploy.yml
              "
            '
          """
        }
      }
    }
  }
}
