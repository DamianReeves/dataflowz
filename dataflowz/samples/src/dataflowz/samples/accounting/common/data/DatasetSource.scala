package dataflowz.samples.accounting.common.data

import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{Dataset, Encoder, SparkSession}

final case class DatasetSource[-S, A](load: S => Dataset[A]) { self =>
  def map[B: Encoder](f: A => B): DatasetSource[S, B] = DatasetSource { src =>
    self.load(src).map(f)
  }
  def schema(source: S): StructType = self.load(source).schema
}

object DatasetSource {
  def fromList[A: Encoder](items: List[A]): DatasetSource[SparkSession, A] =
    DatasetSource { spark: SparkSession => spark.createDataset(items) }

  def fromDataset[A](dataset: Dataset[A]): DatasetSource[Any, A] =
    DatasetSource { _ => dataset }
}

object Demo {
  final case class Person(name: String, age: Int)
  var ds: Dataset[Person] = _

  val datasetSource: DatasetSource[Any, Person] = DatasetSource.fromDataset(ds)
  val personSource: DatasetSource[SparkSession, Person] = datasetSource
}
