def call(Map config) {
    // ECR tags are immutable, so re-running a build of a commit whose image is
    // already pushed (e.g. FORCE_BUILD, or a failure after the push) must not
    // fail the pipeline. The image for a given sha is by definition the same.
    sh """
        aws ecr get-login-password --region ${config.region} | \
          docker login --username AWS --password-stdin ${config.registry}
        TAG=sha-${env.GIT_SHA_SHORT}
        if aws ecr describe-images --region ${config.region} --repository-name ${config.repoName} \
             --image-ids imageTag=\$TAG >/dev/null 2>&1; then
          echo "\$TAG already exists in ${config.repoName}; skipping push (tags are immutable)"
        else
          docker tag ${config.localImage} ${config.registry}/${config.repoName}:\$TAG
          docker push ${config.registry}/${config.repoName}:\$TAG
        fi
    """
}
