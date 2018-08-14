#!/bin/bash

readarray -t buckets < /opt/couchbase/buckets.txt

curl -v http://localhost:8091/node/controller/setupServices -d services=kv%2Cn1ql%2Cindex &&
curl -v http://localhost:8091/settings/web -d port=8091 -d username=${COUCHBASE_USERNAME} -d password=${COUCHBASE_PASSWORD} &&

for bucket in "${buckets[@]}"
    do
        echo $bucket
        curl -X POST -u ${COUCHBASE_USERNAME}:${COUCHBASE_PASSWORD} -d "name=$bucket" -d 'ramQuotaMB=100' http://localhost:8091/pools/default/buckets
    done
sleep 10 &&

curl -u ${COUCHBASE_USERNAME}:${COUCHBASE_PASSWORD} -X POST http://localhost:8091/settings/indexes -d 'storageMode=memory_optimized'
