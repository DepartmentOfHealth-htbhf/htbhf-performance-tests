# htbhf-performance-tests

Collection of performance tests designed to be run using [Gatling](https://gatling.io/)

Currently the tests will ramp up from a low number of new users per second to a higher number of new users per second over a minute,
then sustain that higher number of new users per second for a certain number of minutes.

## Running the tests autonomously
`./gradlew clean build` will create a 'fat jar' with all the dependencies.
This creates an htbhf-performance-tests-n.n.n-all.jar that can be started (or deployed to cloudfoundry)
and will run all the tests, as long as it is provided with the following environment variables:
```
BASE_URL - the url of the UI application to run the tests against.
SESSION_DETAILS_BASE_URL - the url of the session details app (this will need to be started before you run the performance tests)
PERF_TEST_START_NUMBER_OF_USERS - the number of new users per second at the start of the test
PERF_TEST_END_NUMBER_OF_USERS - the number of new users per second at the end of the test
PERF_TEST_SOAK_TEST_DURATION_MINUTES - (default 2) - the number of minutes to maintain that level of load, after ramping up
THRESHOLD_95TH_PERCENTILE_MILLIS - the maximum allowed response time for 95% of the requests, in ms
THRESHOLD_MEAN_MILLIS - the maximum allowed average response time, in ms
```
Note that in order to prevent cloudfoundry from auto-healing and restarting the application,
the jar will not stop once the tests are complete, but will wait to be stopped externally (this is managed by scripts in htbhf-continuous-delivery).

Note also that the session details app must be running for performance tests to succeed (this provides access to the 2fa confirmation code).
See [src/test/session-details-provider/README.md](https://github.com/DepartmentOfHealth-htbhf/htbhf-applicant-web-ui/blob/master/src/test/session-details-provider/README.md)
in htbhf-applicant-web-ui for more details.

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
 * SESSION_DETAILS_BASE_URL - the url of the session details app (this will need to be started before you run the performance tests)
 * PERF_TEST_START_NUMBER_OF_USERS - the number of new users per second at the start of the test
 * PERF_TEST_END_NUMBER_OF_USERS - the number of new users per second at the end of the test
 * PERF_TEST_SOAK_TEST_DURATION_MINUTES - (default 2) - the number of minutes to maintain that level of load, after ramping up
 
Then run `./run_performance_tests.sh`
