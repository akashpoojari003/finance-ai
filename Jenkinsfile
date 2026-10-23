pipeline {
    agent any

    tools {
        maven 'M3'
        nodejs 'Node20'
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        BACKEND_DIR    = 'ai-finance-manager/backend/ai-finance'
        FRONTEND_DIR   = 'ai-finance-manager/frontend/finance-ai-manager'
        IMAGE_NAME     = 'financeai-api'
        // Set to 'true' only after you have a server, a valid deploy-ssh key and the values below
        DEPLOY_ENABLED = 'false'
        DEPLOY_HOST    = 'deploy@your-server.example.com'
        DEPLOY_DIR     = '/opt/ai-finance-manager/infra'
    }

    stages {
        stage('Backend verify') {
            steps {
                script { env.CURRENT_STAGE = env.STAGE_NAME }
                dir(env.BACKEND_DIR) {
                    sh 'test -f pom.xml || { echo "pom.xml not found in $(pwd)"; ls -la; exit 1; }'
                    sh 'mvn -B -ntp verify'
                }
            }
            post {
                always {
                    junit testResults: "${env.BACKEND_DIR}/target/surefire-reports/*.xml", allowEmptyResults: true
                }
            }
        }

        stage('Frontend build') {
            steps {
                script { env.CURRENT_STAGE = env.STAGE_NAME }
                dir(env.FRONTEND_DIR) {
                    sh 'test -f package.json || { echo "package.json not found in $(pwd)"; ls -la; exit 1; }'
                    sh 'npm ci --no-audit --no-fund'
                    sh 'npm run build'
                }
            }
        }

        stage('Docker build and push') {
            when {
                anyOf {
                    branch 'main'
                    expression { env.GIT_BRANCH == 'origin/main' }
                }
            }
            steps {
                script { env.CURRENT_STAGE = env.STAGE_NAME }
                withCredentials([usernamePassword(credentialsId: 'dockerhub',
                                                  usernameVariable: 'DOCKER_USER',
                                                  passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        set -e
                        IMAGE="$DOCKER_USER/$IMAGE_NAME"
                        docker build -t "$IMAGE:$BUILD_NUMBER" -t "$IMAGE:latest" "$BACKEND_DIR"
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push "$IMAGE:$BUILD_NUMBER"
                        docker push "$IMAGE:latest"
                    '''
                }
            }
            post {
                always {
                    sh 'docker logout || true'
                }
            }
        }

        stage('Deploy') {
            when {
                allOf {
                    anyOf {
                        branch 'main'
                        expression { env.GIT_BRANCH == 'origin/main' }
                    }
                    expression { env.DEPLOY_ENABLED == 'true' }
                }
            }
            steps {
                script { env.CURRENT_STAGE = env.STAGE_NAME }
                sshagent(credentials: ['deploy-ssh']) {
                    sh '''
                        set -e
                        ssh -o StrictHostKeyChecking=accept-new "$DEPLOY_HOST" \
                          "cd $DEPLOY_DIR && export API_TAG=$BUILD_NUMBER && docker compose pull api && docker compose up -d --remove-orphans"
                    '''
                }
            }
        }
    }

    post {
        failure {
            echo "Pipeline FAILED in stage: ${env.CURRENT_STAGE ?: 'before the first stage (checkout or setup)'}"
        }
    }
}