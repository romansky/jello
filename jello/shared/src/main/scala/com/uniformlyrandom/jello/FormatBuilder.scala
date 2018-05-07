package com.uniformlyrandom.jello

import com.uniformlyrandom.jello.JelloValue.{ JelloObject, JelloString }

import scala.util.{ Failure, Success }

class FormatBuilder[T] private (siblingBuilders: List[(reflect.ClassTag[_ <: T], JelloFormat[_ <: T])])(
    implicit tm: reflect.ClassTag[T]
) {

  private lazy val parentName = tm.toString()

  def withMember[M <: T](m: M)(implicit tag: reflect.ClassTag[M]): FormatBuilder[T] = {
    val isObject = tag.toString().endsWith("$")
    if (!isObject)
      throw new RuntimeException(s"`FormatBuilder.withMember()` only supports objects [${m.toString}] is not an object")

    val mFormat: JelloFormat[M] = new JelloFormat[M] {
      override def read(jelloValue: JelloValue): scala.util.Try[M] = Success(m)
      override def write(o: M): JelloValue                         = JelloObject(Map.empty[String, JelloValue])
    }
    new FormatBuilder[T]((reflect.ClassTag[M](m.getClass), mFormat) :: siblingBuilders)
  }

  def withMember[M <: T](implicit m: reflect.ClassTag[M], jelloFormat: JelloFormat[M]): FormatBuilder[T] =
    new FormatBuilder[T]((m, jelloFormat) :: siblingBuilders)

  def buildIdPropertyWithFallback(idProperty: String, fallback: JelloFormat[_ <: T])(
      implicit jelloJsonSpec: JelloJsonSpec
  ): JelloFormat[T] =
    buildIdProperty(idProperty, Some(fallback))

  def buildIdProperty(idProperty: String)(implicit jelloJsonSpec: JelloJsonSpec): JelloFormat[T] =
    buildIdProperty(idProperty, None)

  private def buildIdProperty(idProperty: String, fallback: Option[JelloFormat[_ <: T]])(
      implicit jelloJsonSpec: JelloJsonSpec
  ): JelloFormat[T] =
    new JelloFormat[T] {

      override def read(jelloValue: JelloValue): scala.util.Try[T] = {
        if (!jelloValue.isInstanceOf[JelloObject]) {
          Failure(new RuntimeException(s"expected JelloObject got [${jelloValue.getClass.getSimpleName}]"))
        } else {
          val jo = jelloValue.asInstanceOf[JelloObject]
          jo.map.get(idProperty) match {
            case Some(JelloString(formatterName)) =>
              siblingBuilders.find(
                sib ⇒
                  List(sib._1
                         .toString(),
                       sib._1.runtimeClass.getSimpleName).contains(formatterName)
              ) match {
                case Some((_, formatter)) => formatter.asInstanceOf[JelloFormat[T]].read(jelloValue)
                case None =>
                  Failure(
                    new RuntimeException(
                      s"could not find formatter for class [$formatterName] under trait [$parentName] known [${siblingBuilders
                        .map(_._1)}]"
                    )
                  )
              }
            case None if fallback.isDefined =>
              fallback.get.read(jelloValue)
            case _ =>
              Failure(
                new RuntimeException(
                  s"unexpected value at trait [$parentName] id key [$idProperty] obj [${jelloValue.toString}]"
                )
              )
          }
        }
      }

      override def write(o: T): JelloValue = {
        val clsTag = reflect.ClassTag[T](o.getClass)

        siblingBuilders
          .find(_._1 == clsTag)
          .map {
            case (name, jsonFormatter) =>
              val writtenO = jsonFormatter.asInstanceOf[JelloFormat[T]].write(o).asInstanceOf[JelloObject]
              writtenO.copy(map = writtenO.map.updated(idProperty, JelloString(clsTag.toString())))
          }
          .getOrElse(
            throw new RuntimeException(
              s"could not find a formatter under [$parentName] for [${clsTag.toString}] " +
                s"\n available formatters are [${siblingBuilders.map(_._1.toString())}]"
            )
          )

      }
    }

}

object FormatBuilder {
  def apply[T](implicit tm: reflect.ClassTag[T]) =
    new FormatBuilder[T](List.empty[(reflect.ClassTag[T], JelloFormat[_ <: T])])
}
