package org.clulab.pdf2txt.document.logical

import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.document.{Document, DocumentConstructor}
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.{Sentence => ProcessorsSentence}

class DocumentBySentence(rawText: String, range: Range) extends Document(rawText, range) {
  protected val processorsSentences = DocumentBySentence.processor.mkDocument(rawText.substring(range), keepText = false).sentences
  protected val separators = processorsSentences // for easier comparison to DocumentByParagraph
  val preSeparator =
      if (separators.isEmpty) Range(range.start, range.start)
      else Range(range.start, separators.head.startOffsets.head)
  val interSeparators = separators.sliding(2).map { case Array(prev, next) =>
    Range(range.start + prev.endOffsets.last, range.start + next.startOffsets.head)
  }
  val postSeparator =
      if (separators.isEmpty) range
      else Range(range.start + separators.last.endOffsets.last, range.end)

  def parse(): Seq[Sentence] = {
    processorsSentences.map { processorsSentence =>
      // The processorsSentence has start and end based on original range, so reuse it.
      (new Sentence(rawText, range, processorsSentence))
    }
  }

  val sentences: Seq[Sentence] = parse()

  def bySentence: Iterator[Sentence] = sentences.iterator
}

class Sentence(rawText: String, range: Range, processorsSentence: ProcessorsSentence) {
  // If there was an offset in range at the beginning, then it still needs to be taken into account
  val preRange = Range(range.start, range.start)
  val interRanges = processorsSentence.words.indices.toArray.sliding(2).map { case Array(prev, next) =>
    Range(processorsSentence.endOffsets(prev), processorsSentence.startOffsets(next))
  }.toArray
  val postRange = Range(range.end, range.end)
  val leadingRanges = preRange +: interRanges
  val trailingRanges = interRanges :+ postRange

  def parse(): Seq[Word] = {
    processorsSentence.words.indices.map { index =>
      val range = Range(processorsSentence.startOffsets(index), processorsSentence.endOffsets(index))
      val wordContent = new WordContent(rawText, range, processorsSentence.words(index))
      val wordSeparator = new WordSeparator(rawText, leadingRanges(index))

      Word(wordContent, wordSeparator)
    }
  }

  val words = parse()

  def byWord: Iterator[Word] = words.iterator

  def byWordPair: Iterator[(Word, Word)] = {
    null
  }
}

case class Word(wordContent: WordContent, wordSeparator: WordSeparator) {

  def endsWithHyphen: Boolean = wordContent.endsWithHyphen

  def separatedBySingleLine: Boolean = wordSeparator.isNewline

  def separatedBySpace: Boolean = wordSeparator.isSpace
}

class WordContent(rawText: String, range: Range, cookedText: String) {
  def endsWithHyphen = cookedText.endsWith("-")
}

class WordSeparator(rawText: String, range: Range) {
  def isNewline = rawText == "\n" // use matches string
  def isSpace = rawText == " "
}

object DocumentBySentence {
  val processor = new CluProcessor()

  def apply(rawText: String): DocumentBySentence = apply(rawText, rawText.range)
  def apply(rawText: String, range: Range): DocumentBySentence = new DocumentBySentence(rawText, range)
}
