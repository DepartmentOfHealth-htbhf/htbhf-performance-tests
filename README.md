# htbhf-performance-tests

Collection of performance tests designed to be run using [Gatling](https://gatling.io/)

Currently the tests will ramp up from a low number of new users per second to a higher number of new users per second over a minute,
then sustain that higher number of new users per second for a certain number of minutes.

## Running the tests in the PaaS
`./gradlew clean build` will create a 'fat jar' with all the dependencies.
This creates an htbhf-performance-tests-n.n.n-all.jar that can be started (or deployed to GOV.UK PaaS)
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

See the [`run_performance_tests`](https://github.com/DepartmentOfHealth-htbhf/htbhf-continous-delivery/blob/master/cd_scripts/cd_functions.sh#L171)
function in htbhf-continuous-delivery to see how the performance tests are deployed and run in the CD pipeline.

Note also that the session details app must be running for performance tests to succeed (this provides access to the 2fa confirmation code).
See [src/test/session-details-provider/README.md](https://github.com/DepartmentOfHealth-htbhf/htbhf-applicant-web-ui/blob/master/src/test/session-details-provider/README.md)
in htbhf-applicant-web-ui for more details.

## Running the tests against your local machine
First start the session-details-app locally (see above).
Then run `./run_performance_tests.sh`
