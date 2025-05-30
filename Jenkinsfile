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
        dir('wingterview') {
          sh 'chmod +x ./gradlew'
          sh './gradlew clean build -x test'
        }
      }
    }

    stage('Docker Build & Push') {
      steps {
        dir('wingterview') {
          withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
            sh """
              docker build -t \$DOCKER_IMAGE .
              echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
