#!/bin/bash

export PATH=$PATH:${BIN_DIR}
export CURRENT_DIR=`pwd`

check_variable_is_set(){
    if [[ -z ${!1} ]]; then
        echo "$1 must be set and non empty"
        exit 1
    fi
}

# rename RunGatlingTests.scala so that Gatling doesn't try to compile it
mv src/main/scala/uk/gov/dhsc/htbhf/RunGatlingTests.scala src/main/scala/uk/gov/dhsc/htbhf/RunGatlingTests.scala.tmp

export BIN_DIR="./bin"
export GATLING_FOLDER_NAME="gatling-charts-highcharts-bundle-3.0.1.1"
export GATLING_URL="https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/3.0.1.1/gatling-charts-highcharts-bundle-3.0.1.1-bundle.zip"
export PERFORMANCE_RESULTS_DIRECTORY="./performance-test-results"
export PERF_TEST_START_NUMBER_OF_USERS="1"
export PERF_TEST_END_NUMBER_OF_USERS="25"
export PERF_TEST_SOAK_TEST_DURATION_MINUTES="1"
export THRESHOLD_95TH_PERCENTILE_MILLIS="2000"
export THRESHOLD_MEAN_MILLIS="800"

if [[ ! -e ${BIN_DIR}/${GATLING_FOLDER_NAME} ]]; then
  echo "Downloading gatling"
  mkdir -p ${BIN_DIR}
  cd ${BIN_DIR}
  wget -q -O gatling.zip ${GATLING_URL}
  unzip -q -o gatling.zip
  rm gatling.zip
  cd ${CURRENT_DIR}
fi

export BASE_URL="http://localhost:8080"
export SESSION_DETAILS_BASE_URL="http://localhost:8081/"

echo "${BIN_DIR}/${GATLING_FOLDER_NAME}/bin/gatling.sh -sf src/main/scala/uk/gov/dhsc/htbhf --run-description \"Performance tests\" --results-folder ${PERFORMANCE_RESULTS_DIRECTORY}"
${BIN_DIR}/${GATLING_FOLDER_NAME}/bin/gatling.sh -sf src/main/scala/uk/gov/dhsc/htbhf --run-description "Performance tests" --results-folder ${PERFORMANCE_RESULTS_DIRECTORY}

# rename RunGatlingTests.scala
mv src/main/scala/uk/gov/dhsc/htbhf/RunGatlingTests.scala.tmp src/main/scala/uk/gov/dhsc/htbhf/RunGatlingTests.scala
