package object dataflowz {

  type Step[S, +A] = FlowStep[S, S, A]
  val Step: FlowStep.type = FlowStep

  type UDataReader[-Req, +Resp] = DataReader[Req, Any, Nothing, Resp]
}
