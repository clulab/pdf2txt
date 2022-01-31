package org.clulab.pdf2txt.languageModel

import org.clulab.pdf2txt.common.utils.{StringUtils, TextRange, TripleIndexedSeq, TripleOptIndexedSeq}
import org.clulab.pdf2txt.document.logical.{DocumentByWord, WordDocument}
import org.clulab.utils.ClassLoaderObjectInputStream
import org.clulab.utils.Closer.AutoCloser

class SetLanguageModel(val words: Set[String]) extends LanguageModel {

  override def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean = {
    lazy val contains = words.contains(left + right)
    lazy val betweenDigits = left.last.isDigit || right.head.isDigit
    lazy val containsBetweenHyphens = {
      val leftAfterHyphen = StringUtils.afterLast(left, StringUtils.HYPHEN, all = true)
      val rightBeforeHyphen = StringUtils.beforeFirst(right, StringUtils.HYPHEN, all = true)

      words.contains(leftAfterHyphen + rightBeforeHyphen)
    }

    (contains || containsBetweenHyphens) && !betweenDigits
  }

  // This is used for ligatures like "coe ffi cient".
  override def shouldJoin(left: String, middle: String, right: String, prevWords: Seq[String]): Boolean = {
    lazy val contains = words.contains(left + middle + right)
    lazy val betweenDigits = left.last.isDigit || middle.head.isDigit || middle.last.isDigit || right.head.isDigit
    lazy val containsBetweenHyphens = !middle.exists(_ == StringUtils.HYPHEN) && {
      val leftAfterHyphen = StringUtils.afterLast(left, StringUtils.HYPHEN, all = true)
      val rightBeforeHyphen = StringUtils.beforeFirst(right, StringUtils.HYPHEN, all = true)

      words.contains(leftAfterHyphen + middle + rightBeforeHyphen)
    }

    (contains || containsBetweenHyphens) && !betweenDigits
  }
}

object GloveLanguageModel {

  def apply(): SetLanguageModel = {
    val words: Set[String] = {
      val resource = "org/clulab/pdf2txt/dict.ser"
      val classLoader = this.getClass.getClassLoader

      new ClassLoaderObjectInputStream(classLoader, classLoader.getResourceAsStream(resource)).autoClose { objectInputStream =>
        val string = objectInputStream.readObject().asInstanceOf[String]

        string.split(" ").toSet
      }
    }

    new SetLanguageModel(words)
  }
}

object LocalSetLanguageModel {

  def isHyphen(wordDocument: WordDocument): Boolean = StringUtils.WORD_BREAK_CHARS.exists(wordDocument.contents.head.matches)

  def apply(textRange: TextRange): SetLanguageModel = {
    val documentByWord = DocumentByWord(textRange)
    val hyphenIndexes = TripleOptIndexedSeq(documentByWord.contents.indices).flatMap { tripleOpt =>
      tripleOpt match {
        case (None, Some(prevIndex), Some(nextIndex)) =>
          if (isHyphen(documentByWord.contents(prevIndex))) Seq(prevIndex, nextIndex)
          else Seq.empty
        case (Some(prevIndex), Some(hyphenIndex), Some(nextIndex)) =>
          if (isHyphen(documentByWord.contents(hyphenIndex))) Seq(prevIndex, hyphenIndex, nextIndex)
          else Seq.empty
        case (Some(prevIndex), Some(nextIndex), None) =>
          if (isHyphen(documentByWord.contents(nextIndex))) Seq(prevIndex, nextIndex)
          else Seq.empty
        case _ => Seq.empty
      }
    }.toSet
    val words: Set[String] = documentByWord.contents.indices
        .filter { index =>
          val wordDocument = documentByWord.contents(index)

          !hyphenIndexes.contains(index) && !isHyphen(wordDocument)
        }
        .map(documentByWord.contents(_).processorsWord)
        .toSet

    new SetLanguageModel(words)
  }
}
