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

/*
git checkout -b update-snapshot
git pull origin develop
e version.sbt
git add version.sbt
git commit -m "Bump version"
git push origin update-snapshot:develop
git checkout develop
git branch -D update-snapshot

git checkout -b release-1.1
e version.sbt
git add version.sbt
git commit -m "Bump version"
git tag 1.1
git push origin --tags


*/
  def updateSnapshotVersionAction: (State) => State = { state: State =>
    val git = state.extract.get(releaseVcs).get.asInstanceOf[Git]
    git.cmd("checkout","-b","update-snapshot") ! state.log
    state.log.info(s"####### Checked out local branch")
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

  def merge: (State) => State = { st: State =>
    val git = st.extract.get(releaseVcs).get.asInstanceOf[Git]
    val curBranch = (git.cmd("rev-parse", "--abbrev-ref", "HEAD") !!).trim
    st.log.info(s"####### Current branch: $curBranch")
    val vs = st.get(versions).getOrElse(sys.error(s"No versions are set! Was this release part executed before Monkey death?"))
    st
  }

val updateSnapshotVersion = ReleaseStep(updateSnapshotVersionAction)

 releaseProcess := Seq[ReleaseStep](
//  checkSnapshotDependencies,
  updateSnapshotVersion
  setReleaseVersion,                      // : ReleaseStep
  commitReleaseVersion,                   // : ReleaseStep, performs the initial git checks
  tagRelease                             // : ReleaseStep
//  extractVersions
//  mergeReleaseVersion
//  setNextVersion
/*  inquireVersions,                        // : ReleaseStep
  runTest,                                // : ReleaseStep
  setReleaseVersion,                      // : ReleaseStep
  commitReleaseVersion,                   // : ReleaseStep, performs the initial git checks
  tagRelease,                             // : ReleaseStep
  publishArtifacts,                       // : ReleaseStep, checks whether `publishTo` is properly set up
  setNextVersion,                         // : ReleaseStep
  commitNextVersion,                      // : ReleaseStep
  pushChanges                             // : ReleaseStep, also checks that an upstream branch is properly configured
*/
)

/*lazy val deploySettings: Seq[Def.Setting[_]] = {
  import ReleaseTransformations._
  import ReleasePlugin.autoImport._
  import sbtrelease.{Git, Utilities, ExtraReleaseCommands}
  import Utilities._
  val deployBranch = "master"
  def merge: (State) => State = { st: State =>
    val git = st.extract.get(releaseVcs).get.asInstanceOf[Git]
    val curBranch = (git.cmd("rev-parse", "--abbrev-ref", "HEAD") !!).trim
    st.log.info(s"####### current branch: $curBranch")
    git.cmd("checkout", deployBranch) ! st.log
    st.log.info(s"####### pull $deployBranch")
    git.cmd("pull") ! st.log
    st.log.info(s"####### merge")
    git.cmd("merge", curBranch, "--no-ff", "--no-edit") ! st.log
    st.log.info(s"####### push")
    git.cmd("push", "origin", s"$deployBranch:$deployBranch") ! st.log
    st.log.info(s"####### checkout $curBranch")
    git.cmd("checkout", curBranch) ! st.log
    st
  }
  lazy val mergeReleaseVersionAction = { st: State =>
    val newState = merge(st)
    newState
  }
  val mergeReleaseVersion = ReleaseStep(mergeReleaseVersionAction)
  publishingSettings ++
    Seq(
      releaseProcess := Seq[ReleaseStep](
        checkSnapshotDependencies
        /*inquireVersions,
        runClean,
        runTest,
        setReleaseVersion,
        commitReleaseVersion,
        pushChanges,
        mergeReleaseVersion,
        tagRelease,
        setNextVersion,
        commitNextVersion,
        pushChanges*/
      )
    )
}*/