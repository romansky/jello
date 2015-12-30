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

  }

}
