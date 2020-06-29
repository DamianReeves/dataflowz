package dataflowz

import scala.annotation.tailrec

import FlowStep._

sealed trait FlowStep[-S1, +S2, +A] { self =>

  /**
   * A symbolic alias for `zipRight`.
   */
  final def *>[S3, B](that: FlowStep[S2, S3, B]): FlowStep[S1, S3, B] =
    self zipRight that

  /**
   * A symbolic alias for `zipLeft`.
   */
  final def <*[S3, B](that: FlowStep[S2, S3, B]): FlowStep[S1, S3, A] =
    self zipLeft that

  /**
   * A symbolic alias for `zip`.
   */
  final def <*>[S3, B](that: FlowStep[S2, S3, B]): FlowStep[S1, S3, (A, B)] =
    self zip that

  final def contramap[S0](f: S0 => S1): FlowStep[S0, S2, A] =
    update(f) *> self

  final def flatMap[S3, B](f: A => FlowStep[S2, S3, B]): FlowStep[S1, S3, B] =
    FlatMap(self, f)

  final def flatten[S3, B](implicit ev: A <:< FlowStep[S2, S3, B]): FlowStep[S1, S3, B] =
    flatMap(ev)

  final def map[B](f: A => B): FlowStep[S1, S2, B] =
    flatMap(a => succeed(f(a)))

  final def transform[S3,B](f: (S2,A) => (S3,B)) : FlowStep[S1, S3, B] =
    flatMap(a => modify(s => f(s,a)))
//
//  final def transformM[S3,B](f: (S2,A) => FlowStep[S2,S3,B]) : FlowStep[S1, S3, B] =
//    flatMap(a => ))


  final def mapContext[S3](f: S2 => S3):FlowStep[S1, S3, A] =
    self <* update(f)

  final def mapState[S3](f: S2 => S3):FlowStep[S1, S3, A] =
    self <* update(f)

  final def run(s: S1): (S2, A) = {

    @tailrec
    def loop(self: FlowStep[Any, Any, Any])(s: Any): (Any, Any) =
      self match {
        case Succeed(value)                    => (s, value)
        case Modify(f)                         => f(s)
        case FlatMap(Succeed(value), continue) => loop(continue(value))(s)
        case FlatMap(Modify(f), continue)      => f(s) match { case (s, a) => loop(continue(a))(s) }
        case FlatMap(FlatMap(x, f), g)         => loop(x.flatMap(a => f(a).flatMap(g)))(s)
      }

    loop(self.asInstanceOf[FlowStep[Any, Any, Any]])(s).asInstanceOf[(S2, A)]
  }

  final def runResult(s:S1):A =
    run(s)._2

  final def runState(s:S1):S2 =
    run(s)._1

  final def zip[S3, B](that:FlowStep[S2, S3, B]):FlowStep[S1, S3, (A,B)] =
    zipWith(that)((_,_))

  final def zipLeft[S3,B](that:FlowStep[S2,S3,B]):FlowStep[S1,S3,A] =
    self.zipWith(that)((a,_) => a)

  final def zipRight[S3,B](that:FlowStep[S2,S3,B]):FlowStep[S1,S3,B] =
    self.zipWith(that)((_,b) => b)

  final def zipWith[S3, B, C](that: FlowStep[S2, S3, B])(f:(A,B) => C): FlowStep[S1, S3, C] =
    self.flatMap(a => that.flatMap(b => Step.succeed(f(a,b))))
}

object FlowStep {

  def apply[S1, S2, A](run: S1 => (S2,A) ):FlowStep[S1,S2,A] =
    modify(run)

  def get[S]: Step[S, S] =
    modify(s => (s, s))

  def modify[S1, S2, A](f: S1 => (S2, A)):FlowStep[S1, S2, A] =
    Modify(f)

  def set[S](s:S): FlowStep[Any, S, Unit] =
    modify(_ => (s, ()))

  def succeed[S, A](a: A): Step[S, A] =
    Succeed(a)

  def unit[S]:Step[S, Unit] = Succeed({})

  def update[S1,S2](f: S1 => S2):FlowStep[S1, S2, Unit] =
    modify(s => (f(s), ()))


  private[dataflow] final case class Succeed[S, +A](value:A) extends FlowStep[S, S, A]
  private[dataflow] final case class Modify[-S1,+S2, +A](run: S1 => (S2, A)) extends FlowStep[S1, S2, A]
  private[dataflow] final case class FlatMap[-S1, S2, +S3, A, +B](value: FlowStep[S1, S2, A], continue: A => FlowStep[S2,S3, B])
    extends FlowStep[S1, S3, B]
}
