#!/bin/bash

export PATH=$PATH:${BIN_DIR}
export CURRENT_DIR=`pwd`

check_variable_is_set(){
    if [[ -z ${!1} ]]; then
        echo "$1 must be set and non empty"
        exit 1
    fi
}

check_variable_is_set BIN_DIR
check_variable_is_set GATLING_FOLDER_NAME
check_variable_is_set GATLING_URL
check_variable_is_set CF_API
check_variable_is_set CF_USER
check_variable_is_set CF_PASS
check_variable_is_set CF_ORG
check_variable_is_set CF_SPACE
check_variable_is_set CF_PUBLIC_DOMAIN
check_variable_is_set RESULTS_DIRECTORY
check_variable_is_set PERF_TESTS_DIR

export CF_DIR=${CURRENT_DIR}/cloud_foundry
/bin/bash ${CURRENT_DIR}/install_cf_cli.sh

if [[ ! -e ${BIN_DIR}/${GATLING_FOLDER_NAME} ]]; then
  echo "Downloading gatling"
  mkdir -p ${BIN_DIR}
  cd ${BIN_DIR}
  wget -q -O gatling.zip ${GATLING_URL}
  unzip -q -o gatling.zip
  rm gatling.zip
  cd ${CURRENT_DIR}
fi

echo "Logging into cloud foundry with api:$CF_API, org:$CF_ORG, space:$CF_SPACE with user:$CF_USER"
cf login -a ${CF_API} -u ${CF_USER} -p "${CF_PASS}" -s ${CF_SPACE} -o ${CF_ORG}

#echo "# creating a temporary (public) route to the app"
ROUTE_PREFIX=$(cat /dev/urandom | tr -dc 'a-z' | fold -w 16 | head -n 1)
cf map-route ${UI_APP_NAME} ${CF_PUBLIC_DOMAIN} --hostname ${ROUTE_PREFIX}
export BASE_URL="https://$ROUTE_PREFIX.${CF_PUBLIC_DOMAIN}/"

${BIN_DIR}/${GATLING_FOLDER_NAME}/bin/gatling.sh -sf ${PERF_TESTS_DIR}/uk/gov/dhsc/htbhf --run-description "Performance tests" --results-folder ${RESULTS_DIRECTORY}

cf delete-route -f ${CF_PUBLIC_DOMAIN} --hostname=${ROUTE_PREFIX}
