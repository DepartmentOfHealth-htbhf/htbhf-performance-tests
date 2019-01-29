package uk.gov.dhsc.htbhf

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class ClaimSimulation extends Simulation {

  val baseURl = System.getenv("BASE_URL")
  val numUsers = System.getenv("PERF_TEST_NUMBER_OF_USERS")

  val httpProtocol = http.baseUrl(baseURl)

  val scn = scenario("ClaimSimulation")
    .exec(http("enter_name_page")
      .get("/enter-name")
      .check(status.is(200))
      .check(
        regex("""<input type="hidden" name="_csrf" value="([^"]+)"""").saveAs("csrf_token")
      )
    )
    .exec(http("send_name")
      .post("/enter-name").formParam("firstName", "David").formParam("lastName", "smith").formParam("_csrf", "${csrf_token}"))

    .exec(http("enter_nino_page")
      .get("/enter-nino")
      .check(status.is(200))
    )
    .exec(http("send_nino")
      .post("/enter-nino").formParam("nino", "QQ123456C").formParam("_csrf", "${csrf_token}"))


    .exec(http("enter_dob_page")
      .get("/enter-dob")
      .check(status.is(200))
    )
    .exec(http("send_dob")
      .post("/enter-dob").formParam("dob-day", "1").formParam("dob-month", "11").formParam("dob-year", "1980").formParam("_csrf", "${csrf_token}"))

    .exec(http("are_you_pregnant")
      .get("/are-you-pregnant")
      .check(status.is(200))
    )
    .exec(http("send_are_you_pregnant")
      .post("/are-you-pregnant").formParam("areYouPregnant", "true").formParam("_csrf", "${csrf_token}"))

  setUp(
    scn.inject(atOnceUsers(numUsers.toInt))
  ).protocols(httpProtocol)

}
