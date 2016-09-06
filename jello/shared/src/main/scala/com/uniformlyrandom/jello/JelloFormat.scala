package com.uniformlyrandom.jello

import com.uniformlyrandom.jello.JelloValue.JelloString

import scala.annotation.implicitNotFound
import scala.language.experimental.macros
import scala.reflect.macros.blackbox
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

  def formatSealedTrait[T]: JelloFormat[T] = macro formatSealedTraitImpl[T]

  def formatSealedTraitImpl[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[JelloFormat[T]] = {
    import c.universe._

    val symbol = weakTypeOf[T].typeSymbol
    val companion = symbol.companion
    val tpe = weakTypeTag[T].tpe

    if (!symbol.isClass)
      c.abort(
        c.enclosingPosition,
        "JelloFormat can only enumerate values of a sealed trait or class."
      )
    else if (!symbol.asClass.isSealed)
      c.abort(
        c.enclosingPosition,
        "JelloFormat can only enumerate values of a sealed trait or class."
      )
    else if (!companion.isModule)
      c.abort(c.enclosingPosition,
        "JelloFormat can only enumerate values located inside a companion object"
      )
    else {

      val objects = companion.typeSignature.decls.filter(_.typeSignature <:< tpe).toList
      val classes = companion.typeSignature.decls.filter(_.isClass).filter(_.asClass.baseClasses.contains(symbol)).toList

      val members = (objects ::: classes).map {
        case sym if sym.isClass =>
          // this is a normal class
          q"""
              builder = builder.withMember[$sym]
           """
        case sym =>
          // this is a case object
          q"""
              builder = builder.withMember($sym)
          """

      }

      c.Expr[JelloFormat[T]](
        q"""
           import com.uniformlyrandom.jello._
           import com.uniformlyrandom.jello.JelloValue._

           var builder = FormatBuilder.apply[$tpe]

           ..$members

           builder.buildIdProperty("$$class")

         """)

    }
  }

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
          val (method,args) = if (typeSig.erasure =:= typeOf[Option[_]].erasure)
              (TermName("getOrElse"),List(q"None"))
            else
              (TermName("getOrElse"),List(q"throw new MissingObjectField($nameString, o)"))

          (nameSafe ->
            q"""val $nameSafe: $typeSig = valuesMap.get($nameString)
               .map(implicitly[com.uniformlyrandom.jello.JelloFormat[$typeSig]].read)
               .map(_.toOption)
               .flatten[$typeSig]
               .$method(..$args)""") :: outList
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
              throw new RuntimeException("JelloFormat: failed reading [" + ${tpe.toString} + "] [" + jelloValue + "]",e)
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
