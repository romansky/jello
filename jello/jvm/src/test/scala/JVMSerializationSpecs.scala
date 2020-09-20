import java.util.UUID

import TestClasses._
import com.uniformlyrandom.jello.{JelloFormat, JelloJson, TypesLibrary}
import org.scalatest.funspec.AnyFunSpec

import scala.util.{Success, Try}

class JVMSerializationSpecs extends AnyFunSpec {

  it("serializes traits") {

    implicit val fbFormat = JelloFormat.format[FirstBase]
    implicit val sbFormat = JelloFormat.format[SecondBase]

    val formatter = JelloFormat
      .formatTrait[Base]
      .withMember[FirstBase]
      .withMember[SecondBase]
      .buildIdPropertyWithFallback("$class", JelloFormat.format[DefaultBase])

    val first = FirstBase("first", "name", "some value")

    val jelloValue = formatter.write(first)

    assert(formatter.read(jelloValue) == Try(first))

    val craftedJsonForDefault = """{"value":"some value","name":"some name"}"""
    assert(formatter.read(JelloJson.parse(craftedJsonForDefault)) == Try(DefaultBase("some value", "some name")))

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

  it("supports a case class with methods") {
    import TypesLibrary._
    val fmt = JelloFormat.format[ClassWithVals]
    val o = ClassWithVals("param1value", 2)
    val ow = fmt.write(o)
    val or = fmt.read(ow)
    assert(Success(o) == or)
  }

  it("support resetting values for overrides") {
    import TypesLibrary._

    implicit val fmt: JelloFormat[ClassWithDefaults] = JelloFormat.format[ClassWithDefaults]

    val providedp3 = 50
    val providedp4 = "provided"
    val created = ClassWithDefaults("p1", 1, providedp3, providedp4)
    val str = JelloJson.toJsonString(created)
    val withDefaults: ClassWithDefaults = JelloJson.createWithResetFields[ClassWithDefaults]("param3" :: "param4" :: Nil)(str).get

    assert(withDefaults.param3 == TestClasses.ClassWithDefaultsP3)
    assert(withDefaults.param4 == TestClasses.ClassWithDefaultsP4)
  }

  it("works with nested json classes") {
    implicit val nestedFmt = JelloFormat.format[WithNested]
    implicit val simpleFmt = JelloFormat.format[SimpleTestClass]

    val c = SimpleTestClass("string", 1)

    val nested = WithNested(
      JelloJson.toJsonString(c)
    )

    val nestedString = JelloJson.toJsonString(nested)

    val nestedRead = nestedFmt.read(JelloJson.parse(nestedString))
    val simpleRead = simpleFmt.read(JelloJson.parse(nestedRead.get.cls1))
    assert(simpleRead.isSuccess)
  }

}
