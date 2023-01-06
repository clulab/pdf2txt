package org.clulab.pdf2txt.common.utils

import java.io.{ InputStream, ObjectInputStream, ObjectStreamClass }

class ClassLoaderObjectInputStream(cl: ClassLoader, is: InputStream) extends ObjectInputStream(is) {

  override def resolveClass(osc: ObjectStreamClass): Class[_] = {
    val cOpt = Option(Class.forName(osc.getName, false, cl))
    cOpt.getOrElse(super.resolveClass(osc))
  }
}
