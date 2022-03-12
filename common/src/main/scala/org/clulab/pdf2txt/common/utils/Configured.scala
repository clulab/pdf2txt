package org.clulab.pdf2txt.common.utils

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

/**
 * Classes that are configured with com.typesafe.config.Config
 * User: mihais
 * Date: 9/10/17
 * Last Modified: Update for Scala 2.12: java converters.
 */
trait Configured {

  def getConfig: Config

  def getArgBoolean (argPath: String, defaultValue: Option[Boolean]): Boolean =
    if (getConfig.hasPath(argPath)) getConfig.getBoolean(argPath)
    else if(defaultValue.nonEmpty) defaultValue.get
    else throw new RuntimeException(s"ERROR: parameter $argPath must be defined!")

  def getArgInt (argPath: String, defaultValue: Option[Int]): Int =
    if (getConfig.hasPath(argPath)) getConfig.getInt(argPath)
    else if(defaultValue.nonEmpty) defaultValue.get
    else throw new RuntimeException(s"ERROR: parameter $argPath must be defined!")

  def getArgFloat (argPath: String, defaultValue: Option[Float]): Float =
    if (getConfig.hasPath(argPath)) getConfig.getDouble(argPath).toFloat
    else if(defaultValue.nonEmpty) defaultValue.get
    else throw new RuntimeException(s"ERROR: parameter $argPath must be defined!")

  def getArgString (argPath: String, defaultValue: Option[String]): String =
    if (getConfig.hasPath(argPath)) getConfig.getString(argPath)
    else if(defaultValue.nonEmpty) defaultValue.get
    else throw new RuntimeException(s"ERROR: parameter $argPath must be defined!")

  def getArgStrings (argPath: String, defaultValue: Option[Seq[String]]): Seq[String] =
    if (getConfig.hasPath(argPath)) getConfig.getStringList(argPath).asScala
    else if(defaultValue.nonEmpty) defaultValue.get
    else throw new RuntimeException(s"ERROR: parameter $argPath must be defined!")

  def contains(argPath:String):Boolean = getConfig.hasPath(argPath)
}

class ConfigWithDefaults(config:Config) extends Configured {
  override def getConfig: Config = config
}

object ConfigWithDefaults {
  def apply(config:Config): ConfigWithDefaults = {
    new ConfigWithDefaults(config)
  }

  def apply(configName:String): ConfigWithDefaults = {
    new ConfigWithDefaults(ConfigFactory.load(configName))
  }
}
