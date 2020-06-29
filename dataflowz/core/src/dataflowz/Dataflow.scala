package dataflowz

import Dataflow._

final case class Dataflow [-P1, +P2, +A](private[dataflow] val step:DataflowStep[P1,P2,A]) { self =>

  def setParameters[P](parameters: P): Dataflow[P1, P, A] =
    Dataflow ( step.transform { case (ctx,a) =>
      (ctx.withParameters(parameters),a) }
      )

  def map[B](f:A => B):Dataflow[P1,P2,B] =
    Dataflow(step.map(f))

//  def andThen[P3,B](that:DataflowStep[P2,P3,B]) =
//    Dataflow(step.flatMap())

  def addStep[P3,B](f: A => DataflowStep[P2,P3,B]): Dataflow[P1, P3, B] =
    Dataflow(step.flatMap(a => f(a)))


  def run(parameters:P1):Result[P2,A] = {
    Result.create(step.run(DataflowCtx.fromParameters(parameters)))
  }

  def run(context:DataflowCtx[P1]):Result[P2,A] = {
    Result.create(step.run(context))
  }
}

object Dataflow {

  final case class Result[+P,+A](toTuple:(P,A)) extends AnyVal {
    def parameters:P = toTuple._1
    def value:A = toTuple._2
  }

  object Result {
    def create[P,A](tuple:(DataflowCtx[P], A)):Result[P,A] =
      Result(tuple._1.parameters, tuple._2)

    def apply[P,A](parameters:P, value:A):Result[P,A] =
      Result(parameters -> value)
  }

  val unit:Dataflow[Any, Any, Unit] = new Dataflow(Step.unit)

  def apply[P1, P2, A](run:  DataflowCtx[P1] => (DataflowCtx[P2],A)):Dataflow[P1,P2,A] = {
    val step = DataflowStep(run)
    new Dataflow(step)
  }

  def create[P1,P2,A](run: P1 => (P2,A)):Dataflow[P1,P2,A] = {
    Dataflow { (ctx:DataflowCtx[P1]) =>
      val (params, value) = run(ctx.parameters)
      (ctx.withParameters(params), value)
    }
  }

  def setParameters[P](parameters:P) = {
    val ctx = DataflowCtx.fromParameters(parameters)
    Dataflow(Step.set(ctx))
  }

}




