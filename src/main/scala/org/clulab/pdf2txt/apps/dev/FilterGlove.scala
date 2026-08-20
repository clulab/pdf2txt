package org.clulab.pdf2txt.apps.dev

import org.clulab.pdf2txt.common.utils.StringUtils

import java.io.{BufferedOutputStream, FileOutputStream, ObjectOutputStream}
import java.nio.charset.StandardCharsets
import scala.io.{Codec, Source}
import scala.util.Using

object FilterGlove extends App {
  val inFilename = args.lift(0).getOrElse("glove.840B.300d.txt")
  val outFilename = args.lift(1).getOrElse("glove.ser")

  val words = Using.resource(Source.fromFile(inFilename)(new Codec(StandardCharsets.ISO_8859_1))) { source =>
    source.getLines().drop(1).map { line =>
      StringUtils.beforeFirst(line, ' ', false)
    }.toSet
  }
  val string = words.toArray.mkString(" ")

  Using.resource(new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outFilename)))) { objectOutputStream =>
    objectOutputStream.writeObject(string)
  }
}
