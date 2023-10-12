package org.clulab.pdf2txt.document.logical

import org.clulab.pdf2txt.common.utils.{DoubleIndexedSeq, TextRange}
import org.clulab.pdf2txt.document.physical.CharDocument
import org.clulab.pdf2txt.document.{Document, Separator}
import org.clulab.processors.{Sentence => ProcessorsSentence}

// multiple words comprising entire document, contents are words
class DocumentByWord(parentOpt: Option[Document], textRange: TextRange, processorsSentences: Array[ProcessorsSentence]) extends Document(parentOpt, textRange) {
  override val (preSeparator, contents, postSeparator) = {
    // Processors works on the entire string, so startOffsets and endOffsets need to be adjusted.
    val offset = start
    val processorContents = processorsSentences.flatMap { sentence =>
      assert(sentence.words.length == sentence.raw.length)
      sentence.words.indices.map { index =>
        val lowerWord = sentence.words(index).toLowerCase
        val lowerRaw = sentence.raw(index).toLowerCase
        val bestString = {
            // This is particularly important for n't vs. not,
            // but it also applies to quotes.
            if (lowerWord == lowerRaw) sentence.words(index)
            else sentence.raw(index)
        }

        ContentTextRangeAndProcessorsWord(
          textRange.subRange(sentence.startOffsets(index), sentence.endOffsets(index)) + offset,
          // If only case has changed, take word.  Otherwise, take raw?
          bestString
        )
      }
    }
    val preSeparator =
      if (processorContents.isEmpty) all
      else before(processorContents.head.contentTextRange.start)
    val interSeparators = DoubleIndexedSeq(processorContents).map { case (prev, next) =>
      subRange(prev.contentTextRange.end, next.contentTextRange.start)
    }.toArray
    val postWordSeparator =
      if (processorContents.isEmpty) emptyEnd // none of it
      else after(processorContents.last.contentTextRange.end)
    val postSeparator = emptyEnd // It is used by the word.
    val words = processorContents.indices.map { index =>
      val contentTextRange = processorContents(index).contentTextRange
      val separatorTextRange = interSeparators.lift(index).getOrElse(postWordSeparator)
      val processorsWord = processorContents(index).processorsWord

      new WordDocument(Some(this), contentTextRange, separatorTextRange, processorsWord)
    }

    (newSeparator(preSeparator), words, newSeparator(postSeparator))
  }
}

case class ContentTextRangeAndProcessorsWord(contentTextRange: TextRange, processorsWord: String)

object DocumentByWord {
  lazy val tokenizer = Document.tokenizer

  def apply(text: String): DocumentByWord = apply(TextRange(text))
  def apply(textRange: TextRange): DocumentByWord = {
    val processorsSentences = tokenizer.tokenize(textRange)

    new DocumentByWord(None, textRange, processorsSentences)
  }
}

class WordDocument(parentOpt: Option[Document], contentTextRange: TextRange, separatorTextRange: TextRange, val processorsWord: String)
    extends Document(parentOpt, TextRange(contentTextRange, separatorTextRange)) {
  override val postSeparator: Separator = newSeparator(separatorTextRange)
  val charDocument: CharDocument = new CharDocument(Some(this), contentTextRange)
  override val contents: Seq[CharDocument] = Array(charDocument)
}
