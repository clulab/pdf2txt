package org.clulab.pdf2txt.document.logical

import org.clulab.pdf2txt.common.utils.{PairIterator, StringUtils, TextRange}
import org.clulab.pdf2txt.document.Document
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.{Sentence => ProcessorsSentence}

class DocumentBySentence(textRange: TextRange) extends Document(textRange) {
  val processorsString = " " // trim is for processors
  // Processors works on the entire string, so startOffsets and endOffsets need to be adjusted.
  val offset = textRange.start
  val contents = DocumentBySentence.processor.mkDocument(textRange.toString, keepText = false).sentences
  val preSeparator =
      if (contents.isEmpty) textRange // all of it
      else textRange.subRange(offset, offset + contents.head.startOffsets.head)
  val interSeparators = PairIterator(contents).map { case (prev, next) =>
    textRange.subRange(offset + prev.endOffsets.last, offset + next.startOffsets.head)
  }.toArray
  val postSentenceSeparator =
      if (contents.isEmpty) textRange.emptyEnd // none of it
      else textRange.subRange(offset + contents.last.endOffsets.last, textRange.end)
  val postSeparator = textRange.emptyEnd // it is used by the sentence
  val sentences = contents.indices.map { index =>
    val sentenceContent = SentenceContent(textRange, contents(index))
    val sentenceSeparator = SentenceSeparator(interSeparators.lift(index).getOrElse(postSentenceSeparator))

    Sentence(sentenceContent, sentenceSeparator)
  }

  def bySentence: Iterator[Sentence] = sentences.iterator
}

object DocumentBySentence {
  lazy val processor = new CluProcessor()
}

case class SentenceContent(textRange: TextRange, processorsSentence: ProcessorsSentence) {
  // Processors works on the entire string, so startOffsets and endOffsets need to be adjusted.
  val offset = textRange.start
  val contents = processorsSentence.words.indices
  val preSeparator = textRange.emptyRange(offset + processorsSentence.startOffsets.head)
  val interSeparators = PairIterator(processorsSentence.words.indices).map { case (prev, next) =>
    textRange.subRange(offset + processorsSentence.endOffsets(prev), offset + processorsSentence.startOffsets(next))
  }.toArray
  val postWordSeparator = textRange.emptyRange(offset + processorsSentence.endOffsets.last)
  val postSeparator = textRange.emptyEnd // It is used by the word
  val words = contents.indices.map { index =>
    val wordTextRange = textRange.subRange(offset + processorsSentence.startOffsets(index), offset + processorsSentence.endOffsets(index))
    val wordContent = WordContent(wordTextRange, processorsSentence.words(index))
    val wordSeparator = WordSeparator(interSeparators.lift(index).getOrElse(postWordSeparator))

    Word(wordContent, wordSeparator)
  }

  def byWord: Iterator[Word] = words.iterator

  def byWordPair: Iterator[(Word, Word)] = new PairIterator(words)
}

case class SentenceSeparator(textRange: TextRange)

case class Sentence(content: SentenceContent, separator: SentenceSeparator) {
  val preSeparator = content.preSeparator.emptyStart
  val postSeparator = separator.textRange.emptyEnd
}

case class Word(content: WordContent, separator: WordSeparator)

case class WordContent(textRange: TextRange, processorWord: String)

case class WordSeparator(textRange: TextRange)
