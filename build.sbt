name := "sangria-sqlest"

version := "1.0"

scalaVersion := "2.11.7"

resolvers ++= Seq(
	Resolver.sonatypeRepo("snapshots"),
	Resolver.sonatypeRepo("releases")
)

libraryDependencies ++= Seq(
  "uk.co.jhc" %% "sqlest" % "0.7.4-SNAPSHOT",
  "org.sangria-graphql" %% "sangria" % "0.4.3",
  "com.h2database" % "h2" % "1.4.180",
  "org.flywaydb" % "flyway-core" % "3.2.1",
  "org.sangria-graphql" %% "sangria-play-json" % "0.1.0"
)