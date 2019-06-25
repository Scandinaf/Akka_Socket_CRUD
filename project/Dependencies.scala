import sbt._

object Dependencies {

  object TestDependency {
    val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % Test
    val scalactic = "org.scalactic" %% "scalactic" % "3.0.5"
    val scalaTestMock = "org.scalamock" %% "scalamock" % "4.2.0" % Test
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % "2.5.23" % Test
  }

  object Akka {
    private val akkaHttpVersion = "10.1.8"
    private val akkaVersion = "2.5.19"
    val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
    val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
    val akkaHttpTestKit = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
    val akkaStreamTestKit = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
  }

  object Circe {
    private val circeVersion = "0.11.1"
    private val akkaCirceVersion = "1.25.2"
    val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % akkaCirceVersion
    val circeCore = "io.circe" %% "circe-core" % circeVersion
    val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
    val circeParser = "io.circe" %% "circe-parser" % circeVersion
  }

  object Logback {
    private val version = "1.2.3"
    val core = "ch.qos.logback" % "logback-core" % version
    val classic = "ch.qos.logback" % "logback-classic" % version
  }

  object Cats {
    private val version = "1.6.1"
    val core = "org.typelevel" %% "cats-core" % version
  }

}
