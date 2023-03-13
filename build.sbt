ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.7.0",
  "com.typesafe.akka" %% "akka-stream" % "2.7.0",
  "com.typesafe.akka" %% "akka-http" % "10.5.0",
  "de.heikoseeberger" %% "akka-http-play-json" % "1.39.2",
  "com.typesafe.akka" %% "akka-testkit" % "2.7.0" % Test,
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  "com.typesafe.play" %% "play-streams" % "2.8.18",
  "io.circe" %% "circe-core" % "0.14.4",
  "io.circe" %% "circe-parser" % "0.14.4",
  "io.circe" %% "circe-generic-extras" % "0.14.3"
)

lazy val root = (project in file("."))
  .settings(
    name := "Lab1"
  )
