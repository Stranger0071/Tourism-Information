pipeline {
    agent any

    environment {
        BACKEND_IMAGE  = 'jk-tourism-backend'
        FRONTEND_IMAGE = 'jk-tourism-frontend'
        IMAGE_TAG      = "${env.BUILD_NUMBER ?: 'latest'}"
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
                        if command -v dependency-check.sh >/dev/null 2>&1; then
                            dependency-check.sh --project "tourisminformation" --scan "." --format "HTML" --format "JSON" --failOnCVSS 7.0 --out "."
                        else
                            ./mvnw org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=7.0 -B
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
                    if command -v trivy >/dev/null 2>&1; then
                        echo "Scanning ${BACKEND_IMAGE}:${IMAGE_TAG} for HIGH/CRITICAL vulnerabilities..."
                        trivy image --exit-code 1 --severity HIGH,CRITICAL ${BACKEND_IMAGE}:${IMAGE_TAG}

                        echo "Scanning ${FRONTEND_IMAGE}:${IMAGE_TAG} for HIGH/CRITICAL vulnerabilities..."
                        trivy image --exit-code 1 --severity HIGH,CRITICAL ${FRONTEND_IMAGE}:${IMAGE_TAG}
                    else
                        echo "Trivy CLI is not installed on the build agent."
                        exit 1
                    fi
                """
            }
        }

        stage('Docker — Smoke Test') {
            steps {
                sh '''
                    docker compose down --remove-orphans 2>/dev/null || true
                    docker compose up -d --build
                    sleep 15
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