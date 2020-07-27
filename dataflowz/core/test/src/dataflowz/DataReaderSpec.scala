package dataflowz

import zio.test._
import zio.test.Assertion._

object DataReaderSpec extends DefaultRunnableSpec {
  def spec = suite("DataReader Spec")(
    suite("Reading")(
      testM("Reading allows you to get a value")(
        for {
          result <- DataReader
            .fromRequest[String]
            .map(req => List(req))
            .read("Test")
        } yield assert(result)(equalTo(List("Test")))
      )
    )
  )
}
