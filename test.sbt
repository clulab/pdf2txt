import Tests._

def groupByLanguage(tests: Seq[TestDefinition]) = {
//val newRunPolicy = SubProcess(ForkOptions())
  val newRunPolicy = InProcess
  val namesAndSubstrings = Seq(
    ("englishGroup",          ".text.english."),
    ("englishGroundingGroup", ".text.englishGrounding."),
    ("portugueseGroup",       ".text.portuguese."),
    ("other",                 "")
  )
  val groupedTestDefinitions: Map[String, Seq[TestDefinition]] = tests.groupBy { testDefinition =>
    namesAndSubstrings.find { case (_, substring) =>
      testDefinition.name.contains(substring)
    }.get._1 // Get the name of the Option[NameAndSubstring].
  }
  val groups: Seq[Group] = groupedTestDefinitions.toSeq.map { case (name, testDefinitions) =>
    new Group(name, testDefinitions, newRunPolicy)
  }

  groups
}

ThisBuild / Test / fork := true // also forces sequential operation
ThisBuild / Test / parallelExecution := false // keeps groups in their order
//ThisBuild / Test / testForkedParallel := true // Allow parallel within group?
//ThisBuild / Test / testGrouping := groupByLanguage((Test / definedTests).value)
