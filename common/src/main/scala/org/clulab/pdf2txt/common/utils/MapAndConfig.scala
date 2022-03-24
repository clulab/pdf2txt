package org.clulab.pdf2txt.common.utils

import com.typesafe.config.Config

case class MapAndConfig(map: Map[String, String], config: Config) {

  def apply(key: String): String = {
    map.getOrElse(key, {
      if (config.hasPath(key)) config.getString(key)
      else throw new ConfigError(s"""There is no configured value for "$key".""")
    })
  }

  def getBoolean(key: String): Boolean = {
    val value = this(key)

    value match {
      case "true" => true
      case "false" => false
      case other => throw new ConfigError(s"""For key "$key" the value is "$value", but it should be "true" or "false".""")
    }
  }

  def contains(key: String): Boolean = map.contains(key) || config.hasPath(key)
}
