package com.uniformlyrandom.jello


import scala.scalajs.js
import scala.scalajs.js.Dynamic
import scala.util.Try


object JelloJson extends JelloJsonSpec {

  import com.uniformlyrandom.jello.JelloValue._

  def parse(jsonStr: String): JelloValue = {
    val parsed: Dynamic = js.JSON.parse(jsonStr)
    parseNative(parsed)
  }

  def parseNative(dynamic: Any): JelloValue =
    //has to be Any since its natively parsed
    dynamic match {
      case s: String => JelloString(s)
      case s: Double => JelloNumber(s)
      case true => JelloBool(true)
      case false => JelloBool(false)
      case null => JelloNull
      case a: js.Array[_] =>
        val members = a.map(ai=> parseNative(ai))
        JelloArray(members.toSeq)
      case o: js.Object =>
        val d = o.asInstanceOf[js.Dictionary[_]]
        val values = d.values.map(_v=> parseNative(_v))
        JelloObject(d.keys.zip(values).toMap)
      case unknown=> throw new UnsupportedOperationException(s"unsupported native type [$unknown]")
    }

  // serialize JelloValue into js.Vaue
  private def nativeWrite(jelloValue: JelloValue): Any = {
    jelloValue match {
      case JelloBool(x) => x
      case JelloNull => null
      case JelloNumber(v) => v.toDouble
      case JelloString(s) => s
      case JelloArray(arr) => js.Array(arr.map(nativeWrite):_*)
      case JelloObject(o) => js.Dictionary(o.toSeq.map { case (k,v) => (k, nativeWrite(v)) } :_*)
    }
  }


  def toJson[T](o: T)(implicit jelloWriter: JelloWriter[T]): JelloValue = jelloWriter.write(o)

  def toJsonString[T](o: T)(implicit jelloWriter: JelloWriter[T]): String = toJsonString(jelloWriter.write(o))
  def toJsonString(jelloValue: JelloValue): String = js.JSON.stringify(nativeWrite(jelloValue).asInstanceOf[js.Any])

  def fromJson[T](json: JelloValue)(implicit jelloReader: JelloReader[T]): Try[T] = jelloReader.read(json)

}
