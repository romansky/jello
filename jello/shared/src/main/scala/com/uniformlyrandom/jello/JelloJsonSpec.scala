package com.uniformlyrandom.jello

import com.uniformlyrandom.jello.JelloValue.JelloObject

import scala.util.Try

trait JelloJsonSpec {

  def parse(jsonStr: String): JelloValue

  def toJson[T](o: T)(implicit jelloWriter: JelloWriter[T]): JelloValue
  def toJsonString[T](o: T)(implicit jelloWriter: JelloWriter[T]): String
  def toJsonString(jelloValue: JelloValue): String

  def fromJson[T](jelloValue: JelloValue)(
      implicit jelloReader: JelloReader[T]): Try[T]

  def createWithResetFields[T](fields: List[String])(objectJson: String)(
      implicit jelloReader: JelloReader[T]): Try[T] =
    parse(objectJson) match {
      case o: JelloObject ⇒
        val resetObj = new JelloObject(
          o.map.filterNot(o ⇒ fields.contains(o._1)))
        fromJson(resetObj)
      case _ ⇒
        throw new RuntimeException(s"json is not an object [$objectJson]")
    }

}
