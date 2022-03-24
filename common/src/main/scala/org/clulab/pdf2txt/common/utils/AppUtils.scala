package org.clulab.pdf2txt.common.utils

import com.typesafe.config.{Config, ConfigFactory}

import java.io.{File, PrintStream}

object AppUtils {

  def showSyntax(resourceName: String, printStream: PrintStream, args: Any*): Unit = {
    val rawText = FileUtils.getTextFromResource(resourceName)
    val cookedText = rawText.format(args: _*)
    val splitTexts = cookedText.split("\n")

    printStream.println() // extra line above
    splitTexts.foreach(printStream.println)
    printStream.println() // extra line below
  }

  def syntaxError(resourceName: String, args: Any*): Unit = {
    showSyntax(resourceName, System.err, args)
    System.exit(-1)
  }

  def argsToMap(args: Array[String]): Map[String, String] = null

  def mkMapAndConfig(args: Array[String], params: Map[String, String], resourceConfig: Config, conf: String, configPath: String): MapAndConfig = {
    val argsMap = argsToMap(args)
    val fileConfigOpt = {
      val configNameOpt = argsMap.get(conf)
      val configOpt = configNameOpt.map { configName =>
        ConfigFactory.parseFileAnySyntax(new File(configName)).getConfig(configPath)
      }
      configOpt
    }
    val map = argsMap ++ params // The second will overwrite the first.
    val config = fileConfigOpt.map(_.withFallback(resourceConfig)).getOrElse(resourceConfig)
    val mapAndConfig = MapAndConfig(map, config)

    mapAndConfig
  }
}
