[![CI](https://github.com/keeps/dbptk-ui/actions/workflows/CI.yml/badge.svg)](https://github.com/keeps/dbptk-ui/actions/workflows/CI.yml)
[![Release](https://github.com/keeps/dbptk-ui/actions/workflows/release.yml/badge.svg)](https://github.com/keeps/dbptk-ui/actions/workflows/release.yml)
[![CodeQL](https://github.com/keeps/dbptk-ui/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/keeps/dbptk-ui/actions/workflows/codeql-analysis.yml)

# Developer notes

Hey devs, here are some notes that may be of use to you!

## How to build from source

1. Download the [latest release](https://github.com/keeps/dbptk-ui/releases) or clone the repository.
2. [Configure](https://docs.github.com/en/packages/guides/configuring-apache-maven-for-use-with-github-packages#authenticating-with-a-personal-access-token) Apache Maven to use GitHub packages
3. Build with Maven `mvn -Dmaven.test.skip clean package`

## Debug WUI

```bash
# Open Spring boot
mvn spring-boot:run -Pdebug-server

# Open codeserver
mvn gwt:codeserver -Dscope.gwt-dev=compile -Pdebug-server

# Open codeserver http://127.0.0.1:9876/ and add bookmarks
# Open DBPTK http://localhost:8080 and click the "Dev Mode On" bookmark

```
Optional: Check Google Chrome "RemoteLiveReload" extension for automatic reloading with spring boot.

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
4. I18n check: `./scripts/check_i18n.sh`

### Releasing a new version

Example release new 2.2.0 version (up from 2.1.0) and prepare for next version 2.3.0

1. Run `./scripts/release.sh 2.2.0`
2. Wait for [GitHub action build](https://github.com/keeps/dbptk-ui/actions/workflows/release.yml) to be finished and successful
3. Review release and accept release:
	1. Review issues
	2. Accept release
4. Run `./scripts/prepare_next_version.sh 2.3.0`
