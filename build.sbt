import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Hello",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % "0.8.0",
      "io.circe" %% "circe-generic" % "0.8.0",
      "io.circe" %% "circe-parser" % "0.8.0",
    )
  )
