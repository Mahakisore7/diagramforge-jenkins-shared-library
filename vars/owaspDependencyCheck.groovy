def call(Map config) {
    // Optional path (relative to the dir() the caller is already in) to a
    // dependency-check suppression file, for verified false positives -
    // e.g. NVD CPE collisions between the mongodb npm driver and the
    // MongoDB Server product. Never used to silence a real finding.
    def suppressionArg = config.suppressionFile ? "--suppression ${config.suppressionFile}" : ""

    // Bound from a Jenkins credential so the key is masked in build logs.
    withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY')]) {
        sh """
            mkdir -p dependency-check-report
            dependency-check.sh --project "${config.projectName}" --scan ${config.scanPath} \
              --format HTML --format JSON --out dependency-check-report \
              --data /var/lib/jenkins/dependency-check-data \
              --nvdApiKey \$NVD_API_KEY \
              --disableOssIndex \
              --disableCentral \
              --disableYarnAudit \
              ${suppressionArg} \
              --failOnCVSS 8
        """
    }
    publishHTML(target: [
        reportDir: 'dependency-check-report',
        reportFiles: 'dependency-check-report.html',
        reportName: "OWASP Report - ${config.projectName}"
    ])
}
