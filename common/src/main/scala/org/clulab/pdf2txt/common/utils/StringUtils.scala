package org.clulab.pdf2txt.common.utils

object StringUtils {

  def before(string: String, index: Int, all: Boolean, keep: Boolean): String = {
    if (index < 0)
      if (all) string
      else ""
    else string.substring(0, index + (if (keep) 1 else 0))
  }

  def beforeLast(string: String, char: Char, all: Boolean = true, keep: Boolean = false): String =
    before(string, string.lastIndexOf(char), all, keep)

  def beforeFirst(string: String, char: Char, all: Boolean = true, keep: Boolean = false): String =
    before(string, string.indexOf(char), all, keep)

  def after(string: String, index: Int, all: Boolean, keep: Boolean): String = {
    if (index < 0)
      if (all) string
      else ""
    else string.substring(index + (if (keep) 0 else 1))
  }

  def afterLast(string: String, char: Char, all: Boolean = true, keep: Boolean = false): String =
    after(string, string.lastIndexOf(char), all, keep)

  def afterFirst(string: String, char: Char, all: Boolean = true, keep: Boolean = false): String =
    after(string, string.indexOf(char), all, keep)
}
