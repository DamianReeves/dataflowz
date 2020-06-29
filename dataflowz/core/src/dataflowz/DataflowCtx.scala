package dataflowz

final case class DataflowCtx[+P](parameters:P) {
  def withParameters[P2](parameters:P2):DataflowCtx[P2] =
    copy(parameters=parameters)
}
object DataflowCtx {
  val unit:DataflowCtx[Unit] = DataflowCtx({})
  def fromParameters[P](parameters:P):DataflowCtx[P] = DataflowCtx(parameters)
}
