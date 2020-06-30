package dataflowz
import zio.test.DefaultRunnableSpec

import zio.test._
import zio.test.Assertion._

object DataflowSpec extends DefaultRunnableSpec {
  def spec = suite("Dataflow Spec")(
    test("A unit dataflow should return a unit value") {
      val dataflow = Dataflow.unit
      pprint.log(dataflow)
      assert(dataflow.run(42))(equalTo(Dataflow.Result(42, {})))
    }
  )
}
