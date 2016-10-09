import java.util.UUID

import TestClasses._
import com.uniformlyrandom.jello.{JelloFormat, JelloJson, TypesLibrary}
import org.scalatest.FunSpec

import scala.util.{Success, Try}

class JVMSerializationSpecs extends FunSpec {

  it("serializes serializes traits") {

    implicit val fbFormat = JelloFormat.format[FirstBase]
    implicit val sbFormat = JelloFormat.format[SecondBase]

    val formatter = JelloFormat
      .formatTrait[Base]
      .withMember[FirstBase]
      .withMember[SecondBase]
      .buildIdProperty("$class")

    val first = FirstBase("first", "name", "some value")

    val jelloValue = formatter.write(first)

    assert(formatter.read(jelloValue) == Try(first))

  }

  it("handles enumerations") {
    implicit val jformat = JelloFormat.formatEnumeration(TestEnumeration)

    val jsItem = jformat.write(TestEnumeration.FirstE)
    assert(jformat.read(jsItem) == Try(TestEnumeration.FirstE))
  }

  it("support optional values fall back to None") {

    val o = WithOptionals("zubiname", None)
    val fmt = JelloFormat.format[WithOptionals]
    val ow = fmt.write(o)
    val or = fmt.read(ow)
    assert(Success(o) == or)

    val json = """{"name":"zubiname"}"""
    val op = JelloJson.parse(json)
    val or2 = fmt.read(op)

    assert(Success(o) == or2)

  }

  it("serializes traits with multiple case object descendants") {
    val formatter = JelloFormat.formatSealedTrait[TraitEnum]
    assert(
      formatter.read(formatter.write(TraitEnum.Desz)) == Success(
        TraitEnum.Desz))
    assert(
      formatter.read(formatter.write(TraitEnum.Unoz)) == Success(
        TraitEnum.Unoz))
    assert(
      formatter.read(formatter.write(TraitEnum.Tresz("ppp", 32))) == Success(
        TraitEnum.Tresz("ppp", 32)))
  }

  it("serializes and de-serializes Try's") {
    import TypesLibrary._
    val message = s"MESSAGE ${UUID.randomUUID().toString}"
    val badTry: Try[String] = Try(throw new RuntimeException(message))
    val goodTry = Try(message)
    val badSerialized = JelloJson.toJsonString(badTry)
    val goodSeralized = JelloJson.toJsonString(goodTry)
    val badJello = JelloJson.parse(badSerialized)
    val goodJello = JelloJson.parse(goodSeralized)
    val badTryUnserialized: Try[Try[String]] =
      JelloJson.fromJson[Try[String]](badJello)
    val goodTryUnserialized: Try[Try[String]] =
      JelloJson.fromJson[Try[String]](goodJello)
    assert(
      badTryUnserialized.get.isFailure && badTryUnserialized.get.failed.get.getMessage == message)
    assert(
      goodTryUnserialized.get.isSuccess && goodTryUnserialized.get.get == message)
  }

}
