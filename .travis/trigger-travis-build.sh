#!/bin/bash

set -ex

SERVER_REPO=dbptk-enterprise
ELECTRON_REPO=dbptk-desktop

function build_body_request() {
  BRANCH=$1
  TAG=$2

  if [ -n "${TAG}" ]; then
    CONFIG=",\"config\": {
      \"env\": [\"TAG=$TAG\"]
    }"
  fi

  BODY="{
   \"request\": {
    \"message\": \"This is an api request\",
    \"branch\":\"$BRANCH\"
    $CONFIG
  }}"
}

function trigger_travis_build(){
  REPO=$1
  BRANCH=$2
  TAG=$3

  echo "Triggered build for ${REPO}"
  build_body_request "$2" "$3"

  curl -s -X POST \
   -H "Content-Type: application/json" \
   -H "Accept: application/json" \
   -H "Travis-API-Version: 3" \
   -H "Authorization: token $TRAVIS_TOKEN" \
   -d "$BODY" \
   https://api.travis-ci.org/repo/keeps%2F$REPO/requests
}

if [ "$TRAVIS_BRANCH" == "staging" ]; then
  echo "Logic for staging"
  trigger_travis_build $SERVER_REPO "staging"
elif [ "`echo $TRAVIS_BRANCH | egrep "^v[1-9]+" | wc -l`" -eq "1" ]; then
  echo "Logic for $TRAVIS_BRANCH tag"
  trigger_travis_build $SERVER_REPO "master" $TRAVIS_BRANCH
elif [ "$TRAVIS_BRANCH" == "master" ]; then
  echo "Logic for $TRAVIS_BRANCH branch"
  trigger_travis_build $SERVER_REPO $TRAVIS_BRANCH
fi