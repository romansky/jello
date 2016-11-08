package com.uniformlyrandom.jello

import com.uniformlyrandom.jello.JelloValue.JelloObject

class JelloError private(ex: RuntimeException) extends Exception(ex) {
  def this(message: String) = this(new RuntimeException(message))
  def this(message: String, cause: RuntimeException) = this(new RuntimeException(message, cause))
}

object JelloError {
  case class NotAnObject(jelloValue: JelloValue) extends JelloError(s"input needs to be an object and not [${jelloValue.name}]")
  // TODO serialize the jello object inside to JSON
  case class MissingObjectField(fieldName: String, jelloObject: JelloObject)
    extends JelloError(s"field [$fieldName] in object [$jelloObject]")

}
