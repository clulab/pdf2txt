package org.clulab.pdf2txt.common.utils

class Holder[T](protected var value: T) {

  def set(value: T): T = {
    this.value = value
    value
  }

  def get(): T = value
}
