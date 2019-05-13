#!/bin/bash

set -ex

################################################
# functions
################################################

function deploy_to_artifactory(){
  echo "Deploy to artifactory"
  mvn $MAVEN_CLI_OPTS clean package deploy -Dmaven.test.skip=true -Denforcer.skip=true -Pmodel
}

function deploy_to_dockerhub(){
  echo "Deploy to docker hub"

  DOCKER_TAG=${1:-$TRAVIS_BRANCH}
  DBVTK_DEV_BRANCH=${2:-$TRAVIS_BRANCH}

  if [[ "$DOCKER_TAG" != "latest" ]]; then
    docker tag keeps/dbvtk:latest keeps/dbvtk:$TRAVIS_BRANCH
  fi

  # Push to https://hub.docker.com/r/keeps/dbvtk/
  docker push keeps/dbvtk:$DOCKER_TAG

  ## Docker App
  export OSTYPE="$(uname | tr A-Z a-z)"
  curl -fsSL --output "/tmp/docker-app-${OSTYPE}.tar.gz" "https://github.com/docker/app/releases/download/v0.6.0/docker-app-${OSTYPE}.tar.gz"
  tar xf "/tmp/docker-app-${OSTYPE}.tar.gz" -C /tmp/
  sudo install -b "/tmp/docker-app-standalone-${OSTYPE}" /usr/local/bin/docker-app
  
  cd deploys/development
  docker-app inspect 
  docker-app push --tag keeps/dbvtk:$DOCKER_TAG
  cd $TRAVIS_BUILD_DIR
}

################################################
# Compile, test, code analysis
################################################

mvn $MAVEN_CLI_OPTS -Dtestng.groups="travis-ci" -Denforcer.skip=true -Pdocker clean install

################################################
# Deploy
################################################

if [[ ! -z "$DOCKER_USERNAME" ]]; then
  # init
  echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

  if [ "$TRAVIS_BRANCH" == "master" ]; then
    echo "Logic for master branch"
    deploy_to_dockerhub "latest" "$TRAVIS_BRANCH"
    deploy_to_artifactory

  elif [ "$TRAVIS_BRANCH" == "development" ]; then
    echo "Logic for development branch"
    deploy_to_dockerhub "$TRAVIS_BRANCH" "$TRAVIS_BRANCH"
    deploy_to_artifactory

  elif [ "$TRAVIS_BRANCH" == "staging" ]; then
    echo "Logic for staging branch"
    deploy_to_dockerhub "$TRAVIS_BRANCH" "staging"

  elif [ "`echo $TRAVIS_BRANCH | egrep "^v[1-9]+" | wc -l`" -eq "1" ]; then
    echo "Logic for tags"
    deploy_to_dockerhub "$TRAVIS_BRANCH" "master"
    deploy_to_artifactory
  fi

  # clean up
  docker logout
fi
