

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

}


