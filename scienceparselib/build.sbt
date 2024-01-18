name := "pdf2txt-scienceparselib"
description := "The pdf2txt-scienceparselib subproject provides access to pre-compiled allenai/science-parser libraries."

resolvers ++= Seq(
//  Resolvers.localResolver, // Reserve for Two Six.
//  Resolvers.clulabResolver // processors-models, transitive dependency
)

unmanagedBase := {
  val suffix = CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((major, minor)) => s"-$major.$minor"
    case None => ""
  }

  baseDirectory.value / ("lib" + suffix)
}

// https://stackoverflow.com/questions/67126344/sbt-plugin-add-an-unmanaged-jar-file
Compile / packageBin := unmanagedBase.value / "pdf2txt-scienceparselib.jar"

libraryDependencies ++= {
  Seq(
    // science-parse-core_2.12 3.0.1
    "org.apache.pdfbox"       % "pdfbox"             % "2.0.9"    exclude("commons-logging", "commons-logging"),
    "org.apache.pdfbox"       % "fontbox"            % "2.0.9"    exclude("commons-logging", "commons-logging"),
    "org.projectlombok"       % "lombok"             % "1.16.20",
    "com.goldmansachs"        % "gs-collections"     % "6.1.0",
    "org.bouncycastle"        % "bcprov-jdk15on"     % "1.54",
    "org.bouncycastle"        % "bcmail-jdk15on"     % "1.54",
    "org.bouncycastle"        % "bcpkix-jdk15on"     % "1.54",
    "org.jsoup"               % "jsoup"              % "1.8.1",
    "org.apache.commons"      % "commons-lang3"      % "3.4",
    "commons-io"              % "commons-io"         % "2.4",
    "com.amazonaws"           % "aws-java-sdk-s3"    % "1.11.213" exclude("commons-logging", "commons-logging"),
    "com.google.guava"        % "guava"              % "18.0",
    "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
    "org.scalaj"             %% "scalaj-http"        % "2.3.0",
    "io.spray"               %% "spray-json"         % "1.3.5",
    "de.ruedigermoeller"      % "fst"                % "2.47",
    "org.apache.opennlp"      % "opennlp-tools"      % "1.7.2",

    // These are already automatically included in a Scala project.
    // "org.scala-lang" % scala-library" % "2.12.9", // or "2.11.12"
    // "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
    // These next two are included in the lib directory and their dependencies are listed in this file.
    // "org.allenai.common" %% "common-core" % "2.0.0" exclude("org.apache.common", "commons-math3"),
    // "org.allenai.datastore" %% "datastore" % "2.0.0",
    // "org.allenai" %% "pdffigures2" % "0.1.0",
    // "org.allenai.word2vec" %% "word2vecjava" % "2.0.0" exclude("commons-logging", "commons-logging") exclude("log4j", "log4j"), 
    // "org.allenai" % "ml" % "0.16" exclude("args4j", "*") exclude("org.slf4j", "slf4j-simple"),
    // These are for internal testing only.
    // "org.scalatest" %% "scalatest" % "3.0.9" % Test,
    // "org.testng" % "testng" % "6.8.1" % Test,
    // "org.allenai.common" %% "common-testkit" % "2.0.0" % Test,
    // Logging is arranged elsewhere.
    // "org.slf4j" % "jcl-over-slf4j" % "1.7.7",

    // common-core_2.12 2.0.0
    "ch.qos.logback"     % "logback-classic" % "1.2.3",
    "ch.qos.logback"     % "logback-core"    % "1.2.3",
    "net.sf.opencsv"     % "opencsv"         % "2.1",
    "io.spray"          %% "spray-json"      % "1.3.5",
    "com.typesafe"       % "config"          % "1.2.1",

    // This is already automatically included in a Scala project.
    // "org.scala-lang" % scala-library" % "2.12.9", // or "2.11.12"
    // This is for internal testing only.
    // "org.allenai.common" %% "common-testkit" % "2.0.0" % Test,
    // This is ruled out by an exclude().
    // "org.apache.commons" % "commons-lang3"   % "3.4",
    // Logging is arranged elsewhere.
    // "org.slf4j" % "slf4j-api" % "1.7.28",

    // datastore_2.12 2.0.0
    "com.amazonaws"  % "aws-java-sdk-s3" % "1.10.29" exclude("commons-logging", "commons-logging"),
    "commons-io"     % "commons-io"      % "2.4",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "ch.qos.logback" % "logback-core"    % "1.2.3",

    // This is already automatically included in a Scala project.
    // "org.scala-lang" % scala-library" % "2.12.9", // or "2.11.12"
    // This is included in the lib directory and its dependencies are listed in this file.    
    // "org.allenai.common" %% "common-core" % "2.0.0",
    // This is for internal testing only.
    // "org.allenai.common" %% "common-testkit" % "2.0.0" % Test,
    // Logging is arranged elsewhere.
    // "org.slf4j" % "slf4j-api" % "1.7.28",
    // "org.slf4j" % "jcl-over-slf4j" % "1.7.7",

    // pdffigures2_2.12 0.1.0
    "io.spray"         %% "spray-json"      % "1.3.5",
    "com.github.scopt" %% "scopt"           % "3.7.1",
    "ch.qos.logback"    % "logback-classic" % "1.1.7",
    "org.apache.pdfbox" % "pdfbox"          % "2.0.1",
    "org.apache.pdfbox" % "fontbox"         % "2.0.1",
    "com.typesafe"      % "config"          % "1.3.0",
    "org.bouncycastle"  % "bcprov-jdk15on"  % "1.54",
    "org.bouncycastle"  % "bcmail-jdk15on"  % "1.54",
    "org.bouncycastle"  % "bcpkix-jdk15on"  % "1.54",
    
    // This is already automatically included in a Scala project.
    // "org.scala-lang" % scala-library" % "2.12.9", // or "2.11.12"
    // Logging is arranged elsewhere.
    // "org.slf4j" % "jcl-over-slf4j" % "1.7.7",

    // word2vecjava_2.12 2.0.0
    "org.apache.commons" % "commons-lang3" % "3.9",
    "com.google.guava"   % "guava"         % "18.0",
    "commons-io"         % "commons-io"    % "2.4",
    "log4j"              % "log4j"         % "1.2.17",
    "joda-time"          % "joda-time"     % "2.3",
    "org.apache.thrift"  % "libfb303"      % "0.9.3",
    "org.apache.commons" % "commons-math3" % "3.6.1",

    // This is already automatically included in a Scala project.
    // "org.scala-lang" % scala-library" % "2.12.9", // or "2.11.12"
    // These are for internal testing only.
    // "org.scalacheck" %% "scalacheck" % "1.14.0" % Test,
    // "com.novocode" % "junit-interface<" % "0.11" % Test,

    // ml 0.16
    "com.goldmansachs"   % "gs-collections" % "6.1.0",
    "de.ruedigermoeller" % "fst"            % "2.47",

    // This is ruled out by an exclude().
    // "args4j" % "args4j" % "2.32",
    // Logging is arranged elsewhere.
    // "org.slf4j" % "slf4j-simple" % "1.7.7",

    // This is used to produce the metadata.
    "com.lihaoyi"       %% "ujson"          % "2.0.0"
  )
}
