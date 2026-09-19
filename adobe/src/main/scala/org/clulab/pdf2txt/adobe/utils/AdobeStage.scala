package org.clulab.pdf2txt.adobe.utils

import org.clulab.pdf2txt.common.utils.Logging

class AdobeStage(val name: String, val index: Int)

object AdobeStage extends Logging {

  def apply(string: String): AdobeStage = {
    val (name, index) = {
      val indexEnd = string.indexOf(']')

      if (0 <= indexEnd) {
        val indexStart = string.indexOf('[')
        assert(0 <= indexStart && indexStart < indexEnd)
        assert(indexEnd == string.length - 1)
        val name = string.substring(0, indexStart)
        val index = (string.substring(indexStart + 1, indexEnd)).toInt

        // The convention is that 1 is implied and explicit values start with 2.
        assert(2 <= index)
        (name, index)
      }
      else
        (string, 1)
    }

    assert(1 <= index)
    if (!AdobeNames.names.contains(name))
      logger.warn(s"""PDF element "$name" is unrecognized.""")
    new AdobeStage(name, index)
  }
}
