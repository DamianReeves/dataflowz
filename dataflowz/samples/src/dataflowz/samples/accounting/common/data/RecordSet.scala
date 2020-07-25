package dataflowz.samples.accounting.common.data

import org.apache.spark.sql.{Dataset, Encoder, SparkSession}

import scala.reflect.ClassTag

sealed abstract class RecordSet[A] { self =>

  def count: Long

  def isEmpty: Boolean = self.count == 0

  def toArray: Array[A]

  def toDataset[A1 >: A: Encoder]: SparkSession => Dataset[A1]

  def toList: List[A]

}

object RecordSet {

  def ofDataset[A](dataset: Dataset[A]): RecordSet[A] =
    RecordSetOfDataSet(dataset)

  def ofList[A: ClassTag](data: List[A] = List.empty): RecordSet[A] =
    RecordSetOfList(data)
  def ofList[A: ClassTag](first: A, rest: A*): RecordSet[A] =
    RecordSetOfList(first :: rest.toList)

  private final case class RecordSetOfDataSet[A](data: Dataset[A])
      extends RecordSet[A] { self =>

    def count: Long = data.count()

    def toDataset[A1 >: A: Encoder]: SparkSession => Dataset[A1] =
      _ => data.as[A1]

    def toArray: Array[A] = data.collect()

    def toList: List[A] = data.collect().toList

  }

  private final case class RecordSetOfList[A: ClassTag](
      data: List[A]
  ) extends RecordSet[A] { self =>

    def count: Long = data.size.toLong

    override def isEmpty: Boolean = data.isEmpty

    def toDataset[A1 >: A: Encoder]: SparkSession => Dataset[A1] = {
      sparkSession => sparkSession.createDataset(data)
    }

    def toArray: Array[A] = {
      data.toArray[A]
    }

    def toList: List[A] = data
  }
}
