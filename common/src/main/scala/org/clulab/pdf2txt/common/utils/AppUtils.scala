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

  def checkArgs(args: Array[String], mapAndConfig: MapAndConfig): Unit = {
    val badArgOpt = mapAndConfig.map.keySet.find { key =>
      !args.contains(key)
    }

    badArgOpt.foreach { badArg =>
      System.err.println(s"""The command line argument "$badArg" is unexpected.  Use -help for assistance.""")
      System.exit(-1)
    }
  }

  def showArgs(args: Array[String], mapAndConfig: MapAndConfig): Unit = {
    System.out.println()
    System.out.println("Configuration:")
    args.foreach { arg =>
      val valueOpt = mapAndConfig.get(arg)

      valueOpt.foreach { value =>
        System.out.println(s"\t$arg = $value")
      }
    }
    System.out.println()
  }

  def mkMapAndConfig(argsMap: Map[String, String], paramsMap: Map[String, String], resourceConfig: Config, conf: String, configPath: String): MapAndConfig = {
    val fileConfigOpt = {
      val configNameOpt = argsMap.get(conf)
      val configOpt = configNameOpt.map { configName =>
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
