package org.clulab.pdf2txt.languageModel

import org.clulab.embeddings.CompactWordEmbeddingMap.{BuildType, mkMapFromText}
import org.clulab.pdf2txt.common.utils.StringUtils
import org.clulab.utils.ClassLoaderObjectInputStream
import org.clulab.utils.Closer.AutoCloser

class DictionaryLanguageModel extends LanguageModel {
  val words = {
    val resource = "org/clulab/pdf2txt/dict.ser"
    val classLoader = this.getClass.getClassLoader

    new ClassLoaderObjectInputStream(classLoader, classLoader.getResourceAsStream(resource)).autoClose { objectInputStream =>
      val string = objectInputStream.readObject().asInstanceOf[String]

      string.split(" ").toSet
    }
  }

  override def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean = {
    val leftAfterHyphen = StringUtils.afterLast(left, StringUtils.HYPHEN, all = true)
    val rightBeforeHyphen = StringUtils.beforeFirst(right, StringUtils.HYPHEN, all = true)
    val contains = words.contains(leftAfterHyphen + rightBeforeHyphen)
    val betweenDigits = leftAfterHyphen.last.isDigit && rightBeforeHyphen.head.isDigit

    contains && !betweenDigits
  }
}
