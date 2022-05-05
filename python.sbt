import ai.kien.python.Python

lazy val python = Python()

lazy val javaOpts = {
  println("Keith was up here!")
  python.scalapyProperties.get.map { case (key, value) =>
    println("Keith was here!")
    println(s"$key = $value")
    s"-D$key=$value"
  }.toSeq
}

ThisBuild / run / javaOptions ++= javaOpts
