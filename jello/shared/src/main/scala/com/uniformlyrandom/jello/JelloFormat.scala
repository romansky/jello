package com.uniformlyrandom.jello

import com.uniformlyrandom.jello.JelloValue.JelloString

import scala.annotation.implicitNotFound
import scala.language.experimental.macros
import scala.util.{Failure, Success, Try}


@implicitNotFound(
  "No JelloFormat found for type [${A}]. Try to implement an implicit JelloFormat for this type."
)
trait JelloFormat[A] extends JelloWriter[A] with JelloReader[A]

object JelloFormat extends TypesLibrary {

  /**
    *
    * @tparam T a trait
    * @return
    */
  def formatTrait[T](implicit tm: reflect.ClassTag[T]): FormatBuilder[T] = FormatBuilder[T]

  def formatEnumeration[E <: Enumeration](enum: E): JelloFormat[E#Value] = new JelloFormat[E#Value] {

    override def read(jelloValue: JelloValue): Try[E#Value] = jelloValue match {
      case JelloString(s) =>
        try {
          Success(enum.withName(s))
        } catch {
          case _: NoSuchElementException=> Failure(new RuntimeException(s"could not find enum member with value [$s]"))
        }

      case _=> Failure(new RuntimeException(s"string value expected got [$jelloValue]"))
    }

    import scala.language.implicitConversions
    override def write(o: E#Value): JelloValue = JelloString(o.toString)
  }

  def format[A]: JelloFormat[A] = macro formatMacroImpl[A]

  implicit def implicitFormat[A](implicit reader: JelloReader[A], writer: JelloWriter[A]) =
    JelloFormat[A](reader, writer)

  def apply[A](reader: JelloReader[A], writer: JelloWriter[A]) = new JelloFormat[A] {
    override def read(jelloValue: JelloValue): Try[A] = reader.read(jelloValue)
    override def write(o: A): JelloValue = writer.write(o)
  }
  import scala.reflect.macros.blackbox
  // TODO harden code to check its working with case class, only.
  def formatMacroImpl[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[JelloFormat[T]] = {
    import c.universe._
    val tpe = weakTypeTag[T].tpe
    val tpeComp = weakTypeOf[T].companion

    if (tpeComp.orElse(null) == null)
      c.abort(c.enclosingPosition, s"${tpe.toString} has no companion defined")

    if (tpe.typeSymbol.asClass.isCaseClass){
      val tc = tpe.typeSymbol.companion
      val compApply = Select(Ident(tc), TermName("apply"))

      val classMembers = tpe.members.toList.filter(m=> !m.isMethod)

      val readMemberValues = classMembers
        .foldLeft(List.empty[(c.universe.TermName,c.universe.Tree)]) { case (outList,m) =>
          val typeSig = m.typeSignature
          val nameSafe = TermName(s"${m.name.toString.trim}_value")
          val nameString = m.name.toString.trim


          // for options allow JelloNull value
          val (method,args) = if (typeSig =:= typeOf[Option[_]])
              (TermName("getOrElse"),List(q"$nameString", q"JelloNull"))
            else
              (TermName("get"),List(q"$nameString"))

          (nameSafe ->
            q"""val $nameSafe: $typeSig = valuesMap.$method(...$args)
               .map(implicitly[com.uniformlyrandom.jello.JelloFormat[$typeSig]].read)
               .map(_.toOption)
               .flatten[$typeSig]
               .getOrElse[$typeSig](throw new MissingObjectField($nameString, o))""") :: outList
        }

      val writeValues = classMembers.reverse
        .foldLeft(Map.empty[TermName, Type]) { case (outMap, m) =>
            outMap + (TermName(m.name.decodedName.toTermName.toString.trim) -> m.typeSignature)
        }.map {
          mv => q"""(${mv._1.toString},implicitly[com.uniformlyrandom.jello.JelloFormat[${mv._2}]].write(o.${mv._1}))"""
        }

      // TODO do we need our own exception class?
      val out = c.Expr[JelloFormat[T]](q"""
      new JelloFormat[$tpe] {
        import scala.util.Try
        import com.uniformlyrandom.jello.JelloValue
        import com.uniformlyrandom.jello.JelloValue._
        import scala.util.control.NonFatal
        import com.uniformlyrandom.jello.TypesLibrary._
        import java.lang.RuntimeException
        import com.uniformlyrandom.jello.JelloError._

        override def read(jelloValue: JelloValue): Try[$tpe] = {
          try {
            jelloValue match {
              case o @ JelloObject(valuesMap) =>
                ..${readMemberValues.map(_._2)}
                Try($compApply(..${readMemberValues.map(_._1)}))
              case unknown: JelloValue => throw new NotAnObject(unknown)
            }
          } catch {
            case e: RuntimeException=> throw e
            case NonFatal(e)=>
              throw new RuntimeException("JelloFormat: failed reading [" + ${tpe.toString} + "] [" + jelloValue + "]")
          }
        }
        override def write(o: $tpe): JelloValue =
          JelloObject(scala.collection.immutable.Map(..$writeValues))
      }
      """)
      //println(showCode(out.tree))
      out
    } else {
      c.abort(c.enclosingPosition, tpe.typeSymbol.fullName + " is not a case class")
    }

  }

}
