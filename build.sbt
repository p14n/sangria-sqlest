name := "sangria-sqlest"

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

credentials += Credentials("Sonatype Nexus Repository Manager", "nexus.jhc.co.uk", "dev", "jhcjhc")

publishTo := {
	val nexus = "http://nexus.jhc.co.uk/nexus/"

    if (isSnapshot.value) {
        Some(Resolver.url("snapshots", new URL(nexus + "content/repositories/snapshots")))
    } else {
        Some(Resolver.url("releases", new URL(nexus + "content/repositories/releases")))
    }
}


import ReleaseTransformations._
import ReleasePlugin.autoImport._
import sbtrelease.{Git, Utilities, ExtraReleaseCommands}
import Utilities._
import ReleaseKeys._

  def incVal = (s:String) => String.valueOf(Integer.parseInt(s)+1)
  def incMinor = (a:Array[String]) => { Array(a(0),incVal(a(1)),a(2))}

  def extractVersions: (State) => State = { st: State =>
    val file = st.extract.get(releaseVersionFile)
    val extracted = IO.readLines(file) map {
    	line => if(!line.isEmpty){
    	  "\"(.*)\"".r.findFirstIn(line) flatMap { ss =>
    	    val ssVersion = ss.replace("\"","")
			st.log.info(s"####### Current version: $ssVersion")
    	    if(ssVersion.endsWith("-SNAPSHOT")){
    	      val relVersion = ssVersion.replace("-SNAPSHOT","")
    	      val nextVersion = incMinor(relVersion.split("\\.")).mkString(".") + "-SNAPSHOT"
			  st.log.info(s"####### Release version: $relVersion, next version: $nextVersion")
			  Some( (relVersion,nextVersion) )
    	    } else {
			  st.log.error("####### Non-snapshot version found in "+line)
   	    	  None
    	    }
    	  }
    	} else None
    } flatten

    if(extracted.isEmpty) 
    	st 
    	else 
    	st.put(versions,extracted(0))
  }

  def updateSnapshotVersionAction: (State) => State = { state: State =>
    val git = state.extract.get(releaseVcs).get.asInstanceOf[Git]
    git.cmd("checkout","-b","update-snapshot") ! state.log
    state.log.info(s"####### Checked out local branch update-snapshot")
    git.cmd("pull","origin","develop") ! state.log
    val st = extractVersions(state)
    val vs = st.get(versions)
    val newState = if(vs.isDefined){
    	val afterWrite = commitNextVersion(setNextVersion(st))
	    git.cmd("push","origin","update-snapshot:develop") ! afterWrite.log
	    git.cmd("checkout","develop") ! afterWrite.log
    	git.cmd("branch","-D","update-snapshot") ! afterWrite.log
    	afterWrite
    } else {
	    git.cmd("checkout","develop") ! st.log
    	git.cmd("branch","-D","update-snapshot") ! st.log
    	sys.error(s"No versions are set! Could not determine from HEAD - see previous log entries")
    	st
    }
    newState
  }

val updateSnapshotVersion = ReleaseStep(updateSnapshotVersionAction)

 releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  updateSnapshotVersion,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts
)
