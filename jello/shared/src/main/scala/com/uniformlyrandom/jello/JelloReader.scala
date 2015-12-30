package com.uniformlyrandom.jello

import scala.annotation.implicitNotFound
import scala.util.{Try, Success}

@implicitNotFound(
  "uPickle does not know how to read [${T}]s; define an implicit Reader[${T}] to teach it how"
)
trait JelloReader[T] {

  def read(jelloValue: JelloValue): Try[T]

}



object JelloReader {

  def readJson[T](jsonString: String)(implicit reader: JelloReader[T]): Try[T] = ???

  def read[T](jelloValue: JelloValue)(implicit reader: JelloReader[T]): Try[T] = reader.read(jelloValue)


}