// This next line of code results in constantly changing source files which then require
// constant repackaging.  Absent an active use case, BuildTime is therefore skipped.
// buildInfoOptions += BuildInfoOption.BuildTime,
buildInfoKeys := Seq[BuildInfoKey](
  name, version, scalaVersion, sbtVersion, libraryDependencies, scalacOptions,
  "gitCurrentBranch" -> { git.gitCurrentBranch.value },
  "gitHeadCommit" -> { git.gitHeadCommit.value.getOrElse("") },
  "gitHeadCommitDate" -> { git.gitHeadCommitDate.value.getOrElse("") },
  "gitUncommittedChanges" -> { git.gitUncommittedChanges.value }
)
buildInfoPackage := "org.clulab.pdf2txt"
