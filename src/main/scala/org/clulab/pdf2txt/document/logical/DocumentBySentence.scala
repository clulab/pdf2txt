package org.clulab.pdf2txt.document.logical

import org.clulab.pdf2txt.common.utils.{PairIndexedSeq, TextRange}
import org.clulab.pdf2txt.document.Document
import org.clulab.processors.{Sentence => ProcessorsSentence}

// multiple sentences comprising entire document, contents are sentences
class DocumentBySentence(parentOpt: Option[Document], textRange: TextRange, processorContents: Array[ProcessorsSentence]) extends Document(parentOpt, textRange) {
  override val (preSeparator, contents, postSeparator) = {
    // Processors works on the entire string, so startOffsets and endOffsets need to be adjusted.
    val offset = start
    val preSeparator =
        if (processorContents.isEmpty) all
        else before(offset + processorContents.head.startOffsets.head)
    val interSeparators = PairIndexedSeq(processorContents).map { case (prev, next) =>
      subRange(prev.endOffsets.last, next.startOffsets.head) + offset
    }.toArray
    val postSentenceSeparator =
        if (processorContents.isEmpty) emptyEnd // none of it
        else after(offset + processorContents.last.endOffsets.last)
    val postSeparator = emptyEnd // It is used by the sentence.
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
  lazy val processor = Document.processor

  def apply(text: String): DocumentBySentence = apply(TextRange(text))
  def apply(textRange: TextRange): DocumentBySentence = {
    val processorsSentences = processor.mkDocument(textRange.toString, keepText = false).sentences

    new DocumentBySentence(None, textRange, processorsSentences)
  }
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
