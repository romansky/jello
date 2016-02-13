package com.uniformlyrandom.jello

import com.uniformlyrandom.jello.JelloErrors.ValidationError
import com.uniformlyrandom.jello.JelloValue._

import scala.collection.{Traversable, generic}
import scala.util.{Failure, Success, Try}

import scala.language.higherKinds

trait TypesLibrary extends LowPriorityDefaultReads {
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
  //numbers - double
  implicit val numberReader: JelloReader[Double] = new JelloReader[Double] {
    override def read(jelloValue: JelloValue): Try[Double] =
      jelloValue match {
        case JelloNumber(v: BigDecimal) => Success(v.toDouble)
        case unknown => Failure(new ValidationError(unknown, classOf[JelloNumber]))
      }
  }
  implicit val numberWriter: JelloWriter[Double] = new JelloWriter[Double] {
    override def write(o: Double): JelloValue = JelloNumber(o)
  }
  //numbers - int
  implicit val numberIntReader: JelloReader[Int] = new JelloReader[Int] {
    override def read(jelloValue: JelloValue): Try[Int] =
      jelloValue match {
        case JelloNumber(v: BigDecimal) => Success(v.toInt)
        case unknown => Failure(new ValidationError(unknown, classOf[JelloNumber]))
      }
  }
  implicit val numberIntWriter: JelloWriter[Int] = new JelloWriter[Int] {
    override def write(o: Int): JelloValue = JelloNumber(o.toDouble)
  }
  //numbers - big decimal
  implicit val numberBigDecimalReader: JelloReader[BigDecimal] = new JelloReader[BigDecimal] {
    override def read(jelloValue: JelloValue): Try[BigDecimal] =
      jelloValue match {
        case JelloNumber(v: BigDecimal) => Success(v)
        case unknown => Failure(new ValidationError(unknown, classOf[JelloNumber]))
      }
  }
  implicit val numberBigDecimalWriter: JelloWriter[BigDecimal] = new JelloWriter[BigDecimal] {
    override def write(o: BigDecimal): JelloValue = JelloNumber(o)
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
  // option
  implicit def optionWriter[T](implicit jelloWriter: JelloWriter[T]) = new JelloWriter[Option[T]] {
    override def write(o: Option[T]): JelloValue =
      o.map(jelloWriter.write).getOrElse(JelloNull)
  }

  implicit def optionReader[T](implicit jelloReader: JelloReader[T]) = new JelloReader[Option[T]] {
    override def read(jelloValue: JelloValue): Try[Option[T]] =
      jelloValue match {
        case JelloNull => Success(None)
        case othervalue => jelloReader.read(othervalue).map(Some(_))
      }
  }

}

/**
  * Low priority reads.
  *
  * This exists as a compiler performance optimisation, so that the compiler doesn't have to rule them out when
  * DefaultReads provides a simple match.
  *
  * See https://github.com/playframework/playframework/issues/4313 for more details.
  */
trait LowPriorityDefaultReads {
  // traversable
  implicit def traversableReader[F[_], A](implicit bf: generic.CanBuildFrom[F[_], A, F[A]], ra: JelloReader[A]): JelloReader[F[A]] = new JelloReader[F[A]] {

    override def read(jelloValue: JelloValue): Try[F[A]] =
      jelloValue match {
        case JelloArray(values) =>
          values.foldLeft(Right(List.empty[A]): Either[Failure[F[A]],List[A]]) {
            case (left: Left[Failure[F[A]],List[A]], item) => left
            case (Right(items), item)=> ra.read(item) match {
              case Success(itemRead) => Right(itemRead :: items)
              case Failure(e: Throwable)=> Left[Failure[F[A]],List[A]](Failure[F[A]](e))
            }
          } match {
            case Left(failure)=> failure
            case Right(items)=>
              val builder = bf()
              builder.sizeHint(items)
              builder ++= items.reverse
              Success(builder.result())
          }
        case unknown => Failure(new ValidationError(unknown, classOf[JelloArray]))
      }
  }

  implicit def traversableWriter[A](implicit wa: JelloWriter[A]): JelloWriter[Traversable[A]] = new JelloWriter[Traversable[A]] {
    override def write(o: scala.Traversable[A]): JelloValue = JelloArray(o.map(wa.write).toSeq)
  }

  implicit def arrayWriter[A](implicit wa: JelloWriter[A]): JelloWriter[Array[A]] = new JelloWriter[Array[A]] {
    override def write(o: Array[A]): JelloValue = JelloArray(o.map(wa.write))
  }
}


object TypesLibrary extends TypesLibrary