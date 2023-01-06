package org.clulab.sbt

import sbt._ // at

object Resolvers {
  val clulabResolver = "clulab" at "https://artifactory.clulab.org/artifactory/sbt-release"
//  val jitpackResolver = "jitpack" at "https://jitpack.io"
//  val localResolver = "Local Ivy Repository" at s"file://${System.getProperty( "user.home" )}/.ivy2/local"
}
