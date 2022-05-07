package org.clulab.pdf2txt.common.utils

import com.typesafe.config.Config

case class MapAndConfig(map: Map[String, String], config: Config) {

  def apply(key: String): String = getString(key)

  def getString(key: String): String = {
    map.getOrElse(key, {
      if (config.hasPath(key)) config.getString(key)
      else throw new ConfigError(s"""There is no configured string value for "$key".""")
    })
  }

  def getBoolean(key: String): Boolean = {
    val value = this(key)

    value match {
      case "true" => true
      case "false" => false
      case _ => throw new ConfigError(s"""For argument "$key" the value is "$value", but it should be "true" or "false".""")
    }
  }

  def getInt(key: String): Int = {
    map.get(key).map(_.toInt).getOrElse {
      if (config.hasPath(key)) config.getInt(key)
      else throw new ConfigError(s"""There is no configured integer value for "$key".""")
    }
  }

  def getFloat(key: String, defaultOpt: Option[Float] = None): Float = {
    map.get(key).map(_.toFloat).getOrElse {
      if (config.hasPath(key)) config.getDouble(key).toFloat
      else defaultOpt.getOrElse(throw new ConfigError(s"""There is no configured float value for "$key"."""))
    }
  }

  def contains(key: String): Boolean = map.contains(key) || config.hasPath(key)

  def get(key: String): Option[String] = {
    if (map.contains(key)) Some(map(key))
    else if (config.hasPath(key)) Some(config.getString(key))
    else None
  }
}
