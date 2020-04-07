#! /bin/bash


cat << EOF
################################
# Run Staging pipeline
################################
EOF

STAGING_PREFIX=DBPTK-E-
CURRENT_DATE=$(date "+%Y-%m-%d-%H%M%S")

# Updating Maven modules
mvn versions:set versions:commit -DnewVersion=$STAGING_PREFIX$CURRENT_DATE

# Commit Maven version update
git add -u
git commit -m "Run staging pipeline version $CURRENT_DATE"

# Push tag
git push
