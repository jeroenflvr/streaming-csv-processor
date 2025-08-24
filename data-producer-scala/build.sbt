name := "data-producer-scala"

version := "1.0"

scalaVersion := "2.13.14"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "3.7.0",
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  "org.mockito" % "mockito-core" % "5.2.0" % Test
)

testFrameworks += new TestFramework("org.scalatest.tools.Framework")
