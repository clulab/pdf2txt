package org.clulab.sbt

import sbt._ // at

object Resolvers {
  val clulabResolver = ("artifactory.cs.arizona.edu" at "http://artifactory.cs.arizona.edu:8081/artifactory/sbt-release").withAllowInsecureProtocol(true)
//  val jitpackResolver = "jitpack" at "https://jitpack.io"
//  val localResolver = "Local Ivy Repository" at s"file://${System.getProperty( "user.home" )}/.ivy2/local"
}
