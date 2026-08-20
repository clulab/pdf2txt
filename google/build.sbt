name := "pdf2txt-google"
description := "The pdf2txt-google subproject implements an interface to the google converter."

resolvers ++= Seq(
//  Resolvers.localResolver, // Reserve for Two Six.
//  Resolvers.clulabResolver // processors-models, transitive dependency
)

libraryDependencies ++= {
  // val json4sVersion = "4.0.6"
  val json4sVersion = {
    CrossVersion.partialVersion(scalaVersion.value) match {
      // Spark may have problems above 3.2.11, but processors has runtime errors much below 3.5.5.
      case Some((2, minor)) if minor <= 12 => "3.5.5"
      case Some((3, 0)) => "4.0.3"  // This is as close as we can get.
      case _ => "4.0.6"
    }
  }

  Seq(
    "com.google.apis"  % "google-api-services-vision"      % "v1-rev451-1.25.0",
    "com.google.cloud" % "google-cloud-storage"            % "2.10.0",
    "com.google.cloud" % "google-cloud-vision"             % "3.0.1",
    "com.google.auth"  % "google-auth-library-oauth2-http" % "1.8.1",
    "org.json4s"      %% "json4s-core"                     % json4sVersion,
    "org.json4s"      %% "json4s-jackson"                  % json4sVersion
  )
}
