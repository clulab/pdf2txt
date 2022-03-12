package org.clulab.pdf2txt.common.utils

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

trait Pdf2txtConfiguredApp extends Configured {
  // This is used instead of load so that no default references or default overrides are involved.
  // In other words, the values you are looking for had better be in this file (resource).
  // This line doesn't work if there is a leading / in the resource name.  I tried.
  lazy val config = ConfigFactory.parseResourcesAnySyntax(BuildUtils.pkgToDir("org.clulab.pdf2txt") + "/apps")

  def getConfig: Config = config
}

trait Pdf2txtApp extends App with Logging with Pdf2txtConfiguredApp
