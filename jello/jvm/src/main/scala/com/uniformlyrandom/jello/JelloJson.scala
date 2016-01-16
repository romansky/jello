package com.uniformlyrandom.jello

import scala.util.Try

object JelloJson extends JelloJsonSpec {

  override def parse(jsonStr: String): JelloValue = JelloJacksonJson.parseJsValue(jsonStr)

  override def toJson[T](o: T)(implicit jelloWriter: JelloWriter[T]): JelloValue = jelloWriter.write(o)

  override def toJsonString[T](o: T)(implicit jelloWriter: JelloWriter[T]): String = toJsonString(jelloWriter.write(o))

  override def toJsonString(jelloValue: JelloValue): String = JelloJacksonJson.generateFromJsValue(jelloValue)

  override def fromJson[T](jelloValue: JelloValue)(implicit jelloReader: JelloReader[T]): Try[T] = jelloReader.read(jelloValue)

}
