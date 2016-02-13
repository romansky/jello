package com.uniformlyrandom.jello

import scala.language.existentials

sealed trait JelloErrors extends RuntimeException {
  def message: String
  def input: JelloValue
}

object JelloErrors {

  case class ValidationError(
    input: JelloValue,
    expectedType: Class[_ <: JelloValue]
  ) extends JelloErrors {
    override def message: String  = "unexpected input"
  }

}

