import com.uniformlyrandom.jello.JelloFormat

object TestClasses {

  case class SimpleTestClass(
      param1: String,
      param2: Int
  )

  trait Base {
    def name: String
    def value: String
  }

  case class FirstBase(
      value: String,
      name: String,
      someOtherValue: String
  ) extends Base

  case class SecondBase(
      value: String,
      name: String,
      blahBlah: Int
  ) extends Base

  sealed trait TraitEnum
  object TraitEnum {
    case object Unoz extends TraitEnum
    case object Desz extends TraitEnum
    case class Tresz(p: String, p2: Int) extends TraitEnum
    object Tresz {
      implicit val fmt = JelloFormat.format[Tresz]
    }
  }

  object TestEnumeration extends Enumeration {
    type TestEnumeration = Value
    val FirstE, SecondE, ThirdE = Value
  }

  case class WithOptionals(
      name: String,
      optional: Option[String]
  )

  case class ClassWithVals(
      param1: String,
      param2: Int
  ) {
    val isSmart = true
    val numGreatSuccess = 42
  }

  val ClassWithDefaultsP3 = 100
  val ClassWithDefaultsP4 = "default string"

  case class ClassWithDefaults(
      param1: String,
      param2: Int,
      param3: Int = ClassWithDefaultsP3,
      param4: String = ClassWithDefaultsP4
  )

  case class WithNested(
    cls1: String
  )



}
