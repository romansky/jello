package com.uniformlyrandom.jello

sealed trait JelloErrors extends Throwable {
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

