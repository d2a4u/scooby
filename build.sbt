name := "scooby"

version := "0.1"

scalaVersion := "2.12.8"

scalacOptions += "-Ypartial-unification"

libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core" % "0.6.0",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.h2database" % "h2" % "1.4.199" % "test"
)
