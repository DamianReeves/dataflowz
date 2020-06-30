package dataflowz

import zio.test._
import zio.test.Assertion._
object FlowStepSpec extends DefaultRunnableSpec {
  def spec = suite("Step Spec")(
    test("A unit step should retun a unit when run")(
      assert(Step.unit.run(None))(equalTo((None, {})))
    ),
    test("Steps should support chaining") {
      val step = Step.get[Int].map(_ => "Unit")
      pprint.log(step)
      assert(step.run(1))(equalTo((1, "Unit")))
    }
  )
}
