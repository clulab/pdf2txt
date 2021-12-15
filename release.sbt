import ReleaseTransformations._
import org.clulab.sbt.BuildUtils

ThisBuild / credentials ++= {
  // Favor ~/.sbt/<version>/credentials.sbt which is automatically read.
  val file = Path.userHome / ".sbt" / ".clulab-credentials"

  if (file.exists) Seq(Credentials(file))
  else Seq.empty
}

releaseProcess :=
    Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease
    ) ++
    Seq[ReleaseStep](releaseStepCommandAndRemaining(
      if (BuildUtils.artifactory) "+publish"
      else "+publishSigned"
    )) ++
    Seq[ReleaseStep](
      setNextVersion,
      commitNextVersion
    ) ++
    (
      if (BuildUtils.artifactory) Seq.empty[ReleaseStep]
      else Seq[ReleaseStep](releaseStepCommandAndRemaining("sonatypeReleaseAll"))
    ) ++
    Seq[ReleaseStep](
      pushChanges
    )
