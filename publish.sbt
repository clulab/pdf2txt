import org.clulab.sbt.BuildUtils

val publication = "pdf2txt"
val publicationNorm = "pdf2txt"

ThisBuild / developers := List(
  Developer(
    id    = "mihai.surdeanu",
    name  = "Mihai Surdeanu",
    email = "msurdeanu@email.arizona.edu",
    url   = url("https://www.cs.arizona.edu/person/mihai-surdeanu")
  )
)
ThisBuild / homepage := Some(url(s"https://github.com/clulab/$publicationNorm"))
ThisBuild / licenses := List(
  "Apache License, Version 2.0" ->
      url("http://www.apache.org/licenses/LICENSE-2.0.html")
)
ThisBuild / organization := "org.clulab"
ThisBuild / organizationHomepage := Some(url("http://clulab.org/"))
ThisBuild / organizationName := "Computational Language Understanding (CLU) Lab"
// The sonatype plugin seems to overwrite these two values.
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishMavenStyle := true
ThisBuild / publishTo := {
  val useArtifactory = BuildUtils.artifactory

  if (useArtifactory) {
    val realm = "Artifactory Realm"
    val provider = "http://artifactory.cs.arizona.edu:8081/artifactory/"
    val repository = "sbt-release-local"
    val details =
      if (isSnapshot.value) ";build.timestamp=" + new java.util.Date().getTime
      else ""
    val location = provider + repository + details

    Some((realm at location).withAllowInsecureProtocol(true))
  }
  else {
    val realm = if (isSnapshot.value) "snapshots" else "releases"
    val provider = "https://oss.sonatype.org/"
    val repository = ""
    val details =
      if (isSnapshot.value) "content/repositories/snapshots"
      else "service/local/staging/deploy/maven2"
    val location = provider + repository + details

    Some(realm at location)
  }
}
ThisBuild / scmInfo := Some(
  ScmInfo(
    url(s"https://github.com/clulab/$publicationNorm"),
    s"scm:git@github.com:clulab/$publicationNorm.git"
  )
)

ThisBuild / Compile / packageBin / publishArtifact := true // Do include the resources.
ThisBuild / Compile / packageDoc / publishArtifact := true // Do include the documentation.
ThisBuild / Compile / packageSrc / publishArtifact := true // Do include the source code.
ThisBuild / Test    / packageBin / publishArtifact := false

// Please add your credentials to ~/.sbt/<version>/credentials.sbt in lines that looks like this:
// credentials += Credentials("<realm>", "<host>", "<user>", "<password>")

// Alternatively, place the credentials in a file ~/.sbt/.credentials-<name> and refer to the file here:
// credentials += Credentials(Path.userHome / ".sbt" / ".credentials-<name>")
// The file should be formatted like a Java properties file:
// realm=<realm>
// host=<host>
// user=<user>
// password=<password>

// Example realms: Artifactory Realm, Sonatype Nexus Repository Manager
// Example hosts: artifactory.cs.arizona.edu, oss.sonatype.org
