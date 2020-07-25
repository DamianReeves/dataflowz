package dataflowz.spark.models

import dataflowz.SequenceProvider
import dataflowz.SequenceProvider.Aux
import izumi.reflect.Tag

import scala.util.control.NonFatal
import WorkItem._

/**
  * The `WorkItem` class provides a wrapper that maintains the state and the history of state changes for
  */
final case class WorkItem[+Data](
    data: Data,
    status: WorkItemStatus,
    history: List[HistoryEntry]
) { self =>
  def isOk: Boolean = status.isOk
  def isSkipped: Boolean = status.isSkipped
  def isExcluded: Boolean = status.isExcluded
  def isFailed: Boolean = status.isFailed

  def statusCode: String = status.code

  def exclude[D >: Data: Tag](reason: String, message: String): WorkItem[D] =
    copy(
      status = WorkItemStatus.Excluded,
      history = nextHistoryEntry[D](
        WorkItemStatus.Excluded,
        reason = reason,
        message = message
      ) :: self.history
    )

  /**
    * Marks the `WorkItem` as failed.
    */
  def fail[D >: Data: Tag](reason: String, message: String): WorkItem[D] =
    copy(
      status = WorkItemStatus.Failed,
      history = nextHistoryEntry[D](
        WorkItemStatus.Failed,
        reason = reason,
        message = message
      ) :: self.history
    )

  /**
    * Recovers the `WorkItem` by marking it as ok.
    */
  def recover[D >: Data: Tag](reason: String, message: String): WorkItem[D] =
    copy(
      status = WorkItemStatus.Ok,
      history = nextHistoryEntry[D](
        WorkItemStatus.Ok,
        reason = reason,
        message = message
      ) :: self.history
    )

  /**
    * Marks the `WorkItem` as skipped.
    */
  def skip[D >: Data: Tag](reason: String, message: String): WorkItem[D] =
    copy(
      status = WorkItemStatus.Skipped,
      history =
        nextHistoryEntry[D](WorkItemStatus.Skipped, reason, message) :: self.history
    )

  def whenOk[D >: Data: Tag](action: WorkItem[D] => Unit)(
      onError: (self.type, Throwable) => (WorkItemStatus, String, String)
  ): WorkItem[D] =
    try {
      action(self)
      self
    } catch {
      case NonFatal(error) =>
        val (newStatus, reason, message) = onError(self, error)
        newStatus match {
          case WorkItemStatus.Ok       => recover[D](reason, message)
          case WorkItemStatus.Skipped  => skip[D](reason, message)
          case WorkItemStatus.Excluded => exclude[D](reason, message)
          case WorkItemStatus.Failed   => fail[D](reason, message)
        }
    }

  private def nextHistoryEntry[D >: Data](
      status: WorkItemStatus,
      reason: String,
      message: String
  )(
      implicit evTag: Tag[D],
      sequenceProvider: WorkItemSequenceProvider[D]
  ): WorkItem.HistoryEntry =
    WorkItem.HistoryEntry(
      status = status,
      reason = reason,
      dataType = evTag.tag.longName,
      message = message,
      sequenceProvider.next(self)
    )

}
object WorkItem {
  type WorkItemSequenceProvider[Data] =
    SequenceProvider.Aux[WorkItem[Data], Long]

  implicit def workItemSequenceProviding[A]: Aux[WorkItem[A], Long] =
    SequenceProvider.longSequenceProvider { workItem: WorkItem[A] =>
      workItem.history.headOption.map(_.sequenceNumber) getOrElse 0
    }

  def of[Data: Tag](data: Data): WorkItem[Data] =
    WorkItem(data = data, status = WorkItemStatus.Ok, List.empty)

  def newOk[Data: Tag](data: Data, message: Option[String]): WorkItem[Data] = {
    val entry = HistoryEntry.initialFor[Data](message)
    WorkItem(data = data, status = WorkItemStatus.Ok, List(entry))
  }

  // Note: The constructor of the below class is private to help ensure that history entries are only created by
  //       the `WorkItem` type.
  final case class HistoryEntry private (
      status: WorkItemStatus,
      reason: String,
      dataType: String,
      message: String,
      sequenceNumber: Long
  )
  object HistoryEntry {
    private[WorkItem] def initialFor[Data](
        message: Option[String]
    )(implicit tagged: Tag[Data]): HistoryEntry =
      HistoryEntry(
        status = WorkItemStatus.Ok,
        reason = "Created",
        dataType = tagged.tag.longName,
        message = message getOrElse "Created",
        0
      )
  }
}
