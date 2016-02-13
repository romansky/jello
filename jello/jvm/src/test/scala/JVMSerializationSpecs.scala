import TestClasses.{TestEnumeration, Base, SecondBase, FirstBase}
import com.uniformlyrandom.jello.JelloFormat
import org.scalatest.FunSpec

import scala.util.Try

class JVMSerializationSpecs extends FunSpec {

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

}
