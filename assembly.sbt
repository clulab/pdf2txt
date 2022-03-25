import org.clulab.sbt.BuildUtils.FavoriteMergeStrategy

assembly / aggregate := false
assembly / assemblyJarName := "pdf2txt.jar"
assembly / assemblyMergeStrategy := {
  // See https://github.com/sbt/sbt-assembly.
  case PathList(paths @ _*) if paths.last == "module-info.class"                          => MergeStrategy.discard
  case PathList("org", "apache", "commons", "logging", "Log.class")                       => MergeStrategy.first
  case PathList("org", "apache", "commons", "logging", "LogConfigurationException.class") => MergeStrategy.first
  case PathList("org", "apache", "commons", "logging", "LogFactory.class")                => MergeStrategy.first

  case PathList("org", "apache", "commons", "logging", "impl", "NoOpLog.class")           => MergeStrategy.first
  case PathList("org", "apache", "commons", "logging", "impl", "SimpleLog$1.class")       => MergeStrategy.first
  case PathList("org", "apache", "commons", "logging", "impl", "SimpleLog.class")         => MergeStrategy.first

  case PathList("org", "slf4j", "impl", "StaticLoggerBinder.class")                       => MergeStrategy.first
  case PathList("org", "slf4j", "impl", "StaticMDCBinder.class")                          => MergeStrategy.first
  case PathList("org", "slf4j", "impl", "StaticMarkerBinder.class")                       => MergeStrategy.first

  case PathList("logback.xml") => new FavoriteMergeStrategy("./src/main/resources/logback.xml")

  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}
// This prevents testing in core, then non-aggregation prevents it in other subprojects.
assembly / test := {}
