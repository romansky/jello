package com.uniformlyrandom.jello

import com.uniformlyrandom.jello.JelloValue._
import play.api.libs.json._

import scala.util.{Failure, Success}

trait PlayJsonAdapter {

  private def playJsToJelloJs(json: JsValue): JelloValue =
    json match {
      case JsNull => JelloNull
      case JsArray(values) => JelloArray(values.map(playJsToJelloJs))
      case JsObject(values) => JelloObject(values.toSeq.map { case (k,v) => k -> playJsToJelloJs(v) })
      case JsBoolean(b) => JelloBool(b)
      case JsNumber(n) => JelloNumber(n)
      case JsString(s) => JelloString(s)
    }

  private def jelloJsToPlayJs(jelloValue: JelloValue): JsValue =
    jelloValue match {
      case JelloNull => JsNull
      case JelloArray(seq) => JsArray(seq.map(jelloJsToPlayJs))
      case JelloObject(map) => JsObject(map.toSeq.map { case (k,v) => k -> jelloJsToPlayJs(v) })
      case JelloBool(b) => JsBoolean(b)
      case JelloNumber(n) => JsNumber(n)
      case JelloString(s) => JsString(s)
    }

  implicit def playJsonAdapter[T](implicit jelloFormat: JelloFormat[T]): Format[T] =
    new Format[T] {

      override def reads(json: JsValue): JsResult[T] =
        jelloFormat.read(playJsToJelloJs(json)) match {
          case Success(value)=> JsSuccess(value)
          case Failure(e)=> JsError(e.getMessage)
        }

      override def writes(o: T): JsValue = jelloJsToPlayJs(jelloFormat.write(o))
    }

}

object PlayJsonAdapter extends PlayJsonAdapter
