import SttpOpenApiCodegenPlugin._

ThisBuild / scalaVersion := "3.2.1"

val circeVersion = "0.14.1"

lazy val root = (project in file("."))
  .enablePlugins(SttpOpenApiCodegenPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.4.6",
      "org.typelevel" %% "cats-core" % "2.9.0",
      "com.softwaremill.sttp.client3" %% "core" % "3.8.11",
      "com.softwaremill.sttp.client3" %% "circe" % "3.8.11",
      "com.softwaremill.sttp.client3" %% "cats" % "3.8.11",
      "com.lihaoyi" %% "pprint" % "0.7.0",
      "com.softwaremill.sttp.client3" %% "slf4j-backend" % "3.8.11",
      "org.slf4j" % "slf4j-simple" % "2.0.6",
      "com.olvind.tui" %% "tui" % "0.0.5",
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
    ).map(_ % circeVersion)
  )
