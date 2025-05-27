pipeline {
    agent any

    environment {
        // Docker 이미지 이름은 파이프라인 전반에 걸쳐 사용됩니다.
        DOCKER_IMAGE = "v1999vvv/wingterview-be"
        // EC2 인스턴스 IP 주소 또는 호스트 이름 (실제 환경에 맞게 변경하세요)
        EC2_HOST = "172.31.11.169"
        // 애플리케이션 포트 (백엔드가 사용하는 포트)
        APP_PORT = "8080"
    }

    // 파이프라인 시작 시 워크스페이스를 깨끗하게 초기화합니다.
    options {
        // 이 옵션은 빌드 시작 시 워크스페이스를 자동으로 정리합니다.
        // cleanWs()는 Workspace Cleanup Plugin이 설치되어 있어야 합니다.
        // 또는 stages 블록 내 첫 번째 스텝으로 cleanWs()를 사용할 수도 있습니다.
        skipDefaultCheckout() // 기본 체크아웃을 건너뛰고, 'Initialize & Clone Repository'에서 수동으로 checkout scm을 사용
    }

    stages {
        stage('Clean Workspace') {
            steps {
                echo 'Cleaning Jenkins workspace...'
                cleanWs() // Workspace Cleanup Plugin이 필요합니다.
            }
        }

        stage('Initialize & Clone Repository') {
            steps {
                // Jenkins Job 설정에서 SCM 정보를 가져와 저장소를 체크아웃합니다.
                // Job 설정에 'Repository URL', 'Credentials', 'Branches to build'가 올바르게 설정되어야 합니다.
                checkout scm
                script {
                    if (fileExists('.git')) {
                        echo "Git repository cloned successfully."
                    } else {
                        error "Failed to clone Git repository. .git directory not found."
                    }
                }
            }
        }

        stage('Build Application') {
            steps {
                echo 'Building the application with Gradle...'
                sh './gradlew clean build -x test'
            }
        }

        stage('Docker Build & Push') {
            steps {
                echo 'Building and pushing Docker image to Docker Hub...'
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh """
                    docker build -t ${DOCKER_IMAGE} .
                    echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin
                    docker push ${DOCKER_IMAGE}
                    """
                }
            }
        }

        stage('Deploy to EC2') {
            steps {
                echo "Deploying Docker image to EC2 instance: <span class="math-inline">\{EC2\_HOST\}\.\.\."
sshagent \(credentials\: \['ec2\-user'\]\) \{
sh """
ssh \-o StrictHostKeyChecking\=no ec2\-user@</span>{EC2_HOST} '
                        echo "Pulling latest Docker image..."
                        docker pull ${DOCKER_IMAGE}

                        echo "Stopping and removing existing container (if any)..."
