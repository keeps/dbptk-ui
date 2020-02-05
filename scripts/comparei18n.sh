#!/bin/bash          

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

FILE_1=$1
FILE_2=${2:-${DIR}/../src/main/resources/config/i18n/client/ClientMessages.properties}

if ! test -f "$FILE_1"; then
    echo "$FILE_1 doesn't exits"
    if ! test -f "$FILE_2"; then
        echo "$FILE_2 doesn't exits"
        exit 1
    fi
    exit 1
fi

grep -F -f <( \
    comm -13 \
        <(sed -e '/^$/d' -e '/^#/ d' -e 's/^\(.*\)=.*$/\1/' -e 's/=[A-Z].*//' -e 's/=<.*//' $FILE_1 | sort) \
        <(sed -e '/^$/d' -e '/^#/ d' -e 's/^\(.*\)=.*$/\1/' -e 's/=[A-Z].*//' -e 's/=<.*//' $FILE_2 | sort) \
) $FILE_2