package com.uniformlyrandom.jello

import scala.util.Try

object JelloJson extends JelloJsonSpec {

  override def parse(jsonStr: String): JelloValue = ???

  override def toJson[T](o: T)(implicit jelloWriter: JelloWriter[T]): JelloValue = ???

  override def fromJson[T](jelloValue: JelloValue)(implicit jelloReader: JelloReader[T]): Try[T] = ???

}
