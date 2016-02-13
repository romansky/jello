package com.uniformlyrandom.jello

import com.uniformlyrandom.jello.JelloValue.{JelloString, JelloObject}

import scala.util.{Failure, Try}

class FormatBuilder[T] private (siblingBuilders: List[(String, JelloFormat[_ <: T])])(implicit tm: reflect.ClassTag[T]) {

  def withMember[M <: T](implicit m: reflect.ClassTag[M], jelloFormat: JelloFormat[M]): FormatBuilder[T] =
    new FormatBuilder[T]((m.runtimeClass.getSimpleName, jelloFormat) :: siblingBuilders)


  def buildIdProperty(idProperty: String)(implicit jelloJsonSpec: JelloJsonSpec): JelloFormat[T] =
    new JelloFormat[T] {

      override def read(jelloValue: JelloValue): Try[T] = {
        if (!jelloValue.isInstanceOf[JelloObject]){
          Failure(new RuntimeException(s"expected JelloObject got [${jelloValue.getClass.getSimpleName}]"))
        } else {
          val jo = jelloValue.asInstanceOf[JelloObject]
          jo.map.get(idProperty).map {
            case JelloString(formatterName) =>
              siblingBuilders.find(_._1 == formatterName) match {
                case Some((_, formatter))=> formatter.asInstanceOf[JelloFormat[T]].read(jelloValue)
                case None => Failure(new RuntimeException(s"could not find formatter for class [$formatterName] under trait [${tm.getClass.getSimpleName}]"))
              }
            case _ => Failure(new RuntimeException(s"unexpected value at trait [${tm.getClass.getSimpleName}] id key [$idProperty] obj [${jelloValue.toString}]"))
          }.getOrElse(Failure(new RuntimeException(s"could not find property [${tm.getClass.getSimpleName}] under key [$idProperty] obj [${jelloValue.toString}]")))
        }
      }

      override def write(o: T): JelloValue = {
        val clsName = o.getClass.getSimpleName

        siblingBuilders.find(_._1 == clsName).map {
          case (name, jsonFormatter)=>
            val writtenO = jsonFormatter.asInstanceOf[JelloFormat[T]].write(o).asInstanceOf[JelloObject]
            writtenO.copy(map = writtenO.map.updated(idProperty,JelloString(clsName)))
        }.getOrElse(
          throw new RuntimeException(s"could not find a formatter under [${tm.getClass.getSimpleName}] for [$clsName]")
        )

      }
    }

}

object FormatBuilder {
  def apply[T](implicit tm: reflect.ClassTag[T]) = new FormatBuilder[T](List.empty[(String, JelloFormat[_ <: T])])
}