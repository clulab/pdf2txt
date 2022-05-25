package org.clulab.pdf2txt.common.utils

import java.io.File
import java.nio.file.Paths

class FileEditor(protected var file: File) {
  import FileEditor._

  def get = file

  def setDir(dir: String): FileEditor = {
    val name = file.getName
    val newPath = Paths.get(dir, name).toString

    file = new File(newPath)
    this
  }

  def incDir(dir: String): FileEditor = {
    val name = file.getName
    val pathOpt = Option(file.getParentFile)
    val newPath =
      if (pathOpt.isDefined)
        Paths.get(Paths.get(pathOpt.get.getPath, dir).toString, name).toString
      else Paths.get(dir, name).toString

    file = new File(newPath)
    this
  }

  def setName(name: String): FileEditor = {
    val pathOpt = Option(file.getParentFile)
    val newPath =
      if (pathOpt.isDefined) Paths.get(pathOpt.get.getPath, name).toString
      else name

    file = new File(newPath)
    this
  }

  def incName(name: String): FileEditor = {
    file = new File(file.getPath + name)
    this
  }

  def distinguish(distinguisher: Int): FileEditor = {
    // Add s"-$distinguisher" to name before the extension
    // Note the beforeFirst, so that keep everything with a period at the very end.
    val extensionless = StringUtils.beforeFirst(file.getName, '.', all = true)
    val extension = file.getName.substring(extensionless.length)

    // A dot gets added here, so one was expected before.
    setName(s"$extensionless-$distinguisher.$extension")
  }

  def setExt(ext: String, last: Boolean = true): FileEditor = {
    // Being an extension implies that it starts with a period, but don't add a second period,
    // because then it's not an extension but something else.
    val dottedExt = if (ext.startsWith(dots)) ext else dots + ext
    val path = file.getPath
    val extensionlessPath =
      if (last) StringUtils.beforeLast(path, dot)
      else StringUtils.beforeFirst(path, dot)
    val newPath = extensionlessPath + dottedExt

    file = new File(newPath)
    this
  }
}

object FileEditor {
  val dot = '.'
  val dots = dot.toString

  def apply(file: File): FileEditor = new FileEditor(file)
}
