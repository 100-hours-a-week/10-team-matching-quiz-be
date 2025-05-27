pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "v1999vvv/wingterview-be"
        EC2_HOST = "172.31.11.169"
        APP_PORT = "8080"
    }

    stages {
        stage('Clone Repository') {
            steps {
                checkout scm
            }
        }

        stage('Build Application') {
            steps {
                dir('wingterview') {
                    sh './gradlew clean build -x test'
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                dir('wingterview') {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh """
                        docker build -t ${DOCKER_IMAGE} .
                        echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin
                        docker push ${DOCKER_IMAGE}
                        """
                    }
                }
            }
        }

        stage('Deploy to EC2') {
            steps {
                sshagent (credentials: ['backend-ec2-key']) {
                    sh """
                    ssh -o StrictHostKeyChecking=no ec2-user@${EC2_HOST} '
                        docker pull ${DOCKER_IMAGE}
                        docker stop wingterview || true
                        docker rm wingterview || true
                        docker run -d -p ${APP_PORT}:${APP_PORT} --name wingterview ${DOCKER_IMAGE}
                    '
                    """
                }
            }
        }
    }
}
