Test / testOptions := Seq (
  Tests.Filter { string =>
    // Performing this test will results in approx. 2GB of data being downloaded.
    // Skip it unless it is called directly from IntelliJ, for example.
    !string.endsWith(".ScienceParseTest") }
)
