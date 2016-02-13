import TestClasses.{Base, SecondBase, FirstBase}
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

}
