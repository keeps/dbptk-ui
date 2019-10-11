#!/bin/bash

SERVER_REPO=db-visualization-toolkit-docker
ELECTRON_REPO=db-visualization-toolkit-electron

function trigger_travis_build(){
  REPO=$1
  BRANCH=$2

  echo "Triggered build for ${REPO}"
  body="{
   \"request\": {
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
fi