package dataflowz

import zio._

final case class DataReader[-Req, -Ctx, +Err, +Resp](
    read: Req => ZIO[Ctx, Err, Resp]
) { self =>

  def contramap[Request0](f: Request0 => Req): DataReader[Request0, Ctx, Err, Resp] =
    DataReader { request: Request0 => self.read(f(request)) }

  def flatMap[Req1 <: Req, C1 <: Ctx, E1 >: Err, Resp1](
      f: (Req1, Resp) => DataReader[Req1, C1, E1, Resp1]
  ): DataReader[Req1, C1, E1, Resp1] =
    DataReader { request =>
      val res: ZIO[Ctx, Err, Resp] = self.read(request)
      res.flatMap { response => f(request, response).read(request) }
    }

  def map[Response1](f: Resp => Response1): DataReader[Req, Ctx, Err, Response1] =
    DataReader { request: Req => self.read(request).map(f) }
}

object DataReader {

  def succeed[Response](response: => Response): DataReader[Any, Any, Nothing, Response] =
    DataReader(_ => ZIO.succeed(response))

  def fromRequest[Request]: DataReader[Request, Any, Nothing, Request] =
    DataReader { request: Request => ZIO.succeed(request) }

  object Usage {
    final case class Order(product: String, quantity: Int, price: BigDecimal)
    final case class CustomerOrder(customerNo: String, items: List[Order])

    val order: CustomerOrder =
      CustomerOrder(
        "JohnDoe365",
        List(
          Order("widget", 2, 12.99),
          Order("sprocket", 1, 9.95)
        )
      )
    val customerOrderReader: DataReader[CustomerOrder, Any, Nothing, CustomerOrder] =
      DataReader.fromRequest[CustomerOrder]
    val reader1: DataReader[Any, Any, Nothing, Seq[Int]] = DataReader.succeed(Seq(1, 2, 3))
    val reader2: DataReader[Any, Any, Nothing, Seq[String]] =
      DataReader.succeed(Seq("One", "Two", "Three"))

    val reader =
      for {
        co <- customerOrderReader
      } yield co

  }
}
