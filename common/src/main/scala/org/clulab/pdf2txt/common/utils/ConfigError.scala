package org.clulab.pdf2txt.common.utils

class ConfigError(message: String) extends Pdf2txtException(message)

object ConfigError {

  def apply(mapAndConfig: MapAndConfig, key: String, value: String): ConfigError =
      apply(key, value)

  def apply(key: String, value: String): ConfigError = {
    val message = s"""The $key configuration of "$value" is not recognized."""

    new ConfigError(message)
  }
}
