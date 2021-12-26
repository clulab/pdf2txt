package org.clulab.pdf2txt.document.logical

import org.clulab.pdf2txt.common.utils.{PairIndexedSeq, TextRange}
import org.clulab.pdf2txt.document.physical.CharDocument
import org.clulab.pdf2txt.document.{Document, Separator}
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.{Sentence => ProcessorsSentence}

// multiple sentences comprising entire document, contents are sentences
class DocumentBySentence(parentOpt: Option[Document], textRange: TextRange) extends Document(parentOpt, textRange) {
  override val (preSeparator, contents, postSeparator) = {
    // Processors works on the entire string, so startOffsets and endOffsets need to be adjusted.
    val offset = start
    val processorContents = DocumentBySentence.processor.mkDocument(textRange.toString, keepText = false).sentences
    val preSeparator =
        if (processorContents.isEmpty) all
        else before(offset + processorContents.head.startOffsets.head)
    val interSeparators = PairIndexedSeq(processorContents).map { case (prev, next) =>
      subRange(prev.endOffsets.last, next.startOffsets.head) + offset
    }.toArray
    val postSentenceSeparator =
        if (processorContents.isEmpty) emptyEnd // none of it
        else after(offset + processorContents.last.endOffsets.last)
    val postSeparator = emptyEnd // it is used by the sentence.
    val sentences = processorContents.indices.map { index =>
      val processorsSentence: ProcessorsSentence = processorContents(index)
      val contentTextRange = subRange(processorsSentence.startOffsets.head, processorsSentence.endOffsets.last) + offset
      val separatorTextRange = interSeparators.lift(index).getOrElse(postSentenceSeparator)

      new SentenceDocument(Some(this), contentTextRange, separatorTextRange, offset, processorsSentence)
    }

    (newSeparator(preSeparator), sentences, newSeparator(postSeparator))
  }
}

object DocumentBySentence {
  lazy val processor = new CluProcessor()

  def apply(text: String): DocumentBySentence = apply(TextRange(text))
  def apply(textRange: TextRange): DocumentBySentence = new DocumentBySentence(None, textRange)
}

class SentenceDocument(parentOpt: Option[Document], contentTextRange: TextRange, separatorTextRange: TextRange,
    processorsOffset: Int, processorsSentence: ProcessorsSentence)
    extends Document(parentOpt, TextRange(contentTextRange, separatorTextRange)) {
  override val (preSeparator, contents, postSeparator) = {
    // Processors works on the entire string, so startOffsets and endOffsets need to be adjusted.
    val offset = processorsOffset
    val contents = processorsSentence.words.indices
    val interSeparators = PairIndexedSeq(processorsSentence.words.indices).map { case (prev, next) =>
      subRange(processorsSentence.endOffsets(prev), processorsSentence.startOffsets(next)) + offset
    }
    val postWordSeparator = emptyRange(offset + processorsSentence.endOffsets.last)
    val wordDocuments = contents.indices.map { index =>
      val processorsWord = processorsSentence.words(index)
      val contentTextRange = subRange(processorsSentence.startOffsets(index), processorsSentence.endOffsets(index)) + offset
      val separatorTextRange = interSeparators.lift(index).getOrElse(postWordSeparator)

      new WordDocument(Some(this), contentTextRange, separatorTextRange, processorsWord)
    }

    (newSeparator(emptyStart), wordDocuments, newSeparator(separatorTextRange))
  }
}

class WordDocument(parentOpt: Option[Document], contentTextRange: TextRange, separatorTextRange: TextRange, val processorsWord: String)
    extends Document(parentOpt, TextRange(contentTextRange, separatorTextRange)) {
  override val postSeparator: Separator = newSeparator(separatorTextRange)
  val charDocument: CharDocument = new CharDocument(Some(this), contentTextRange)
  override val contents: Seq[CharDocument] = Array(charDocument)
}
