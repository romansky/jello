import com.uniformlyrandom.jello.JelloFormat
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

  // TODO: write test for all failure conditions

}
