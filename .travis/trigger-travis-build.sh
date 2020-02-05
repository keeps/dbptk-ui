#!/bin/bash

SERVER_REPO=db-visualization-toolkit-docker
ELECTRON_REPO=db-visualization-toolkit-electron

function trigger_travis_build(){
  REPO=$1
  BRANCH=$2

  echo "Triggered build for ${REPO}"
  body="{
   \"request\": {
    \"message\": \"This is an api request\",
    \"branch\":\"$BRANCH\"
  }}"

  curl -s -X POST \
   -H "Content-Type: application/json" \
   -H "Accept: application/json" \
   -H "Travis-API-Version: 3" \
   -H "Authorization: token $TRAVIS_TOKEN" \
   -d "$body" \
   https://api.travis-ci.org/repo/keeps%2F$REPO/requests
}

if [ "$TRAVIS_BRANCH" == "staging" ]; then
  echo "Logic for staging"
  trigger_travis_build $SERVER_REPO "staging"
elif [ "`echo $TRAVIS_BRANCH | egrep "^v[1-9]+" | wc -l`" -eq "1" ]; then
  echo "Logic for $TRAVIS_BRANCH tag"
  trigger_travis_build $SERVER_REPO $TRAVIS_BRANCH
elif [ "$TRAVIS_BRANCH" == "master" ]; then
  echo "Logic for $TRAVIS_BRANCH branch"
  trigger_travis_build $SERVER_REPO $TRAVIS_BRANCH
fi