package com.uniformlyrandom.jello

import scala.util.Try

trait JelloJsonSpec {

  def parse(jsonStr: String): JelloValue

  def toJson[T](o: T)(implicit jelloWriter: JelloWriter[T]): JelloValue

  def fromJson[T](jelloValue: JelloValue)(implicit jelloReader: JelloReader[T]): Try[T]

}
