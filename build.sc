import mill._
import mill.scalalib._

object root extends RootModule with ScalaModule {
  def scalaVersion = "3.3.0"

  private val circeVersion = "0.14.1"

  def ivyDeps = Agg(
    ivy"org.typelevel::cats-effect:3.4.6",
    ivy"org.typelevel::cats-core:2.9.0",
    ivy"com.softwaremill.sttp.client3::core:3.8.11",
    ivy"com.softwaremill.sttp.client3::circe:3.8.11",
    ivy"com.softwaremill.sttp.client3::cats:3.8.11",
    ivy"com.lihaoyi::pprint:0.7.0",
    ivy"com.softwaremill.sttp.client3::slf4j-backend:3.8.11",
    ivy"org.slf4j:slf4j-simple:2.0.6",
    ivy"com.olvind.tui::tui:0.0.5",
    ivy"com.monovore::decline:2.4.1",
    ivy"com.monovore::decline-effect:2.4.1",
    ivy"io.circe::circe-core:$circeVersion",
    ivy"io.circe::circe-generic:$circeVersion",
    ivy"io.circe::circe-parser:$circeVersion"
  )

  object test extends ScalaTests {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.15"
    )

    def testFramework = "org.scalatest.tools.Framework"
  }
}
