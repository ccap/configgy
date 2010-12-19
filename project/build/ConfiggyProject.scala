import sbt._
import com.twitter.sbt._

class ConfiggyProject(info: ProjectInfo) extends StandardProject(info) with SubversionPublisher {
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.6" % "test"

  override def pomExtra =
    (<name>Configgy</name>
    <description>Configgy logging removed</description>
    <url>http://github.com/derekjw/configgy</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        <distribution>repo</distribution>
      </license>
    </licenses>)

  override def subversionRepository = Some("http://svn.local.twitter.com/maven-public")
}
