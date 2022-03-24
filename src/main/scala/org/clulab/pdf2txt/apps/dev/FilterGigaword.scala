package org.clulab.pdf2txt.apps.dev

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser

import java.io.{BufferedOutputStream, FileOutputStream, ObjectOutputStream}
import java.nio.charset.StandardCharsets
import scala.io.{Codec, Source}

object FilterGigaword extends App {
  val inFilename = args.lift(0).getOrElse("gigawordDocFreq.sorted.freq.txt")
  val outFilename = args.lift(1).getOrElse("gigaword.ser")
  val lowerLimit = 2

  val substitutions = Seq(
    ("-lrb-", "("), ("-rrb-", ")"), // round
    ("-lsb-", "["), ("-rsb-", "]"), // square
    ("-lcb-", "{"), ("-rcb-", "}") // curvy
  )
  val wordFrequencies = Source.fromFile(inFilename)(new Codec(StandardCharsets.UTF_8)).autoClose { source =>
    source.getLines()
      .map { line =>
        val Array(rawWord, freq) = line.split('\t')
        val cookedWord = substitutions.foldLeft(rawWord) { case (word, (remove, insert)) =>
          word.replace(remove, insert)
        }

        cookedWord -> freq.toInt
      }
      .filter { pair => pair._1.nonEmpty && !pair._1.contains(" ") && pair._2 >= lowerLimit }
      .toVector
  }
  val string = wordFrequencies.map(_._1).mkString(" ")
  val frequencies = wordFrequencies.map(_._2).toArray

  new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outFilename))).autoClose { objectOutputStream =>
    objectOutputStream.writeObject(string)
    objectOutputStream.writeObject(frequencies)
  }
}
