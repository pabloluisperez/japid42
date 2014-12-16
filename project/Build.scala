import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._

object ApplicationBuild extends Build {

  val appName         = "japid42"
  val appVersion      = "0.10" // note: update the version name in the JapidRenderer to match this.

  val appDependencies = Seq(
    javaCore
    ,cache
    ,"org.apache.commons" % "commons-email" % "1.2"
    ,"org.apache.commons" % "commons-lang3" % "3.3.2"
    ,"org.eclipse.tycho" % "org.eclipse.jdt.core" % "3.10.0.v20140604-1726"
    ,"com.google.code.javaparser" % "javaparser" % "1.0.11"
    ,"javax.ws.rs" % "jsr311-api" % "1.1.1"
  )

  val main = Project(appName, file(".")).enablePlugins(play.PlayJava).settings(
    version := appVersion,
    libraryDependencies ++= appDependencies,
    javacOptions in Compile ++= Seq("-source", "1.6", "-target", "1.6"),
//    scalaVersion := "2.10.4"
    scalaVersion := "2.11.1" 
  )  

  
  publishTo := Some(Resolver.file("file",  new File( "C:\\Portables\\Despliegues" )) )
  publishTo <<= version { (v: String) =>
	  val nexus = "https://oss.sonatype.org/"
	  if (v.trim.endsWith("SNAPSHOT")) 
	    Some("snapshots" at nexus + "snapshots")
	  else                             
	    Some("releases" at nexus + "releases")
  }
  
  	organization := "com.github.branaway"

	publishMavenStyle := true
	
	publishArtifact in Test := false
	
	pomIncludeRepository := { x => false }
	
	pomExtra := (
		  <url>http://branaway.github.com/japid42.hrml</url>
		  <licenses>
    <license>
      <name>BSD-style</name>
      <url>http://www.opensource.org/licenses/bsd-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
		  <scm>
    <url>git@github.com:pabloluisperez/japid42.git</url>
    <connection>scm:git:git@github.com:pabloluisperez/japid42.git</connection>
  </scm>
		  <developers>
    <developer>
      <id>pabloluisperez</id>
      <name>Pablo Perez</name>
      <url>http://hablandodeweb.com</url>
    </developer>
  </developers>
	)

}
