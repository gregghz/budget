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
      "com.lihaoyi" %% "pprint" % "0.7.0"
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  )
