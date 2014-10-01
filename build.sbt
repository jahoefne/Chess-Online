name := """Chess-Online"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.json4s" %% "json4s-native" % "3.2.10",
  "org.mongodb" %% "casbah" % "2.7.3"
)