package dataflowz.samples.accounting.common.data

import org.apache.spark.sql.{Encoder, Dataset, SparkSession}
import scala.reflect.ClassTag

sealed trait RecordSet[A] { self =>
  def toDataset(implicit ev: Encoder[A]): SparkSession => Dataset[A]
  def toArray(implicit ev: ClassTag[A]): Array[A]
}

object RecordSet {

  def fromDataset[A](dataset: Dataset[A]): RecordSet[A] =
    RecordSetOfDataSet(dataset)

  def fromList[A: Encoder](data: List[A]): RecordSet[A] = RecordSetOfList(data)

  private final case class RecordSetOfDataSet[A](data: Dataset[A])
      extends RecordSet[A] {

    def toDataset(implicit ev: Encoder[A]): SparkSession => Dataset[A] =
      _ => data

    def toArray(implicit ev: ClassTag[A]): Array[A] = data.collect()
  }

  private final case class RecordSetOfList[A](
      data: List[A]
  ) extends RecordSet[A] {

    def toDataset(implicit ev: Encoder[A]): SparkSession => Dataset[A] = {
      sparkSession => sparkSession.createDataset(data)
    }

    def toArray(implicit ev: ClassTag[A]): Array[A] = {
      data.toArray
    }
  }
}
