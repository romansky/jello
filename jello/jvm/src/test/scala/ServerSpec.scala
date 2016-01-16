
import com.uniformlyrandom.jello.{JelloJson, JelloFormat, JelloReader}
import org.scalatest.FunSpec

import scala.util.{Try, Success}

class ServerSpec extends FunSpec {
  import com.uniformlyrandom.jello.JelloValue._
  import com.uniformlyrandom.jello.TypesLibrary._

  describe("reads basic JSON values"){

    it("reads JSON strings"){
      assert(Success("string-value") == JelloReader.read[String](JelloString("string-value")))
    }

    it("reads  JSON numbers"){
      assert(Success(100.100) == JelloReader.read[Double](JelloNumber(100.100)))
    }

    it("reads JSON bools"){
      assert(Success(true) == JelloReader.read[Boolean](JelloBool(true)))
      assert(Success(false) == JelloReader.read[Boolean](JelloBool(false)))
    }

  }

  describe("end to end"){
    import TestClasses._

    it("reading and writing case classes"){
      val formatter = JelloFormat.format[SimpleTestClass]
      val c = SimpleTestClass("string",1)
      val written = formatter.write(c)
      val read = formatter.read(written)

      assert(read == Try(c))
    }

    it("reading and writing case classes to JSON"){

      implicit val formatter = JelloFormat.format[SimpleTestClass]
      val c = SimpleTestClass("string",1)
      val json = JelloJson.toJsonString(c)

      val cFromJson = JelloJson.fromJson[SimpleTestClass](JelloJson.parse(json))

      assert(cFromJson == Try(c))

    }


  }

}
