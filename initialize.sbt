import org.clulab.sbt.BuildUtils

// The server usually doesn't work on Windows, at least not for Keith,
// and failing on the timeout is very time consuming, so skip it.
// Recent versions of sbt, 1.4.5+, have been working better.
// Global / autoStartServer := !BuildUtils.isWindows()
// Global / excludeLintKeys += autoStartServer
// Certain library dependencies, particularly Stanford NLP, have been observed to have problems with
// versions of Java other than the required one.  Proceed with caution if you ignore this check.
// See https://stackoverflow.com/questions/19208942/enforcing-java-version-for-scala-project-in-sbt
ThisBuild / initialize := {
  val _ = initialize.value // Run the previous initialization.
  val required = "1.8"
  val current  = sys.props("java.specification.version")
  val approved = current == required

  // To stop sbt in its tracks, make this assumption.
  // assume(approved)
  // or otherwise just log the situation.
  if (approved)
    sLog.value.info(s"Java $current was detected and approved.")
  else
    sLog.value.error(s"Unsupported Java version: Eidos requires $required but found $current instead.")
}
