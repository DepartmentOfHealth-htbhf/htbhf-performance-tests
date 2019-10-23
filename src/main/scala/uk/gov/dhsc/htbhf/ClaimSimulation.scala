package uk.gov.dhsc.htbhf

import java.net.URI
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Random

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.parsing.json.JSON

/**
 * Runs performance tests against applicant UI.
 * The version of the performance tests to run is specified in test_version.properties in htbhf-applicant-web-ui.
 */
class ClaimSimulation extends Simulation {

  val baseURl = sys.env("BASE_URL")
  val sessionDetailsBaseURl = sys.env("SESSION_DETAILS_BASE_URL")
  var sessionDetailsDomain = new URI(sessionDetailsBaseURl).getHost
  val numStartUsers = sys.env.getOrElse("PERF_TEST_START_NUMBER_OF_USERS", "1").toInt
  val numEndUsers = sys.env.getOrElse("PERF_TEST_END_NUMBER_OF_USERS", "20").toInt
  val rampUpUsersDuration = sys.env.getOrElse("PERF_TEST_RAMP_UP_USERS_DURATION", "2").toInt
  val soakTestDuration = sys.env.getOrElse("PERF_TEST_SOAK_TEST_DURATION_MINUTES", "2").toInt
  val responseTimeThresholdFor95thPercentile = sys.env.getOrElse("THRESHOLD_95TH_PERCENTILE_MILLIS", "2000").toInt
  val responseTimeThresholdForMean = sys.env.getOrElse("THRESHOLD_MEAN_MILLIS", "500").toInt

  val httpProtocol = http.baseUrl(baseURl)

  private val today: LocalDate = LocalDate.now()

  private val random = new Random
  private val ninoFormat = getNinoFormat

  private val dateAYearAgo: LocalDate = LocalDate.now().minusYears(1)

  private val features: Map[String, Boolean] = JSON.parseFull(sys.env.getOrElse("FEATURE_TOGGLES", "{}")).get.asInstanceOf[Map[String, Boolean]]

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

    .exec(http("scotland-yes")
      .get("/scotland")
      .check(
        regex("""<input type="hidden" name="_csrf" value="([^"]+)"""")
          .saveAs("csrf_token1")
      )
    )
    .pause(1, 2)

    .exec(http("send_do_you_live_in_scotland_yes")
      .post("/scotland")
      .formParam("scotland", "yes")
      .formParam("_csrf", "${csrf_token1}"))
    .pause(1, 2)


  val fullClaim = feed(randomNinos)

    .exec(http("scotland-no")
      .get("/scotland")
      .check(
        regex("""<input type="hidden" name="_csrf" value="([^"]+)"""")
          .saveAs("csrf_token2")
      ).check(headerRegex(HttpHeaderNames.SetCookie, "htbhf.sid=(.*)").saveAs("sid"))
    )

    .exec(addCookie((Cookie("htbhf.sid", "${sid}").withDomain(sessionDetailsDomain))))

    .exec(http("send_do_you_live_in_scotland_no")
      .post("/scotland")
      .formParam("scotland", "no")
      .formParam("_csrf", "${csrf_token2}"))
    .pause(1, 2)

    .exec(http("send_dob")
      .post("/date-of-birth")
      .formParam("dateOfBirth-day", "1")
      .formParam("dateOfBirth-month", "11")
      .formParam("dateOfBirth-year", "1980")
      .formParam("_csrf", "${csrf_token2}"))
    .pause(1, 2)

    .exec(http("send_do_you_have_children")
      .post("/do-you-have-children")
      .formParam("doYouHaveChildren", "yes")
      .formParam("_csrf", "${csrf_token2}"))
    .pause(1, 2)

    .exec(http("send_children_dob")
      .post("/child-date-of-birth")
      .formParam("childName-1", "Joe")
      .formParam("childDob-1-day", dateAYearAgo.getDayOfMonth())
      .formParam("childDob-1-month", dateAYearAgo.getMonthValue())
      .formParam("childDob-1-year", dateAYearAgo.getYear())
      .formParam("_csrf", "${csrf_token2}"))
    .pause(1, 2)

    .exec(http("send_are_you_pregnant")
      .post("/are-you-pregnant")
      .formParam("areYouPregnant", "yes")
      .formParam("expectedDeliveryDate-day", today.getDayOfMonth())
      .formParam("expectedDeliveryDate-month", today.getMonthValue())
      .formParam("expectedDeliveryDate-year", today.getYear())
      .formParam("_csrf", "${csrf_token2}")
    )
    .pause(1, 2)

    .exec(http("send_name")
      .post("/name").formParam("firstName", "David").formParam("lastName", "smith").formParam("_csrf", "${csrf_token2}"))
    .pause(1, 2)

    .exec(http("send_nino")
      .post("/national-insurance-number").formParam("nino", "${nino}").formParam("_csrf", "${csrf_token2}"))
    .pause(1, 2)

    // TODO Randomly have some users use address lookup and others use manual address.
    // See https://gatling.io/docs/current/general/scenario/#scenario-conditions
    .doIfEquals(features.getOrElse("ADDRESS_LOOKUP_ENABLED", false), true) {
      exec(
        http("send_postcode")
          .post("/postcode")
          .formParam("postcode", "AA1 1AA")
          .formParam("_csrf", "${csrf_token2}")
      )
      .pause(1, 2)
    }

    .doIfEquals(features.getOrElse("ADDRESS_LOOKUP_ENABLED", false), true) {
      exec(
        http("select_address")
          .post("/select-address")
          .formParam("_csrf", "${csrf_token2}")
      )
        .pause(1, 2)
    }

    .exec(
      http("send_manual_address")
        .post("/manual-address")
        .formParam("addressLine1", "Flat B")
        .formParam("addressLine2", "221 Baker Street")
        .formParam("townOrCity", "London")
        .formParam("postcode", "AA1 1AA")
        .formParam("county", "Devon")
        .formParam("_csrf", "${csrf_token2}")
    )
    .pause(1, 2)

    .exec(http("send_phone_number")
      .post("/phone-number")
      .formParam("phoneNumber", "07123456789")
      .formParam("_csrf", "${csrf_token2}")
    )
    .pause(1, 2)

    .exec(http("send_email_address")
      .post("/email-address")
      .formParam("emailAddress", "test@email.com")
      .formParam("_csrf", "${csrf_token2}")
    )
    .pause(1, 2)

    .exec(http("send_code")
      .post("/send-code")
      .formParam("channelForCode", "text")
      .formParam("_csrf", "${csrf_token2}")
    )
    .pause(1, 2)

    .exec(http("get unique code from session")
      .get(sessionDetailsBaseURl + "/session-details/confirmation-code")
      .check(bodyString.saveAs("confirmationCode"))
    )
    .pause(1, 2)

    .exec(http("enter_code")
      .post("/enter-code")
      .formParam("confirmationCode", "${confirmationCode}")
      .formParam("_csrf", "${csrf_token2}")
    )
    .pause(1, 2)

    .exec(http("accept_terms_and_conditions")
      .post("/terms-and-conditions")
      .formParam("agree", "agree")
      .formParam("_csrf", "${csrf_token2}")
    )

  val scottishScenario = scenario("Scottish Scenario").exec(scotland)
  val fullScenarios = scenario("Full scenarios").exec(fullClaim)

  setUp(
    scottishScenario.inject(rampUsersPerSec(numStartUsers) to numEndUsers during (rampUpUsersDuration minutes),
      constantUsersPerSec(numEndUsers) during (soakTestDuration minutes)),
    fullScenarios.inject(rampUsersPerSec(numStartUsers) to numEndUsers during (rampUpUsersDuration minutes),
      constantUsersPerSec(numEndUsers) during (soakTestDuration minutes))
  ).protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.is(100),
      // percentile3 default is 95th percentile, see https://gatling.io/docs/2.3/general/assertions/
      global.responseTime.percentile3.lt(responseTimeThresholdFor95thPercentile),
      global.responseTime.mean.lt(responseTimeThresholdForMean)
    )

}
