def call(Map config) {
    // Bound from a Jenkins credential so the key is masked in build logs.
    withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY')]) {
        sh """
            mkdir -p dependency-check-report
            dependency-check.sh --project "${config.projectName}" --scan ${config.scanPath} \
              --format HTML --format JSON --out dependency-check-report \
              --nvdApiKey \$NVD_API_KEY \
              --failOnCVSS 8
        """
    }
    publishHTML(target: [
        reportDir: 'dependency-check-report',
        reportFiles: 'dependency-check-report.html',
        reportName: "OWASP Report - ${config.projectName}"
    ])
}
