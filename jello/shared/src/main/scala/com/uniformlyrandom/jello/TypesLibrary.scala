package com.uniformlyrandom.jello

import com.uniformlyrandom.jello.JelloError.{MissingObjectField, NotAnObject}
import com.uniformlyrandom.jello.JelloErrors.ValidationError
import com.uniformlyrandom.jello.JelloValue.{JelloObject, JelloBool, JelloNumber, JelloString}

import scala.util.{Failure, Success, Try}

trait TypesLibrary {

  // handling primitive types
  // strings
  implicit val stringReader: JelloReader[String] = new JelloReader[String] {
    override def read(jelloValue: JelloValue): Try[String] =
      jelloValue match {
        case JelloString(v: String) => Success(v)
        case unknown => Failure(new ValidationError(unknown, classOf[JelloString]))
      }
  }
  implicit val stringWriter: JelloWriter[String] = new JelloWriter[String] {
    override def write(o: String): JelloValue = JelloString(o)
  }
  //double
  implicit val numberReader: JelloReader[Double] = new JelloReader[Double] {
    override def read(jelloValue: JelloValue): Try[Double] =
      jelloValue match {
        case JelloNumber(v: Double) => Success(v)
        case unknown => Failure(new ValidationError(unknown, classOf[JelloNumber]))
      }
  }
  implicit val numberWriter: JelloWriter[Double] = new JelloWriter[Double] {
    override def write(o: Double): JelloValue = JelloNumber(o)
  }
  // boolean
  implicit val boolReader: JelloReader[Boolean] = new JelloReader[Boolean] {
    override def read(jelloValue: JelloValue): Try[Boolean] =
      jelloValue match {
        case JelloBool(v) => Success(v)
        case unknown => Failure(new ValidationError(unknown, classOf[JelloBool]))
      }
  }
  implicit val boolWriter: JelloWriter[Boolean] = new JelloWriter[Boolean] {
    override def write(o: Boolean): JelloValue = JelloBool(o)
  }
  // int
  implicit val numberIntReader: JelloReader[Int] = new JelloReader[Int] {
    override def read(jelloValue: JelloValue): Try[Int] =
      jelloValue match {
        case JelloNumber(v: Double) => Success(v.toInt)
        case unknown => Failure(new ValidationError(unknown, classOf[JelloNumber]))
      }
  }
  implicit val numberIntWriter: JelloWriter[Int] = new JelloWriter[Int] {
    override def write(o: Int): JelloValue = JelloNumber(o.toDouble)
  }
}