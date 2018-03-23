import sbt.url

sbtVersion := "1.0.4"

organization := "com.github.thanhtien522"

name := "scala-kafka-client"

version := "0.1.0"

scalaVersion := "2.11.11"

isSnapshot := true

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

resolvers += Resolver.bintrayRepo("cakesolutions", "maven")

libraryDependencies ++= Seq(
  "com.twitter" % "util-core_2.11" % "6.37.0",
  "net.cakesolutions" %% "scala-kafka-client" % "0.10.0.0" exclude("org.slf4j", "log4j-over-slf4j"),
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

pomIncludeRepository := { _ => false }

licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.html"))

publishArtifact in(Test, packageBin) := false

publishMavenStyle := true

homepage := Some(url("https://github.com/thanhtien522/scala-kafka-client"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/thanhtien522/scala-kafka-client"),
    "scm:git@github.com:thanhtien522/scala-kafka-client.git"
  )
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

developers := List(
  Developer(id = "thanhtien522", name = "Tien Nguyen", email = "thanhtien522@gmail.com", url = url("https://github.com/thanhtien522"))
)

