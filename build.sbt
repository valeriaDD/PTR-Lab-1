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
  "io.circe" %% "circe-generic-extras" % "0.14.3",
  "com.typesafe.akka" %% "akka-cluster" % "2.7.0",
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "org.postgresql" % "postgresql" % "42.3.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  "com.github.tminglei" %% "slick-pg" % "0.20.3",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.20.3"
)

lazy val root = (project in file("."))
  .settings(
    name := "Lab1"
  )
