package uk.gov.dhsc.htbhf

import java.text.NumberFormat
import java.time.LocalDate
import java.util.Random

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.language.postfixOps

class ClaimSimulation extends Simulation {

  val baseURl = sys.env("BASE_URL")
  val numStartUsers = sys.env("PERF_TEST_START_NUMBER_OF_USERS").toInt
  val numEndUsers = sys.env("PERF_TEST_END_NUMBER_OF_USERS").toInt
  val soakTestDuration = sys.env("PERF_TEST_SOAK_TEST_DURATION_MINUTES").toInt
  val responseTimeThresholdFor95thPercentile = sys.env("THRESHOLD_95TH_PERCENTILE_MILLIS").toInt
  val responseTimeThresholdForMean = sys.env("THRESHOLD_MEAN_MILLIS").toInt

  val httpProtocol = http.baseUrl(baseURl)

  private val today: LocalDate = LocalDate.now()

  private val random = new Random
  private val ninoFormat = getNinoFormat

  private def getNinoFormat = {
    val ninoFormat = NumberFormat.getInstance
    ninoFormat.setMaximumFractionDigits(0)
    ninoFormat.setMinimumIntegerDigits(6)
    ninoFormat.setGroupingUsed(false)
    ninoFormat
  }

  private def getRandomAlphabetCharAsString() = {
    val randomChar = (random.nextInt(26) + 'A').asInstanceOf[Char]
    randomChar.toString
  }

  val randomNinos = Iterator.continually(
    // Random number will be accessible in session under variable "OrderRef"
    Map("nino" -> {
      getRandomAlphabetCharAsString + getRandomAlphabetCharAsString + ninoFormat.format(random.nextInt(999999)) + "D"
    })
  )


  val scotland = feed(randomNinos)

    .exec(http("do-you-live-in-scotland-yes")
      .get("/do-you-live-in-scotland")
      .check(
        regex("""<input type="hidden" name="_csrf" value="([^"]+)"""")
          .saveAs("csrf_token1")
      )
    )

    .exec(http("send_do_you_live_in_scotland_yes")
      .post("/do-you-live-in-scotland")
      .formParam("doYouLiveInScotland", "yes")
      .formParam("_csrf", "${csrf_token1}"))


  val fullClaim = feed(randomNinos)

    .exec(http("do-you-live-in-scotland-no")
      .get("/do-you-live-in-scotland")
      .check(
        regex("""<input type="hidden" name="_csrf" value="([^"]+)"""")
          .saveAs("csrf_token2")
      )
    )

    .exec(http("send_do_you_live_in_scotland_no")
      .post("/do-you-live-in-scotland")
      .formParam("doYouLiveInScotland", "no")
      .formParam("_csrf", "${csrf_token2}"))

    .exec(http("send_dob")
      .post("/enter-dob")
      .formParam("dateOfBirth-day", "1")
      .formParam("dateOfBirth-month", "11")
      .formParam("dateOfBirth-year", "1980")
      .formParam("_csrf", "${csrf_token2}"))

    .exec(http("send_are_you_pregnant")
      .post("/are-you-pregnant")
      .formParam("areYouPregnant", "yes")
      .formParam("expectedDeliveryDate-day", today.getDayOfMonth())
      .formParam("expectedDeliveryDate-month", today.getMonthValue())
      .formParam("expectedDeliveryDate-year", today.getYear())
      .formParam("_csrf", "${csrf_token2}")
    )

    .exec(http("send_name")
      .post("/enter-name").formParam("firstName", "David").formParam("lastName", "smith").formParam("_csrf", "${csrf_token2}"))

    .exec(http("send_nino")
      .post("/enter-nino").formParam("nino", "${nino}").formParam("_csrf", "${csrf_token2}"))


    .exec(http("send_card_address")
      .post("/card-address")
      .formParam("addressLine1", "Flat B")
      .formParam("addressLine2", "221 Baker Street")
      .formParam("townOrCity", "London")
      .formParam("postcode", "AA1 1AA")
      .formParam("_csrf", "${csrf_token2}")
    )

    .exec(http("send_phone_number")
      .post("/phone-number")
      .formParam("phoneNumber", "07123456789")
      .formParam("_csrf", "${csrf_token2}")
    )

    .exec(http("send_email_address")
      .post("/email-address")
      .formParam("emailAddress", "test@email.com")
      .formParam("_csrf", "${csrf_token2}")
    )

    .exec(http("choose_channel_for_code")
      .post("/choose-channel-for-code")
      .formParam("chooseChannelForCode", "text")
      .formParam("_csrf", "${csrf_token}")
    )

    .exec(http("accept_terms_and_conditions")
      .post("/terms-and-conditions")
      .formParam("agree", "agree")
      .formParam("_csrf", "${csrf_token2}")
    )

  val scottishScenario = scenario("Scottish Scenario").exec(scotland)
  val fullScenarios = scenario("Full scenarios").exec(fullClaim)

  setUp(
    scottishScenario.inject(rampUsersPerSec(numStartUsers) to numEndUsers during (1 minutes),
      constantUsersPerSec(numEndUsers) during (soakTestDuration minutes)),
    fullScenarios.inject(rampUsersPerSec(numStartUsers) to numEndUsers during (1 minutes),
      constantUsersPerSec(numEndUsers) during (soakTestDuration minutes))
  ).protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.is(100),
      // percentile3 default is 95th percentile, see https://gatling.io/docs/2.3/general/assertions/
      global.responseTime.percentile3.lt(responseTimeThresholdFor95thPercentile),
      global.responseTime.mean.lt(responseTimeThresholdForMean)
    )

}
