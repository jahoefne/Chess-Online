name := """Chess-Online"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.mongodb" %% "casbah" % "2.7.3",
  "com.novus" %% "salat" % "1.9.9"
)