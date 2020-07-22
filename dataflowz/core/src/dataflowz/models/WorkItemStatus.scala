package dataflowz.models

import frameless._


sealed abstract class WorkItemStatus(val label:String, val id:Int, val code:String) extends Product with Serializable { self =>
  def isOk:Boolean = false
  def isSkipped:Boolean = false
  def isFailed:Boolean = false
  def isExcluded:Boolean = false

  def fold[A](ifFailed: => A)(ifExcluded: => A)(ifSkipped: => A)(ifOk: => A):A = self match {
    case WorkItemStatus.Ok => ifOk
    case WorkItemStatus.Skipped => ifSkipped
    case WorkItemStatus.Excluded => ifExcluded
    case WorkItemStatus.Failed =>ifFailed
  }
}
object WorkItemStatus {
  case object Ok extends WorkItemStatus("Ok", 0, "O") {
    override def isOk: Boolean = true
  }
  case object Skipped extends WorkItemStatus("Skipped", 10, "S") {
    override def isSkipped: Boolean = true
  }
  case object Excluded extends WorkItemStatus("Excluded", 20, "X") {
    override def isSkipped: Boolean = true
  }

  case object Failed extends WorkItemStatus("Failed", 30, "F") {
    override def isFailed: Boolean = true
  }

  implicit val workItemStatusToCode: Injection[WorkItemStatus, String] = Injection(
    {
      case Ok => Ok.label
      case Skipped => Skipped.label
      case Excluded => Excluded.label
      case Failed => Failed.label
    },
    {
      case "Ok"|"O" => Ok
      case "Skipped"|"Skip" => Skipped
      case "Excluded"|"X" => Excluded
      case "Failed"|"F" => Failed
      case _ => Failed //TODO: Consider if we need a Unknown status
    }
  )
}
