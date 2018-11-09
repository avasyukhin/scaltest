organization := "io.github.macs-club"
name := "http4s-rest-api-example"
version := "0.0.1-SNAPSHOT"
scalaVersion := "2.12.3"

val Http4sVersion = "0.16.0a"

libraryDependencies ++= Seq(
 "org.http4s"     %% "http4s-blaze-server" % Http4sVersion,
 "org.http4s"     %% "http4s-circe"        % Http4sVersion,
 "org.http4s"     %% "http4s-dsl"          % Http4sVersion,
 "ch.qos.logback" %  "logback-classic"     % "1.2.1"
)

val DoobieVersion = "0.4.4"

libraryDependencies ++= Seq(
  "org.tpolecat"   %% "doobie-core"         % DoobieVersion,
  "org.tpolecat"   %% "doobie-h2"           % DoobieVersion,
  "org.tpolecat"   %% "doobie-specs2"       % DoobieVersion
)

val CirceVersion = "0.8.0"

libraryDependencies ++= Seq(
  "io.circe"       %% "circe-generic"       % CirceVersion
)
