def call(Map config) {
    // ECR tags are immutable, so re-running a build of a commit whose image is
    // already pushed (e.g. FORCE_BUILD, or a failure after the push) must not
    // fail the pipeline: the image for a given sha is by definition the same.
    // The Jenkins role may push but not describe, so the push itself is the
    // existence check, and only the immutable-tag refusal is tolerated.
    sh """
        aws ecr get-login-password --region ${config.region} | \
          docker login --username AWS --password-stdin ${config.registry}
        TAG=sha-${env.GIT_SHA_SHORT}
        docker tag ${config.localImage} ${config.registry}/${config.repoName}:\$TAG
        LOG=\$(mktemp)
        if docker push ${config.registry}/${config.repoName}:\$TAG 2>&1 | tee "\$LOG"; then
          :
        elif grep -q "tag is immutable" "\$LOG"; then
          echo "\$TAG already exists in ${config.repoName}; treating the push as done"
        else
          rm -f "\$LOG"; exit 1
        fi
        rm -f "\$LOG"
    """
}
