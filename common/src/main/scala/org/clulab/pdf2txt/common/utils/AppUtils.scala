package org.clulab.pdf2txt.common.utils

import com.typesafe.config.{Config, ConfigFactory}

import java.io.{File, PrintStream}

object AppUtils {

  def showSyntax(resourceName: String, printStream: PrintStream, args: Any*): Unit = {
    val rawText = FileUtils.getTextFromResource(resourceName)
    val cookedText = rawText.format(args: _*)
    val splitTexts = cookedText.split("\r?\n")

    printStream.println() // extra line above
    splitTexts.foreach(printStream.println)
    printStream.println() // extra line below
  }

  def syntaxError(resourceName: String, system: Systemish, args: Any*): Unit = {
    showSyntax(resourceName, system.err, args)
    system.exit(-1)
  }

  def checkArgs(args: Array[String], mapAndConfig: MapAndConfig, system: Systemish): Unit = {
    val badArgOpt = mapAndConfig.map.keySet.find { key =>
      !args.contains(key)
    }

    badArgOpt.foreach { badArg =>
      system.err.println(s"""The command line argument "$badArg" is unexpected.  Use -help for assistance.""")
      system.exit(-1)
    }
  }

  def showArgs(args: Array[String], mapAndConfig: MapAndConfig, printStream: PrintStream): Unit = {
    printStream.println()
    printStream.println("Configuration:")
    args.foreach { arg =>
      val valueOpt = mapAndConfig.get(arg)

      valueOpt.foreach { value =>
        printStream.println(s"\t$arg = $value")
      }
    }
    printStream.println()
  }

  def mkArgsString(cmd: AnyRef, args: Array[String], mapAndConfig: MapAndConfig): String = {
    val command = cmd.getClass.getName
    val argsAndValues = args.flatMap { arg =>
      val valueOpt = mapAndConfig.get(arg)

      valueOpt.map { value =>
        s"-$arg $value"
      }
    }
    val argsString = (command +: argsAndValues).mkString(" ")

    argsString
  }

  def mkMapAndConfig(argsMap: Map[String, String], paramsMap: Map[String, String], resourceConfig: Config, conf: String, configPath: String): MapAndConfig = {
    val fileConfigOpt = {
      val configNameOpt = argsMap.get(conf)
      val configOpt = configNameOpt.map { configName =>
        val configFileName = configName + ".conf"
        if (!new File(configFileName).exists)
          throw new ConfigError(s"""Configuration file "$configFileName" does not exist.""")
        ConfigFactory.parseFileAnySyntax(new File(configName)).getConfig(configPath)
      }
      configOpt
    }
    val map = argsMap ++ paramsMap // The second will overwrite the first.
    val config = fileConfigOpt.map(_.withFallback(resourceConfig)).getOrElse(resourceConfig)
    val mapAndConfig = MapAndConfig(map, config)

    mapAndConfig
  }
}
