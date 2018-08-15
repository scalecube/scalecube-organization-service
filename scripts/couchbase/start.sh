#!/bin/bash

# Start couchbase in the bg
/entrypoint.sh couchbase-server &

check_db() {
  curl --silent http://localhost:8091/pools > /dev/null
  echo $?
}

until [[ $(check_db) = 0 ]]; do
  sleep 1
done

/opt/couchbase/init.sh


(while read line; do
  eval "curl -u ${COUCHBASE_USERNAME}:${COUCHBASE_PASSWORD} -v http://localhost:8093/query/service -H 'content-type: text/plain' -d '$line'"
done) < /opt/couchbase/dml.txt | grep -v "^$"

wait
