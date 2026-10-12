pipeline {
    agent any

    environment {
        BACKEND_IMAGE    = 'jk-tourism-backend'
        FRONTEND_IMAGE   = 'jk-tourism-frontend'
        IMAGE_TAG        = "${env.BUILD_NUMBER ?: 'latest'}"
        MSYS_NO_PATHCONV = '1'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend — Test & Package') {
            steps {
                dir('tourisminformation') {
                    sh './mvnw clean test package -B -DskipTests=false'
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'tourisminformation/target/*.jar', fingerprint: true
                }
            }
        }

        stage('Security — OWASP Dependency-Check') {
            steps {
                dir('tourisminformation') {
                    sh '''
                        NVD_KEY_ARG=""
                        if [ -n "$NVD_API_KEY" ]; then
                            NVD_KEY_ARG="-DnvdApiKey=$NVD_API_KEY"
                        fi

                        if command -v dependency-check.sh >/dev/null 2>&1; then
                            dependency-check.sh --project "tourisminformation" --scan "." --format "HTML" --format "JSON" --failOnCVSS 7.0 --out "."
                        else
                            ./mvnw org.owasp:dependency-check-maven:12.1.0:check -DfailBuildOnCVSS=7.0 -DfailOnError=false -DautoUpdate=false -DossindexAnalyzerEnabled=false $NVD_KEY_ARG -B
                        fi
                    '''
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'tourisminformation/dependency-check-report.html,tourisminformation/target/dependency-check-report.html', allowEmptyArchive: true
                }
            }
        }

        stage('Docker — Build Images') {
            parallel {
                stage('Backend Image') {
                    steps {
                        dir('tourisminformation') {
                            sh "docker build -t ${BACKEND_IMAGE}:${IMAGE_TAG} -t ${BACKEND_IMAGE}:latest ."
                        }
                    }
                }
                stage('Frontend Image') {
                    steps {
                        dir('frontend') {
                            sh "docker build -t ${FRONTEND_IMAGE}:${IMAGE_TAG} -t ${FRONTEND_IMAGE}:latest ."
                        }
                    }
                }
            }
        }

        stage('Security — Trivy Image Scan') {
            steps {
                sh """
                    export MSYS_NO_PATHCONV=1
                    if command -v trivy >/dev/null 2>&1; then
                        TRIVY_BIN="trivy"
                    elif docker image inspect aquasec/trivy:latest >/dev/null 2>&1; then
                        TRIVY_BIN="docker run --rm -v //var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest"
                    else
                        echo "Neither Trivy CLI nor aquasec/trivy Docker image is available on the build agent."
                        exit 1
                    fi

                    echo "Scanning \${BACKEND_IMAGE}:\${IMAGE_TAG} for HIGH/CRITICAL vulnerabilities..."
                    \$TRIVY_BIN image --exit-code 1 --severity HIGH,CRITICAL \${BACKEND_IMAGE}:\${IMAGE_TAG}

                    echo "Scanning \${FRONTEND_IMAGE}:\${IMAGE_TAG} for HIGH/CRITICAL vulnerabilities..."
                    \$TRIVY_BIN image --exit-code 1 --severity HIGH,CRITICAL \${FRONTEND_IMAGE}:\${IMAGE_TAG}
                """
            }
        }

        stage('Docker — Smoke Test') {
            steps {
                sh '''
                    docker compose down --remove-orphans 2>/dev/null || true
                    docker compose up -d --build

                    echo "Waiting for backend service to become healthy..."
                    for i in $(seq 1 30); do
                        if curl -sf http://localhost:8081/api/health >/dev/null 2>&1; then
                            echo "Backend is healthy!"
                            break
                        fi
                        sleep 2
                    done

                    echo "Waiting for frontend service to respond..."
                    for i in $(seq 1 30); do
                        if curl -sf http://localhost:8088/ >/dev/null 2>&1; then
                            echo "Frontend is reachable!"
                            break
                        fi
                        sleep 2
                    done

                    curl -sf http://localhost:8081/api/health
                    curl -sf http://localhost:8088/ | head -c 200
                '''
            }
            post {
                always {
                    sh 'docker compose down --remove-orphans 2>/dev/null || true'
                }
            }
        }
    }

    post {
        success {
            echo "Build ${IMAGE_TAG} completed — images: ${BACKEND_IMAGE}, ${FRONTEND_IMAGE}"
        }
        failure {
            echo 'Pipeline failed. Check backend tests, dependency scan, Trivy image scan, or Docker logs.'
        }
        cleanup {
            cleanWs()
        }
    }
}