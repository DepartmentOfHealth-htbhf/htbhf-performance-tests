#!/bin/bash

if [[ ! -e ${BIN_DIR}/${GATLING_FOLDER_NAME} ]]; then
  echo "Downloading gatling"
  mkdir -p ${BIN_DIR}
  cd ${BIN_DIR}
  wget -q -O gatling.zip ${GATLING_URL}
  unzip -q -o gatling.zip
  rm gatling.zip
  cd ..
fi

#echo "# creating a temporary (public) route to the app"
ROUTE_PREFIX=$(cat /dev/urandom | tr -dc 'a-z' | fold -w 16 | head -n 1)
cf map-route ${UI_APP_NAME} ${CF_PUBLIC_DOMAIN} --hostname ${ROUTE_PREFIX}
export BASE_URL="https://$ROUTE_PREFIX.${CF_PUBLIC_DOMAIN}/help-to-buy-healthy-foods"


RESULTS_DIRECTORY=`pwd`/${PERF_TESTS_RESULTS_DIRECTORY}

${BIN_DIR}/${GATLING_FOLDER_NAME}/bin/gatling.sh -sf uk/gov/dhsc/htbhf --run-description "Performance tests" --results-folder ${RESULTS_DIRECTORY}

cf delete-route -f ${CF_PUBLIC_DOMAIN} --hostname=${ROUTE_PREFIX}