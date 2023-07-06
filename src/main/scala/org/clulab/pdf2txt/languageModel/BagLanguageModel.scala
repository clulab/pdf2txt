package org.clulab.pdf2txt.languageModel

import org.clulab.pdf2txt.common.utils.ClassLoaderObjectInputStream
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.StringUtils
import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.common.utils.TripleOptIndexedSeq
import org.clulab.pdf2txt.document.logical.DocumentByWord
import org.clulab.pdf2txt.document.logical.WordDocument

import scala.collection.mutable

class BagLanguageModel(val wordFrequencies: Map[String, Int], lowercase: Boolean = false, lowerLimit: Int = 1) extends LanguageModel {
  // TODO: Should the frequency of the joined and unjoined words be compared?
  // Need to know if both hyphenated and unhyphenated variations are contained
  // and then pick the one with the highest frequency.
  // In that case we need shouldJoinWithoutHyphen to distinguish between joins without spaces.

  override def shouldJoin(rawLeft: String, rawRight: String, prevWords: Seq[String]): Boolean = {
    val left = if (lowercase) rawLeft.toLowerCase else rawLeft
    val right = if (lowercase) rawRight.toLowerCase else rawRight

    lazy val contains = wordFrequencies.getOrElse(left + right, 0) >= lowerLimit
    lazy val betweenDigits = left.last.isDigit || right.head.isDigit
    lazy val containsBetweenHyphens = {
      val leftAfterHyphen = StringUtils.afterLast(left, StringUtils.HYPHEN, all = true)
      val rightBeforeHyphen = StringUtils.beforeFirst(right, StringUtils.HYPHEN, all = true)

      wordFrequencies.getOrElse(leftAfterHyphen + rightBeforeHyphen, 0) >= lowerLimit
    }

    (contains || containsBetweenHyphens) && !betweenDigits
  }

  // This is used for ligatures like "coe ffi cient".
  def shouldJoinWithMiddle(rawLeft: String, rawMiddle: String, rawRight: String, prevWords: Seq[String]): Boolean = {
    val left = if (lowercase) rawLeft.toLowerCase else rawLeft
    val middle = if (lowercase) rawMiddle.toLowerCase else rawMiddle
    val right = if (lowercase) rawRight.toLowerCase else rawRight

    lazy val contains = wordFrequencies(left + middle + right) >= lowerLimit
    lazy val betweenDigits = left.last.isDigit || middle.head.isDigit || middle.last.isDigit || right.head.isDigit
    lazy val containsBetweenHyphens = !middle.exists(_ == StringUtils.HYPHEN) && {
      val leftAfterHyphen = StringUtils.afterLast(left, StringUtils.HYPHEN, all = true)
      val rightBeforeHyphen = StringUtils.beforeFirst(right, StringUtils.HYPHEN, all = true)

      wordFrequencies(leftAfterHyphen + middle + rightBeforeHyphen) >= lowerLimit
    }

    (contains || containsBetweenHyphens) && !betweenDigits
  }

  // This is used for hyphenations like "imple - ment".
  def shouldJoinWithoutMiddle(rawLeft: String, rawMiddle: String, rawRight: String, prevWords: Seq[String], withMiddle: Boolean = true): Boolean = {
    val left = if (lowercase) rawLeft.toLowerCase else rawLeft
    val middle = if (lowercase) rawMiddle.toLowerCase else rawMiddle
    val right = if (lowercase) rawRight.toLowerCase else rawRight

    lazy val contains = wordFrequencies(left + right) >= lowerLimit
    lazy val betweenDigits = left.last.isDigit || middle.head.isDigit || middle.last.isDigit || right.head.isDigit
    lazy val containsBetweenHyphens = !middle.exists(_ == StringUtils.HYPHEN) && {
      val leftAfterHyphen = StringUtils.afterLast(left, StringUtils.HYPHEN, all = true)
      val rightBeforeHyphen = StringUtils.beforeFirst(right, StringUtils.HYPHEN, all = true)

      wordFrequencies(leftAfterHyphen + rightBeforeHyphen) >= lowerLimit
    }

    (contains || containsBetweenHyphens) && !betweenDigits
  }

  override def shouldJoin(rawLeft: String, rawMiddle: String, rawRight: String, prevWords: Seq[String], withMiddle: Boolean = true): Boolean = {
    if (withMiddle) shouldJoinWithMiddle(rawLeft, rawMiddle, rawRight, prevWords)
    else shouldJoinWithoutMiddle(rawLeft, rawMiddle, rawRight, prevWords)
  }
}

object GigawordLanguageModel {

  def apply(): BagLanguageModel = {
    val wordFrequencies: Map[String, Int] = {
      val resource = "org/clulab/pdf2txt/gigaword.ser"
      val classLoader = this.getClass.getClassLoader

      new ClassLoaderObjectInputStream(classLoader, classLoader.getResourceAsStream(resource)).autoClose { objectInputStream =>
        val string = objectInputStream.readObject().asInstanceOf[String].split(' ')
        val counts = objectInputStream.readObject().asInstanceOf[Array[Int]]

        string.zip(counts).toMap.withDefaultValue(0)
      }
    }

    new BagLanguageModel(wordFrequencies, lowercase = true, lowerLimit = 5)
  }
}

object LocalBagLanguageModel {

  def isHyphen(wordDocument: WordDocument): Boolean = StringUtils.WORD_BREAK_CHARS.exists(wordDocument.contents.head.matches)

  def apply(textRange: TextRange): BagLanguageModel = {
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
    }
    val words: Seq[String] = documentByWord.contents.indices
        .filter { index =>
          val wordDocument = documentByWord.contents(index)

          !hyphenIndexes.contains(index) && !isHyphen(wordDocument)
        }
        .map(documentByWord.contents(_).processorsWord)
    val wordFrequencies = {
      val wordFrequencies = new mutable.HashMap[String, Int]().withDefaultValue(0)

      words.foreach { word =>
        wordFrequencies.update(word, wordFrequencies(word) + 1)
      }
      wordFrequencies.toMap.withDefaultValue(0)
    }
    new BagLanguageModel(wordFrequencies, lowercase = false, lowerLimit = 1)
  }
}
