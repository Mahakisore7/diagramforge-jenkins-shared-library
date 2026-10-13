def call(Map config) {
    withSonarQubeEnv('sonarqube-server') {
        // The scanner reads the token from SONAR_TOKEN, so it is not passed as
        // an argument (and stays out of the process list).
        sh """
            SONAR_TOKEN=\$SONAR_AUTH_TOKEN sonar-scanner \
              -Dsonar.projectKey=${config.projectKey} \
              -Dsonar.sources=${config.sourcePath} \
              -Dsonar.host.url=\$SONAR_HOST_URL
        """
    }
    timeout(time: 5, unit: 'MINUTES') {
        def qg = waitForQualityGate()
        if (qg.status != 'OK') {
            error "SonarQube Quality Gate failed: ${qg.status}"
        }
    }
}
