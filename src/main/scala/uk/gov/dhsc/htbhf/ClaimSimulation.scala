package uk.gov.dhsc.htbhf

import io.gatling.http.Predef._
import io.gatling.core.Predef._

class ClaimSimulation extends Simulation {

  val baseURl = System.getenv("BASE_URL")
  val numUsers = System.getenv("PERF_TEST_NUMBER_OF_USERS")

  val httpProtocol = http
    .baseUrl(baseURl)
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate, br")
    .acceptLanguageHeader("en-GB,en;q=0.9,de-DE;q=0.8,de;q=0.7,en-US;q=0.6")
    .doNotTrackHeader("1")
    .userAgentHeader("mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.80 Safari/537.36")

  val scn = scenario("ClaimSimulation")
    .exec(http("home_page")
      .get("/")
      .check(
        regex("""<input type="hidden" name="x-csrf-token" value="(.*)" />""").saveAs("csrf_token"))
      )
    .exec(http("send_name")
      .post("/name").formParam("name", "david").formParam("x-csrf-token", "${csrf_token}"))
     .exec(http("confirm")
      .post("/confirm").formParam("x-csrf-token", "${csrf_token}"))

  setUp(
    scn.inject(atOnceUsers(numUsers.toInt))
  ).protocols(httpProtocol)

}