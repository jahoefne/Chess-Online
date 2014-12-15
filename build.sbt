name := """Chess-Online"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(play.PlayScala)

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.json4s" %% "json4s-native" % "3.2.10",
  "org.mongodb" %% "casbah" % "2.7.3",
  "net.liftweb" %% "lift-json" % "2.5.1",
  "ws.securesocial" % "securesocial_2.10" % "3.0-M1-play-2.2.x"
)