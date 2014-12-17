import play.PlayImport._
import PlayKeys._

name := """Chess-Online"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(play.PlayScala)

scalaVersion := "2.10.4"

routesImport ++= Seq("scala.language.reflectiveCalls")

scalacOptions := Seq("-encoding", "UTF-8", "-Xlint", "-deprecation", "-unchecked", "-feature", "-language:implicitConversions")

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  javaCore,
  ws,
  "org.json4s" %% "json4s-native" % "3.2.10",
  "org.mongodb" %% "casbah" % "2.7.3",
  "net.liftweb" %% "lift-json" % "2.5.1",
  "ws.securesocial" %% "securesocial" % "master-SNAPSHOT"
)