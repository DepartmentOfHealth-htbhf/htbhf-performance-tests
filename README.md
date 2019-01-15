# htbhf-performance-tests

Collection of performance tests designed to be run using [Gatling](https://gatling.io/)


## Running the tests
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