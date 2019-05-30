# Before run or build
```bash

# Linux
docker run -d -p 8983:8983 -p 9983:9983 solr:7 -c

# MacOS
docker run -d -p 8983:8983 -p 9983:9983 -e SOLR_HOST=$(hostname) solr:7 -c

# Windows
set HOSTNAME=(hostname)
docker run -d -p 8983:8983 -p 9983:9983 -e SOLR_HOST=%HOSTNAME% solr:7 -c
```

# Run

Execute the appropriate Electron App

# Build Electron App

```bash
# First setup
npm install

# Run, (needs war compiled: mvn clean package on project root)
npm start

# Build for host OS
npm run-script build

# Build for all OSes
npm run-script build-all
```