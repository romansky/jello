import TestClasses._
import com.uniformlyrandom.jello.{JelloJson, TypesLibrary, JelloFormat}
import minitest.SimpleTestSuite

import scala.util.{Success, Try}

object JsSerializationSpecs extends SimpleTestSuite {

  def it = test _

  it("serializes serializes traits"){

    implicit val fbFormat = JelloFormat.format[FirstBase]
    implicit val sbFormat = JelloFormat.format[SecondBase]

    val formatter = JelloFormat.formatTrait[Base]
      .withMember[FirstBase]
      .withMember[SecondBase]
      .buildIdProperty("$class")

    val first = FirstBase("first","name","some value")

    val jelloValue = formatter.write(first)

    assert(formatter.read(jelloValue) == Try(first))

  }

  it("handles enumerations") {
    implicit val jformat = JelloFormat.formatEnumeration(TestEnumeration)

    val jsItem = jformat.write(TestEnumeration.FirstE)
    assert(jformat.read(jsItem) == Try(TestEnumeration.FirstE))
  }

  it("support optional values fall back to None"){

    import TypesLibrary._

    val o = WithOptionals("zubiname",None)
    val fmt = JelloFormat.format[WithOptionals]
    val ow = fmt.write(o)
    val or = fmt.read(ow)
    assert(Success(o) == or)

    val json = """{"name":"zubiname"}"""
    val op = JelloJson.parse(json)
    val or2 = fmt.read(op)

    assert(Success(o) == or2)

  }




}
