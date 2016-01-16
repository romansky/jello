package com.uniformlyrandom.jello

import scala.annotation.implicitNotFound


@implicitNotFound(
  "don't know how to write [${T}]s; define an implicit JelloWriter[${T}] to fix"
)
trait JelloWriter[T] {

  def write(o: T): JelloValue

}
