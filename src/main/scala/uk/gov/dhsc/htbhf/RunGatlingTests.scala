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
      .runDescription("Web-UI performance tests")
      .resultsDirectory(resultsDir)
      .build
    try {
      val result = Gatling.fromMap(config)
      // write the result to system.out so we can identify success or failure from the logs
      System.out.println("Finished running gatling tests with feature toggles: " + sys.env.get("FEATURE_TOGGLES") + " - result=" + result)
      System.out.println("Zipping reports to " + resultsZip.getAbsolutePath)
      ZipUtil.pack(new File(resultsDir), resultsZip)
    } catch {
      case e: Exception => {
        System.out.println("Finished running gatling tests - result=1")
        e.printStackTrace()
      }
    }
    // cloud foundry will try to restart the application if it quits (re-running the performance tests)
    // so we need to keep going until stopped externally
    while ( { true }) {
      Thread.sleep(60000)
      System.out.println("Please stop the application using 'cf stop'")
    }

  }
}
