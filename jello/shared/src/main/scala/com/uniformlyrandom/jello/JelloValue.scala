package com.uniformlyrandom.jello

sealed trait JelloValue

object JelloValue {
  case class JelloNumber(v: Double) extends JelloValue
  case class JelloBool(v: Boolean) extends JelloValue
  case class JelloString(v: String) extends JelloValue
  case class JelloObject(map: Map[String, JelloValue]) extends JelloValue
  case class JelloArray(list: Seq[JelloValue]) extends JelloValue
  case object JelloNull extends JelloValue
}

