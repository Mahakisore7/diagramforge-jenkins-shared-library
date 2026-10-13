def call(Map config) {
    // Optional path (relative to the dir() the caller is already in) to a
    // dependency-check suppression file, for verified false positives -
    // e.g. NVD CPE collisions between the mongodb npm driver and the
    // MongoDB Server product. Never used to silence a real finding.
    def suppressionArg = config.suppressionFile ? "--suppression ${config.suppressionFile}" : ""

    // Bound from a Jenkins credential so the key is masked in build logs. It is
    // handed to the CLI through a private properties file rather than an
    // argument, so it never appears in the process list (ps).
    withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY')]) {
        sh """
            set -e
            PROPS=\$(mktemp)
            trap 'rm -f \"\$PROPS\"' EXIT
            chmod 600 \"\$PROPS\"
            printf 'nvd.api.key=%s\\n' \"\$NVD_API_KEY\" > \"\$PROPS\"
            mkdir -p dependency-check-report
            dependency-check.sh --project "${config.projectName}" --scan ${config.scanPath} \
              --format HTML --format JSON --out dependency-check-report \
              --data /var/lib/jenkins/dependency-check-data \
              --propertyfile \"\$PROPS\" \
              --disableOssIndex \
              --disableCentral \
              --disableYarnAudit \
              --disablePnpmAudit \
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
