package uk.gov.dhsc.htbhf

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class ClaimSimulation extends Simulation {

  val baseURl = System.getenv("BASE_URL")
  val numUsers = System.getenv("PERF_TEST_NUMBER_OF_USERS")

  val httpProtocol = http
    .baseUrl(baseURl)

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
    .exec(http("confirm")
      .post("/confirm").formParam("firstName", "David").formParam("lastName", "smith").formParam("_csrf", "${csrf_token}"))

  setUp(
    scn.inject(atOnceUsers(numUsers.toInt))
  ).protocols(httpProtocol)

}
