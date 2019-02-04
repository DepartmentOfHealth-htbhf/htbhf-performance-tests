package uk.gov.dhsc.htbhf

import java.time.LocalDate

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

import scala.language.postfixOps

class ClaimSimulation extends Simulation {

  val baseURl = System.getenv("BASE_URL")
  val numStartUsers = System.getenv("PERF_TEST_START_NUMBER_OF_USERS").toInt
  val numEndUsers = System.getenv("PERF_TEST_END_NUMBER_OF_USERS").toInt
  val responseTimeThresholdFor95thPercentile = System.getenv("THRESHOLD_95TH_PERCENTILE_MILLIS").toInt
  val responseTimeThresholdForMean = System.getenv("THRESHOLD_MEAN_MILLIS").toInt

  val httpProtocol = http.baseUrl(baseURl)

  private val today: LocalDate = LocalDate.now()

  val scn = scenario("ClaimSimulation")
    .exec(http("enter_name_page")
      .get("/enter-name")
      .check(
        regex("""<input type="hidden" name="_csrf" value="([^"]+)"""").saveAs("csrf_token")
      )
    )
    .exec(http("send_name")
      .post("/enter-name").formParam("firstName", "David").formParam("lastName", "smith").formParam("_csrf", "${csrf_token}"))

    .exec(http("send_nino")
      .post("/enter-nino").formParam("nino", "QQ123456C").formParam("_csrf", "${csrf_token}"))


    .exec(http("send_dob")
      .post("/enter-dob")
      .formParam("dateOfBirth-day", "1")
      .formParam("dateOfBirth-month", "11")
      .formParam("dateOfBirth-year", "1980")
      .formParam("_csrf", "${csrf_token}"))

    .exec(http("send_are_you_pregnant")
      .post("/are-you-pregnant")
      .formParam("areYouPregnant", "yes")
      .formParam("expectedDeliveryDate-day", today.getDayOfMonth())
      .formParam("expectedDeliveryDate-month", today.getMonthValue())
      .formParam("expectedDeliveryDate-year", today.getYear)
      .formParam("_csrf", "${csrf_token}")
    )

    .exec(http("send_card_address")
      .post("/card-address")
      .formParam("addressLine1", "Flat B")
      .formParam("addressLine2", "221 Baker Street")
      .formParam("townOrCity", "London")
      .formParam("postcode", "AA1 1AA")
      .formParam("_csrf", "${csrf_token}")
    )

    .exec(http("submit")
      .post("/check")
      .formParam("_csrf", "${csrf_token}"))

  setUp(
    scn.inject(rampUsersPerSec(numStartUsers) to numEndUsers during (1 minutes) randomized)
  ).protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.is(100),
      // percentile3 default is 95th percentile, see https://gatling.io/docs/2.3/general/assertions/
      global.responseTime.percentile3.lt(responseTimeThresholdFor95thPercentile),
      global.responseTime.mean.lt(responseTimeThresholdForMean)
    )

}
