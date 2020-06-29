package dataflowz

import zio._

trait Step[-R, -I, +E, +A] {
  def description: Option[String]
  def action: I => ZIO[R, E, A]
}

object Step {

  case class Given[+A](value: A, description: Option[String])
      extends Step[Any, Any, Nothing, A] {
    val action: Any => ZIO[Any, Nothing, A] = _ => ZIO.succeed(value)
  }

  case class Transform[-R, -I, +E, +A](
      action: I => ZIO[R, E, A],
      description: Option[String]
  ) extends Step[R, I, E, A]

}
