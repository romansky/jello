package com.uniformlyrandom.jello

import java.io.InputStream

import JelloValue._
import com.fasterxml.jackson.core._
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.Module.SetupContext
import com.fasterxml.jackson.databind.module.SimpleModule
import com.uniformlyrandom.jello.JelloValue.JelloNull

import scala.annotation.{switch, tailrec}
import scala.collection.mutable.ListBuffer

/**
  * this uses similar impl as Play Json play.api.libs.json.jackson.PlayJsonModule
  * to map JelloValue to JSON
  */
object JelloJacksonModule extends SimpleModule("JelloJson", Version.unknownVersion()){
  override def setupModule(context: SetupContext): Unit = {
    context.addDeserializers(new JelloDeserializers)
    context.addSerializers(new JelloSerializers)
  }
}


private[jello] class JelloDeserializers extends Deserializers.Base {
  override def findBeanDeserializer(javaType: JavaType, config: DeserializationConfig, beanDesc: BeanDescription) = {
    val klass = javaType.getRawClass
    if (classOf[JelloValue].isAssignableFrom(klass) || klass == JelloNull.getClass) {
      new JelloValueDeserializer(config.getTypeFactory, klass)
    } else null
  }
}

private[jello] sealed trait DeserializerContext {
  def addValue(value: JelloValue): DeserializerContext
}

private[jello] case class ReadingList(content: ListBuffer[JelloValue]) extends DeserializerContext {
  override def addValue(value: JelloValue): DeserializerContext = {
    ReadingList(content += value)
  }
}

// Context for reading an Object
private[jello] case class KeyRead(content: ListBuffer[(String, JelloValue)], fieldName: String) extends DeserializerContext {
  def addValue(value: JelloValue): DeserializerContext = ReadingMap(content += (fieldName -> value))
}

// Context for reading one item of an Object (we already red fieldName)
private[jello] case class ReadingMap(content: ListBuffer[(String, JelloValue)]) extends DeserializerContext {
  def setField(fieldName: String) = KeyRead(content, fieldName)
  def addValue(value: JelloValue): DeserializerContext = throw new Exception("Cannot add a value on an object without a key, malformed JSON object!")
}

private[jello] class JelloValueDeserializer(factory: TypeFactory, klass: Class[_]) extends JsonDeserializer[JelloValue] {

  override def isCachable: Boolean = true

  override def deserialize(jp: JsonParser, ctxt: DeserializationContext): JelloValue = {
    val value = deserialize(jp, ctxt, List())

    if (!klass.isAssignableFrom(value.getClass)) {
      throw ctxt.mappingException(klass)
    }
    value
  }

  @tailrec
  final def deserialize(jp: JsonParser, ctxt: DeserializationContext, parserContext: List[DeserializerContext]): JelloValue = {
    if (jp.getCurrentToken == null) {
      jp.nextToken()
    }

    val (maybeValue, nextContext) = (jp.getCurrentToken.id(): @switch) match {

      case JsonTokenId.ID_NUMBER_INT | JsonTokenId.ID_NUMBER_FLOAT => (Some(JelloNumber(jp.getDecimalValue)), parserContext)

      case JsonTokenId.ID_STRING => (Some(JelloString(jp.getText)), parserContext)

      case JsonTokenId.ID_TRUE => (Some(JelloBool(true)), parserContext)

      case JsonTokenId.ID_FALSE => (Some(JelloBool(false)), parserContext)

      case JsonTokenId.ID_NULL => (Some(JelloNull), parserContext)

      case JsonTokenId.ID_START_ARRAY => (None, ReadingList(ListBuffer()) +: parserContext)

      case JsonTokenId.ID_END_ARRAY => parserContext match {
//        case ReadingList(content) :: stack => (Some(JelloArray(content)), stack)
        case ReadingList(content) :: stack => (Some(JelloArray(content.toSeq)), stack)
        case _ => throw new RuntimeException("We should have been reading list, something got wrong")
      }

      case JsonTokenId.ID_START_OBJECT => (None, ReadingMap(ListBuffer()) +: parserContext)

      case JsonTokenId.ID_FIELD_NAME => parserContext match {
        case (c: ReadingMap) :: stack => (None, c.setField(jp.getCurrentName) +: stack)
        case _ => throw new RuntimeException("We should be reading map, something got wrong")
      }

      case JsonTokenId.ID_END_OBJECT => parserContext match {
        case ReadingMap(content) :: stack => (Some(JelloObject(content.toSeq)), stack)
        case _ => throw new RuntimeException("We should have been reading an object, something got wrong")
      }

      case JsonTokenId.ID_NOT_AVAILABLE => throw new RuntimeException("We should have been reading an object, something got wrong")

      case JsonTokenId.ID_EMBEDDED_OBJECT => throw new RuntimeException("We should have been reading an object, something got wrong")
    }

    // Read ahead
    jp.nextToken()

    maybeValue match {
      case Some(v) if nextContext.isEmpty =>
        // done, no more tokens and got a value!
        // note: jp.getCurrentToken == null happens when using treeToValue (we're not parsing tokens)
        v

      case _maybeValue =>
        val toPass = _maybeValue.map { v =>
          val previous :: stack = nextContext
          previous.addValue(v) +: stack
        }.getOrElse(nextContext)

        deserialize(jp, ctxt, toPass)

    }

  }

  override def getNullValue = JelloNull

}

private[jello] object JelloValueSerializer extends JsonSerializer[JelloValue] {
  import java.math.{ BigDecimal => JBigDec, BigInteger }
  import com.fasterxml.jackson.databind.node.{ BigIntegerNode, DecimalNode }

  override def serialize(value: JelloValue, json: JsonGenerator, provider: SerializerProvider) {
    value match {
      case JelloNumber(v) =>
        // Workaround #3784: Same behaviour as if JsonGenerator were
        // configured with WRITE_BIGDECIMAL_AS_PLAIN, but forced as this
        // configuration is ignored when called from ObjectMapper.valueToTree
        val raw = v.bigDecimal.stripTrailingZeros.toPlainString

        if (raw contains ".") json.writeTree(new DecimalNode(new JBigDec(raw)))
        else json.writeTree(new BigIntegerNode(new BigInteger(raw)))

      case JelloString(v) => json.writeString(v)
      case JelloBool(v) => json.writeBoolean(v)
      case JelloArray(elements) =>
        json.writeStartArray()
        elements.foreach { t =>
          serialize(t, json, provider)
        }
        json.writeEndArray()
      case JelloObject(values) =>
        json.writeStartObject()
        values.foreach { t =>
          json.writeFieldName(t._1)
          serialize(t._2, json, provider)
        }
        json.writeEndObject()
      case JelloNull => json.writeNull()
    }
  }
}

private[jello] class JelloSerializers extends Serializers.Base {
  override def findSerializer(config: SerializationConfig, javaType: JavaType, beanDesc: BeanDescription) = {
    val ser: Object = if (classOf[JelloValue].isAssignableFrom(beanDesc.getBeanClass)) {
      JelloValueSerializer
    } else {
      null
    }
    ser.asInstanceOf[JsonSerializer[Object]]
  }
}

object JelloJacksonJson {

  private val mapper = (new ObjectMapper).registerModule(JelloJacksonModule)

  private val jsonFactory = new JsonFactory(mapper)

  private def stringJsonGenerator(out: java.io.StringWriter) =
    jsonFactory.createGenerator(out)

  private def jsonParser(c: String): JsonParser =
    jsonFactory.createParser(c)

  private def jsonParser(data: Array[Byte]): JsonParser =
    jsonFactory.createParser(data)

  private def jsonParser(stream: InputStream): JsonParser =
    jsonFactory.createParser(stream)

  def parseJsValue(data: Array[Byte]): JelloValue =
    mapper.readValue(jsonParser(data), classOf[JelloValue])

  def parseJsValue(input: String): JelloValue =
    mapper.readValue(jsonParser(input), classOf[JelloValue])

  def parseJsValue(stream: InputStream): JelloValue =
    mapper.readValue(jsonParser(stream), classOf[JelloValue])

  def generateFromJsValue(jsValue: JelloValue, escapeNonASCII: Boolean = false): String = {
    val sw = new java.io.StringWriter
    val gen = stringJsonGenerator(sw)

    if (escapeNonASCII) {
      gen.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII)
    }

    mapper.writeValue(gen, jsValue)
    sw.flush()
    sw.getBuffer.toString
  }

  def prettyPrint(jsValue: JelloValue): String = {
    val sw = new java.io.StringWriter
    val gen = stringJsonGenerator(sw).setPrettyPrinter(
      new DefaultPrettyPrinter()
    )
    val writer: ObjectWriter = mapper.writerWithDefaultPrettyPrinter()
    writer.writeValue(gen, jsValue)
    sw.flush()
    sw.getBuffer.toString
  }

  def jsValueToJsonNode(jsValue: JelloValue): JsonNode =
    mapper.valueToTree(jsValue)

  def jsonNodeToJsValue(jsonNode: JsonNode): JelloValue =
    mapper.treeToValue(jsonNode, classOf[JelloValue])

}
