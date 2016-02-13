import com.uniformlyrandom.jello.{JelloWriter, JelloReader, JelloFormat, TypesLibrary}
import org.scalatest.FunSpec

import scala.util.Try

class JelloFormatSpec extends FunSpec {

  import TestClasses._

  it("reading and writing case classes"){

    val formatter = JelloFormat.format[SimpleTestClass]
    val c = SimpleTestClass("string",1)
    val written = formatter.write(c)
    val read = formatter.read(written)

    assert(read == Try(c))
  }

  it("reads and writes lists of case class types"){

    import TypesLibrary._

    implicit val formatter = JelloFormat.format[SimpleTestClass]
    val formatterList = implicitly[JelloFormat[List[SimpleTestClass]]]
    val c1 = SimpleTestClass("string1",1)
    val c2 = SimpleTestClass("string2",2)
    val c3 = SimpleTestClass("string3",3)

    val written =  formatterList.write(c1 :: c2 :: c3 :: Nil)
    val read = formatterList.read(written)

    assert(read == Try(c1 :: c2 :: c3 :: Nil))

  }


  it("reads and writes primitives"){

    import TypesLibrary._

    assert(
      Try(Option("testing")) ==
        implicitly[JelloFormat[Option[String]]].read(implicitly[JelloFormat[Option[String]]].write(Option("testing"))))

  }

  it("reads and writes maps"){
    import TypesLibrary._

    val map = Map[String,String]("key1"-> "value1", "key2"-> "value2")
    val formatter = implicitly[JelloFormat[Map[String,String]]]

    val jjson = formatter.write(map)

    assert(formatter.read(jjson) == Try(map))

  }



  // TODO: write test for all failure conditions



}
