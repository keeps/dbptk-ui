#!/bin/bash

set -ex

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
}

if [[ ! -z "$DOCKER_USERNAME" ]]; then
  # init
  docker login -u "$DOCKER_USERNAME" -p "$DOCKER_PASSWORD"

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
