logLevel := Level.Warn

lazy val root = project.in( file(".") ).dependsOn( sbtExtrasPlugin )
lazy val sbtExtrasPlugin = uri("file:///Users/p14n/dev/tools/sbt-release-extras/")