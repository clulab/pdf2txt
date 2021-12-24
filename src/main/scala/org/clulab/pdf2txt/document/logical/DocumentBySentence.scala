package org.clulab.pdf2txt.document.logical

import org.clulab.pdf2txt.common.utils.{PairIterator, StringUtils, TextRange}
import org.clulab.pdf2txt.document.Document
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.{Sentence => ProcessorsSentence}

case class DocumentBySentence(override val parentOpt: Option[Document], textRange: TextRange) extends Document(parentOpt, textRange) {
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
    val processorsSentence: ProcessorsSentence = contents(index)
    val contentTextRange = textRange.subRange(offset + processorsSentence.startOffsets.head, offset + processorsSentence.endOffsets.last)
    val separatorTextRange = interSeparators.lift(index).getOrElse(postSentenceSeparator)

    Sentence(Some(this), contentTextRange, separatorTextRange, processorsSentence)
  }

  def bySentence: Iterator[Sentence] = sentences.iterator
}

object DocumentBySentence {
  lazy val processor = new CluProcessor()
}

case class SentenceContent(override val parentOpt: Option[Document], textRange: TextRange, processorsSentence: ProcessorsSentence)
    extends Document(parentOpt, textRange) {
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
    val processorsWord = processorsSentence.words(index)
    val contentTextRange = textRange.subRange(offset + processorsSentence.startOffsets(index), offset + processorsSentence.endOffsets(index))
    val separatorTextRange = interSeparators.lift(index).getOrElse(postWordSeparator)

    Word(Some(this), contentTextRange, separatorTextRange, processorsWord)
  }

  def byWord: Iterator[Word] = words.iterator

  def byWordPair: Iterator[(Word, Word)] = new PairIterator(words)
}

case class SentenceSeparator(override val parentOpt: Option[Document], textRange: TextRange)
    extends Document(parentOpt, textRange)

case class Sentence(override val parentOpt: Option[Document], contentTextRange: TextRange, separatorTextRange: TextRange, processorsSentence: ProcessorsSentence)
    extends Document(parentOpt, TextRange(contentTextRange, separatorTextRange)) {
  val preSeparator = contentTextRange.emptyStart
  val postSeparator = separatorTextRange.emptyEnd

  val content = SentenceContent(Some(this), contentTextRange, processorsSentence)
  val separator = SentenceSeparator(Some(this), separatorTextRange)
}

case class WordContent(override val parentOpt: Option[Document], textRange: TextRange, processorsWord: String)
    extends Document(parentOpt, textRange)

case class WordSeparator(override val parentOpt: Option[Document], textRange: TextRange)
    extends Document(parentOpt, textRange)

case class Word(override val parentOpt: Option[Document], contentTextRange: TextRange, separatorTextRange: TextRange, processorsWord: String)
    extends Document(parentOpt, TextRange(contentTextRange, separatorTextRange)) {
  val content = WordContent(Some(this), contentTextRange, processorsWord)
  val separator = WordSeparator(Some(this), separatorTextRange)
}
