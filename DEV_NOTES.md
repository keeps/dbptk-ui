# Developer notes

Hey devs, here are some notes that may be of use to you!

[![Build Status](https://travis-ci.org/keeps/db-preservation-toolkit.png?branch=master)](https://travis-ci.org/keeps/db-preservation-toolkit)

## How to prepare and release a new version

This release build/deploy method requires `gren`:

```
curl https://raw.githubusercontent.com/creationix/nvm/v0.33.8/install.sh | bash
source ~/.nvm/nvm.sh
nvm install v8.11.1
npm install github-release-notes -g
```

### Before releasing

1. Make sure the dependencies are installed by running `gren`
2. Security check: `mvn com.redhat.victims.maven:security-versions:check`
3. Update check: `./scripts/check_versions.sh MINOR`

### Releasing a new version

Example release new 2.2.0 version (up from 2.1.0) and prepare for next version 2.3.0

1. Run `./scripts/release.sh 2.2.0`
2. Wait for [travis tag build](https://travis-ci.org/keeps/db-visualization-toolkit/) to be finished and successful
3. Local compile to generate dbptk-app.jar artifact `mvn clean package -Dmaven.test.skip`
4. `gren release --draft -t v2.1.0..v2.2.0`
5. Review release and accept release:
	1. Review issues
	2. Add docker run instructions
	3. Upload dbptk-app.jar artifact
	4. Accept release
6. Run `./scripts/update_changelog.sh 2.2.0`
7. Run `./scripts/prepare_next_version.sh 2.3.0`
