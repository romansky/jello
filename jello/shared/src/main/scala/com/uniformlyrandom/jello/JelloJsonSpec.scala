package com.uniformlyrandom.jello

import scala.util.Try

trait JelloJsonSpec {

  def parse(jsonStr: String): JelloValue

  def toJson[T](o: T)(implicit jelloWriter: JelloWriter[T]): JelloValue
  def toJsonString[T](o: T)(implicit jelloWriter: JelloWriter[T]): String
  def toJsonString(jelloValue: JelloValue): String

  def fromJson[T](jelloValue: JelloValue)(implicit jelloReader: JelloReader[T]): Try[T]

}
