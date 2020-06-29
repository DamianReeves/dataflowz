package object dataflowz {
  type Flow[P,A] = Dataflow[P,P,A]

  type Step[S, +A] = FlowStep[S, S, A]
  val Step:FlowStep.type  = FlowStep

  type DataflowStep[-P1, +P2,+A] = FlowStep[DataflowCtx[P1], DataflowCtx[P2], A]
  val DataflowStep:FlowStep.type  = FlowStep
}
