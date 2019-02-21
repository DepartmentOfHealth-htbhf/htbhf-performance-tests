# htbhf-performance-tests

Collection of performance tests designed to be run using [Gatling](https://gatling.io/)


## Running the tests from the command line
First make sure the necessary environment variables are set:
 * BIN_DIR (directory to install gatling to)
 * GATLING_FOLDER_NAME (the name of the folder gatling unzips to, e.g. gatling-charts-highcharts-bundle-3.0.1.1)
 * GATLING_URL
 * UI_APP_NAME (the name of the UI application as shown in cloud foundry, e.g. help-to-buy-healthy-food-staging)
 * CF_PUBLIC_DOMAIN
 * PERF_TESTS_DIR the directory where these scripts reside (e.g. the current directory)
 * RESULTS_DIRECTORY - the directory (relative path) to store the results to.
 * BASE_URL - the url of the UI application to run the tests against.
 * PERF_TEST_NUMBER_OF_USERS - the number of users to use in the performance test
 
Then run `./run_performance_tests.sh`

## Running the tests autonomously
`./gradlew clean build shadowJar` will create a 'fat jar' with all the dependencies.
This creates an htbhf-performance-tests-n.n.n-all.jar that can be started (or deployed to cloudfoundry)
and will run all the tests, as long as it is provided with the following environment variables:
```
BASE_URL
PERF_TEST_START_NUMBER_OF_USERS
PERF_TEST_END_NUMBER_OF_USERS
THRESHOLD_95TH_PERCENTILE_MILLIS
THRESHOLD_MEAN_MILLIS
```
Note that in order to prevent cloudfoundry from auto-healing and restarting the application,
the jar will not stop once the tests are complete, but will wait to be stopped externally.