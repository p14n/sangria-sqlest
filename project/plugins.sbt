logLevel := Level.Warn

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.0")

//lazy val root = project.in( file(".") ).dependsOn( sbtExtrasPlugin )
//lazy val sbtExtrasPlugin = uri("file:///Users/p14n/dev/tools/sbt-release-extras/")