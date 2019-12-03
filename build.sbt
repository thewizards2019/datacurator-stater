val ScalatraVersion = "2.6.3"

organization := "com.dc"

name := "Data-Curator"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.7"

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % ScalatraVersion,
  "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.9.v20180320" % "container",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "org.scalatra" %% "scalatra-json" % ScalatraVersion,
  "org.json4s"   %% "json4s-jackson" % "3.5.2",
  "javax.ws.rs" % "javax.ws.rs-api" % "2.1" artifacts( Artifact("javax.ws.rs-api", "jar", "jar")), // this is a workaround for https://github.com/jax-rs/api/issues/571
  "org.apache.kafka" %% "kafka-streams-scala" % "2.0.0",
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.9.v20180320" % "container;compile",
  //"org.json4s"   %% "json4s-native" % "3.5.2",
  "com.github.nscala-time" %% "nscala-time" % "2.20.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7" % "runtime",
)

enablePlugins(SbtTwirl)
enablePlugins(ScalatraPlugin)
enablePlugins(PackPlugin)
// publishArtifact := false
