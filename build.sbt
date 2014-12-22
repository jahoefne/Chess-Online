import play.PlayImport._
import PlayKeys._

name := """Chess-Online"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(play.PlayScala)

scalaVersion := "2.10.4"

routesImport ++= Seq("scala.language.reflectiveCalls")

scalacOptions := Seq("-encoding", "UTF-8", "-Xlint", "-deprecation", "-unchecked", "-feature", "-language:implicitConversions")

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  javaCore,
  ws,
  "org.json4s" %% "json4s-native" % "3.2.10",
  "org.mongodb" %% "casbah" % "2.7.3",
  "net.liftweb" %% "lift-json" % "2.5.1",
  "com.novus" %% "salat" % "1.9.9",
  "com.typesafe.akka" %% "akka-contrib" % "2.4-SNAPSHOT",
  "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT",
  "com.typesafe.akka" %% "akka-kernel" % "2.4-SNAPSHOT",
  "com.typesafe.akka" %% "akka-cluster" % "2.4-SNAPSHOT",
  "ws.securesocial" %% "securesocial" % "master-SNAPSHOT"
)