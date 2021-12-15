name := "pdf2txt-common"
description := "The pdf2txt-common subproject implements everything common to pdf2txt projects."

resolvers ++= Seq(
//  Resolvers.localResolver, // Reserve for Two Six.
//  Resolvers.clulabResolver // processors-models, transitive dependency
)

libraryDependencies ++= {
  Seq(
    // local logging
    // These two can be coordinated so that the same org.slf4j:slf4j-api is used, here 1.7.25.
    "ch.qos.logback"              % "logback-classic"          % "1.2.3",       // as of 2021 Aug 11 up to up to 1.2.5
    "com.typesafe.scala-logging" %% "scala-logging"            % "3.7.2",       // as of 2021 Mar 12 up to 3.9.2
    // dependency logging
    "org.apache.logging.log4j"    % "log4j-core"               % "2.15.0",      // as of 2021 Dec 13 up to 2.15.0; less is vulnerable

    // config
    "com.typesafe"                % "config"                   % "1.4.0",       // as of 2021 Mar 12 up to 1.4.1
    "org.scalatest"              %% "scalatest"                % "3.2.5" % Test // as of 2021 Mar 12 up to 3.2.5
  )
}

