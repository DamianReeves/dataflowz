package dataflowz

import dataflowz.dataflow.Dataflow
import zio._

object dataflow {

  sealed trait Dataflow[-R, -I, +E, +A] {}

  object Dataflow {
    def input[A](
        value: A,
        description: Option[String] = None
    ): Dataflow[Any, Nothing, A] = Src(Step.Given(value, description))

    def returning[A](value: A): Dest[Any, Nothing, A] =
      Dest(Step.Given(value, None))

    def returning[A](
        value: A,
        description: Option[String]
    ): Dest[Any, Nothing, A] = Dest(Step.Given(value, description))

    final case class Src[-R, -I, +E, +A](step: Step[R, -I, E, A])
        extends Dataflow[R, E, A]

    final case class Dest[-R, -I, +E, +A](step: Step[R, -I, E, A])
        extends Dataflow[R, E, A] {

      def run: ZIO[R, E, A] = step.action
    }
  }

//  sealed trait DataflowDescriptor[+A, L] {
//    def steps: L
//  }
//  object DataflowDescriptor {
//    final case class Single[+A]()
//  }

  trait SparkSupport {
    import org.apache.spark.sql._
    object SparkSteps {
      case class EnrichDataset[A](
          enrich: Dataset[A] => Dataset[A],
          description: Option[String]
      ) extends Step[Any, Nothing, Dataset[A]] {
        def action: ZIO[Any, Nothing, Dataset[A]] = ???
      }
    }
  }

  object internal {

    private[dataflow] case class DataflowContext()

    private[dataflow] trait Result[-R, +E, +A] {}

    object Result {

      def done[A](value: A): Result[Any, Nothing, A] = Done(value)
      def fail[E](cause: Cause[E]): Result[Any, E, Nothing] = Fail(cause)

      final case class Done[+A](value: A) extends Result[Any, Nothing, A]
      final case class Fail[+E](cause: Cause[E]) extends Result[Any, E, Nothing]
    }
  }
}

object Examples extends App {

  def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    example1.run(args)
  }

  abstract class Example {
    def run(args: List[String]): URIO[zio.ZEnv, ExitCode]
  }

  object example1 extends Example {
    def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
      val dataflow =
        Dataflow.returning(List(1, 2, 3, 4, 5))

      for {
        result <- dataflow.run
        _ <- console.putStrLn(s"Result: $result")
      } yield ExitCode.success

    }
  }
}
