package object dataflowz {

  type Step[S, +A] = FlowStep[S, S, A]
  val Step: FlowStep.type = FlowStep

}
