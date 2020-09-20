package com.uniformlyrandom.jello

import java.time.Instant
import com.uniformlyrandom.jello.JelloErrors.ValidationError
import com.uniformlyrandom.jello.JelloValue._
import scala.collection.Factory
import scala.util.{Failure, Success, Try}

trait TypesLibrary extends LowPriorityDefaultReads {
  // handling primitive types

  // strings
  implicit val stringReader: JelloReader[String] = new JelloReader[String] {
    override def read(jelloValue: JelloValue): Try[String] =
      jelloValue match {
        case JelloString(v: String) => Success(v)
        case unknown =>
          Failure(ValidationError(unknown, classOf[JelloString]))
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
        case unknown =>
          Failure(ValidationError(unknown, classOf[JelloNumber]))
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
        case unknown =>
          Failure(ValidationError(unknown, classOf[JelloNumber]))
      }
  }
  implicit val numberIntWriter: JelloWriter[Int] = new JelloWriter[Int] {
    override def write(o: Int): JelloValue = JelloNumber(o.toDouble)
  }
  //numbers - big decimal
  implicit val numberBigDecimalReader: JelloReader[BigDecimal] =
    new JelloReader[BigDecimal] {
      override def read(jelloValue: JelloValue): Try[BigDecimal] =
        jelloValue match {
          case JelloNumber(v: BigDecimal) => Success(v)
          case unknown =>
            Failure(ValidationError(unknown, classOf[JelloNumber]))
        }
    }
  implicit val numberBigDecimalWriter: JelloWriter[BigDecimal] =
    new JelloWriter[BigDecimal] {
      override def write(o: BigDecimal): JelloValue = JelloNumber(o)
    }
  implicit val instantWriter: JelloWriter[Instant] = new JelloWriter[Instant] {
    override def write(o: Instant): JelloValue = JelloObject(Map("epochSecond" → JelloNumber(o.getEpochSecond), "nanoOfSecond" → JelloNumber(o.getNano)))
  }
  //numbers - long
  implicit val numberLongReader: JelloReader[Long] = new JelloReader[Long] {
    override def read(jelloValue: JelloValue): Try[Long] =
      jelloValue match {
        case JelloNumber(v: BigDecimal) => Success(v.toLong)
        case unknown =>
          Failure(ValidationError(unknown, classOf[JelloNumber]))
      }
  }
  implicit val numberLongWriter: JelloWriter[Long] = new JelloWriter[Long] {
    override def write(o: Long): JelloValue = JelloNumber(BigDecimal(o))
  }
  // boolean
  implicit val boolReader: JelloReader[Boolean] = new JelloReader[Boolean] {
    override def read(jelloValue: JelloValue): Try[Boolean] =
      jelloValue match {
        case JelloBool(v) => Success(v)
        case unknown =>
          Failure(ValidationError(unknown, classOf[JelloBool]))
      }
  }
  implicit val boolWriter: JelloWriter[Boolean] = new JelloWriter[Boolean] {
    override def write(o: Boolean): JelloValue = JelloBool(o)
  }
  // handle jello values
  implicit val jelloNumberReader: JelloReader[JelloNumber] =
    new JelloReader[JelloNumber] {
      override def read(jelloValue: JelloValue): Try[JelloNumber] =
        jelloValue match {
          case x: JelloNumber => Success(x)
          case unknown =>
            Failure(
              new RuntimeException(
                s"expecting ${JelloNumber.getClass.getSimpleName} but passed ${unknown.getClass.getSimpleName}"
              )
            )
        }
    }
  implicit val jelloBoolReader: JelloReader[JelloBool] =
    new JelloReader[JelloBool] {
      override def read(jelloValue: JelloValue): Try[JelloBool] =
        jelloValue match {
          case x: JelloBool => Success(x)
          case unknown =>
            Failure(
              new RuntimeException(
                s"expecting ${JelloBool.getClass.getSimpleName} but passed ${unknown.getClass.getSimpleName}"
              )
            )
        }
    }
  implicit val jelloStringReader: JelloReader[JelloString] =
    new JelloReader[JelloString] {
      override def read(jelloValue: JelloValue): Try[JelloString] =
        jelloValue match {
          case x: JelloString => Success(x)
          case unknown =>
            Failure(
              new RuntimeException(
                s"expecting ${JelloString.getClass.getSimpleName} but passed ${unknown.getClass.getSimpleName}"
              )
            )
        }
    }

  implicit val jelloInstantReader: JelloReader[Instant] =
    new JelloReader[Instant] {
      override def read(jelloValue: JelloValue): Try[Instant] =
        jelloValue match {
          case x: JelloObject if x.map.contains("epochSecond") && x.map.contains("nanoOfSecond") =>
            Success(Instant.ofEpochSecond(x.map("epochSecond").asInstanceOf[JelloNumber].v.toLong, x.map("nanoOfSecond").asInstanceOf[JelloNumber].v.toLong))
          case _ =>
            Failure(
              new RuntimeException(
                s"""expecting {"epochSecond": 123...,"nanoOfSecond":123...} but passed ${jelloValue.toString}"""
              )
            )
        }
    }

  implicit val jelloArrayReader: JelloReader[JelloArray] =
    new JelloReader[JelloArray] {
      override def read(jelloValue: JelloValue): Try[JelloArray] =
        jelloValue match {
          case x: JelloArray => Success(x)
          case x: JelloObject if x.map.isEmpty ⇒ Success(JelloArray(Nil))
          case unknown =>
            Failure(
              new RuntimeException(
                s"expecting ${JelloArray.getClass.getSimpleName} but passed ${unknown.getClass.getSimpleName}"
              )
            )
        }
    }
  implicit val jelloNullReader: JelloReader[JelloNull.type] =
    new JelloReader[JelloNull.type] {
      override def read(jelloValue: JelloValue): Try[JelloNull.type] =
        jelloValue match {
          case JelloNull => Success(JelloNull)
          case unknown =>
            Failure(
              new RuntimeException(
                s"expecting ${JelloNull.getClass.getSimpleName} but passed ${unknown.getClass.getSimpleName}"
              )
            )
        }
    }

  implicit val jelloValueReader: JelloReader[JelloValue] =
    new JelloReader[JelloValue] {
      override def read(jelloValue: JelloValue): Try[JelloValue] =
        Success(jelloValue)
    }
  implicit val jelloValueWriter: JelloWriter[JelloValue] =
    new JelloWriter[JelloValue] {
      override def write(o: JelloValue): JelloValue = o
    }

  // option
  implicit def optionWriter[T](implicit jelloWriter: JelloWriter[T]) =
    new JelloWriter[Option[T]] {
      override def write(o: Option[T]): JelloValue =
        o.map(jelloWriter.write).getOrElse(JelloNull)
    }

  implicit def optionReader[T](implicit jelloReader: JelloReader[T]) =
    new JelloReader[Option[T]] {
      override def read(jelloValue: JelloValue): Try[Option[T]] =
        jelloValue match {
          case JelloNull => Success(None)
          case othervalue => jelloReader.read(othervalue).map(Some(_))
        }
    }

  // handle tuples, to certain extent
  implicit def tuple2Writer[T1, T2](implicit t1JelloWriter: JelloWriter[T1],
    t2JelloWriter: JelloWriter[T2]): JelloWriter[(T1, T2)] =
    new JelloWriter[(T1, T2)] {
      override def write(o: (T1, T2)): JelloValue =
        JelloArray(Seq(t1JelloWriter.write(o._1), t2JelloWriter.write(o._2)))
    }

  implicit def tuple2Reader[T1, T2](implicit t1JelloReader: JelloReader[T1],
    t2JelloReader: JelloReader[T2]): JelloReader[(T1, T2)] =
    new JelloReader[(T1, T2)] {
      override def read(jelloValue: JelloValue): Try[(T1, T2)] =
        jelloValue match {
          case JelloArray(items) if items.length == 2 =>
            Try {
              val t1v = t1JelloReader
                .read(items.head)
                .get
              val t2v = t2JelloReader.read(items.last).get
              (t1v, t2v)
            }

          case otherwise ⇒
            Failure(new RuntimeException(s"failed to convert Tuple2 as expected for key input value=[$otherwise]"))
        }
    }

  // maps with String keys
  implicit def mapReader[V](implicit vReader: JelloReader[V]): JelloReader[collection.immutable.Map[String, V]] =
    new JelloReader[Map[String, V]] {
      override def read(jelloValue: JelloValue): Try[Map[String, V]] =
        jelloValue match {
          case JelloObject(map) =>
            map
              .foldLeft(Success(Map.empty[String, V]): Try[Map[String, V]]) {
                case (fail: Failure[Map[String, V]], _) => fail
                case (Success(out), (key, value)) =>
                  vReader.read(value) match {
                    case Success(valueRead) =>
                      Success(out + (key -> valueRead))
                    case Failure(reason) =>
                      Failure(new RuntimeException(s"failed to convert Map as expected for key [$key]", reason))
                  }
              }
              .map(_.toList.reverse.toMap)

          case unknown =>
            Failure(new RuntimeException(s"was expecting JelloObject instead got [${unknown.toString}]"))
        }
    }

  implicit def mapWriter[V](implicit vWriter: JelloWriter[V]): JelloWriter[collection.immutable.Map[String, V]] =
    new JelloWriter[Map[String, V]] {
      override def write(o: Map[String, V]): JelloValue =
        JelloObject(o.toSeq.map { case (k, v) => (k, vWriter.write(v)) })
    }

  // Try[T]
  val TRY_SUCCESS_KEY = "success"
  val TRY_FAILURE_KEY = "failure"

  implicit def tryWriter[T](implicit jelloWriter: JelloWriter[T]) =
    new JelloWriter[Try[T]] {
      override def write(o: Try[T]): JelloValue =
        o.map(jelloWriter.write) match {
          case Success(jelloValue) ⇒
            JelloObject(Map(TRY_SUCCESS_KEY → jelloValue))
          case Failure(throwable) ⇒
            JelloObject(Map(TRY_FAILURE_KEY → JelloString(throwable.getMessage)))
        }
    }

  implicit def tryReader[T](implicit jelloReader: JelloReader[T]) =
    new JelloReader[Try[T]] {
      override def read(jelloValue: JelloValue): Try[Try[T]] =
        jelloValue match {
          case obj: JelloObject if obj.map.contains(TRY_SUCCESS_KEY) =>
            jelloReader.read(obj.map(TRY_SUCCESS_KEY)).map(Success(_))
          case obj: JelloObject if obj.map.contains(TRY_FAILURE_KEY) =>
            Success(Failure(new RuntimeException(obj.map(TRY_FAILURE_KEY).asInstanceOf[JelloString].v)))
          case othervalue =>
            Failure(
              new RuntimeException(
                s"serialized Try[] object needs to have either `$TRY_SUCCESS_KEY` " +
                  s"or `$TRY_FAILURE_KEY` but found neither"
              )
            )
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
  implicit def traversableReader[F[_], A](implicit bf: Factory[A, F[A]],
    ra: JelloReader[A]): JelloReader[F[A]] = new JelloReader[F[A]] {

    override def read(jelloValue: JelloValue): Try[F[A]] =
      jelloValue match {
        case JelloArray(values) =>
          values
            .foldLeft(Right(List.empty[A]): Either[Failure[F[A]], List[A]]) {
              case (left: Left[Failure[F[A]], List[A]], item) => left
              case (Right(items), item) =>
                ra.read(item) match {
                  case Success(itemRead) => Right(itemRead :: items)
                  case Failure(e: Throwable) =>
                    Left[Failure[F[A]], List[A]](Failure[F[A]](e))
                }
            } match {
            case Left(failure) => failure
            case Right(items) =>
              Success(bf.fromSpecific(items.reverse))
          }
        case unknown =>
          Failure(ValidationError(unknown, classOf[JelloArray]))
      }
  }

  implicit def traversableWriter[A](implicit wa: JelloWriter[A]): JelloWriter[Iterable[A]] =
    new JelloWriter[Iterable[A]] {
      override def write(o: scala.Iterable[A]): JelloValue =
        JelloArray(o.map(wa.write).toSeq)
    }

  implicit def arrayWriter[A](implicit wa: JelloWriter[A]): JelloWriter[Array[A]] =
    new JelloWriter[Array[A]] {
      override def write(o: Array[A]): JelloValue = JelloArray(o.map(wa.write))
    }
}

object TypesLibrary extends TypesLibrary
