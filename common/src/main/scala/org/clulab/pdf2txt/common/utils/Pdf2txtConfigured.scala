package org.clulab.pdf2txt.common.utils

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}

import scala.sys.SystemProperties

trait Pdf2txtConfigured extends Configured {
  // This line doesn't work if there is a leading / in the resource name.  I tried.
  lazy val config = ConfigFactory.parseResourcesAnySyntax("Pdf2txt")
      .resolveWith(ConfigFactory.systemProperties)
      .getConfig("Pdf2txt")

  override def getConfig: Config = config
}
