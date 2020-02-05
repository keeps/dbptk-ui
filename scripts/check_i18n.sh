#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

for translation in $(find $DIR/../src/main/resources/config/i18n/client/ -name 'ClientMessages_*.properties'); do
    echo '######################################'
    echo "# $(basename $translation)"
    echo '######################################'
    echo

    $DIR/comparei18n.sh $translation

done