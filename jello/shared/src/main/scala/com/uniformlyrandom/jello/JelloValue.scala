package com.uniformlyrandom.jello

// trait extends Any so that implementing classes can be value classes
// see http://docs.scala-lang.org/overviews/core/value-classes.html
sealed trait JelloValue extends Any {
  def name = getClass.getSimpleName.replace("$","")


}

object JelloValue {
  case class JelloNumber(v: BigDecimal) extends AnyVal with JelloValue
  case class JelloBool(v: Boolean) extends AnyVal with JelloValue
  case class JelloString(v: String) extends AnyVal with JelloValue
  case class JelloObject(map: Map[String, JelloValue]) extends JelloValue
  case class JelloArray(seq: Seq[JelloValue]) extends JelloValue
  case object JelloNull extends JelloValue

  object JelloObject {
    def apply(fields: Seq[(String,JelloValue)]): JelloObject = JelloObject(fields.toMap)
  }
}

