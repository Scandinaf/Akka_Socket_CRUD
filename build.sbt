import Dependencies._

lazy val root = (project in file("."))
  .settings(
  inThisBuild(
    List(
      organization := "com.assignment",
      scalaVersion := "2.12.8",
      scalacOptions += "-Ypartial-unification"
    )),
  name := "EG test assignment",
  resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases",
  libraryDependencies ++= Seq(
    Cats.core,
    Akka.akkaHttp,
    Akka.akkaStream,
    Circe.akkaHttpCirce,
    Circe.circeCore,
    Circe.circeGeneric,
    Circe.circeParser,
    Logback.classic,
    Logback.core,
    Akka.akkaHttpTestKit,
    Akka.akkaStreamTestKit,
    TestDependency.scalaTest,
    TestDependency.scalaTestMock,
    TestDependency.akkaTestkit,
    TestDependency.scalactic
  )
)
