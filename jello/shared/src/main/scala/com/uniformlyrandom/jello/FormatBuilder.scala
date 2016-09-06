package com.uniformlyrandom.jello

import com.uniformlyrandom.jello.JelloValue.{JelloString, JelloObject}

import scala.util.{Failure, Try}

class FormatBuilder[T] private (siblingBuilders: List[(reflect.ClassTag[_ <: T], JelloFormat[_ <: T])])(implicit tm: reflect.ClassTag[T]) {

  private lazy val parentName = tm.toString()

  def withMember[M <: T](m: M)(implicit jelloFormat: JelloFormat[M]): FormatBuilder[T] =
    new FormatBuilder[T]((reflect.ClassTag[M](m.getClass), jelloFormat) :: siblingBuilders)

  def withMember[M <: T](implicit m: reflect.ClassTag[M], jelloFormat: JelloFormat[M]): FormatBuilder[T] =
    new FormatBuilder[T]((m, jelloFormat) :: siblingBuilders)


  def buildIdProperty(idProperty: String)(implicit jelloJsonSpec: JelloJsonSpec): JelloFormat[T] =
    new JelloFormat[T] {

      override def read(jelloValue: JelloValue): Try[T] = {
        if (!jelloValue.isInstanceOf[JelloObject]){
          Failure(new RuntimeException(s"expected JelloObject got [${jelloValue.getClass.getSimpleName}]"))
        } else {
          val jo = jelloValue.asInstanceOf[JelloObject]
          jo.map.get(idProperty).map {
            case JelloString(formatterName) =>
              siblingBuilders.find(_._1.toString() == formatterName) match {
                case Some((_, formatter))=> formatter.asInstanceOf[JelloFormat[T]].read(jelloValue)
                case None => Failure(new RuntimeException(s"could not find formatter for class [$formatterName] under trait [$parentName] known [${siblingBuilders.map(_._1)}]"))
              }
            case _ => Failure(new RuntimeException(s"unexpected value at trait [$parentName] id key [$idProperty] obj [${jelloValue.toString}]"))
          }.getOrElse(Failure(new RuntimeException(s"could not find property [$parentName] under key [$idProperty] obj [${jelloValue.toString}]")))
        }
      }

      override def write(o: T): JelloValue = {
        val clsTag = reflect.ClassTag[T](o.getClass)

        siblingBuilders.find(_._1 == clsTag).map {
          case (name, jsonFormatter)=>
            val writtenO = jsonFormatter.asInstanceOf[JelloFormat[T]].write(o).asInstanceOf[JelloObject]
            writtenO.copy(map = writtenO.map.updated(idProperty,JelloString(clsTag.toString())))
        }.getOrElse(
          throw new RuntimeException(s"could not find a formatter under [$parentName] for [${clsTag.toString}]")
        )

      }
    }

}

object FormatBuilder {
  def apply[T](implicit tm: reflect.ClassTag[T]) = new FormatBuilder[T](List.empty[(reflect.ClassTag[T], JelloFormat[_ <: T])])
}