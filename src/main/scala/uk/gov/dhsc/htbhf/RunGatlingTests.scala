package uk.gov.dhsc.htbhf

import java.io.File

import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder
import org.zeroturnaround.zip.ZipUtil

object RunGatlingTests {
  def main(args: Array[String]): Unit = {
    val resultsDir = "./performance-test-results"
    val resultsZip = new File("./performance-test-results.zip")
    val props = new GatlingPropertiesBuilder
    val config = props
      .simulationClass("uk.gov.dhsc.htbhf.ClaimSimulation")
      .runDescription("Performance tests")
      .resultsDirectory(resultsDir)
      .build
    Gatling.fromMap(config)
    System.out.println("Finished running gatling tests - zipping to " + resultsZip.getAbsolutePath)
    ZipUtil.pack(new File(resultsDir), resultsZip)
    // cloud foundry will try to restart the application if it quits (re-running the performance tests)
    // so we need to keep going until stopped externally
    while ( { true }) {
      Thread.sleep(60000)
      System.out.println("Please stop the application using 'cf stop'")
    }

  }
}
