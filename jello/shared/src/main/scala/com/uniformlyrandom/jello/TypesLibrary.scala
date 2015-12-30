package com.uniformlyrandom.jello

import com.uniformlyrandom.jello.JelloErrors.ValidationError
import com.uniformlyrandom.jello.JelloValue.JelloString

import scala.util.{Failure, Success, Try}

object TypesLibrary {

  implicit val stringReader: JelloReader[String] = new JelloReader[String] {
    override def read(jelloValue: JelloValue): Try[String] =
      jelloValue match {
        case JelloString(v: String) => Success(v)
        case unknown => Failure(new ValidationError(unknown, classOf[JelloString]))
      }
  }

}
