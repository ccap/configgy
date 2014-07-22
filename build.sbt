name := "configgy"

organization := "net.lag"

version := "2.0.4-nologgy"

resolvers += "ccap-artifactory-scala" at "http://repo.wicourts.gov/artifactory/scala"

crossScalaVersions := Seq("2.11.1")

publishTo in ThisBuild <<= (version) { version: String =>
  val repo =
    if (version.trim.endsWith("SNAPSHOT"))
      "CCAP Snapshots" at "http://repo.wicourts.gov/artifactory/libs-snapshot-local"
    else
      "CCAP Releases" at "http://repo.wicourts.gov/artifactory/libs-release-local"
  Some(repo)
}

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1"

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
