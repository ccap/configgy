name := "configgy"

organization := "net.lag"

version := "2.0.5-nologgy"

resolvers += "ccap-artifactory-scala" at "http://repoman.wicourts.gov/artifactory/scala"

scalaVersion := "2.12.2"

crossScalaVersions := Seq("2.12.2")

publishTo in ThisBuild <<= (version) { version: String =>
  val repo =
    if (version.trim.endsWith("SNAPSHOT"))
      "CCAP Snapshots" at "http://repoman.wicourts.gov/artifactory/libs-snapshot-local"
    else
      "CCAP Releases" at "http://repoman.wicourts.gov/artifactory/libs-release-local"
  Some(repo)
}

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6"

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
