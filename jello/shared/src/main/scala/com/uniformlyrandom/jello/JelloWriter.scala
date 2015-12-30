package com.uniformlyrandom.jello

import scala.annotation.implicitNotFound


@implicitNotFound(
  "uPickle does not know how to write [${T}]s; define an implicit JelloWriter[${T}] to teach it how"
)
trait JelloWriter[T] {

}
