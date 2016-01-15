import com.uniformlyrandom.jello.JelloReader
import org.scalatest.FunSpec

import scala.util.Success

class ReaderSpec extends FunSpec {
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

}
