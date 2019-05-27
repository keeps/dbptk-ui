# Build Electron App


```bash
# First setup
npm install

# Before run, needs Solr
docker run -d -p 8983:8983 -p 9983:9983 solr:7 -c

# Run, (needs war compiled: mvn clean package on project root)
npm start

# Build for host OS
npm run-script build

# Build for all OSes
npm run-script build-all
```