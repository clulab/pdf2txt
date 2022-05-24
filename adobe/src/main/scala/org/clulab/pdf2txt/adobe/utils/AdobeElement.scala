package org.clulab.pdf2txt.adobe.utils

import org.json4s.{JObject, JString}

case class AdobeElement(adobePath: AdobePath, text: String) {

  def name: String = adobePath.name

  def inTable: Boolean = adobePath.hasTable
}

object AdobeElement {

  def apply(jObject: JObject): AdobeElement = {
    val path = (jObject \ "Path").asInstanceOf[JString].values
    val text = jObject.values.get("Text")
        .map(_.asInstanceOf[String])
        .getOrElse("")

    new AdobeElement(AdobePath(path), text)
  }
}
