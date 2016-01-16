import com.uniformlyrandom.jello.{JelloJson, JelloFormat}
import minitest._

import scala.util.Try

object ClientSpecs extends SimpleTestSuite {

	import TestClasses._

	test("reading and writing case classes"){
		val formatter = JelloFormat.format[SimpleTestClass]
		val c = SimpleTestClass("string",1)
		val written = formatter.write(c)
		val read = formatter.read(written)

		assert(read == Try(c))
	}

	test("reading and writing cases classes to JSON"){

		implicit val formatter = JelloFormat.format[SimpleTestClass]
		val c = SimpleTestClass("string",1)
		val json = JelloJson.toJsonString(c)

		val cFromJson = JelloJson.fromJson[SimpleTestClass](JelloJson.parse(json))

		assert(cFromJson == Try(c))

	}

}