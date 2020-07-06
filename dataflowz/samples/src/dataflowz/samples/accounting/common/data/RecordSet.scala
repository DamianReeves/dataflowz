package dataflowz.samples.accounting.common.data

import org.apache.spark.sql.{Encoder, Dataset, SparkSession}
import scala.reflect.ClassTag

sealed trait RecordSet[A] { self =>
  def toDataset[A1 >: A: Encoder]: SparkSession => Dataset[A1]
  def toArray(implicit ev: ClassTag[A]): Array[A]
}

object RecordSet {

  def fromDataset[A](dataset: Dataset[A]): RecordSet[A] =
    RecordSetOfDataSet(dataset)

  def fromList[A: Encoder](data: List[A]): RecordSet[A] = RecordSetOfList(data)

  private final case class RecordSetOfDataSet[A](data: Dataset[A])
      extends RecordSet[A] {

    def toDataset[A1 >: A: Encoder]: SparkSession => Dataset[A1] =
      _ => data.as[A1]

    def toArray(implicit ev: ClassTag[A]): Array[A] = data.collect()
  }

  private final case class RecordSetOfList[A](
      data: List[A]
  ) extends RecordSet[A] {

    def toDataset[A1 >: A: Encoder]: SparkSession => Dataset[A1] = {
      sparkSession => sparkSession.createDataset(data)
    }

    def toArray(implicit ev: ClassTag[A]): Array[A] = {
      data.toArray
    }
  }
}
