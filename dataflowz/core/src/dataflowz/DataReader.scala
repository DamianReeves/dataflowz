package dataflowz

import zio._
import DataReader._

sealed trait DataReader[-Req, -Ctx, +Err, +Resp] { self =>

  def flatMap[Req1 <: Req, C1 <: Ctx, E1 >: Err, Resp1](
      f: Resp => DataReader[Req1, C1, E1, Resp1]
  ): DataReader[Req1, C1, E1, Resp1] =
    FlatMap(self, f)

  def map[Resp1](f: Resp => Resp1): DataReader[Req, Ctx, Err, Resp1] =
    FlatMap(self, (resp: Resp) => DataReader.succeed(f(resp)))

  def read(request: Req): ZIO[Ctx, Err, Resp] = self match {
    case Succeed(value) => ZIO.succeed(value)
    case Fail(error)    => ZIO.fail(error)
    case FlatMap(value, continue) =>
      for {
        resp <- value.read(request)
        result <- continue(resp).read(request)
      } yield result
    case DataReader.Access(access) =>
      ZIO.environment.flatMap { ctx: Ctx =>
        val reader = access(request, ctx)
        reader.read(request)
      }
  }
}

object DataReader {

  val unit: UDataReader[Any, Unit] = Succeed(())

  def fail[E](error: E): DataReader[Any, Any, E, Nothing] = Fail(error)

  def succeed[Response](response: => Response): UDataReader[Any, Response] =
    Succeed(response)

  def context[Ctx]: DataReader[Any, Ctx, Nothing, Ctx] = Access((_, ctx) => succeed(ctx))

  def fromRequest[Request]: DataReader[Request, Any, Nothing, Request] =
    Access { (request, _) => succeed(request) }

  private final case class Succeed[A](value: A) extends DataReader[Any, Any, Nothing, A]
  private final case class Fail[+E](error: E) extends DataReader[Any, Any, E, Nothing]

  private final case class FlatMap[-Req, -Ctx, +E, A, +B](
      value: DataReader[Req, Ctx, E, A],
      continue: A => DataReader[Req, Ctx, E, B]
  ) extends DataReader[Req, Ctx, E, B]

  private final case class Access[Req, Ctx, Err, Resp](
      access: (Req, Ctx) => DataReader[Req, Ctx, Err, Resp]
  ) extends DataReader[Req, Ctx, Err, Resp]

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

  }
}
