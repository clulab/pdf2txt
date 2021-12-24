package org.clulab.pdf2txt.document.logical

import org.clulab.pdf2txt.common.utils.{PairIterator, PairOptIterator, StringUtils, TextRange}
import org.clulab.pdf2txt.document.physical.CharDocument
import org.clulab.pdf2txt.document.{Document, Separator}
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.{Sentence => ProcessorsSentence}

// multiple sentences comprising entire document, contents are sentences
case class DocumentBySentence(override val parentOpt: Option[Document], textRange: TextRange) extends Document(parentOpt, textRange) {
  override val (preSeparatorOpt, contents, postSeparatorOpt) = {
    // Processors works on the entire string, so startOffsets and endOffsets need to be adjusted.
    val offset = textRange.start
    val processorContents = DocumentBySentence.processor.mkDocument(textRange.toString, keepText = false).sentences
    val preSeparator =
        if (processorContents.isEmpty) textRange // all of it
        else textRange.subRange(offset, offset + processorContents.head.startOffsets.head)
    val interSeparators = PairIterator(processorContents).map { case (prev, next) =>
      textRange.subRange(offset + prev.endOffsets.last, offset + next.startOffsets.head)
    }.toArray
    val postSentenceSeparator =
        if (processorContents.isEmpty) textRange.emptyEnd // none of it
        else textRange.subRange(offset + processorContents.last.endOffsets.last, textRange.end)
    val postSeparator = textRange.emptyEnd // it is used by the sentence
    val sentences = processorContents.indices.map { index =>
      val processorsSentence: ProcessorsSentence = processorContents(index)
      val contentTextRange = textRange.subRange(offset + processorsSentence.startOffsets.head, offset + processorsSentence.endOffsets.last)
      val separatorTextRange = interSeparators.lift(index).getOrElse(postSentenceSeparator)

      SentenceDocument(Some(this), contentTextRange, separatorTextRange, processorsSentence)
    }

    (
      Some(new Separator(Some(this), preSeparator)),
      sentences,
      Some(new Separator(Some(this), postSeparator))
    )
  }

  def bySentence: Iterator[SentenceDocument] = contents.iterator
}

object DocumentBySentence {
  lazy val processor = new CluProcessor()
}

case class SentenceDocument(override val parentOpt: Option[Document], contentTextRange: TextRange, separatorTextRange: TextRange, processorsSentence: ProcessorsSentence)
    extends Document(parentOpt, TextRange(contentTextRange, separatorTextRange)) {
  override val (preSeparatorOpt, contents, postSeparatorOpt) = {
    // Processors works on the entire string, so startOffsets and endOffsets need to be adjusted.
    val offset = start
    val contents = processorsSentence.words.indices
    val interSeparators = PairIterator(processorsSentence.words.indices).map { case (prev, next) =>
      subRange(offset + processorsSentence.endOffsets(prev), offset + processorsSentence.startOffsets(next))
    }.toArray
    val postWordSeparator = emptyRange(offset + processorsSentence.endOffsets.last)
    val wordDocuments = contents.indices.map { index =>
      val processorsWord = processorsSentence.words(index)
      val contentTextRange = subRange(offset + processorsSentence.startOffsets(index), offset + processorsSentence.endOffsets(index))
      val separatorTextRange = interSeparators.lift(index).getOrElse(postWordSeparator)

      WordDocument(Some(this), contentTextRange, separatorTextRange, processorsWord)
    }

    (None, wordDocuments, Some(new Separator(Some(this), separatorTextRange)))
  }

  def byWord: Iterator[WordDocument] = contents.iterator

  def byWordPair: Iterator[(WordDocument, WordDocument)] = new PairIterator(contents)

  def byWordPairOpt: Iterator[(Option[WordDocument], Option[WordDocument])] = new PairOptIterator(contents)
}

case class WordDocument(override val parentOpt: Option[Document], contentTextRange: TextRange, separatorTextRange: TextRange, processorsWord: String)
    extends Document(parentOpt, TextRange(contentTextRange, separatorTextRange)) {
  override val postSeparatorOpt = Some(new Separator(Some(this), separatorTextRange))
  val charDocument = CharDocument(Some(this), contentTextRange)
  override val contents = Seq(charDocument)
}
